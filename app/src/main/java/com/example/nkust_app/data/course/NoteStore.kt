package com.example.nkust_app.data.course

import android.content.Context
import androidx.core.content.edit

/** 對應 iOS 的 NoteStore（存在裝置本地） */
class NoteStore(context: Context) {
    private val prefs = context.getSharedPreferences("nkust_notes", Context.MODE_PRIVATE)

    fun load(key: String): String = prefs.getString("note_$key", "").orEmpty()

    fun save(key: String, note: String) = prefs.edit { putString("note_$key", note) }
}

/** 同 iOS 的 courseKey：「課名_節次」 */
fun Course.noteKey(): String = "${name}_$period"