package com.example.nkust_app.data.course

import com.example.nkust_app.data.network.NkustClient
import org.json.JSONObject
import java.io.IOException
import java.util.Calendar

class CourseService(private val client: NkustClient) {

    private val queryPath = "/student/Course/StudentCourseQuery"

    /** 從課表查詢頁解析學號 */
    suspend fun fetchStudentId(): String {
        val html = client.get(queryPath)
        val patterns = listOf(
            Regex("""stdId=([A-Za-z]\d{8,9})"""),
            Regex("""[Ss]td[Ii]d["']?\s*[=:]\s*["']?([A-Za-z]\d{8,9})"""),
            Regex("""學號[：:]\s*([A-Za-z]\d{8,9})"""),
            Regex("""([A-Za-z]\d{9})""")
        )
        for (pattern in patterns) {
            pattern.find(html)?.let { return it.groupValues[1] }
        }
        throw IOException("無法取得學號")
    }

    /** 完整學期清單 */
    suspend fun fetchSemesters(studentId: String): List<CourseSemester> {
        val json = client.postForm(
            path = "/student/WebCode/GetSchoolYearSmsCodes",
            form = mapOf("stdId" to studentId),
            refererPath = queryPath
        )
        val obj = JSONObject(json)
        if (!obj.optBoolean("success")) throw IOException("學期清單載入失敗")

        val array = obj.getJSONArray("result")
        return (0 until array.length()).map { i ->
            val item = array.getJSONObject(i)
            CourseSemester(text = item.getString("text"), value = item.getString("value"))
        }
    }

    /** 抓課表（semester 為空時自動判斷最新學期） */
    suspend fun fetchCourses(semester: String = ""): List<Course> {
        val target = semester.ifEmpty { fetchLatestSemester() }
        val html = client.postForm(
            path = "$queryPath/Query",
            form = mapOf("schoolYearSms" to target),
            refererPath = queryPath
        )
        return CourseParser.parse(html)
    }

    private suspend fun fetchLatestSemester(): String {
        val html = client.get(queryPath)
        Regex("""(?:value="|")(\d{3}-\d)(?:"|")""").find(html)?.let {
            return it.groupValues[1]
        }
        // fallback：用今天日期推算（民國年）
        val now = Calendar.getInstance()
        val year = now.get(Calendar.YEAR) - 1911
        val month = now.get(Calendar.MONTH) + 1
        return if (month >= 8) "$year-1" else "${year - 1}-2"
    }
}