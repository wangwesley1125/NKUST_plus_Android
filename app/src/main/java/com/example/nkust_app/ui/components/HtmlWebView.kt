package com.example.nkust_app.ui.components

import android.webkit.WebView
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

/** 顯示一段 HTML，可縮放；內容沒變就不重新載入，避免縮放位置被重置 */
@Composable
fun HtmlWebView(html: String, baseUrl: String, modifier: Modifier = Modifier) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.builtInZoomControls = true     // 雙指縮放
                settings.displayZoomControls = false    // 不顯示 +/- 按鈕
                settings.useWideViewPort = true         // 讓 <meta viewport width=1100> 生效
                settings.loadWithOverviewMode = true    // 進來時自動縮到符合螢幕
                setBackgroundColor(android.graphics.Color.WHITE)
            }
        },
        update = { webView ->
            if (webView.tag != html) {
                webView.tag = html
                webView.loadDataWithBaseURL(baseUrl, html, "text/html", "UTF-8", null)
            }
        },
        onRelease = { it.destroy() }
    )
}