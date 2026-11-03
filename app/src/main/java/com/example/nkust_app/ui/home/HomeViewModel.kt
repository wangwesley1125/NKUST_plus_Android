package com.example.nkust_app.ui.home

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nkust_app.data.course.Course
import com.example.nkust_app.data.course.CourseService
import com.example.nkust_app.data.graduation.GraduationService
import com.example.nkust_app.data.graduation.GraduationStatus
import com.example.nkust_app.data.network.NkustClient
import com.example.nkust_app.data.network.SessionExpiredException
import com.example.nkust_app.data.profile.ProfileService
import com.example.nkust_app.data.rating.CourseRatingRepository
import com.example.nkust_app.data.rating.CourseStat
import com.example.nkust_app.data.rating.courseStatKey
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import com.example.nkust_app.widget.WidgetCourseStorage

private const val TAG = "NKUST"

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val client = NkustClient(application)
    private val courseService = CourseService(client)
    private val graduationService = GraduationService(client)
    private val profileService = ProfileService(client)

    var courses by mutableStateOf<List<Course>>(emptyList())
        private set
    var graduation by mutableStateOf<GraduationStatus?>(null)
        private set
    var isLoading by mutableStateOf(true)
        private set
    var sessionExpired by mutableStateOf(false)
        private set

    /** 課程推薦數：key = courseStatKey(課名, 老師)；還沒載入時查不到 → 徽章不顯示 */
    var courseStats by mutableStateOf<Map<String, CourseStat>>(emptyMap())
        private set

    init {
        CourseRatingRepository.init(application)
        load()
    }

    fun statFor(course: Course): CourseStat? = courseStats[courseStatKey(course.name, course.teacher)]

    /** 一次批次查詢整份課表的課（通常十門左右）；失敗就不顯示徽章 */
    fun loadCourseStats() {
        val pairs = courses.map { it.name to it.teacher }
        if (pairs.isEmpty()) return
        viewModelScope.launch {
            val stats = CourseRatingRepository.fetchStats(pairs)
            if (stats.isNotEmpty()) courseStats = stats
        }
    }

    fun load() {
        viewModelScope.launch {
            isLoading = true
            coroutineScope {
                val coursesJob = async { runCatching { courseService.fetchCourses() } }
                val gradJob = async { runCatching { graduationService.fetchStatus() } }
                val profileJob = async { runCatching { profileService.fetchProfile() } }

                coursesJob.await()
                    .onSuccess { courses = it }
                    .onFailure { handleError(it, "課表") }
                gradJob.await()
                    .onSuccess { graduation = it }
                    .onFailure { handleError(it, "學分") }

                // 課程推薦要用的學號（伺服器只存雜湊）與科系；會順便補傳之前沒送出的推薦
                profileJob.await()
                    .onSuccess { profile ->
                        if (profile.name.isNotEmpty()) {
                            CourseRatingRepository.setUser(profile.studentId, profile.department)
                        }
                    }
                    .onFailure { handleError(it, "學籍資料") }
            }
            isLoading = false

            // 畫面先顯示，推薦數之後再補上（不擋住首頁載入）
            loadCourseStats()

            // 用同一份課表更新桌面小工具（同 iOS 的 updateWidget）
            if (courses.isNotEmpty()) {
                WidgetCourseStorage.saveAndRefresh(getApplication(), courses)
            }
        }
    }

    private fun handleError(e: Throwable, what: String) {
        when (e) {
            is CancellationException -> throw e
            is SessionExpiredException -> sessionExpired = true
            else -> Log.w(TAG, "首頁$what 載入失敗", e)
        }
    }
}