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

    var courses by mutableStateOf<List<Course>>(emptyList())
        private set
    var graduation by mutableStateOf<GraduationStatus?>(null)
        private set
    var isLoading by mutableStateOf(true)
        private set
    var sessionExpired by mutableStateOf(false)
        private set

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            isLoading = true
            coroutineScope {
                val coursesJob = async { runCatching { courseService.fetchCourses() } }
                val gradJob = async { runCatching { graduationService.fetchStatus() } }

                coursesJob.await()
                    .onSuccess { courses = it }
                    .onFailure { handleError(it, "課表") }
                gradJob.await()
                    .onSuccess { graduation = it }
                    .onFailure { handleError(it, "學分") }
            }
            isLoading = false

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