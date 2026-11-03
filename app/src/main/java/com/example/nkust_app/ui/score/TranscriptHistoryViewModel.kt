package com.example.nkust_app.ui.score

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nkust_app.data.course.CourseService
import com.example.nkust_app.data.network.NkustClient
import com.example.nkust_app.data.network.SessionExpiredException
import com.example.nkust_app.data.network.toUserMessage
import com.example.nkust_app.data.rating.CourseRatingRepository
import com.example.nkust_app.data.rating.normalizeCourseText
import com.example.nkust_app.data.score.ScoreService
import com.example.nkust_app.data.score.TranscriptData
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private const val TAG = "NKUST"

class TranscriptHistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val client = NkustClient(application)
    private val service = ScoreService(client, application)
    private val courseService = CourseService(client)

    /** 學期 → (課名（正規化）→ 老師)；從該學期課表對應，給課程推薦自動帶入老師用 */
    private var teacherLookups by mutableStateOf<Map<String, Map<String, String>>>(emptyMap())
    private var token = ""

    var semesters by mutableStateOf<List<String>>(emptyList())
        private set
    var selectedYM by mutableStateOf("")
        private set
    var data by mutableStateOf<TranscriptData?>(null)
        private set
    var isLoadingSemesters by mutableStateOf(true)
        private set
    var isLoadingData by mutableStateOf(false)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var sessionExpired by mutableStateOf(false)
        private set

    init {
        CourseRatingRepository.init(application)
        load()
    }

    /** 對不到就回傳空字串，讓使用者自己填 */
    fun teacherFor(courseName: String, semester: String): String =
        teacherLookups[semester]?.get(normalizeCourseText(courseName)).orEmpty()

    /** 對應 iOS 的 fetchSemestersAndToken()，預設載入最新學期 */
    fun load() {
        viewModelScope.launch {
            isLoadingSemesters = true
            error = null
            try {
                val page = service.fetchTranscriptPage()
                semesters = page.semesters
                token = page.token
                selectedYM = page.semesters.firstOrNull().orEmpty()
                isLoadingSemesters = false
                if (selectedYM.isNotEmpty()) loadTranscript()
            } catch (e: CancellationException) {
                throw e
            } catch (e: SessionExpiredException) {
                sessionExpired = true
                isLoadingSemesters = false
            } catch (e: Exception) {
                Log.w(TAG, "學期清單載入失敗", e)
                error = "無法載入頁面：${e.toUserMessage()}"
                isLoadingSemesters = false
            }
        }
    }

    fun selectSemester(ym: String) {
        if (ym == selectedYM && data != null) return
        selectedYM = ym
        viewModelScope.launch { loadTranscript() }
    }

    /** 抓該學期課表的老師（每個學期只抓一次；失敗不影響成績顯示） */
    private fun loadTeacherLookup(semester: String) {
        if (semester.isEmpty() || teacherLookups.containsKey(semester)) return
        viewModelScope.launch {
            try {
                val lookup = mutableMapOf<String, String>()
                courseService.fetchCourses(semester).forEach { course ->
                    lookup.putIfAbsent(normalizeCourseText(course.name), course.teacher)
                }
                teacherLookups = teacherLookups + (semester to lookup)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "歷年成績載入課表（老師對應）失敗：$semester", e)
            }
        }
    }

    /** 對應 iOS 的 fetchPDF() */
    private suspend fun loadTranscript() {
        loadTeacherLookup(selectedYM)
        isLoadingData = true
        error = null
        data = null
        try {
            val result = service.fetchTranscript(selectedYM, token)
            if (result.courses.isEmpty() && result.studentName.isEmpty()) {
                error = "無法解析成績單，請重試"
            } else {
                data = result
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: SessionExpiredException) {
            sessionExpired = true
        } catch (e: Exception) {
            Log.w(TAG, "成績單載入失敗", e)
            error = "網路錯誤：${e.toUserMessage()}"
        }
        isLoadingData = false
    }
}