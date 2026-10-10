package com.example.nkust_app.ui.profile

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nkust_app.data.network.NkustClient
import com.example.nkust_app.data.network.SessionExpiredException
import com.example.nkust_app.data.profile.ProfileService
import com.example.nkust_app.data.profile.StudentProfile
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

private const val TAG = "NKUST"

class ProfileViewModel(application: Application) : AndroidViewModel(application) {

    private val service = ProfileService(NkustClient(application))

    var profile by mutableStateOf(StudentProfile())
        private set
    var photo by mutableStateOf<ImageBitmap?>(null)
        private set
    var isLoading by mutableStateOf(true)
        private set
    var sessionExpired by mutableStateOf(false)
        private set

    init {
        load()
    }

    private fun load() {
        viewModelScope.launch {
            try {
                val result = service.fetchProfile()
                profile = result
                isLoading = false
                // 照片慢慢載，不擋住其他資料
                photo = service.fetchPhoto(result)?.asImageBitmap()
            } catch (e: CancellationException) {
                throw e
            } catch (e: SessionExpiredException) {
                sessionExpired = true
            } catch (e: Exception) {
                Log.w(TAG, "載入個人資料失敗", e)
            }
            isLoading = false
        }
    }
}