package com.example.nkust_app.ui.rating

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nkust_app.data.rating.CourseRatingRepository
import com.example.nkust_app.data.rating.CourseStat
import com.example.nkust_app.data.rating.RatableCourse
import com.example.nkust_app.data.rating.RatingLevel
import com.example.nkust_app.data.rating.RatingSubmitResult
import com.example.nkust_app.data.rating.RatingSummary
import com.example.nkust_app.data.rating.normalizeCourseText
import com.example.nkust_app.ui.components.SegmentedPicker
import com.example.nkust_app.ui.theme.IosOrange
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/*
 * 課程推薦的共用元件（對應 iOS CourseRatingSheet.swift）
 *   - RatingDot：等級顏色小圓點
 *   - RecommendButton：成績列右邊的「推薦」/「已推薦」按鈕
 *   - CourseRatingSheet：按下推薦後從下方滑上來的評分表單
 *   - RatingBreakdownView：各等級人數長條（評分表單、課程推薦頁共用）
 */

// ═══════ 顏色小圓點 ═══════

@Composable
fun RatingDot(color: Color, size: Dp = 8.dp) {
    Box(
        Modifier
            .size(size)
            .clip(CircleShape)
            .background(color)
    )
}

// ═══════ 「推薦」按鈕（推薦過就變灰色「已推薦」，不能再按） ═══════

@Composable
fun RecommendButton(
    courseKey: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val repo = CourseRatingRepository
    val rated = repo.isRated(courseKey)
    val busy = repo.isSubmitting(courseKey)
    val enabled = !rated && !busy && repo.studentId.isNotEmpty()

    val background = when {
        rated -> MaterialTheme.colorScheme.surfaceVariant
        enabled || busy -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)   // 學籍還沒載入
    }
    val content = if (rated) MaterialTheme.colorScheme.onSurfaceVariant else Color.White

    Box(
        modifier
            .size(width = 52.dp, height = 28.dp)
            .clip(CircleShape)
            .background(background)
            .clickable(enabled = enabled, role = Role.Button, onClick = onClick)
            .semantics { contentDescription = if (rated) "已推薦過這門課" else "推薦這門課" },
        contentAlignment = Alignment.Center
    ) {
        if (busy) {
            CircularProgressIndicator(
                modifier = Modifier.size(14.dp),
                color = Color.White,
                strokeWidth = 2.dp
            )
        } else {
            Text(
                if (rated) "已推薦" else "推薦",
                color = content,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
        }
    }
}

// ═══════ 評分表單 ═══════

