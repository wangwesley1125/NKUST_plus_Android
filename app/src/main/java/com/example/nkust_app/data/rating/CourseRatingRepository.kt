package com.example.nkust_app.data.rating

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.edit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

private const val TAG = "NKUST"

/**
 * 課程推薦（對應 iOS 的 CourseRatingStore）
 *
 * - 本機（SharedPreferences）：記住「我自己推薦過哪些課」，依學號分開，沒網路也看得到；
 *   送出失敗會留著，下次設定學號時自動補傳
 * - 雲端（Cloudflare Workers + D1，與 iOS 共用同一個後端）：彙總所有人的推薦與修課心得
 *
 * 用法：
 *   1. ViewModel 建立時呼叫 CourseRatingRepository.init(application)
 *   2. 讀到學籍資料後呼叫 setUser(studentId, department)
 *   3. Compose 畫面直接讀 isRated() / rating() / submitting，狀態變了畫面會自動更新
 */
object CourseRatingRepository {

    const val BASE_URL = "https://nkust-plus-ratings.nkust-plus.workers.dev"

    /** 修課心得字數上限（需與後端 COMMENT_MAX_LENGTH 一致） */
    const val COMMENT_MAX_LENGTH = 200

    /** 授課教師字數上限（需與後端一致） */
    const val TEACHER_MAX_LENGTH = 40

    private const val PREFS_NAME = "nkust_course_ratings"
    private const val KEY_BLOCKED = "blockedCommentAuthors"
    private fun ratingsKey(owner: String) = "ratings_$owner"

    private val http = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()
    private val jsonType = "application/json; charset=utf-8".toMediaType()

    private var prefs: SharedPreferences? = null

    // ═══════ Compose 會觀察的狀態 ═══════

    /** 目前登入者學號（讀到學籍資料後由 setUser 設定） */
    var studentId by mutableStateOf("")
        private set

    /** 學籍資料上的科系（原始字串），評分時一起送出，只用來分科系統計 */
    var department by mutableStateOf("")
        private set

    /** 畫面顯示用的科系名稱（例如「電子工程系」）；抓不到時為空字串 */
    val departmentName: String get() = normalizeDepartmentName(department)

    /** courseKey → 我的推薦程度 */
    private val myRatings = mutableStateMapOf<String, RatingLevel>()

    /** 正在送出的課（courseKey） */
    private val submittingKeys = mutableStateMapOf<String, Boolean>()

    /** 已封鎖的匿名同學代號（只存在這支手機） */
    var blockedAuthors by mutableStateOf<Set<String>>(emptySet())
        private set

    // ═══════ 初始化 / 使用者 ═══════

    fun init(context: Context) {
        if (prefs != null) return
        val p = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs = p
        blockedAuthors = p.getStringSet(KEY_BLOCKED, emptySet())?.toSet() ?: emptySet()
    }

    /**
     * 讀到學籍資料後呼叫。換帳號時只會顯示、只會補傳這個學號自己的推薦。
     * 先設科系再補傳，補傳時才帶得到科系。
     */
    suspend fun setUser(studentId: String, department: String) {
        val changed = studentId != this.studentId
        this.department = department
        this.studentId = studentId
        if (changed) {
            reloadMyRatings()
            if (studentId.isNotEmpty()) syncPending()
        }
    }

    fun isRated(courseKey: String): Boolean = myRatings.containsKey(courseKey)

    fun rating(courseKey: String): RatingLevel? = myRatings[courseKey]

    fun isSubmitting(courseKey: String): Boolean = submittingKeys[courseKey] == true

    // ═══════ 推薦（每門課只能一次） ═══════

    /**
     * 先存本機（按鈕立刻變「已推薦」），再送伺服器；
     * 沒網路就留著下次補傳；心得被拒就把本機紀錄刪掉，讓使用者修改後再送。
     */
    suspend fun setRating(
        level: RatingLevel,
        comment: String,
        course: RatableCourse,
        teacher: String
    ): RatingSubmitResult {
        val owner = studentId
        if (owner.isEmpty()) return RatingSubmitResult.SavedOffline
        val key = course.courseKey
        if (loadRecords(owner).containsKey(key)) return RatingSubmitResult.AlreadyRated

        val record = LocalRating(
            year = course.year,
            term = course.term,
            courseCode = course.courseCode,
            courseName = course.name,
            teacher = teacher.trim(),
            courseType = course.type,
            level = level.value,
            comment = comment.trim(),
            synced = false
        )
        saveRecord(owner, key, record)
        myRatings[key] = level

        val result = upload(owner, key, record)
        if (result is RatingSubmitResult.Rejected) {
            removeRecord(owner, key)
            myRatings.remove(key)
        }
        return result
    }

