package com.example.nkust_app.data.rating

import androidx.compose.ui.graphics.Color
import com.example.nkust_app.data.score.ScoreRecord
import com.example.nkust_app.data.score.TranscriptCourse
import org.json.JSONArray
import org.json.JSONObject

/*
 * 課程推薦的資料模型（對應 iOS CourseRatingStore.swift 上半部）
 */

// ═══════ 推薦程度 ═══════

/** 顏色與 iOS 一致：超級推薦 粉紅、還可以 綠、普普通通 橘、拉完了 咖啡 */
enum class RatingLevel(val value: Int, val title: String, val color: Color) {
    Superb(4, "超級推薦", Color(0xFFFF2D55)),
    Good(3, "還可以", Color(0xFF34C759)),
    Meh(2, "普普通通", Color(0xFFFF9500)),
    Awful(1, "拉完了", Color(0xFFA2845E));

    companion object {
        fun from(value: Int): RatingLevel? = entries.firstOrNull { it.value == value }
    }
}

/** 推薦率對應的顏色（推薦頁、首頁共用）：沿用四個等級的顏色 */
fun recommendRateColor(rate: Double): Color = when {
    rate >= 0.7 -> RatingLevel.Superb.color
    rate >= 0.4 -> RatingLevel.Good.color
    rate >= 0.2 -> RatingLevel.Meh.color
    else -> RatingLevel.Awful.color
}

// ═══════ 伺服器回傳的資料 ═══════

/** 一組評分統計（全校、單一科系、或推薦頁的一門課） */
data class RatingSummary(
    val total: Int,
    val hidden: Boolean,
    /** [拉完了, 普普通通, 還可以, 超級推薦] 的人數 */
    val levels: List<Int>?,
    /** 超級推薦 + 還可以 的人數 */
    val recommended: Int?,
    /** 評分者成績單上是必修 / 選修的人數 */
    val required: Int,
    val elective: Int,
    /** 可見的修課心得數 */
    val comments: Int,
    /** 科系名稱（全校統計時為 null） */
    val department: String? = null
) {
    /** 推薦比例 0..1 */
    val recommendRate: Double?
        get() = if (!hidden && recommended != null && total > 0) recommended.toDouble() / total else null

    fun count(level: RatingLevel): Int = levels?.getOrNull(level.value - 1) ?: 0

    /** 修別標籤：只有必修 → 必修、只有選修 → 選修、兩種都有 → 必選修皆有 */
    val courseTypeLabel: String?
        get() = when {
            required > 0 && elective == 0 -> "必修"
            elective > 0 && required == 0 -> "選修"
            required > 0 && elective > 0 -> "必選修皆有"
            else -> null
        }

    companion object {
        fun fromJson(json: JSONObject): RatingSummary = RatingSummary(
            total = json.optInt("total"),
            hidden = json.optBoolean("hidden"),
            levels = json.optJSONArray("levels")?.toIntList(),
            recommended = if (json.isNull("recommended")) null else json.optInt("recommended"),
            required = json.optInt("required"),
            elective = json.optInt("elective"),
            comments = json.optInt("comments"),
            department = json.optStringOrNull("department")
        )
    }
}

/** /v1/stats：overall 是全校，dept 是本系（有帶科系查詢時才有） */
data class CourseStat(
    val name: String,
    val teacher: String,
    val overall: RatingSummary,
    val dept: RatingSummary?
) {
    companion object {
        fun fromJson(json: JSONObject): CourseStat = CourseStat(
            name = json.optString("name"),
            teacher = json.optString("teacher"),
            overall = RatingSummary.fromJson(json),
            dept = json.optJSONObject("dept")?.let { RatingSummary.fromJson(it) }
        )
    }
}

/** /v1/departments */
data class DepartmentInfo(val department: String, val total: Int)

/** /v1/browse：推薦頁的一門課（課名 + 老師） */
data class BrowseCourse(
    val name: String,
    val teacher: String,
    val summary: RatingSummary
) {
    val id: String get() = "$name|$teacher"

    companion object {
        fun fromJson(json: JSONObject): BrowseCourse = BrowseCourse(
            name = json.optString("name"),
            teacher = json.optString("teacher"),
            summary = RatingSummary.fromJson(json)
        )
    }
}

/** /v1/comments：一則修課心得 */
data class CourseComment(
    val id: Int,
    /** 匿名代號（6 碼），同一人在各門課相同，用來封鎖 */
    val author: String,
    val year: String,
    val term: String,
    val department: String,
    val courseType: String,
    val level: Int,
    val text: String,
    val updatedAt: String
) {
    val ratingLevel: RatingLevel? get() = RatingLevel.from(level)

    /** 「電子工程系・匿名同學 #a3f9c1」 */
    val authorName: String
        get() = (if (department.isEmpty()) "" else "$department・") + "匿名同學 #$author"

    companion object {
        fun fromJson(json: JSONObject): CourseComment = CourseComment(
            id = json.optInt("id"),
            author = json.optString("author"),
            year = json.optString("year"),
            term = json.optString("term"),
            department = json.optString("department"),
            courseType = json.optString("courseType"),
            level = json.optInt("level"),
            text = json.optString("text"),
            updatedAt = json.optString("updatedAt")
        )
    }
}

/** 送出推薦的結果 */
sealed interface RatingSubmitResult {
    data object Success : RatingSubmitResult
    /** 沒網路：已存在手機，之後補傳 */
    data object SavedOffline : RatingSubmitResult
    /** 伺服器拒絕心得或老師名稱（不當用語、網址、電話、太長）：本機已還原，可以修改後再送 */
    data class Rejected(val message: String) : RatingSubmitResult
    /** 這門課已經推薦過了（每門課只能推薦一次） */
    data object AlreadyRated : RatingSubmitResult
}

// ═══════ 可以推薦的一門課（成績頁、歷年成績共用；對應 iOS 的 RatableCourse） ═══════

data class RatableCourse(
    val year: String,
    val term: String,
    /** 歷年成績（PDF）沒有課號 → 空字串 */
    val courseCode: String,
    val name: String,
    /** 必修 / 選修 */
    val type: String
) {
    val courseKey: String get() = ratingCourseKey(year, term, name)

    companion object {
        /** 成績頁（期中 / 當學期） */
        fun from(record: ScoreRecord) = RatableCourse(
            year = record.year, term = record.term, courseCode = record.courseCode,
            name = record.name, type = record.type
        )

        /** 歷年成績：semester 例如「113-1」 */
        fun from(course: TranscriptCourse, semester: String): RatableCourse? {
            val parts = semester.split("-")
            if (parts.size != 2) return null
            return RatableCourse(
                year = parts[0], term = parts[1], courseCode = "",
                name = course.name, type = course.type
            )
        }
    }
}

// ═══════ JSON 小工具 ═══════

internal fun JSONArray.toIntList(): List<Int> = List(length()) { optInt(it) }

internal fun JSONObject.optStringOrNull(key: String): String? =
    if (has(key) && !isNull(key)) optString(key) else null