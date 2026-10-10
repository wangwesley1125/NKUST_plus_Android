package com.example.nkust_app.ui.course

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nkust_app.data.SettingsStorage
import com.example.nkust_app.data.course.Course
import com.example.nkust_app.data.course.CourseParser
import com.example.nkust_app.data.course.CourseSemester
import kotlinx.coroutines.launch
import java.util.Calendar
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.automirrored.filled.NoteAdd
import com.example.nkust_app.data.course.NoteStore
import com.example.nkust_app.data.course.noteKey
import com.example.nkust_app.ui.main.TabBarBottomSpace

private val WEEKDAYS = listOf("週一", "週二", "週三", "週四", "週五", "週六", "週日")
private val GRID_WEEKDAYS = listOf("一", "二", "三", "四", "五", "六", "日")
private val CardShape = RoundedCornerShape(14.dp)

@Composable
fun CourseScreen(
    onSessionExpired: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CourseViewModel = viewModel()
) {
    val state = viewModel.uiState
    val context = LocalContext.current
    val settings = remember { SettingsStorage(context) }

    var showGrid by remember { mutableStateOf(settings.showGrid) }
    var showEmptyPeriods by remember { mutableStateOf(false) }

    LaunchedEffect(state.sessionExpired) {
        if (state.sessionExpired) onSessionExpired()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            CourseTopBar(
                semesters = state.semesters,
                selectedSemester = state.selectedSemester,
                onSelectSemester = viewModel::selectSemester,
                showGrid = showGrid,
                onToggleGrid = {
                    showGrid = !showGrid
                    settings.showGrid = showGrid
                },
                showEmptyPeriods = showEmptyPeriods,
                onToggleEmpty = { showEmptyPeriods = !showEmptyPeriods }
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                state.isLoading -> Column(
                    Modifier.align(Alignment.Center),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator()
                    Text("載入課表中...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                state.error != null -> Column(
                    Modifier.align(Alignment.Center).padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("錯誤：${state.error}", color = MaterialTheme.colorScheme.error)
                    Button(onClick = viewModel::load) { Text("重新載入") }
                }

                showGrid -> WeekGrid(courses = state.courses)

                else -> WeekPager(courses = state.courses, showEmptyPeriods = showEmptyPeriods)
            }
        }
    }
}

// ═══════════════ 標題列 ═══════════════

@Composable
private fun CourseTopBar(
    semesters: List<CourseSemester>,
    selectedSemester: CourseSemester?,
    onSelectSemester: (CourseSemester) -> Unit,
    showGrid: Boolean,
    onToggleGrid: () -> Unit,
    showEmptyPeriods: Boolean,
    onToggleEmpty: () -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // 左：列表 / 格狀切換
        CircleIconButton(
            icon = if (showGrid) Icons.AutoMirrored.Filled.ViewList else Icons.Filled.Apps,
            contentDescription = if (showGrid) "列表模式" else "格狀模式",
            onClick = onToggleGrid,
            modifier = Modifier.align(Alignment.CenterStart)
        )

        // 中：學期選單
        SemesterMenu(
            semesters = semesters,
            selected = selectedSemester,
            onSelect = onSelectSemester,
            modifier = Modifier.align(Alignment.Center)
        )

        // 右：顯示 / 隱藏空堂（格狀模式時停用）
        CircleIconButton(
            icon = if (showEmptyPeriods) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
            contentDescription = if (showEmptyPeriods) "隱藏空堂" else "顯示空堂",
            onClick = onToggleEmpty,
            enabled = !showGrid,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}

@Composable
private fun CircleIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    FilledIconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.size(48.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
            contentColor = MaterialTheme.colorScheme.primary
        )
    ) {
        Icon(icon, contentDescription = contentDescription)
    }
}

@Composable
private fun SemesterMenu(
    semesters: List<CourseSemester>,
    selected: CourseSemester?,
    onSelect: (CourseSemester) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .clickable(enabled = semesters.isNotEmpty()) { expanded = true }
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                selected?.text ?: "我的課表",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            if (semesters.isNotEmpty()) {
                Icon(
                    Icons.Filled.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            semesters.forEach { semester ->
                DropdownMenuItem(
                    text = { Text(semester.text) },
                    onClick = {
                        expanded = false
                        onSelect(semester)
                    },
                    trailingIcon = if (semester == selected) {
                        { Icon(Icons.Filled.Check, contentDescription = null) }
                    } else null
                )
            }
        }
    }
}

