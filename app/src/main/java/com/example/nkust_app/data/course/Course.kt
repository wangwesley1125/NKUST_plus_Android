package com.example.nkust_app.data.course

data class Course(
    val name: String,
    val teacher: String,
    val room: String,
    val period: String,
    val weekday: Int   // 0 = 週一 … 6 = 週日
)

data class CourseSemester(
    val text: String,
    val value: String
) {
    /** 正規學期（非暑修）：-1 或 -2 */
    val isRegular: Boolean
        get() = value.endsWith("-1") || value.endsWith("-2")
}