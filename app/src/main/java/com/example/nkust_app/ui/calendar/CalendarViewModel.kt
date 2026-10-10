package com.example.nkust_app.ui.calendar

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nkust_app.data.calendar.CalendarSemester
import com.example.nkust_app.data.calendar.CalendarService
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import java.io.File

private const val TAG = "NKUST"

class CalendarViewModel(application: Application) : AndroidViewModel(application) {

    private val service = CalendarService(File(application.cacheDir, "pdf"))

    var semesters by mutableStateOf<List<CalendarSemester>>(emptyList())
        private set
    var selectedIndex by mutableIntStateOf(0)
        private set
    var isLoading by mutableStateOf(true)
        private set
    var error by mutableStateOf<String?>(null)
        private set

    init {
        load()
    }

    fun select(index: Int) {
        selectedIndex = index
    }

    fun load() {
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                val result = service.load()
                if (result.isEmpty()) {
                    error = "無法取得行事曆，請稍後再試"
                } else {
                    semesters = result
                    selectedIndex = 0   // 最新學年度、有資料的第一個學期
                    Log.d(TAG, "行事曆：下載完成 ${result.map { it.label }}")
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "行事曆載入失敗", e)
                error = "無法取得行事曆，請稍後再試"
            }
            isLoading = false
        }
    }
}