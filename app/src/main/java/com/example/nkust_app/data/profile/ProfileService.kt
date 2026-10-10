package com.example.nkust_app.data.profile

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import com.example.nkust_app.data.SessionStorage
import com.example.nkust_app.data.network.NkustClient
import org.jsoup.Jsoup

/** 對應 iOS 的 StudentProfile */
data class StudentProfile(
    val name: String = "",
    val englishName: String = "",
    val studentId: String = "",
    val department: String = "",
    val className: String = "",
    val status: String = "",
    val photoUrl: String = ""
)

/** 對應 iOS 的 ProfileService + ProfileParser */
class ProfileService(private val client: NkustClient) {

    private val profilePath = "/student/Register/StudentDataQuery"

    suspend fun fetchProfile(): StudentProfile = parse(client.get(profilePath))

    /** 對應 iOS 的 loadPhoto(from:) */
    suspend fun fetchPhoto(profile: StudentProfile): Bitmap? {
        val url = profile.photoUrl
        if (url.isEmpty()) return null

        val bytes = if (url.startsWith("data:")) {
            Base64.decode(url.substringAfter(","), Base64.DEFAULT)
        } else {
            client.getBytes(url.removePrefix(SessionStorage.BASE_URL), refererPath = profilePath)
        }
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private fun parse(html: String): StudentProfile {
        val doc = Jsoup.parse(html)
        var profile = StudentProfile()

        doc.select(".info-item").forEach { item ->
            val label = item.select(".info-label").text().trim()
            val value = item.select(".info-value").text().trim()
            profile = when (label) {
                "姓名" -> profile.copy(name = value)
                "英文姓名" -> profile.copy(englishName = value)
                "學號" -> profile.copy(studentId = value)
                "科系" -> profile.copy(department = value)
                "班級" -> profile.copy(className = value)
                "在學狀態" -> profile.copy(status = value)
                else -> profile
            }
        }

        doc.selectFirst("img.student-photo")?.attr("src")
            ?.takeIf { it.isNotEmpty() }
            ?.let { src ->
                val url = if (src.startsWith("http") || src.startsWith("data:")) src
                else SessionStorage.BASE_URL + src
                profile = profile.copy(photoUrl = url)
            }

        return profile
    }
}