/**
 * @param teacher 從課表偵測到的老師（對不到時為空字串），會自動填進「授課教師」欄位
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseRatingSheet(
    course: RatableCourse,
    teacher: String,
    onDismiss: () -> Unit
) {
    val repo = CourseRatingRepository
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val focus = LocalFocusManager.current
    val haptic = LocalHapticFeedback.current

    var teacherName by remember(course.courseKey) { mutableStateOf(teacher) }
    var teacherSuggestions by remember { mutableStateOf<List<String>>(emptyList()) }
    var selected by remember { mutableStateOf<RatingLevel?>(null) }
    var comment by remember { mutableStateOf("") }
    var stat by remember { mutableStateOf<CourseStat?>(null) }
    var isLoadingStat by remember { mutableStateOf(true) }
    var isSubmitting by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    /** 統計顯示：false = 全校、true = 本系 */
    var showDepartment by remember { mutableStateOf(false) }
    /** 送出前的確認視窗（送出後無法修改，讓使用者再看一眼） */
    var confirmLevel by remember { mutableStateOf<RatingLevel?>(null) }

    val current = repo.rating(course.courseKey)
    /** 學號還沒載入前不能評（伺服器需要學號判斷一人一票） */
    val canRate = repo.studentId.isNotEmpty()
    val editable = canRate && !isSubmitting && current == null

    val trimmedComment = comment.trim()
    val commentTooLong = trimmedComment.length > CourseRatingRepository.COMMENT_MAX_LENGTH
    val trimmedTeacher = teacherName.trim()
    val teacherTooLong = trimmedTeacher.length > CourseRatingRepository.TEACHER_MAX_LENGTH
    val isAutoDetected = teacher.isNotEmpty() && trimmedTeacher == teacher
    val departmentName = repo.departmentName

    // 其他同學填過的老師（點一下帶入，讓大家寫法一致）
    LaunchedEffect(course.courseKey) {
        val target = normalizeCourseText(course.name)
        val courses = runCatching { repo.browse(department = "", query = course.name) }.getOrNull()
            ?: return@LaunchedEffect
        teacherSuggestions = courses
            .filter { normalizeCourseText(it.name) == target && it.teacher.isNotEmpty() }
            .sortedByDescending { it.summary.total }       // 評分多的老師排前面
            .map { it.teacher }
            .distinct()
    }

    // 老師改了就重新查統計（打字時稍等一下再查）
    LaunchedEffect(normalizeCourseText(teacherName)) {
        if (trimmedTeacher != teacher) delay(400)
        val result = repo.fetchStat(course.name, teacherName.trim())
        stat = result
        // 本系有人評就預設顯示本系，比較貼近自己
        if (isLoadingStat && (result?.dept?.total ?: 0) > 0) showDepartment = true
        isLoadingStat = false
    }

    fun close() {
        scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
    }

    fun submit(level: RatingLevel) {
        focus.clearFocus()
        isSubmitting = true
        errorText = null
        scope.launch {
            val result = repo.setRating(level, trimmedComment, course, trimmedTeacher)
            isSubmitting = false
            when (result) {
                RatingSubmitResult.Success -> close()
                RatingSubmitResult.SavedOffline -> errorText = "目前無法連線，已先存在手機，之後會自動補傳"
                is RatingSubmitResult.Rejected -> errorText = "${result.message}，請修改後再送出"
                RatingSubmitResult.AlreadyRated -> errorText = "你已經推薦過這門課了"
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(Modifier.fillMaxWidth()) {
            // 可捲動的內容
            Column(
                Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp)
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                // ─── 課名 ───
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(course.name, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(
                        if (course.courseCode.isEmpty())
                            "${course.year} 學年第 ${course.term} 學期・${course.type}"
                        else
                            "${course.year} 學年第 ${course.term} 學期・課號 ${course.courseCode}・${course.type}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // ─── 授課教師 ───
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SectionTitle("授課教師")
                        Spacer(Modifier.width(6.dp))
                        if (isAutoDetected) {
                            Icon(
                                Icons.Filled.CheckCircle, contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(13.dp)
                            )
                            Spacer(Modifier.width(3.dp))
                            Text("已從課表自動帶入", fontSize = 11.sp, color = MaterialTheme.colorScheme.primary)
                        } else {
                            OptionalHint()
                        }
                    }

                    OutlinedTextField(
                        value = teacherName,
                        onValueChange = { teacherName = it },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = editable,
                        singleLine = true,
                        isError = teacherTooLong,
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text("例如：王小明，多位老師用「、」分開", fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                        trailingIcon = {
                            if (teacherName.isNotEmpty() && editable) {
                                IconButton(onClick = { teacherName = "" }) {
                                    Icon(Icons.Filled.Cancel, contentDescription = "清除")
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, autoCorrectEnabled = false),
                        keyboardActions = KeyboardActions(onDone = { focus.clearFocus() }),
                        supportingText = if (teacherTooLong) {
                            { Text("授課教師最多 ${CourseRatingRepository.TEACHER_MAX_LENGTH} 字") }
                        } else null
                    )

                    // 其他同學填過的老師：點一下帶入
                    val others = teacherSuggestions.filter {
                        normalizeCourseText(it) != normalizeCourseText(teacherName)
                    }
                    if (others.isNotEmpty() && current == null) {
                        Text("其他同學填過的老師", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(
                            Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            others.forEach { name ->
                                Text(
                                    name,
                                    fontSize = 14.sp,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .border(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f), CircleShape)
                                        .clickable(enabled = editable) {
                                            teacherName = name
                                            focus.clearFocus()
                                        }
                                        .padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider()

                // ─── 膠囊選項 ───
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SectionTitle("你會推薦這門課嗎？")
                    RatingLevel.entries.chunked(2).forEach { row ->
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            row.forEach { level ->
                                LevelCapsule(
                                    level = level,
                                    isSelected = selected == level || current == level,
                                    enabled = editable,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        selected = level
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                    if (!canRate) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.HourglassEmpty, contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "正在載入學籍資料，稍後即可評分",
                                fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                // ─── 修課心得 ───
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SectionTitle("你的修課心得")
                        OptionalHint()
                        Spacer(Modifier.weight(1f))
                        Text(
                            "${trimmedComment.length}/${CourseRatingRepository.COMMENT_MAX_LENGTH}",
                            fontSize = 12.sp,
                            color = if (commentTooLong) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedTextField(
                        value = comment,
                        onValueChange = { comment = it },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = editable,
                        isError = commentTooLong,
                        minLines = 3,
                        maxLines = 6,
                        shape = RoundedCornerShape(12.dp),
                        placeholder = { Text("分享老師的教學方式、作業量、考試難度或修課建議…", fontSize = 14.sp) }
                    )
                    Text(
                        "請針對課程內容分享，勿人身攻擊或透露他人個資。被多位同學檢舉的心得會自動隱藏。",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // ─── 同學們怎麼說 ───
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SectionTitle("同學們怎麼說")
                        Spacer(Modifier.weight(1f))
                        if (departmentName.isNotEmpty()) {
                            SegmentedPicker(
                                options = listOf("全校", "本系"),
                                selectedIndex = if (showDepartment) 1 else 0,
                                onSelect = { showDepartment = it == 1 },
                                modifier = Modifier.width(130.dp)
                            )
                        }
                    }
                    when {
                        isLoadingStat -> CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                        stat == null -> Text(
                            "目前無法取得評分統計",
                            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        else -> {
                            val s = stat!!
                            val summary = (if (showDepartment) s.dept else null) ?: s.overall
                            val who = if (showDepartment) "${departmentName}同學" else "全校同學"
                            RatingBreakdownView(summary = summary, who = who)
                        }
                    }
                }

                // ─── 匿名說明 ───
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.VisibilityOff, contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("匿名發表", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Text(
                            if (departmentName.isEmpty())
                                "心得會顯示為「匿名同學」，其他同學看不到你是誰，也不會顯示學號"
                            else
                                "心得會顯示為「${departmentName}・匿名同學」，其他同學看不到你是誰，也不會顯示學號",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.Filled.CheckCircle, contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            // ─── 底部按鈕（固定在下方，不跟著捲） ───
            HorizontalDivider()
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 10.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val message = errorText
                if (message != null) {
                    Text(message, fontSize = 12.sp, color = IosOrange, textAlign = TextAlign.Center)
                } else {
                    Text(
                        "每門課只能推薦一次，送出後無法修改，如需更正請聯絡開發者",
                        fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val level = selected
                Button(
                    onClick = {
                        focus.clearFocus()
                        confirmLevel = level
                    },
                    enabled = level != null && current == null && !commentTooLong &&
                            !teacherTooLong && canRate && !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = level?.color ?: MaterialTheme.colorScheme.primary,
                        contentColor = Color.White
                    )
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text(
                            if (current == null) "送出推薦" else "已推薦",
                            fontSize = 16.sp, fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }

    // 送出前確認
    confirmLevel?.let { level ->
        AlertDialog(
            onDismissRequest = { confirmLevel = null },
            title = { Text("確定要送出嗎？") },
            text = { Text(confirmMessage(course, trimmedTeacher, trimmedComment, level)) },
            confirmButton = {
                TextButton(onClick = {
                    confirmLevel = null
                    submit(level)
                }) { Text("送出", fontWeight = FontWeight.SemiBold) }
            },
            dismissButton = {
                TextButton(onClick = { confirmLevel = null }) { Text("再看看") }
            }
        )
    }
}

/** 確認視窗內容：列出要送出的內容；沒填老師時多提醒一句 */
private fun confirmMessage(course: RatableCourse, teacher: String, comment: String, level: RatingLevel): String =
    buildString {
        append("${course.name}・${teacher.ifEmpty { "老師未提供" }}・${level.title}")
        if (comment.isNotEmpty()) {
            append("\n心得：")
            append(if (comment.length > 30) comment.take(30) + "…" else comment)
        }
        if (teacher.isEmpty()) append("\n\n你還沒填授課教師，其他同學會看到「老師未提供」。")
        append("\n\n送出後無法修改，如需更正請透過「關於」頁面聯絡開發者。")
    }

@Composable
private fun SectionTitle(text: String) {
    Text(text, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
}

@Composable
private fun OptionalHint() {
    Text("（選填）", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

/** 一個等級的膠囊：選中時底色換成等級顏色、圓點變白 */
@Composable
private fun LevelCapsule(
    level: RatingLevel,
    isSelected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background by animateColorAsState(
        if (isSelected) level.color else MaterialTheme.colorScheme.surfaceContainerHighest,
        label = "capsuleBackground"
    )
    val scale by animateFloatAsState(if (isSelected) 1.03f else 1f, label = "capsuleScale")

    Surface(
        onClick = onClick,
        enabled = enabled,
        shape = CircleShape,
        color = background,
        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
        border = if (isSelected) null else BorderStroke(1.dp, level.color.copy(alpha = 0.35f)),
        modifier = modifier.scale(scale)
    ) {
        Row(
            Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            RatingDot(if (isSelected) Color.White else level.color, size = 9.dp)
            Spacer(Modifier.width(6.dp))
            Text(
                level.title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ═══════ 各等級人數長條（評分表單、課程推薦頁共用） ═══════

/**
 * @param who 例如「全校同學」「電子工程系同學」
 */
@Composable
fun RatingBreakdownView(summary: RatingSummary, who: String) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (summary.total > 0 && !summary.hidden) {
            val rateText = summary.recommendRate?.let { "・${Math.round(it * 100)}% 推薦" } ?: ""
            Text(
                "共 ${summary.total} 位${who}評分$rateText",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            RatingLevel.entries.forEach { level ->
                val count = summary.count(level)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Row(Modifier.width(110.dp), verticalAlignment = Alignment.CenterVertically) {
                        RatingDot(level.color)
                        Spacer(Modifier.width(6.dp))
                        Text(level.title, fontSize = 12.sp)
                    }
                    LinearProgressIndicator(
                        progress = { count.toFloat() / maxOf(summary.total, 1) },
                        modifier = Modifier
                            .weight(1f)
                            .height(6.dp),
                        color = level.color,
                        trackColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        strokeCap = androidx.compose.ui.graphics.StrokeCap.Round,
                        gapSize = 0.dp,
                        drawStopIndicator = {}
                    )
                    Text(
                        "$count",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.End,
                        modifier = Modifier.width(28.dp)
                    )
                }
            }
        } else {
            Text(
                "還沒有${who}評分，成為第一個吧！",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}