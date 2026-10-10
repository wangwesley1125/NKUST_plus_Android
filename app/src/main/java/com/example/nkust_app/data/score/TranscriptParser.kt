package com.example.nkust_app.data.score

object TranscriptParser {

    // 格式：<課程名稱> <必修|選修> <學分> <成績>
    private val courseRegex = Regex("""^(.+?)\s+(必修|選修)\s+(\d+\.?\d*)\s+(\d{1,3})$""")

    fun parse(text: String): TranscriptData {
        var data = TranscriptData()
        val courses = mutableListOf<TranscriptCourse>()

        text.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .forEach { line ->
                // 科系 / 班級
                if ("科系所：" in line) {
                    val dept = line.substringAfter("科系所：")
                        .substringBefore("班 級")
                        .trim()
                    data = data.copy(department = dept)
                    if ("班 級：" in line) {
                        data = data.copy(classInfo = line.substringAfter("班 級：").trim())
                    }
                }

                // 學號 / 姓名
                if ("學 號：" in line) {
                    val parts = line.split("姓 名：")
                    if (parts.size == 2) {
                        data = data.copy(
                            studentId = parts[0].replace("學 號：", "").trim(),
                            studentName = parts[1].trim()
                        )
                    }
                }

                // 科目行
                courseRegex.find(line)?.let { match ->
                    val (name, type, creditStr, scoreStr) = match.destructured
                    val credits = creditStr.toDoubleOrNull()
                    val score = scoreStr.toIntOrNull()
                    if (credits != null && score != null) {
                        courses += TranscriptCourse(name, type, credits, score)
                    }
                }

                // 摘要欄位
                if ("修習學分：" in line) data = data.copy(requiredCredits = extractDouble("修習學分：", line) ?: 0.0)
                if ("實得學分：" in line) data = data.copy(earnedCredits = extractDouble("實得學分：", line) ?: 0.0)
                if ("學業成績：" in line) data = data.copy(academicScore = extractDouble("學業成績：", line) ?: 0.0)
                if ("操行成績：" in line) data = data.copy(conductScore = extractDouble("操行成績：", line) ?: 0.0)
                if ("班 排 名：" in line || "班排名：" in line) {
                    data = data.copy(classRank = line.substringAfter("：").trim())
                }
            }

        return data.copy(courses = courses)
    }

    private fun extractDouble(prefix: String, line: String): Double? =
        line.substringAfter(prefix)
            .trim()
            .split(Regex("\\s+"))
            .firstOrNull()
            ?.toDoubleOrNull()
}