// ═══════════════ 列表模式 ═══════════════

@Composable
private fun WeekPager(courses: List<Course>, showEmptyPeriods: Boolean) {
    val pagerState = rememberPagerState(initialPage = todayIndex()) { WEEKDAYS.size }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize()) {
        // 星期膠囊
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WEEKDAYS.forEachIndexed { index, name ->
                val selected = pagerState.currentPage == index
                Text(
                    name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(
                            if (selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable { scope.launch { pagerState.animateScrollToPage(index) } }
                        .padding(horizontal = 18.dp, vertical = 10.dp)
                )
            }
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            DayCourseList(
                courses = courses.filter { it.weekday == page },
                showEmptyPeriods = showEmptyPeriods
            )
        }
    }
}

/** 今天是第幾頁（週一 = 0 … 週日 = 6），同 iOS */
private fun todayIndex(): Int {
    val day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    return if (day == Calendar.SUNDAY) 6 else day - 2
}

@Composable
private fun DayCourseList(courses: List<Course>, showEmptyPeriods: Boolean) {
    if (courses.isEmpty()) {
        Column(
            Modifier.fillMaxSize().padding(top = 200.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Filled.Coffee,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                modifier = Modifier.size(50.dp)
            )
            Text(
                "今天沒有課！",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    val rows = CourseParser.periods.mapNotNull { period ->
        val course = courses.firstOrNull { it.period == period }
        if (course != null || showEmptyPeriods) period to course else null
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = TabBarBottomSpace),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(rows, key = { it.first }) { (period, course) ->
            if (course != null) CourseCard(course) else EmptyPeriodCard(period)
        }
    }
}

@Composable
private fun PeriodColumn(period: String, background: Color) {
    Column(
        modifier = Modifier
            .width(64.dp)
            .fillMaxHeight()
            .background(background)
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(period, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        CourseParser.periodTimes[period]?.let { (start, end) ->
            Text(start, fontSize = 11.sp, lineHeight = 13.sp)
            Text("|", fontSize = 8.sp, lineHeight = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(end, fontSize = 11.sp, lineHeight = 13.sp)
        }
    }
}

@Composable
private fun CourseCard(course: Course) {
    val context = LocalContext.current
    val noteStore = remember { NoteStore(context) }
    val noteKey = course.noteKey()
    var note by remember(noteKey) { mutableStateOf(noteStore.load(noteKey)) }
    var showNoteSheet by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .shadow(3.dp, CardShape)
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable { showNoteSheet = true },
        verticalAlignment = Alignment.CenterVertically
    ) {
        PeriodColumn(course.period, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))

        Column(
            Modifier.weight(1f).padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                course.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconLabel(Icons.Filled.Person, course.teacher, MaterialTheme.colorScheme.onSurfaceVariant)
                IconLabel(Icons.Filled.LocationOn, course.room, MaterialTheme.colorScheme.primary)
            }
            // 備注預覽
            if (note.isNotEmpty()) {
                Text(
                    note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // 右側備注圖示（對應 note.text.badge.plus / note.text）
        Icon(
            if (note.isEmpty()) Icons.AutoMirrored.Filled.NoteAdd else Icons.Filled.Description,
            contentDescription = "備注",
            tint = if (note.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            else MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(end = 14.dp).size(22.dp)
        )
    }

    if (showNoteSheet) {
        NoteSheet(
            courseName = course.name,
            initialNote = note,
            onSave = {
                note = it
                noteStore.save(noteKey, it)
                showNoteSheet = false
            },
            onDismiss = { showNoteSheet = false }
        )
    }
}

@Composable
private fun EmptyPeriodCard(period: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, CardShape),
        verticalAlignment = Alignment.CenterVertically
    ) {
        PeriodColumn(period, MaterialTheme.colorScheme.surfaceContainerLow)
        Text(
            "空堂",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

@Composable
private fun IconLabel(icon: ImageVector, text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(18.dp))
        Text(text, style = MaterialTheme.typography.bodyLarge, color = color)
    }
}

// ═══════════════ 格狀模式（WeekGridView） ═══════════════

private val GridColWidth = 70.dp
private val GridRowHeight = 70.dp
private val GridHeaderWidth = 36.dp

@Composable
private fun WeekGrid(courses: List<Course>) {
    var detailCourse by remember { mutableStateOf<Course?>(null) }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .horizontalScroll(rememberScrollState())
            .padding(bottom = TabBarBottomSpace)
    ) {
        // 表頭：一 二 三 …
        Row {
            Spacer(Modifier.size(GridHeaderWidth, 36.dp))
            GRID_WEEKDAYS.forEach { day ->
                Box(Modifier.size(GridColWidth, 36.dp), contentAlignment = Alignment.Center) {
                    Text(day, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                }
            }
        }

        // 每一節
        CourseParser.periods.forEach { period ->
            Row {
                Column(
                    Modifier.size(GridHeaderWidth, GridRowHeight),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically)
                ) {
                    CourseParser.periodTimes[period]?.let { (start, end) ->
                        Text(start, fontSize = 10.sp, lineHeight = 11.sp)
                        Text(
                            period,
                            fontSize = 11.sp,
                            lineHeight = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(end, fontSize = 10.sp, lineHeight = 11.sp)
                    }
                }
                for (day in 0 until 7) {
                    val course = courses.firstOrNull { it.weekday == day && it.period == period }
                    GridCell(
                        course = course,
                        onClick = { detailCourse = course },
                        modifier = Modifier.size(GridColWidth, GridRowHeight)
                    )
                }
            }
        }
    }

    detailCourse?.let { course ->
        CourseDetailSheet(course = course, onDismiss = { detailCourse = null })
    }
}

@Composable
private fun GridCell(course: Course?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = modifier
            .padding(3.dp)
            .clip(shape)
            .background(
                course?.let { courseColor(it.name).copy(alpha = 0.85f) }
                    ?: MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
            .then(if (course != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        if (course != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    course.name,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 12.sp
                )
                Text(
                    course.room,
                    fontSize = 9.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    maxLines = 1
                )
            }
        }
    }
}

/** 同 iOS：用課名的字元碼總和決定顏色，同一門課永遠同色 */
private val GridColors = listOf(
    Color(0xFF30B0C7), // teal
    Color(0xFF007AFF), // blue
    Color(0xFF5856D6), // indigo
    Color(0xFFAF52DE), // purple
    Color(0xFFFF2D55), // pink
    Color(0xFFFF9500), // orange
    Color(0xFF00C7BE), // mint
    Color(0xFF32ADE6), // cyan
    Color(0xFF34C759), // green
    Color(0xFF6699FF), // 淡藍紫
    Color(0xFFFF9933), // 暖橘
    Color(0xFF66CC99)  // 薄荷綠
)

private fun courseColor(name: String): Color =
    GridColors[name.sumOf { it.code } % GridColors.size]

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CourseDetailSheet(course: Course, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(horizontal = 20.dp).padding(bottom = 32.dp)) {
            Box(Modifier.fillMaxWidth().padding(bottom = 12.dp)) {
                Text(
                    course.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterEnd)) {
                    Text("完成", fontWeight = FontWeight.Bold)
                }
            }
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            ) {
                DetailRow(Icons.Filled.Book, "課程", course.name)
                HorizontalDivider(Modifier.padding(start = 52.dp), color = MaterialTheme.colorScheme.outlineVariant)
                DetailRow(Icons.Filled.Person, "老師", course.teacher)
                HorizontalDivider(Modifier.padding(start = 52.dp), color = MaterialTheme.colorScheme.outlineVariant)
                DetailRow(Icons.Filled.LocationOn, "教室", course.room)
                HorizontalDivider(Modifier.padding(start = 52.dp), color = MaterialTheme.colorScheme.outlineVariant)
                DetailRow(Icons.Filled.Schedule, "節次", course.period)
            }
        }
    }
}

@Composable
private fun DetailRow(icon: ImageVector, label: String, value: String) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(22.dp))
        Spacer(Modifier.width(14.dp))
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.weight(1f))
        Text(value, fontWeight = FontWeight.Bold)
    }
}