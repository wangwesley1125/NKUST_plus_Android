package com.example.nkust_app.ui.profile

import android.app.Application
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.nkust_app.data.certificate.CertificateLanguage
import com.example.nkust_app.data.certificate.CertificateService
import com.example.nkust_app.data.network.NkustClient
import com.example.nkust_app.data.network.SessionExpiredException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

private const val TAG = "NKUST"

const val ENGLISH_NAME_MISSING = """您尚未建立英文姓名，倘欲下載英文在學證明書，請依下列流程辦理：

一、確認當學期註冊費已繳納完畢。

二、填寫「學生變更基本資料申請書」，請於【更改項目】勾選【其他】，並填寫「新增英文姓名：ＯＯＯ」（建議依護照英文姓名填寫）。

三、將「學生變更基本資料申請書」繳交至所屬校區綜合業務處。

四、俟承辦人登打完資料即可再次登入此畫面下載英文在學證明書。"""

class CertificateViewModel(application: Application) : AndroidViewModel(application) {

    private val service = CertificateService(NkustClient(application))
    private val pdfDir = File(application.cacheDir, "pdf").apply { mkdirs() }

    var language by mutableStateOf(CertificateLanguage.Chinese)
        private set
    var pdfFile by mutableStateOf<File?>(null)
        private set
    var isLoading by mutableStateOf(true)
        private set
    var isSwitching by mutableStateOf(false)    // 切換中英文版
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var sessionExpired by mutableStateOf(false)
        private set

    init {
        load()
    }

    fun selectLanguage(newLanguage: CertificateLanguage) {
        if (newLanguage == language && error == null) return
        language = newLanguage
        load()
    }

    fun load() {
        viewModelScope.launch {
            // 第一次載入用 isLoading，之後用 isSwitching
            if (pdfFile == null) isLoading = true else isSwitching = true
            error = null
            try {
                val bytes = service.fetch(language)
                if (bytes == null) {
                    error = if (language == CertificateLanguage.English) ENGLISH_NAME_MISSING
                    else "無法取得在學證明，請稍後再試"
                } else {
                    val file = File(pdfDir, "在學證明_${language.label}.pdf")
                    withContext(Dispatchers.IO) { file.writeBytes(bytes) }
                    pdfFile = file
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: SessionExpiredException) {
                sessionExpired = true
            } catch (e: Exception) {
                Log.w(TAG, "在學證明載入失敗", e)
                error = "無法取得在學證明，請稍後再試"
            }
            isLoading = false
            isSwitching = false
        }
    }
}