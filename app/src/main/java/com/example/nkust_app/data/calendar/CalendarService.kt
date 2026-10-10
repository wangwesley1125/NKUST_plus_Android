package com.example.nkust_app.data.calendar

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

private const val TAG = "NKUST"

/** CSV 裡的一列：學年度分類 / 中英文分類 / 檔名 / PDF 網址 */
data class CalendarItem(
    val group: String,      // 例："115學年度"
    val subgroup: String,   // 例："中文版"
    val filename: String,   // 例："115學年度第1學期行事曆 (NEW!!)"
    val url: String
)

/** 某學年度某學期的行事曆 PDF */
data class CalendarSemester(
    val year: Int,
    val semester: Int,
    val file: File
) {
    val label: String get() = "$year-$semester"
}

class CalendarService(private val pdfDir: File) {

    private val http = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * 對應 iOS 的 loadCalendar()：
     * CSV → 找出所有學年度 → 每個學年度 × 每個學期同時下載 PDF
     * 回傳的順序：最新學年度在前，同學年度第 1 學期在前
     */
    suspend fun load(): List<CalendarSemester> = coroutineScope {
        val items = fetchItems()
        Log.d(TAG, "行事曆：CSV 解析出 ${items.size} 筆")
        if (items.isEmpty()) throw IOException("CSV 沒有資料")

        val years = items.mapNotNull { extractYear(it.group) }.distinct().sortedDescending()
        Log.d(TAG, "行事曆：找到學年度 $years")
        if (years.isEmpty()) throw IOException("找不到學年度")

        pdfDir.mkdirs()
        val jobs = years.flatMap { year ->
            listOf(1, 2).map { semester ->
                async {
                    val url = matchedUrl(items, year, semester) ?: return@async null
                    downloadPdf(absoluteUrl(url), File(pdfDir, "行事曆_$year-$semester.pdf"))
                        ?.let { CalendarSemester(year, semester, it) }
                }
            }
        }
        jobs.awaitAll().filterNotNull()
    }

    /** 只取「中文版」，並依檔名判斷第幾學期 */
    private fun matchedUrl(items: List<CalendarItem>, year: Int, semester: Int): String? =
        items.firstOrNull {
            it.group == "${year}學年度" &&
                    "中文" in it.subgroup &&
                    "第${semester}學期" in it.filename
        }?.url

    private suspend fun fetchItems(): List<CalendarItem> = withContext(Dispatchers.IO) {
        // 刻意不設定手機版 User-Agent：Google 試算表遇到手機 UA 可能回傳「用 App 開啟」的提示頁
        val request = Request.Builder().url(SHEET_URL).build()
        http.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("CSV 下載失敗（${response.code}）")
            parseCsv(response.body?.string().orEmpty())
        }
    }

    /** 下載單一學期 PDF，失敗回傳 null（某一學期抓不到不影響其他學期） */
    private suspend fun downloadPdf(url: String, target: File): File? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .header("Referer", "https://acad.nkust.edu.tw/p/426-1063-6.php?Lang=zh-tw")
                .header(
                    "User-Agent",
                    "Mozilla/5.0 (Linux; Android 14) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0 Mobile Safari/537.36"
                )
                .header("Accept", "application/pdf,*/*")
                .build()
            http.newCall(request).execute().use { response ->
                val bytes = response.body?.bytes() ?: return@use null
                val isPdf = bytes.size > 4 && String(bytes, 0, 4, Charsets.US_ASCII) == "%PDF"
                if (!response.isSuccessful || !isPdf) {
                    Log.w(TAG, "行事曆 PDF 下載失敗：${response.code}，是 PDF = $isPdf，$url")
                    return@use null
                }
                target.writeBytes(bytes)
                target
            }
        } catch (e: Exception) {
            Log.w(TAG, "行事曆 PDF 下載失敗：$url", e)
            null
        }
    }

    companion object {
        // 學校頁面內嵌的 Google 試算表（CSV 匯出）；換試算表時只要改這裡
        private const val SHEET_URL =
            "https://docs.google.com/spreadsheets/d/e/2PACX-1vSgjFXXnuyCosq2gWkvldnFtNPQl8mDU1d13UVIOx_IInPPeSsTXDUlTThakD_NAZKhM16O_1TwgOlT/pub?gid=1497644044&single=true&output=csv"

        private const val SCHOOL_BASE_URL = "https://acad.nkust.edu.tw"

        /** CSV 裡的網址可能是相對路徑，補上學校網域 */
        private fun absoluteUrl(raw: String): String = when {
            raw.startsWith("http://", ignoreCase = true) || raw.startsWith("https://", ignoreCase = true) -> raw
            raw.startsWith("/") -> SCHOOL_BASE_URL + raw
            else -> "$SCHOOL_BASE_URL/$raw"
        }

        /** "115學年度" → 115 */
        private fun extractYear(group: String): Int? =
            group.takeWhile { it.isDigit() }.toIntOrNull()

        private fun parseCsv(csv: String): List<CalendarItem> {
            val rows = parseCsvRows(csv)
            if (rows.size <= 1) return emptyList()   // 第一列是標題
            return rows.drop(1).mapNotNull { row ->
                if (row.size < 4) return@mapNotNull null
                val group = row[0].trim()
                val url = row[3].trim()
                if (group.isEmpty() || url.isEmpty()) return@mapNotNull null
                CalendarItem(group, row[1].trim(), row[2].trim(), url)
            }
        }

        /** 逐字元解析 CSV，處理雙引號欄位內的逗號、換行、跳脫的雙引號 ("") */
        private fun parseCsvRows(csv: String): List<List<String>> {
            val text = csv.replace("\r\n", "\n").replace('\r', '\n')
            val rows = mutableListOf<List<String>>()
            var row = mutableListOf<String>()
            val field = StringBuilder()
            var insideQuotes = false
            var i = 0

            while (i < text.length) {
                val c = text[i]
                if (insideQuotes) {
                    if (c == '"') {
                        if (i + 1 < text.length && text[i + 1] == '"') {
                            field.append('"')
                            i++
                        } else {
                            insideQuotes = false
                        }
                    } else {
                        field.append(c)
                    }
                } else {
                    when (c) {
                        '"' -> insideQuotes = true
                        ',' -> {
                            row.add(field.toString())
                            field.clear()
                        }
                        '\n' -> {
                            row.add(field.toString())
                            rows.add(row)
                            row = mutableListOf()
                            field.clear()
                        }
                        else -> field.append(c)
                    }
                }
                i++
            }
            if (field.isNotEmpty() || row.isNotEmpty()) {
                row.add(field.toString())
                rows.add(row)
            }
            return rows
        }
    }
}