    /** 把之前送失敗的補傳 */
    suspend fun syncPending() {
        val owner = studentId
        if (owner.isEmpty()) return
        loadRecords(owner).filterValues { !it.synced }.forEach { (key, record) ->
            if (upload(owner, key, record) is RatingSubmitResult.Rejected) {
                // 補傳時心得被拒（例如規則變嚴）：保留推薦、清掉心得，下次再送
                saveRecord(owner, key, record.copy(comment = ""))
            }
        }
    }

    // ═══════ 查詢推薦數（首頁、評分卡片） ═══════

    /** 單門課（評分卡片用，會一起查本系的數字） */
    suspend fun fetchStat(name: String, teacher: String): CourseStat? =
        fetchStats(listOf(name to teacher), includeDepartment = true)[courseStatKey(name, teacher)]

    /** 批次查詢（一次最多 30 門），回傳 key = courseStatKey(name, teacher)；失敗的部分略過 */
    suspend fun fetchStats(
        courses: List<Pair<String, String>>,
        includeDepartment: Boolean = false
    ): Map<String, CourseStat> {
        val unique = courses.distinctBy { (name, teacher) -> courseStatKey(name, teacher) }
        if (unique.isEmpty()) return emptyMap()

        val result = mutableMapOf<String, CourseStat>()
        for (chunk in unique.chunked(30)) {
            val body = JSONObject().apply {
                put("courses", JSONArray().apply {
                    chunk.forEach { (name, teacher) ->
                        put(JSONObject().put("name", name).put("teacher", teacher))
                    }
                })
                if (includeDepartment && departmentName.isNotEmpty()) put("department", department)
            }
            val stats = try {
                JSONObject(post("/v1/stats", body)).getJSONArray("stats")
            } catch (e: Exception) {
                Log.w(TAG, "推薦數查詢失敗", e)
                continue
            }
            // 伺服器回傳順序與請求相同
            chunk.forEachIndexed { i, (name, teacher) ->
                stats.optJSONObject(i)?.let { result[courseStatKey(name, teacher)] = CourseStat.fromJson(it) }
            }
        }
        return result
    }

    // ═══════ 課程推薦頁 ═══════

    /** 有人評分過的科系（膠囊按鈕） */
    suspend fun fetchDepartments(): List<DepartmentInfo> {
        val array = JSONObject(get("/v1/departments")).getJSONArray("departments")
        return List(array.length()) { i ->
            val o = array.getJSONObject(i)
            DepartmentInfo(o.optString("department"), o.optInt("total"))
        }
    }

    /** 推薦列表；department 空字串 = 全校；query = 課名關鍵字 */
    suspend fun browse(department: String, query: String): List<BrowseCourse> {
        val params = buildMap {
            put("limit", "100")
            if (department.isNotEmpty()) put("department", department)
            query.trim().takeIf { it.isNotEmpty() }?.let { put("q", it) }
        }
        val array = JSONObject(get("/v1/browse", params)).getJSONArray("courses")
        return List(array.length()) { BrowseCourse.fromJson(array.getJSONObject(it)) }
    }

    /** 某門課的修課心得（已過濾掉被我封鎖的同學） */
    suspend fun comments(name: String, teacher: String, department: String): List<CourseComment> {
        val params = buildMap {
            put("name", name)
            put("teacher", teacher)
            if (department.isNotEmpty()) put("department", department)
        }
        val array = JSONObject(get("/v1/comments", params)).getJSONArray("comments")
        return List(array.length()) { CourseComment.fromJson(array.getJSONObject(it)) }
            .filterNot { it.author in blockedAuthors }
    }

    /** 檢舉心得；回傳是否已被隱藏 */
    suspend fun report(comment: CourseComment, reason: String): Boolean {
        if (studentId.isEmpty()) throw IOException("請先登入後再檢舉")
        val body = JSONObject()
            .put("id", comment.id)
            .put("studentId", studentId)
            .put("reason", reason)
        return JSONObject(post("/v1/comments/report", body)).optBoolean("hidden")
    }

    /** 封鎖某位匿名同學：之後在這支手機上看不到他的心得 */
    fun block(author: String) {
        blockedAuthors = blockedAuthors + author
        prefs?.edit { putStringSet(KEY_BLOCKED, blockedAuthors) }
    }

    fun unblockAll() {
        blockedAuthors = emptySet()
        prefs?.edit { remove(KEY_BLOCKED) }
    }

    // ═══════ 私有：上傳 ═══════

    private class RejectedException(message: String) : IOException(message)
    private class AlreadyRatedException(val level: Int?, val comment: String?) : IOException("already rated")

