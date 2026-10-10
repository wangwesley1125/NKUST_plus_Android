package com.example.nkust_app.data

import android.content.Context
import androidx.core.content.edit

class ConsentStorage(context: Context) {
    private val prefs = context.getSharedPreferences("nkust_prefs", Context.MODE_PRIVATE)

    var hasAgreed: Boolean
        get() = prefs.getBoolean(KEY, false)
        set(value) = prefs.edit { putBoolean(KEY, value) }

    companion object {
        private const val KEY = "hasAgreedPrivacyPolicy"
    }
}