package com.example.nkust_app.data.graduation

import com.example.nkust_app.data.SessionStorage
import com.example.nkust_app.data.network.NkustClient
import com.example.nkust_app.data.network.SessionExpiredException

/**
 * 抓「畢業修課狀況預覽」（Kendo TabStrip 用 AJAX 載入）
 * - 課規課程修課狀況：/StudyStatusPreview/CourseStructureView
 * - 課規課程修課學分狀況：/StudyStatusPreview/HierarchyStudyCheckView
 */
class GraduationService(private val client: NkustClient) {

    private val basePath = "/student/Graduation/StudyStatusPreview"

    /** 課規課程修課學分狀況（首頁學分卡、詳細頁） */
    suspend fun fetchStatus(): GraduationStatus =
        GraduationParser.parse(fetchFragment("/HierarchyStudyCheckView"))

    /** 課規課程修課狀況（課程規劃表，回傳 HTML 片段） */
    suspend fun fetchCourseStructure(): String = fetchFragment("/CourseStructureView")

    private suspend fun fetchFragment(path: String): String {
        val html = client.get(
            basePath + path,
            headers = mapOf(
                "X-Requested-With" to "XMLHttpRequest",
                "Referer" to SessionStorage.BASE_URL + basePath
            )
        )
        // 拿到的是登入頁而不是資料
        if ("Account/Login" in html && "課規" !in html) throw SessionExpiredException()
        return html
    }
}