package com.example.nkust_app.data.graduation

/** 單一課群 / 科目項目（樹狀結構） */
data class CreditItem(
    val name: String,
    val status: Status,
    val required: Int?,          // 需通過學分數
    val earned: Int?,            // 已通過學分數
    val requiredCourses: Int?,   // 需通過課程數（體育、博雅通識）
    val passedCourses: Int?,     // 已通過課程數
    val children: List<CreditItem>
) {
    /** 網頁上的圖示：藍✓ 通過 / 橘☑ 修習後通過 / 紅✗ 未通過 / 藍ⓘ 資訊 */
    enum class Status { Passed, Conditional, Failed, Info, Unknown }

    /** 這一層以及所有子層中，未通過的「最底層」項目 */
    fun failedLeaves(): List<CreditItem> =
        if (children.isEmpty()) {
            if (status == Status.Failed) listOf(this) else emptyList()
        } else {
            children.flatMap { it.failedLeaves() }
        }
}

/** 其他課規外修習課程（只取需要的欄位，原始 JSON 的個資全部忽略） */
data class OtherCourse(
    val electiveKey: Int,
    val schoolYear: Int,
    val semester: Int,
    val selectType: String?,
    val credit: Double?,
    val orgCredit: Double?,
    val openDeptName: String?,
    val courseName: String?,
    val gradeScoreText: String?,
    val withdraw: String?,
    val isGraduCredit: Boolean?,
    val isTotallyPass: Boolean?,
    val vaildKindName: String?
) {
    /** 1 → 上學期、2 → 下學期、3 → 寒修、4 → 暑修 */
    val semesterText: String
        get() = when (semester) {
            1 -> "上學期"
            2 -> "下學期"
            3 -> "寒修"
            4 -> "暑修"
            else -> "第${semester}學期"
        }

    val termText: String get() = "$schoolYear $semesterText"

    val selectTypeText: String
        get() = when (selectType) {
            "M" -> "必修"
            "O" -> "選修"
            else -> selectType.orEmpty()
        }

    /** 抵充（Withdraw == "3"）時顯示「原學分(充 x)」 */
    val creditText: String
        get() {
            val org = format(orgCredit ?: credit ?: 0.0)
            return if (withdraw == "3" && credit != null) "$org(充${format(credit)})" else org
        }

    val isInProgress: Boolean get() = gradeScoreText.isNullOrBlank()

    val gradeText: String get() = if (isInProgress) "修習中" else gradeScoreText!!.trim()

    val reviewText: String
        get() {
            val text = vaildKindName?.trim().orEmpty()
            return if (text.isEmpty() || text == "---") "尚未審認" else text
        }

    private fun format(value: Double): String =
        if (value == Math.floor(value)) value.toInt().toString() else value.toString()
}

/** 校定英語畢業門檻 */
data class EnglishThreshold(
    val resultText: String,
    val isPassed: Boolean
)

/** 整份畢業修課狀況 */
data class GraduationStatus(
    val groups: List<CreditItem> = emptyList(),        // 課規課群比對結果
    val summary: List<CreditItem> = emptyList(),       // 修課學分結果預覽
    val otherCourses: List<OtherCourse> = emptyList(), // 其他課規外修習課程
    val englishThreshold: EnglishThreshold? = null
) {
    /** 畢業審查學分數（需通過 128 / 已通過 130） */
    val total: CreditItem? get() = summary.firstOrNull()

    val unmetItems: List<CreditItem> get() = groups.flatMap { it.failedLeaves() }

    val isAllPassed: Boolean
        get() = groups.isNotEmpty() && groups.all { it.status == CreditItem.Status.Passed }
}