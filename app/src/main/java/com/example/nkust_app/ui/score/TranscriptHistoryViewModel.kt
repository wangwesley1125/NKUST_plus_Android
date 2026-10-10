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
import com.example.nkust_app.data.score.ScoreService
import com.example.nkust_app.data.score.TranscriptData
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private const val TAG = "NKUST"

class TranscriptHistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val service = ScoreService(NkustClient(application), application)
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
        load()
    }

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

    /** 對應 iOS 的 fetchPDF() */
    private suspend fun loadTranscript() {
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