    private suspend fun upload(owner: String, key: String, record: LocalRating): RatingSubmitResult {
        submittingKeys[key] = true
        try {
            val body = JSONObject().apply {
                put("studentId", owner)
                put("year", record.year)
                put("term", record.term)
                put("courseName", record.courseName)
                put("teacher", record.teacher)
                put("department", department)
                put("courseType", record.courseType)
                put("level", record.level)
                put("comment", record.comment)
                if (record.courseCode.isNotEmpty()) put("courseCode", record.courseCode)
            }
            post("/v1/ratings", body)
            saveRecord(owner, key, record.copy(synced = true))
            return RatingSubmitResult.Success
        } catch (e: RejectedException) {
            return RatingSubmitResult.Rejected(e.message ?: "內容不符合規範")
        } catch (e: AlreadyRatedException) {
            // 伺服器已有這門課的推薦（例如重灌 App 後）：以伺服器為準，本機也標成已推薦
            val level = e.level ?: record.level
            saveRecord(owner, key, record.copy(level = level, comment = e.comment ?: record.comment, synced = true))
            RatingLevel.from(level)?.let { if (owner == studentId) myRatings[key] = it }
            return RatingSubmitResult.AlreadyRated
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "推薦送出失敗，稍後補傳", e)
            return RatingSubmitResult.SavedOffline
        } finally {
            submittingKeys.remove(key)
        }
    }

    // ═══════ 私有：HTTP ═══════

    private suspend fun get(path: String, params: Map<String, String> = emptyMap()): String {
        val url = BASE_URL.toHttpUrl().newBuilder()
            .addPathSegments(path.trimStart('/'))
            .apply { params.forEach { (k, v) -> addQueryParameter(k, v) } }
            .build()
        return execute(Request.Builder().url(url).get().header("Cache-Control", "no-cache").build())
    }

    private suspend fun post(path: String, body: JSONObject): String {
        val request = Request.Builder()
            .url(BASE_URL + path)
            .post(body.toString().toRequestBody(jsonType))
            .build()
        return execute(request)
    }

    private suspend fun execute(request: Request): String = withContext(Dispatchers.IO) {
        http.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            when {
                response.isSuccessful -> text
                // 409：這門課已經推薦過了
                response.code == 409 -> {
                    val json = runCatching { JSONObject(text) }.getOrNull()
                    throw AlreadyRatedException(
                        level = json?.takeIf { it.has("level") }?.optInt("level"),
                        comment = json?.optStringOrNull("comment")
                    )
                }
                // 400 且是內容被拒：把伺服器的原因（中文）帶回畫面
                response.code == 400 -> {
                    val json = runCatching { JSONObject(text) }.getOrNull()
                    if (json != null && json.optString("code") == "comment_rejected") {
                        throw RejectedException(json.optString("error"))
                    }
                    throw IOException("伺服器錯誤（400）")
                }
                else -> throw IOException("伺服器錯誤（${response.code}）")
            }
        }
    }

    // ═══════ 私有：本機紀錄（SharedPreferences，依學號分開） ═══════

    private data class LocalRating(
        val year: String,
        val term: String,
        val courseCode: String,
        val courseName: String,
        val teacher: String,
        val courseType: String,
        val level: Int,
        val comment: String,
        val synced: Boolean
    ) {
        fun toJson(): JSONObject = JSONObject()
            .put("year", year).put("term", term).put("courseCode", courseCode)
            .put("courseName", courseName).put("teacher", teacher).put("courseType", courseType)
            .put("level", level).put("comment", comment).put("synced", synced)

        companion object {
            fun fromJson(o: JSONObject) = LocalRating(
                year = o.optString("year"),
                term = o.optString("term"),
                courseCode = o.optString("courseCode"),
                courseName = o.optString("courseName"),
                teacher = o.optString("teacher"),
                courseType = o.optString("courseType"),
                level = o.optInt("level"),
                comment = o.optString("comment"),
                synced = o.optBoolean("synced")
            )
        }
    }

    private fun loadRecords(owner: String): Map<String, LocalRating> {
        val raw = prefs?.getString(ratingsKey(owner), null) ?: return emptyMap()
        val json = runCatching { JSONObject(raw) }.getOrNull() ?: return emptyMap()
        return json.keys().asSequence().associateWith { LocalRating.fromJson(json.getJSONObject(it)) }
    }

    private fun writeRecords(owner: String, records: Map<String, LocalRating>) {
        val json = JSONObject()
        records.forEach { (key, record) -> json.put(key, record.toJson()) }
        prefs?.edit { putString(ratingsKey(owner), json.toString()) }
    }

    private fun saveRecord(owner: String, key: String, record: LocalRating) =
        writeRecords(owner, loadRecords(owner) + (key to record))

    private fun removeRecord(owner: String, key: String) =
        writeRecords(owner, loadRecords(owner) - key)

    private fun reloadMyRatings() {
        myRatings.clear()
        if (studentId.isEmpty()) return
        loadRecords(studentId).forEach { (key, record) ->
            RatingLevel.from(record.level)?.let { myRatings[key] = it }
        }
    }
}