package com.example.nkust_app.ui.components

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent

/**
 * 對應 iOS 的 openLinksInSafariSheet()：
 * 一般網頁在 App 內用 Custom Tabs 開啟；Instagram、Google Play 交給系統開對應的 App
 */
fun openWebLink(context: Context, url: String) {
    val uri = Uri.parse(url)
    val host = uri.host?.lowercase().orEmpty()

    if (host.endsWith("instagram.com") || host == "play.google.com") {
        startSafely(context, Intent(Intent.ACTION_VIEW, uri))
        return
    }

    val light = CustomTabColorSchemeParams.Builder()
        .setToolbarColor(android.graphics.Color.WHITE)
        .build()
    val dark = CustomTabColorSchemeParams.Builder()
        .setToolbarColor(android.graphics.Color.BLACK)
        .build()

    val customTab = CustomTabsIntent.Builder()
        .setShowTitle(true)
        .setShareState(CustomTabsIntent.SHARE_STATE_ON)
        .setColorScheme(CustomTabsIntent.COLOR_SCHEME_SYSTEM)
        .setColorSchemeParams(CustomTabsIntent.COLOR_SCHEME_LIGHT, light)
        .setColorSchemeParams(CustomTabsIntent.COLOR_SCHEME_DARK, dark)
        .build()

    try {
        customTab.launchUrl(context, uri)
    } catch (e: ActivityNotFoundException) {
        // 裝置沒有支援 Custom Tabs 的瀏覽器
        startSafely(context, Intent(Intent.ACTION_VIEW, uri))
    }
}

/** 對應 iOS 的 openFeedbackMail()：開啟郵件 App，自動附上版本與裝置資訊 */
fun sendFeedbackMail(context: Context, appVersion: String) {
    val body = buildString {
        append("\n\n\n—— 以下資訊有助於我們找出問題 ——\n")
        append("App 版本：$appVersion\n")
        append("Android ${Build.VERSION.RELEASE}（API ${Build.VERSION.SDK_INT}）\n")
        append("裝置：${Build.MANUFACTURER} ${Build.MODEL}")
    }
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf(FEEDBACK_EMAIL))
        putExtra(Intent.EXTRA_SUBJECT, "問題回報")
        putExtra(Intent.EXTRA_TEXT, body)
    }
    startSafely(context, intent, failMessage = "找不到郵件 App，請寄信至 $FEEDBACK_EMAIL")
}

/** 對應 iOS 的 UIActivityViewController：分享 App 連結 */
fun shareApp(context: Context) {
    val playStoreUrl = "https://play.google.com/store/apps/details?id=${context.packageName}"
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, "推薦你使用 NKUST plus！\n$playStoreUrl")
    }
    context.startActivity(Intent.createChooser(intent, "分享此 App"))
}

private fun startSafely(context: Context, intent: Intent, failMessage: String = "找不到可以開啟的 App") {
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, failMessage, Toast.LENGTH_LONG).show()
    }
}

const val FEEDBACK_EMAIL = "nkustplus123@gmail.com"