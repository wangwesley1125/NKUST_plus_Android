package com.example.nkust_app.widget

import android.content.Context
import androidx.core.content.edit
import androidx.glance.appwidget.updateAll
import com.example.nkust_app.data.course.Course
import org.json.JSONArray
import org.json.JSONObject

/** 給小工具讀的課表（對應 iOS 的 CourseStorage / App Group courses.json） */
class WidgetCourseStorage(context: Context) {
    private val prefs = context.getSharedPreferences("nkust_widget", Context.MODE_PRIVATE)

    fun save(courses: List<Course>) {
        val array = JSONArray()
        courses.forEach {
            array.put(
                JSONObject()
                    .put("name", it.name)
                    .put("teacher", it.teacher)
                    .put("room", it.room)
                    .put("period", it.period)
                    .put("weekday", it.weekday)
            )
        }
        prefs.edit { putString(KEY, array.toString()) }
    }

    fun load(): List<Course> {
        val json = prefs.getString(KEY, null) ?: return emptyList()
        return try {
            val array = JSONArray(json)
            (0 until array.length()).map { i ->
                val o = array.getJSONObject(i)
                Course(
                    name = o.getString("name"),
                    teacher = o.optString("teacher"),
                    room = o.optString("room"),
                    period = o.getString("period"),
                    weekday = o.getInt("weekday")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun clear() = prefs.edit { remove(KEY) }

    companion object {
        private const val KEY = "courses"

        /** 存課表並刷新桌面上的小工具（對應 iOS 的 updateWidget + reloadAllTimelines） */
        suspend fun saveAndRefresh(context: Context, courses: List<Course>) {
            WidgetCourseStorage(context).save(courses)
            runCatching { TodayCourseWidget().updateAll(context) }
        }

        /** 登出時清空小工具 */
        suspend fun clearAndRefresh(context: Context) {
            WidgetCourseStorage(context).clear()
            runCatching { TodayCourseWidget().updateAll(context) }
        }
    }
}