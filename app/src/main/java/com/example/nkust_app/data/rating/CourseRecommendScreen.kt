package com.example.nkust_app.ui.rating

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.outlined.Chat
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material.icons.outlined.WifiOff
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nkust_app.data.rating.BrowseCourse
import com.example.nkust_app.data.rating.CourseComment
import com.example.nkust_app.data.rating.CourseRatingRepository
import com.example.nkust_app.data.rating.DepartmentInfo
import com.example.nkust_app.data.rating.RatingLevel
import com.example.nkust_app.data.rating.RatingSummary
import com.example.nkust_app.data.rating.recommendRateColor
import com.example.nkust_app.ui.components.BackTopBar
import com.example.nkust_app.ui.main.TabBarBottomSpace
import com.example.nkust_app.ui.theme.IosBlue
import com.example.nkust_app.ui.theme.IosOrange
import com.example.nkust_app.ui.theme.IosPurple
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/*
 * 課程推薦頁（對應 iOS CourseRecommendView.swift，從首頁左上角進入）
 *   - 最上方：搜尋框（查課名）
 *   - 下方：科系膠囊（全部 / 本系 / 其他有人評過的科系，後端有新科系就自動多一顆）
 *   - 列表：同課名的課集中在一起，底下列出各老師的推薦與心得數
 *   - 點一門課：看該科系（或全校）同學的修課心得，可檢舉、封鎖
 */

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseRecommendScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    val repo = CourseRatingRepository
    val focus = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    var departments by remember { mutableStateOf<List<DepartmentInfo>>(emptyList()) }
    /** 空字串 = 全部（全校） */
    var selectedDepartment by rememberSaveable { mutableStateOf("") }
    var searchText by rememberSaveable { mutableStateOf("") }
    var courses by remember { mutableStateOf<List<BrowseCourse>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var isRefreshing by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var detail by remember { mutableStateOf<BrowseCourse?>(null) }
    /** 改這個值會強制重新載入（重試用） */
    var reloadToken by remember { mutableIntStateOf(0) }

    val myDepartment = repo.departmentName

    /** 膠囊順序：全部 → 本系 → 其他科系（依評分數多到少） */
    val capsuleDepartments = remember(departments, myDepartment) {
        buildList {
            add("")
            if (myDepartment.isNotEmpty()) add(myDepartment)
            addAll(departments.map { it.department }.filter { it != myDepartment })
        }
    }

    /** 同課名集中：依第一次出現的順序分組（後端已依推薦人數排序） */
    val groupedCourses = remember(courses) {
        courses.groupBy { it.name }.toList()
    }

    suspend fun loadDepartments() {
        try {
            departments = repo.fetchDepartments()
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            // 科系清單抓不到就只顯示「全部」和本系
        }
    }

    suspend fun loadCourses() {
        isLoading = true
        try {
            courses = repo.browse(selectedDepartment, searchText)
            loadError = null
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            loadError = "目前無法取得課程推薦"
        }
        isLoading = false
    }

    LaunchedEffect(Unit) { loadDepartments() }

    // 搜尋字或科系改變時重新載入；打字時稍等一下再查，不要每個字都打一次 API
    LaunchedEffect(selectedDepartment, searchText, reloadToken) {
        if (searchText.isNotEmpty()) delay(350)
        loadCourses()
    }

    Scaffold(
        modifier = modifier,
        topBar = { BackTopBar("課程推薦", onBack) }
    ) { padding ->
        Column(
            Modifier
                .padding(top = padding.calculateTopPadding())
                .fillMaxSize()
        ) {
            // ─── 搜尋框 ───
            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text("搜尋課程名稱，例如：工程數學") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchText.isNotEmpty()) {
                        IconButton(onClick = { searchText = "" }) {
                            Icon(Icons.Filled.Cancel, contentDescription = "清除")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search, autoCorrectEnabled = false),
                keyboardActions = KeyboardActions(onSearch = { focus.clearFocus() })
            )

            // ─── 科系膠囊（與校園地圖的校區膠囊同樣式） ───
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                capsuleDepartments.forEach { dept ->
                    val selected = dept == selectedDepartment
                    val textColor = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                    Row(
                        Modifier
                            .clip(CircleShape)
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { selectedDepartment = dept }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (dept.isNotEmpty() && dept == myDepartment) {
                            Icon(
                                Icons.Filled.Star, contentDescription = "本系",
                                tint = textColor, modifier = Modifier.size(14.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                        }
                        Text(
                            dept.ifEmpty { "全部" },
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // ─── 列表 ───
            val error = loadError
            when {
                isLoading && courses.isEmpty() -> CenterMessage {
                    CircularProgressIndicator()
                    Text("載入中…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                error != null && courses.isEmpty() -> CenterMessage {
                    Icon(Icons.Outlined.WifiOff, contentDescription = null, tint = IosOrange, modifier = Modifier.size(40.dp))
                    Text(error, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedButton(onClick = {
                        scope.launch { loadDepartments() }
                        reloadToken++
                    }) { Text("重試") }
                }

                courses.isEmpty() -> CenterMessage {
                    Icon(
                        if (searchText.isEmpty()) Icons.Outlined.ThumbUp else Icons.Filled.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(40.dp)
                    )
                    Text(
                        when {
                            searchText.isNotEmpty() -> "找不到「$searchText」的推薦\n可能還沒有同學評過這門課"
                            selectedDepartment.isEmpty() -> "還沒有人評分\n到成績頁點一門課，成為第一個推薦的人！"
                            else -> "${selectedDepartment}還沒有同學評分"
                        },
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                }

                else -> PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = {
                        scope.launch {
                            isRefreshing = true
                            loadDepartments()
                            loadCourses()
                            isRefreshing = false
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    LazyColumn(
                        Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = 16.dp, top = 12.dp, end = 16.dp, bottom = TabBarBottomSpace
                        ),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        groupedCourses.forEach { (name, items) ->
                            item(key = "header|$name") {
                                Row(
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(start = 4.dp, end = 4.dp, top = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    if (items.size > 1) {
                                        Text(
                                            "${items.size} 位老師",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                            item(key = "group|$name") {
                                Column(
                                    Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                                ) {
                                    items.forEachIndexed { index, course ->
                                        CourseRecommendRow(course) {
                                            focus.clearFocus()
                                            detail = course
                                        }
                                        if (index < items.lastIndex) {
                                            HorizontalDivider(
                                                Modifier.padding(start = 16.dp),
                                                color = MaterialTheme.colorScheme.outlineVariant
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    detail?.let { course ->
        CourseCommentsSheet(
            course = course,
            department = selectedDepartment,
            onDismiss = { detail = null }
        )
    }
}

@Composable
private fun CenterMessage(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier
            .fillMaxSize()
            .padding(24.dp)
            .padding(bottom = TabBarBottomSpace),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        content = content
    )
}

private fun typeColor(type: String): Color = when (type) {
    "必修" -> IosBlue
    "選修" -> IosPurple
    else -> Color.Gray
}

// ═══════ 一門課（某位老師）的列 ═══════

@Composable
private fun CourseRecommendRow(course: BrowseCourse, onClick: () -> Unit) {
    val summary = course.summary

    Column(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.Person, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                course.teacher.ifEmpty { "老師未提供" },
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
            )
            summary.courseTypeLabel?.let { type ->
                Spacer(Modifier.width(8.dp))
                val color = typeColor(type)
                Text(
                    type,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = color,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.12f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
            Spacer(Modifier.weight(1f))
            summary.recommendRate?.let { rate ->
                val color = recommendRateColor(rate)
                RatingDot(color)
                Spacer(Modifier.width(5.dp))
                Text(
                    "${Math.round(rate * 100)}% 推薦",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = color
                )
            }
        }

        LevelStackBar(summary)

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("${summary.total} 人評分", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (summary.comments > 0) {
                Spacer(Modifier.width(12.dp))
                Icon(
                    Icons.AutoMirrored.Outlined.Chat, contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(Modifier.width(3.dp))
                Text(
                    "${summary.comments} 則心得",
                    fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.weight(1f))
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/** 四個等級的比例條（超級推薦 → 還可以 → 普普通通 → 拉完了） */
@Composable
private fun LevelStackBar(summary: RatingSummary) {
    val counts = RatingLevel.entries.map { it to summary.count(it) }.filter { it.second > 0 }
    Box(
        Modifier
            .fillMaxWidth()
            .height(6.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
    ) {
        if (counts.isNotEmpty()) {
            Row(Modifier.fillMaxSize(), horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                counts.forEach { (level, count) ->
                    Box(
                        Modifier
                            .weight(count.toFloat())
                            .fillMaxHeight()
                            .background(level.color)
                    )
                }
            }
        }
    }
}

// ═══════ 課程心得（從下方滑上來） ═══════

private val reportReasons = listOf("人身攻擊或謾罵", "不實或誤導內容", "洩漏個人資料", "廣告或與課程無關")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseCommentsSheet(
    course: BrowseCourse,
    /** 目前選的科系（空字串 = 全校） */
    department: String,
    onDismiss: () -> Unit
) {
    val repo = CourseRatingRepository
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var comments by remember { mutableStateOf<List<CourseComment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var reportTarget by remember { mutableStateOf<CourseComment?>(null) }
    var blockTarget by remember { mutableStateOf<CourseComment?>(null) }

    fun toast(message: String) = Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

    LaunchedEffect(course.id, department) {
        try {
            comments = repo.comments(course.name, course.teacher, department)
            loadError = null
        } catch (e: CancellationException) {
            throw e
        } catch (_: Exception) {
            loadError = "目前無法取得心得"
        }
        isLoading = false
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        LazyColumn(
            Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ─── 課名、老師、統計 ───
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(course.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Person, contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "授課教師：${course.teacher.ifEmpty { "未提供" }}",
                            fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    course.summary.courseTypeLabel?.let { type ->
                        Text(
                            if (department.isEmpty()) "修別：$type" else "${department}修別：$type",
                            fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    RatingBreakdownView(
                        summary = course.summary,
                        who = if (department.isEmpty()) "全校同學" else "${department}同學"
                    )
                }
            }

            item { HorizontalDivider(Modifier.padding(vertical = 4.dp)) }

            item {
                Text(
                    if (department.isEmpty()) "修課心得" else "${department}同學的修課心得",
                    fontSize = 17.sp, fontWeight = FontWeight.SemiBold
                )
            }

            val error = loadError
            when {
                isLoading -> item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                }
                error != null -> item {
                    Text(error, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                comments.isEmpty() -> item {
                    Text("還沒有同學留下心得", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> items(comments, key = { it.id }) { comment ->
                    CommentRow(
                        comment = comment,
                        onReport = { reportTarget = comment },
                        onBlock = { blockTarget = comment }
                    )
                }
            }

            item {
                Text(
                    "心得皆為匿名發表。點心得右上角的 ⋮ 可以檢舉或封鎖；被多位同學檢舉的心得會自動隱藏。",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // ─── 檢舉：選原因 ───
    reportTarget?.let { comment ->
        AlertDialog(
            onDismissRequest = { reportTarget = null },
            title = { Text("檢舉原因") },
            text = {
                Column {
                    Text(
                        if (repo.studentId.isEmpty()) "請先登入後再檢舉" else "多位同學檢舉後，這則心得會自動隱藏",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    reportReasons.forEach { reason ->
                        Text(
                            reason,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .clickable(enabled = repo.studentId.isNotEmpty()) {
                                    reportTarget = null
                                    scope.launch {
                                        try {
                                            if (repo.report(comment, reason)) {
                                                comments = comments.filterNot { it.id == comment.id }
                                            }
                                            toast("已收到檢舉，謝謝你")
                                        } catch (e: CancellationException) {
                                            throw e
                                        } catch (_: Exception) {
                                            toast(if (repo.studentId.isEmpty()) "請先登入後再檢舉" else "檢舉失敗，請稍後再試")
                                        }
                                    }
                                }
                                .padding(vertical = 12.dp, horizontal = 8.dp)
                        )
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { reportTarget = null }) { Text("取消") }
            }
        )
    }

    // ─── 封鎖：確認 ───
    blockTarget?.let { comment ->
        AlertDialog(
            onDismissRequest = { blockTarget = null },
            title = { Text("封鎖這位匿名同學？") },
            text = { Text("之後在這支手機上，所有課程都不會再顯示他的心得") },
            confirmButton = {
                TextButton(onClick = {
                    repo.block(comment.author)
                    comments = comments.filterNot { it.author == comment.author }
                    blockTarget = null
                    toast("已封鎖，之後不會再看到這位同學的心得")
                }) { Text("封鎖", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { blockTarget = null }) { Text("取消") }
            }
        )
    }
}

@Composable
private fun CommentRow(
    comment: CourseComment,
    onReport: () -> Unit,
    onBlock: () -> Unit
) {
    var menuOpen by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(start = 14.dp, top = 6.dp, end = 4.dp, bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                Icons.Filled.AccountCircle, contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp)
            )
            Spacer(Modifier.width(6.dp))
            Text(
                comment.authorName,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                modifier = Modifier.weight(1f)
            )
            comment.ratingLevel?.let { level ->
                Row(
                    Modifier
                        .clip(CircleShape)
                        .background(level.color.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 3.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    RatingDot(level.color, size = 7.dp)
                    Spacer(Modifier.width(5.dp))
                    Text(level.title, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = level.color)
                }
            }
            Box {
                IconButton(onClick = { menuOpen = true }, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Filled.MoreVert, contentDescription = "更多",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
                DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                    DropdownMenuItem(
                        text = { Text("檢舉這則心得") },
                        leadingIcon = { Icon(Icons.Outlined.Flag, contentDescription = null, tint = IosOrange) },
                        onClick = { menuOpen = false; onReport() }
                    )
                    DropdownMenuItem(
                        text = { Text("封鎖這位匿名同學", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = {
                            Icon(Icons.Outlined.Block, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        },
                        onClick = { menuOpen = false; onBlock() }
                    )
                }
            }
        }

        Text(comment.text, fontSize = 14.sp, modifier = Modifier.padding(end = 10.dp))

        Text(
            "${comment.year}-${comment.term} 修課" +
                    if (comment.courseType.isNotEmpty()) "・${comment.courseType}" else "",
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.outline
        )
    }
}