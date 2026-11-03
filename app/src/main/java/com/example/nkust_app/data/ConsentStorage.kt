package com.example.nkust_app.data

import android.content.Context
import androidx.core.content.edit

class ConsentStorage(context: Context) {
    private val prefs = context.getSharedPreferences("nkust_prefs", Context.MODE_PRIVATE)

    var hasAgreed: Boolean
        get() = prefs.getBoolean(KEY, false)
        set(value) = prefs.edit { putBoolean(KEY, value) }

    companion object {
        /**
         * 條文有重大變更時把版本號 +1，所有人會重新看到同意畫面。
         * v2：加入課程推薦（學號雜湊、科系、修課心得上傳至伺服器）
         */
        private const val KEY = "hasAgreedPrivacyPolicy_v2"
    }
}