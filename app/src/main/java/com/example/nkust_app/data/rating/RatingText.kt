package com.example.nkust_app.data.rating

import java.text.Normalizer

/*
 * 課程推薦用的文字正規化（對應 iOS 的 normalizeCourseText 等函式）
 *
 * ⚠️ 這裡的規則必須跟 iOS 版、後端完全一樣，否則同一門課會被當成不同課、統計被拆散：
 *   - 課名 / 老師：NFKC（全形轉半形）＋拿掉所有空白
 *   - 科系：再去掉中括號附註，例如「電子工程系[建工|燕巢]」→「電子工程系」
 *   - 同一門課：學年-學期-課名
 */

/** NFKC（全形轉半形）+ 去掉所有空白 */
fun normalizeCourseText(text: String): String =
    Normalizer.normalize(text, Normalizer.Form.NFKC).filterNot { it.isWhitespace() }

/** 「課名 + 老師」的查詢 key（首頁徽章、評分卡片統計用） */
fun courseStatKey(name: String, teacher: String): String =
    normalizeCourseText(name) + "|" + normalizeCourseText(teacher)

private val bracketNote = Regex("""\[[^\]]*]""")

/** 科系：「電子工程系[建工|燕巢]」→「電子工程系」 */
fun normalizeDepartmentName(department: String): String =
    normalizeCourseText(department).replace(bracketNote, "").take(30)

/** 同一學期同一門課（課名）＝同一筆；成績頁（有課號）和歷年成績（沒有課號）推薦同一門課會被視為同一筆 */
fun ratingCourseKey(year: String, term: String, courseName: String): String =
    "$year-$term-${normalizeCourseText(courseName)}"