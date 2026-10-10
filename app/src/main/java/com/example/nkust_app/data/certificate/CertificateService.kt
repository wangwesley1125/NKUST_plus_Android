package com.example.nkust_app.data.certificate

import com.example.nkust_app.data.network.NkustClient

/** 學校新系統有分中文和英文版 */
enum class CertificateLanguage(val path: String, val label: String) {
    Chinese("/student/Doc/Status/ChinesePDF", "中文"),
    English("/student/Doc/Status/EnglishPDF", "English")
}

class CertificateService(private val client: NkustClient) {

    /** 回傳 PDF 位元組；伺服器回傳的不是 PDF 時回傳 null（例如沒有英文姓名） */
    suspend fun fetch(language: CertificateLanguage): ByteArray? {
        val headers = buildMap {
            put("X-Requested-With", "XMLHttpRequest")
            put("Accept", "application/pdf")
            client.decodedXsrfToken()?.let { put("X-XSRF-TOKEN", it) }
        }
        val bytes = client.getBytes(language.path, refererPath = "", headers = headers)
        val isPdf = bytes.size > 4 && String(bytes, 0, 4, Charsets.US_ASCII) == "%PDF"
        return if (isPdf) bytes else null
    }
}