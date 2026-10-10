package com.example.nkust_app.data.course

import org.jsoup.Jsoup

object CourseParser {

    val periods = listOf(
        "M", "1", "2", "3", "4", "A", "5", "6", "7", "8", "9", "10", "11", "12", "13"
    )

    val periodTimes: Map<String, Pair<String, String>> = mapOf(
        "M" to ("07:10" to "08:00"), "1" to ("08:10" to "09:00"), "2" to ("09:10" to "10:00"),
        "3" to ("10:10" to "11:00"), "4" to ("11:10" to "12:00"), "A" to ("12:10" to "13:00"),
        "5" to ("13:30" to "14:20"), "6" to ("14:30" to "15:20"), "7" to ("15:30" to "16:20"),
        "8" to ("16:30" to "17:20"), "9" to ("17:30" to "18:20"), "10" to ("18:30" to "19:20"),
        "11" to ("19:25" to "20:15"), "12" to ("20:20" to "21:10"), "13" to ("21:15" to "22:05")
    )

    fun parse(html: String): List<Course> {
        val doc = Jsoup.parse(html)
        val rows = doc.select("#detail-table tbody tr")
        val courses = mutableListOf<Course>()

        rows.forEachIndexed { rowIndex, row ->
            if (rowIndex >= periods.size) return@forEachIndexed
            val period = periods[rowIndex]

            val cells = row.select("td")
            if (cells.size != 8) return@forEachIndexed

            for (weekday in 0 until 7) {
                val cell = cells[weekday + 1]
                val courseName = cell.selectFirst("a")?.text().orEmpty()
                if (courseName.isEmpty()) continue

                // cell 文字格式：「課名 老師 教室」，先移掉課名（可能含空白）
                val parts = cell.text().split(" ").filter { it.isNotEmpty() }.toMutableList()
                val nameWords = courseName.split(" ").size
                repeat(minOf(nameWords, parts.size)) { parts.removeAt(0) }

                courses += Course(
                    name = courseName,
                    teacher = parts.getOrElse(0) { "" },
                    room = parts.getOrElse(1) { "" },
                    period = period,
                    weekday = weekday
                )
            }
        }
        return courses
    }
}