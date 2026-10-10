package com.example.nkust_app.data.score

/** 期中、當學期共用（對應 iOS 的 ScoreRecord） */
data class ScoreRecord(
    val year: String,
    val term: String,
    val courseCode: String,
    val name: String,
    val type: String,      // 必修 / 選修
    val credits: Double,
    val score: String      // 可能是「不開放」或數字
)

/** 成績單 PDF 裡的一科（對應 iOS 的 CourseRecord） */
data class TranscriptCourse(
    val name: String,
    val type: String,
    val credits: Double,
    val score: Int
)

/** 成績單 PDF 解析結果（對應 iOS 的 TranscriptData） */
data class TranscriptData(
    val semester: String = "",
    val studentId: String = "",
    val studentName: String = "",
    val department: String = "",
    val classInfo: String = "",
    val courses: List<TranscriptCourse> = emptyList(),
    val earnedCredits: Double = 0.0,
    val requiredCredits: Double = 0.0,
    val academicScore: Double = 0.0,
    val conductScore: Double = 0.0,
    val classRank: String = ""
)