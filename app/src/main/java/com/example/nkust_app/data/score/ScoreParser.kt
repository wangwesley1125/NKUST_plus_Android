package com.example.nkust_app.data.score

import org.jsoup.Jsoup

object ScoreParser {

    /**
     * @param scoreLabels 成績欄候選的 data-label，挑第一個存在的。
     * 期中 ["期中成績"]，當學期 ["學期成績", "成績", "當學期成績", "期末成績"]
     */
    fun parse(html: String, scoreLabels: List<String>): List<ScoreRecord> {
        val doc = Jsoup.parse(html)

        return doc.select("tr").mapNotNull { row ->
            // 把整列的 td 收成 [data-label: 純文字]
            val cells = row.select("td[data-label]")
                .associate { it.attr("data-label") to it.text().trim() }

            val year = cells["學年"].orEmpty()
            if (year.isEmpty() || year.toIntOrNull() == null) return@mapNotNull null
            val credits = cells["學分"]?.toDoubleOrNull() ?: return@mapNotNull null

            val scoreKey = scoreLabels.firstOrNull { it in cells }

            ScoreRecord(
                year = year,
                term = cells["學期"].orEmpty(),
                courseCode = cells["課號"].orEmpty(),
                name = cells["課程名稱"].orEmpty(),
                type = cells["修別"].orEmpty(),
                credits = credits,
                score = scoreKey?.let { cells[it] }.orEmpty()
            )
        }
    }
}