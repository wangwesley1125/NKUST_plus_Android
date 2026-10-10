package com.example.nkust_app.data.score

import android.content.Context
import android.util.Log
import com.example.nkust_app.data.network.NkustClient
import com.example.nkust_app.data.network.SessionExpiredException
import com.example.nkust_app.data.profile.ProfileService
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URLEncoder

private const val TAG = "NKUST"
private const val TRANSCRIPT_PAGE = "/student/Score/SingleSemesterTranscript"

/** 單學期成績單頁面上的資料：學期清單（新到舊）+ 驗證 Token */
data class TranscriptPage(
    val semesters: List<String>,   // 例如 ["114-2", "114-1", ...]
    val token: String
)

class ScoreService(private val client: NkustClient, context: Context) {

    private val profileService = ProfileService(client)

    init {
        PDFBoxResourceLoader.init(context.applicationContext)
    }

    // ═══════ 期中 / 當學期 ═══════

    suspend fun fetchMidterm(): List<ScoreRecord> =
        ScoreParser.parse(
            html = client.get("/student/Score/MidTerm"),
            scoreLabels = listOf("期中成績")
        )

    suspend fun fetchPresentSemester(): List<ScoreRecord> =
        ScoreParser.parse(
            html = client.get("/student/Score/PresentSemester"),
            scoreLabels = listOf("學期成績", "成績", "當學期成績", "期末成績")
        )

    // ═══════ 歷年成績（成績單 PDF） ═══════

    /** 對應 iOS 的 fetchSemestersAndToken() */
    suspend fun fetchTranscriptPage(): TranscriptPage {
        val html = client.get(TRANSCRIPT_PAGE)

        val semesters = Regex("\"(\\d{3}-\\d)\"")
            .findAll(html)
            .map { it.groupValues[1] }
            .distinct()
            .sortedDescending()
            .toList()

        val token = Regex("name=\"__RequestVerificationToken\"[^>]*value=\"([^\"]+)\"")
            .find(html)?.groupValues?.get(1).orEmpty()

        Log.d(TAG, "成績單：找到 ${semesters.size} 個學期，有 Token = ${token.isNotEmpty()}")
        return TranscriptPage(semesters, token)
    }

    /** 對應 iOS 的 fetchPDF()：下載指定學期的成績單並解析 */
    suspend fun fetchTranscript(ym: String, token: String): TranscriptData {
        val ymForApi = ym.replace("-", "")
        val encodedToken = URLEncoder.encode(token, "UTF-8")
        val path = "$TRANSCRIPT_PAGE/PrintTranscript?YM=$ymForApi&ShowRank=true" +
                "&__RequestVerificationToken=$encodedToken&ShowRank=false"

        val bytes = client.getBytes(path, refererPath = TRANSCRIPT_PAGE)
        val isPdf = bytes.size > 4 && String(bytes, 0, 4, Charsets.US_ASCII) == "%PDF"
        Log.d(TAG, "成績單 $ym：下載 ${bytes.size} bytes，是 PDF = $isPdf")
        if (!isPdf) throw IOException("無法取得成績單，請重試")

        val text = extractPdfText(bytes)
        val data = TranscriptParser.parse(text).copy(semester = ym)
        // 只記錄解析結果的數量，不印出個人資料
        Log.d(
            TAG,
            "成績單 $ym：文字長度 ${text.length}，解析到 ${data.courses.size} 科，" +
                    "有姓名 = ${data.studentName.isNotEmpty()}，有排名 = ${data.classRank.isNotEmpty()}"
        )
        return data
    }

    // ═══════ 學生資訊卡 ═══════

    /** 先讀最新一學期成績單，讀不到姓名就改用個人資料頁 */
    suspend fun fetchStudentInfo(): TranscriptData? {
        val fromPdf = try {
            val page = fetchTranscriptPage()
            page.semesters.firstOrNull()?.let { fetchTranscript(it, page.token) }
        } catch (e: CancellationException) {
            throw e
        } catch (e: SessionExpiredException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "成績單 PDF 讀取失敗", e)
            null
        }
        if (fromPdf != null && fromPdf.studentName.isNotEmpty()) {
            Log.d(TAG, "學生資料：來自成績單 PDF")
            return fromPdf
        }

        Log.d(TAG, "成績單 PDF 解析不到姓名，改用個人資料頁")
        val profile = profileService.fetchProfile()
        if (profile.name.isEmpty()) return null
        Log.d(TAG, "學生資料：來自個人資料頁")
        return TranscriptData(
            studentId = profile.studentId,
            studentName = profile.name,
            department = profile.department,
            classInfo = profile.className
        )
    }

    /** 對應 iOS 的 PDFDocument.page(at:).string */
    private suspend fun extractPdfText(bytes: ByteArray): String = withContext(Dispatchers.Default) {
        PDDocument.load(bytes).use { doc ->
            PDFTextStripper().apply { setSortByPosition(true) }.getText(doc)
        }
    }
}