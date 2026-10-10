package com.example.nkust_app.data

import android.content.Context
import android.webkit.CookieManager
import androidx.core.content.edit

class SessionStorage(context: Context) {
    private val prefs = context.getSharedPreferences("nkust_prefs", Context.MODE_PRIVATE)

    /** 有登入旗標，而且 Cookie 還在，才算登入中 */
    var isLoggedIn: Boolean
        get() = prefs.getBoolean(KEY_LOGGED_IN, false) && !cookieHeader().isNullOrBlank()
        set(value) = prefs.edit { putBoolean(KEY_LOGGED_IN, value) }

    var isGuest: Boolean
        get() = prefs.getBoolean(KEY_GUEST, false)
        set(value) = prefs.edit { putBoolean(KEY_GUEST, value) }

    /** 之後用 OkHttp 抓課表時，會把這串放進 HTTP Header 的 Cookie 欄位 */
    fun cookieHeader(): String? = CookieManager.getInstance().getCookie(BASE_URL)

    fun logout() {
        isLoggedIn = false
        CookieManager.getInstance().apply {
            removeAllCookies(null)
            flush()
        }
    }

    companion object {
        const val BASE_URL = "https://stdsys.nkust.edu.tw"
        const val LOGIN_URL = "$BASE_URL/student/Account/Login"
        private const val KEY_LOGGED_IN = "isLoggedIn"

        private const val KEY_GUEST = "isGuest"
    }
}