package com.example.nkust_app.data.graduation

import android.util.Log
import org.json.JSONObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.IOException

private const val TAG = "NKUST"

object GraduationParser {

    fun parse(html: String): GraduationStatus {
        val doc = Jsoup.parse(html)

        val groups = section("課規課群比對結果", doc)
        val summary = section("修課學分結果預覽", doc)
        if (groups.isEmpty() && summary.isEmpty()) throw IOException("找不到畢業學分資料")

        return GraduationStatus(
            groups = groups,
            summary = summary,
            // 下面兩區解析失敗不影響主要資料
            otherCourses = parseOtherCourses(html),
            englishThreshold = parseEnglishThreshold(doc)
        )
    }

    /** 找到標題所在區塊，往上最多 5 層找第一個直接子元素 <ul> */
    private fun section(title: String, doc: Document): List<CreditItem> {
        var node: Element = doc.selectFirst(":containsOwn($title)") ?: return emptyList()
        repeat(5) {
            node.children().firstOrNull { it.tagName() == "ul" }?.let { return parseList(it) }
            node = node.parent() ?: return emptyList()
        }
        return emptyList()
    }

    private fun parseList(ul: Element): List<CreditItem> =
        ul.children().filter { it.tagName() == "li" }.map { parseItem(it) }

    /** 解析單一 <li>（遞迴處理子清單） */
    private fun parseItem(li: Element): CreditItem {
        val childUl = li.children().firstOrNull { it.tagName() == "ul" }

        // 複製一份並移除子清單，只留下這一層自己的內容
        val own = li.clone()
        own.children().filter { it.tagName() == "ul" }.forEach { it.remove() }
        val text = own.text()

        return CreditItem(
            name = text.substringBefore("（").trim(),
            status = status(own),
            required = number("需通過學分數", text),
            earned = number("已通過學分數", text) ?: passedCredits(text),
            requiredCourses = number("需通過課程數", text),
            passedCourses = number("已通過課程數", text),
            children = childUl?.let { parseList(it) }.orEmpty()
        )
    }

    /** 看第一個 <i> 圖示的 class */
    private fun status(element: Element): CreditItem.Status {
        val cls = element.selectFirst("i")?.className()?.lowercase() ?: return CreditItem.Status.Unknown
        return when {
            "times" in cls || "xmark" in cls -> CreditItem.Status.Failed
            "square" in cls -> CreditItem.Status.Conditional
            "check" in cls -> CreditItem.Status.Passed
            "info" in cls -> CreditItem.Status.Info
            else -> CreditItem.Status.Unknown
        }
    }

    /** 「需通過學分數：12」 */
    private fun number(label: String, text: String): Int? =
        Regex("$label[：:]\\s*\\d+").find(text)?.value?.filter { it.isDigit() }?.toIntOrNull()

    /** 「（已通過35學分）」 */
    private fun passedCredits(text: String): Int? =
        Regex("已通過\\s*\\d+\\s*學分").find(text)?.value?.filter { it.isDigit() }?.toIntOrNull()

    // ── 其他課規外修習課程（Kendo Grid 內嵌 JSON） ──

    private fun parseOtherCourses(html: String): List<OtherCourse> = try {
        val json = extractKendoData(html, "OtherStudiesGrid") ?: return emptyList()
        val array = JSONObject(json).optJSONArray("Data") ?: return emptyList()
        (0 until array.length()).mapNotNull { i ->
            array.optJSONObject(i)?.let { o ->
                OtherCourse(
                    electiveKey = o.optInt("ElectiveKey"),
                    schoolYear = o.optInt("SchoolYear"),
                    semester = o.optInt("Semester"),
                    selectType = o.str("SelectType"),
                    credit = o.dbl("Credit"),
                    orgCredit = o.dbl("OrgCredit"),
                    openDeptName = o.str("OpenDeptName"),
                    courseName = o.str("CourseName"),
                    gradeScoreText = o.str("GradeScoreText"),
                    withdraw = o.str("Withdraw"),
                    isGraduCredit = o.bool("IsGraduCredit"),
                    isTotallyPass = o.bool("IsTotallyPass"),
                    vaildKindName = o.str("VaildKindName")
                )
            }
        }
    } catch (e: Exception) {
        Log.w(TAG, "其他課規外：解析失敗", e)
        emptyList()
    }

    // JSON 裡的 null 要當成 null，不能變成字串 "null"
    private fun JSONObject.str(key: String): String? = if (isNull(key)) null else optString(key)
    private fun JSONObject.dbl(key: String): Double? = if (isNull(key)) null else optDouble(key)
    private fun JSONObject.bool(key: String): Boolean? = if (isNull(key)) null else optBoolean(key)

    /** 從 HTML 中取出 `"data":{...}`（大括號配對，略過字串內的括號） */
    private fun extractKendoData(html: String, gridId: String): String? {
        val gridIndex = html.indexOf("\"#$gridId\"")
        if (gridIndex < 0) return null
        val keyIndex = html.indexOf("\"data\":{\"Data\":", gridIndex)
        if (keyIndex < 0) return null

        val start = keyIndex + "\"data\":".length
        var depth = 0
        var inString = false
        var escaped = false

        for (i in start until html.length) {
            val ch = html[i]
            if (inString) {
                when {
                    escaped -> escaped = false
                    ch == '\\' -> escaped = true
                    ch == '"' -> inString = false
                }
                continue
            }
            when (ch) {
                '"' -> inString = true
                '{' -> depth++
                '}' -> {
                    depth--
                    if (depth == 0) return html.substring(start, i + 1)
                }
            }
        }
        return null
    }

    // ── 校定英語畢業門檻 ──

    private fun parseEnglishThreshold(doc: Document): EnglishThreshold? {
        val label = doc.selectFirst("#langStatus") ?: return null
        val text = label.text().trim()
        if (text.isEmpty()) return null
        val isPassed = label.select(".text-success").isNotEmpty() ||
                ("通過" in text && "未通過" !in text)
        return EnglishThreshold(text, isPassed)
    }
}