package com.example.nkust_app.data

import android.content.Context
import androidx.core.content.edit

/** 對應 iOS 的 @AppStorage("showGrid") */
class SettingsStorage(context: Context) {
    private val prefs = context.getSharedPreferences("nkust_prefs", Context.MODE_PRIVATE)

    var showGrid: Boolean
        get() = prefs.getBoolean(KEY_SHOW_GRID, false)
        set(value) = prefs.edit { putBoolean(KEY_SHOW_GRID, value) }

    /** 對應 iOS 的 dismissedInstagramBanner */
    var dismissedInstagramBanner: Boolean
        get() = prefs.getBoolean(KEY_DISMISSED_IG, false)
        set(value) = prefs.edit { putBoolean(KEY_DISMISSED_IG, value) }

    companion object {
        private const val KEY_SHOW_GRID = "showGrid"
        private const val KEY_DISMISSED_IG = "dismissedInstagramBanner"
    }
}