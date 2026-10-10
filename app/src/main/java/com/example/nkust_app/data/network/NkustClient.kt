package com.example.nkust_app.data.network

import android.content.Context
import android.webkit.CookieManager
import android.webkit.WebSettings
import com.example.nkust_app.data.SessionStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

/** 學校系統把請求導回登入頁 = Cookie 過期 */
class SessionExpiredException : IOException("登入已過期，請重新登入")

class NkustClient(context: Context) {

    private val baseUrl = SessionStorage.BASE_URL
    private val userAgent = WebSettings.getDefaultUserAgent(context)

    private val http = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /** GET，回傳文字（HTML / JSON），可以加自訂標頭 */
    suspend fun get(path: String, headers: Map<String, String> = emptyMap()): String {
        val builder = Request.Builder().url(baseUrl + path).get()
        headers.forEach { (key, value) -> builder.header(key, value) }
        return String(execute(builder), Charsets.UTF_8)
    }

    /** GET，回傳原始位元組（PDF、圖片等檔案） */
    suspend fun getBytes(
        path: String,
        refererPath: String,
        headers: Map<String, String> = emptyMap()
    ): ByteArray {
        val builder = Request.Builder()
            .url(baseUrl + path)
            .get()
            .header("Referer", baseUrl + refererPath)
        headers.forEach { (key, value) -> builder.header(key, value) }
        return execute(builder)
    }

    /** POST 表單，回傳文字 */
    suspend fun postForm(path: String, form: Map<String, String>, refererPath: String): String {
        val body = FormBody.Builder().apply {
            form.forEach { (key, value) -> add(key, value) }
        }.build()
        val builder = Request.Builder()
            .url(baseUrl + path)
            .post(body)
            .header("Origin", baseUrl)
            .header("Referer", baseUrl + refererPath)
        return String(execute(builder, withXsrf = true), Charsets.UTF_8)
    }

    private suspend fun execute(builder: Request.Builder, withXsrf: Boolean = false): ByteArray =
        withContext(Dispatchers.IO) {
            val cookieManager = CookieManager.getInstance()
            val cookies = cookieManager.getCookie(baseUrl) ?: throw SessionExpiredException()

            builder
                .header("Cookie", cookies)
                .header("User-Agent", userAgent)
                .header("Accept-Language", "zh-TW,zh;q=0.9")
            if (withXsrf) {
                xsrfToken(cookies)?.let { builder.header("X-XSRF-TOKEN", it) }
            }

            http.newCall(builder.build()).execute().use { response ->
                response.headers("Set-Cookie").forEach { cookieManager.setCookie(baseUrl, it) }

                val landedOnLogin = response.request.url.encodedPath.contains("Login", ignoreCase = true)
                if (landedOnLogin || response.code == 401) throw SessionExpiredException()
                if (!response.isSuccessful) throw IOException("伺服器錯誤（${response.code}）")

                response.body?.bytes() ?: ByteArray(0)
            }
        }

    /** 目前 Cookie 裡的 XSRF-TOKEN（URL 解碼後） */
    fun decodedXsrfToken(): String? =
        CookieManager.getInstance().getCookie(baseUrl)
            ?.let { xsrfToken(it) }
            ?.let { android.net.Uri.decode(it) }

    private fun xsrfToken(cookies: String): String? =
        cookies.split(";")
            .map { it.trim() }
            .firstOrNull { it.startsWith("XSRF-TOKEN=") }
            ?.substringAfter("=")
}

/** 把例外轉成給使用者看的訊息 */
fun Throwable.toUserMessage(): String = when (this) {
    is SessionExpiredException -> "登入已過期，請重新登入"
    is UnknownHostException, is ConnectException -> "無法連線到學校系統"
    is SocketTimeoutException -> "連線逾時，學校系統可能較忙碌"
    else -> message ?: "載入失敗，請稍後再試"
}