package com.example.nkust_app.ui.course

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nkust_app.data.course.Course
import com.example.nkust_app.data.course.CourseSemester
import com.example.nkust_app.data.course.CourseService
import com.example.nkust_app.data.network.NkustClient
import com.example.nkust_app.data.network.SessionExpiredException
import com.example.nkust_app.data.network.toUserMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private const val TAG = "NKUST"

data class CourseUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val courses: List<Course> = emptyList(),
    val semesters: List<CourseSemester> = emptyList(),
    val selectedSemester: CourseSemester? = null,
    val sessionExpired: Boolean = false
)

class CourseViewModel(application: Application) : AndroidViewModel(application) {

    private val service = CourseService(NkustClient(application))

    var uiState by mutableStateOf(CourseUiState())
        private set

    init {
        load()
    }

    /** 抓學號 → 學期清單 → 課表 */
    fun load() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val studentId = service.fetchStudentId()
                val semesters = service.fetchSemesters(studentId)
                uiState = uiState.copy(
                    semesters = semesters,
                    // 預設選第一個非暑修的學期
                    selectedSemester = semesters.firstOrNull { it.isRegular } ?: semesters.firstOrNull()
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: SessionExpiredException) {
                uiState = uiState.copy(isLoading = false, sessionExpired = true)
                return@launch
            } catch (e: Exception) {
                Log.w(TAG, "學期載入失敗，使用 fallback", e)
            }
            loadCourses(uiState.selectedSemester)
        }
    }

    fun selectSemester(semester: CourseSemester) {
        if (semester == uiState.selectedSemester) return
        uiState = uiState.copy(selectedSemester = semester)
        viewModelScope.launch { loadCourses(semester) }
    }

    private suspend fun loadCourses(semester: CourseSemester?) {
        uiState = uiState.copy(isLoading = true, error = null)
        try {
            val courses = service.fetchCourses(semester?.value.orEmpty())
            Log.d(TAG, "課表載入完成：${courses.size} 堂課")
            uiState = uiState.copy(isLoading = false, courses = courses)
        } catch (e: CancellationException) {
            throw e
        } catch (e: SessionExpiredException) {
            uiState = uiState.copy(isLoading = false, sessionExpired = true)
        } catch (e: Exception) {
            Log.w(TAG, "課表載入失敗", e)
            uiState = uiState.copy(isLoading = false, error = e.toUserMessage())
        }
    }
}