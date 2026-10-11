package com.example.nkust_app.ui.components

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.core.content.edit
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.ktx.launchReview
import com.google.android.play.core.ktx.requestReview

/** 同 iOS：第 2 次開啟 App 時請求評分（只請求一次） */
object InAppReview {
    private const val PREFS = "nkust_review"
    private const val KEY_LAUNCH = "launchCount"
    private const val KEY_DONE = "reviewRequested"

    /** 在 MainActivity.onCreate 呼叫，記錄開啟次數 */
    fun recordLaunch(context: Context) {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        prefs.edit { putInt(KEY_LAUNCH, prefs.getInt(KEY_LAUNCH, 0) + 1) }
    }

    /** 是否該跳出評分 */
    fun shouldRequest(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_LAUNCH, 0) >= 2 && !prefs.getBoolean(KEY_DONE, false)
    }

    /** 跳出 Google Play 評分視窗；失敗就安靜略過 */
    suspend fun request(context: Context) {
        val activity = context.findActivity() ?: return
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit { putBoolean(KEY_DONE, true) }
        runCatching {
            val manager = ReviewManagerFactory.create(activity)
            val info = manager.requestReview()
            manager.launchReview(activity, info)
        }
    }

    private tailrec fun Context.findActivity(): Activity? = when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
}