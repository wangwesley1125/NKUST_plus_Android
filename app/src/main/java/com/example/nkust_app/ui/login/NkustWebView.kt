package com.example.nkust_app.ui.login

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.util.Log
import android.webkit.CookieManager
import android.webkit.RenderProcessGoneDetail
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.example.nkust_app.data.Credentials
import com.example.nkust_app.data.SessionStorage

private const val TAG = "NKUST"

@SuppressLint("SetJavaScriptEnabled", "JavascriptInterface")
@Composable
fun NkustWebView(
    onWebViewCreated: (WebView) -> Unit,
    onLoadingChange: (Boolean) -> Unit,
    onLoginPageLoaded: () -> Unit,
    onLoginStarted: () -> Unit,
    onLoginSuccess: (Credentials?) -> Unit,
    onError: (String) -> Unit,
    onCanGoBackChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentOnLoadingChange by rememberUpdatedState(onLoadingChange)
    val currentOnLoginPageLoaded by rememberUpdatedState(onLoginPageLoaded)
    val currentOnLoginStarted by rememberUpdatedState(onLoginStarted)
    val currentOnLoginSuccess by rememberUpdatedState(onLoginSuccess)
    val currentOnError by rememberUpdatedState(onError)
    val currentOnCanGoBackChange by rememberUpdatedState(onCanGoBackChange)

    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true

                val webView = this
                CookieManager.getInstance().apply {
                    setAcceptCookie(true)
                    setAcceptThirdPartyCookies(webView, true)
                }

                // JS 裡用 NkustBridge.onCredentials(...) 呼叫到這個物件
                val bridge = CredentialBridge()
                addJavascriptInterface(bridge, "NkustBridge")

                webViewClient = object : WebViewClient() {
                    private var didHandleLogin = false

                    override fun onPageStarted(view: WebView, url: String, favicon: Bitmap?) {
                        Log.d(TAG, "開始載入：$url")
                        bridge.currentHost = Uri.parse(url).host
                        currentOnLoadingChange(true)
                        if (!didHandleLogin && isStudentHome(url)) {
                            currentOnLoginStarted()
                        }
                    }

                    override fun onPageCommitVisible(view: WebView, url: String) {
                        if (!didHandleLogin && isStudentHome(url)) {
                            didHandleLogin = true
                            CookieManager.getInstance().flush()
                            Log.d(TAG, "登入成功")
                            currentOnLoginSuccess(bridge.captured)
                            bridge.reset()
                        }
                    }

                    override fun onPageFinished(view: WebView, url: String) {
                        Log.d(TAG, "載入完成：$url")
                        currentOnLoadingChange(false)
                        if (url.contains("Login", ignoreCase = true)) {
                            didHandleLogin = false
                            view.evaluateJavascript(CAPTURE_CREDENTIALS_JS, null)
                            currentOnLoginPageLoaded()
                        }
                    }

                    override fun doUpdateVisitedHistory(view: WebView, url: String?, isReload: Boolean) {
                        currentOnCanGoBackChange(view.canGoBack())
                    }

                    override fun onReceivedError(
                        view: WebView,
                        request: WebResourceRequest,
                        error: WebResourceError
                    ) {
                        if (!request.isForMainFrame || didHandleLogin) return
                        Log.d(TAG, "載入失敗：${error.errorCode} ${error.description}")
                        currentOnError(errorMessage(view.context, error.errorCode))
                    }

                    override fun onRenderProcessGone(view: WebView, detail: RenderProcessGoneDetail): Boolean {
                        Log.w(TAG, "WebView 繪圖程序結束，crash = ${detail.didCrash()}")
                        return true
                    }
                }

                onWebViewCreated(this)
                loadUrl(SessionStorage.LOGIN_URL)
            }
        },
        onRelease = {
            it.stopLoading()
            it.destroy()
        }
    )
}

private fun isStudentHome(url: String): Boolean =
    url.contains("/student") && !url.contains("Login", ignoreCase = true)

private fun errorMessage(context: Context, code: Int): String {
    if (!context.isOnline()) return "目前沒有網路連線"
    return when (code) {
        WebViewClient.ERROR_TIMEOUT -> "連線逾時，學校系統可能較忙碌"
        WebViewClient.ERROR_HOST_LOOKUP,
        WebViewClient.ERROR_CONNECT -> "無法連線到學校系統"
        else -> "載入失敗，請稍後再試"
    }
}

private fun Context.isOnline(): Boolean {
    val cm = getSystemService(ConnectivityManager::class.java)
    val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
    return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
}