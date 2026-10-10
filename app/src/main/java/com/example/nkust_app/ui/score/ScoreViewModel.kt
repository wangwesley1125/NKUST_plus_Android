package com.example.nkust_app.ui.score

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nkust_app.data.network.NkustClient
import com.example.nkust_app.data.network.SessionExpiredException
import com.example.nkust_app.data.network.toUserMessage
import com.example.nkust_app.data.score.ScoreRecord
import com.example.nkust_app.data.score.ScoreService
import com.example.nkust_app.data.score.TranscriptData
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private const val TAG = "NKUST"

enum class ScoreTab { Midterm, Semester }

data class ScoreListState(
    val isLoading: Boolean = false,
    val loaded: Boolean = false,
    val error: String? = null,
    val records: List<ScoreRecord> = emptyList()
)

class ScoreViewModel(application: Application) : AndroidViewModel(application) {

    private val service = ScoreService(NkustClient(application), application)

    var studentInfo by mutableStateOf<TranscriptData?>(null)
        private set
    var isLoadingStudentInfo by mutableStateOf(true)
        private set
    var midterm by mutableStateOf(ScoreListState())
        private set
    var semester by mutableStateOf(ScoreListState())
        private set
    var sessionExpired by mutableStateOf(false)
        private set

    init {
        loadStudentInfo()
    }

    fun stateOf(tab: ScoreTab) = if (tab == ScoreTab.Midterm) midterm else semester

    /** 第一次切到某個分頁才抓（同 iOS 的 .task） */
    fun loadIfNeeded(tab: ScoreTab) {
        val state = stateOf(tab)
        if (!state.loaded && !state.isLoading) load(tab)
    }

    fun load(tab: ScoreTab) {
        viewModelScope.launch {
            update(tab) { it.copy(isLoading = true, error = null) }
            try {
                val records = if (tab == ScoreTab.Midterm) service.fetchMidterm()
                else service.fetchPresentSemester()
                update(tab) { it.copy(isLoading = false, loaded = true, records = records) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: SessionExpiredException) {
                sessionExpired = true
                update(tab) { it.copy(isLoading = false) }
            } catch (e: Exception) {
                Log.w(TAG, "成績載入失敗", e)
                update(tab) { it.copy(isLoading = false, loaded = true, error = "網路錯誤：${e.toUserMessage()}") }
            }
        }
    }

    private fun loadStudentInfo() {
        viewModelScope.launch {
            try {
                studentInfo = service.fetchStudentInfo()?.takeIf { it.studentName.isNotEmpty() }
            } catch (e: CancellationException) {
                throw e
            } catch (e: SessionExpiredException) {
                sessionExpired = true
            } catch (e: Exception) {
                Log.w(TAG, "學生資料載入失敗", e)
            }
            isLoadingStudentInfo = false
        }
    }

    private fun update(tab: ScoreTab, transform: (ScoreListState) -> ScoreListState) {
        if (tab == ScoreTab.Midterm) midterm = transform(midterm)
        else semester = transform(semester)
    }
}