package com.example.nkust_app.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Coffee
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nkust_app.data.SettingsStorage
import com.example.nkust_app.data.course.Course
import com.example.nkust_app.data.course.CourseParser
import com.example.nkust_app.data.graduation.GraduationStatus
import com.example.nkust_app.ui.main.PlaceholderScreen
import com.example.nkust_app.ui.profile.ProfileScreen
import com.example.nkust_app.ui.theme.IosGreen
import com.example.nkust_app.ui.theme.IosOrange
import kotlinx.coroutines.delay
import java.util.Calendar
import com.example.nkust_app.ui.main.TabBarBottomSpace
import com.example.nkust_app.ui.components.InAppReview
import kotlin.time.Duration.Companion.seconds

private enum class HomePage { Home, CreditDetail, CoursePlan, Profile }

@Composable
fun HomeScreen(
    onSessionExpired: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = viewModel()
) {
    var page by rememberSaveable { mutableStateOf(HomePage.Home) }

    LaunchedEffect(viewModel.sessionExpired) {
        if (viewModel.sessionExpired) onSessionExpired()
    }

    val graduation = viewModel.graduation
    when {
        page == HomePage.CreditDetail && graduation != null -> CreditDetailScreen(
            status = graduation,
            onBack = { page = HomePage.Home },
            onOpenCoursePlan = { page = HomePage.CoursePlan },
            modifier = modifier
        )

        page == HomePage.CoursePlan -> CoursePlanScreen(
            onBack = { page = HomePage.CreditDetail },
            onSessionExpired = onSessionExpired,
            modifier = modifier
        )

        page == HomePage.Profile -> ProfileScreen(
            onBack = { page = HomePage.Home },
            onLogout = onLogout,
            onSessionExpired = onSessionExpired,
            modifier = modifier
        )

        else -> HomeContent(
            viewModel = viewModel,
            onOpenCredit = { page = HomePage.CreditDetail },
            onOpenProfile = { page = HomePage.Profile },
            modifier = modifier
        )
    }
}

// ═══════════════ 時間計算（對應 iOS 的 courseStatus / courseProgress / remainingMinutes） ═══════════════

private enum class CourseStatus { Ongoing, Upcoming, Finished, Future }

private fun toMinutes(hhmm: String): Int {
    val (h, m) = hhmm.split(":")
    return h.toInt() * 60 + m.toInt()
}

private fun currentMinutes(): Int {
    val now = Calendar.getInstance()
    return now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
}

/** 週一 = 0 … 週日 = 6 */
private fun weekdayIndex(): Int {
    val day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    return if (day == Calendar.SUNDAY) 6 else day - 2
}

private fun courseStatus(period: String, now: Int): CourseStatus {
    val (start, end) = CourseParser.periodTimes[period] ?: return CourseStatus.Future
    val s = toMinutes(start)
    val e = toMinutes(end)
    return when {
        now in s..e -> CourseStatus.Ongoing
        now < s -> CourseStatus.Upcoming
        else -> CourseStatus.Finished
    }
}

private fun courseProgress(period: String, now: Int): Float {
    val (start, end) = CourseParser.periodTimes[period] ?: return 0f
    val s = toMinutes(start)
    val total = toMinutes(end) - s
    if (total <= 0) return 0f
    return ((now - s).toFloat() / total).coerceIn(0f, 1f)
}

private fun remainingMinutes(period: String, now: Int): Int {
    val (_, end) = CourseParser.periodTimes[period] ?: return 0
    return (toMinutes(end) - now).coerceAtLeast(0)
}

// ═══════════════ 首頁內容 ═══════════════

@Composable
private fun HomeContent(
    viewModel: HomeViewModel,
    onOpenCredit: () -> Unit,
    onOpenProfile: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val settings = remember { SettingsStorage(context) }

    // 同 iOS：第 2 次開啟時，2 秒後請求評分
//    LaunchedEffect(Unit) {
//        if (InAppReview.shouldRequest(context)) {
//            delay(2.seconds)
//            InAppReview.request(context)
//        }
//    }

    var showInstagramBanner by remember { mutableStateOf(!settings.dismissedInstagramBanner) }

    // 每分鐘更新一次（進度條 + 課程狀態）
    var nowMinutes by remember { mutableIntStateOf(currentMinutes()) }
    var today by remember { mutableIntStateOf(weekdayIndex()) }
    LaunchedEffect(Unit) {
        while (true) {
            delay(60.seconds)
            nowMinutes = currentMinutes()
            today = weekdayIndex()
        }
    }

    val todayCourses = remember(viewModel.courses, today) {
        viewModel.courses
            .filter { it.weekday == today }
            .sortedBy { CourseParser.periods.indexOf(it.period) }
    }
    val ongoing = todayCourses.firstOrNull { courseStatus(it.period, nowMinutes) == CourseStatus.Ongoing }
    val upcoming = todayCourses.filter { courseStatus(it.period, nowMinutes) == CourseStatus.Upcoming }

    Scaffold(
        modifier = modifier,
        topBar = { HomeTopBar(onOpenProfile) }
    ) { padding ->
        if (viewModel.isLoading) {
            Column(
                Modifier.padding(padding).fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
            ) {
                CircularProgressIndicator()
                Text("載入中...", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@Scaffold
        }

        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = TabBarBottomSpace),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // 大標題（對應 .navigationBarTitleDisplayMode(.large)）
            Text("首頁", fontSize = 34.sp, fontWeight = FontWeight.Bold)

            AnimatedVisibility(visible = showInstagramBanner) {
                InstagramBanner(onDismiss = {
                    showInstagramBanner = false
                    settings.dismissedInstagramBanner = true
                })
            }

            viewModel.graduation?.let { CreditCard(it, onClick = onOpenCredit) }

            // 上課中
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader("上課中", Icons.Filled.Sensors, MaterialTheme.colorScheme.primary)
                if (ongoing != null) {
                    OngoingCourseCard(ongoing, nowMinutes)
                } else {
                    EmptyStateCard(Icons.Filled.Coffee, "目前沒有正在進行中的課程")
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // 即將上課
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SectionHeader("即將上課", Icons.Filled.Schedule, IosOrange)
                if (upcoming.isEmpty()) {
                    EmptyStateCard(Icons.Filled.Verified, "沒有課程了喔～🎉")
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        upcoming.forEach { UpcomingCourseRow(it) }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTopBar(onOpenProfile: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            "NKUST Plus",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )
        FilledIconButton(
            onClick = onOpenProfile,
            modifier = Modifier.size(48.dp).align(Alignment.CenterEnd),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Filled.AccountCircle, contentDescription = "個人資料", modifier = Modifier.size(34.dp))
        }
    }
}

// ═══════════════ Instagram 橫幅 ═══════════════

private const val INSTAGRAM_URL =
    "https://www.instagram.com/nkust_plus?igsh=MXR0NXR3MmJiczAybg%3D%3D&utm_source=qr"
private val IosPink = Color(0xFFFF2D55)

@Composable
private fun InstagramBanner(onDismiss: () -> Unit) {
    val uriHandler = LocalUriHandler.current
    val shape = RoundedCornerShape(14.dp)

    Row(
        Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(IosPink.copy(alpha = 0.08f))
            .border(1.dp, IosPink.copy(alpha = 0.2f), shape)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Instagram 漸層圖示（之後可換成你 iOS 的 instagram-logo 圖片）
        Box(
            Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.linearGradient(listOf(Color(0xFFFEDA75), Color(0xFFD62976), Color(0xFF4F5BD5)))
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        }

        Column(Modifier.weight(1f)) {
            Text("歡迎追蹤我們的 Instagram！", style = MaterialTheme.typography.titleSmall)
            Text(
                "隨時掌握新功能與最新消息 🎉",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            "追蹤",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            modifier = Modifier
                .clip(CircleShape)
                .background(IosPink)
                .clickable { uriHandler.openUri(INSTAGRAM_URL) }
                .padding(horizontal = 12.dp, vertical = 6.dp)
        )

        Icon(
            Icons.Filled.Close,
            contentDescription = "關閉",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp).clip(CircleShape).clickable(onClick = onDismiss)
        )
    }
}

// ═══════════════ 畢業學分卡（CreditCard） ═══════════════

@Composable
private fun CreditCard(status: GraduationStatus, onClick: () -> Unit) {
    val earned = status.total?.earned ?: 0
    val required = maxOf(status.total?.required ?: 0, 1)
    val progress = (earned.toFloat() / required).coerceAtMost(1f)
    val unmetCount = status.unmetItems.size
    // 還有未通過項目用橘色提醒，全部通過用 teal
    val accent = if (unmetCount > 0) IosOrange else MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(16.dp)

    Column(
        Modifier
            .fillMaxWidth()
            .shadow(4.dp, shape)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.School, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
            Spacer(Modifier.width(8.dp))
            Text(
                "畢業學分",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.weight(1f))
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("$earned", fontSize = 34.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(6.dp))
            Text(
                "/ $required 學分",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.weight(1f))
            when {
                unmetCount > 0 -> StatusBadge(Icons.Filled.Error, "$unmetCount 項未通過", IosOrange)
                status.isAllPassed -> StatusBadge(Icons.Filled.Verified, "已達畢業標準", IosGreen)
            }
        }

        // 進度條
        Box(
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(accent.copy(alpha = 0.15f))
        ) {
            Box(
                Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(accent.copy(alpha = 0.7f), accent)))
            )
        }
    }
}

@Composable
private fun StatusBadge(icon: ImageVector, text: String, color: Color) {
    Row(
        Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 5.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Text(text, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = color)
    }
}

// ═══════════════ 上課中 / 即將上課 ═══════════════

@Composable
private fun SectionHeader(title: String, icon: ImageVector, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(icon, contentDescription = null, tint = color)
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun OngoingCourseCard(course: Course, nowMinutes: Int) {
    val teal = MaterialTheme.colorScheme.primary
    val shape = RoundedCornerShape(16.dp)
    val progress = courseProgress(course.period, nowMinutes)
    val remaining = remainingMinutes(course.period, nowMinutes)
    val times = CourseParser.periodTimes[course.period]

    Column(
        Modifier
            .fillMaxWidth()
            .shadow(8.dp, shape, ambientColor = teal, spotColor = teal)
            .clip(shape)
            .background(MaterialTheme.colorScheme.background)
            .border(2.dp, teal, shape)
    ) {
        // 頂部資訊
        Row(Modifier.padding(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Row(
                    Modifier
                        .clip(CircleShape)
                        .background(teal)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Filled.Sensors, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Text("上課中", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Text(
                    course.name,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
            Box(
                Modifier.size(56.dp).clip(CircleShape).background(teal.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(course.period, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = teal)
            }
        }

        // 進度條
        Column(Modifier.padding(horizontal = 16.dp).padding(bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(teal.copy(alpha = 0.12f))
            ) {
                Box(
                    Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(progress)
                        .clip(RoundedCornerShape(4.dp))
                        .background(Brush.horizontalGradient(listOf(teal.copy(alpha = 0.7f), teal)))
                )
            }
            Row {
                Text(times?.first.orEmpty(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.weight(1f))
                Text("剩餘 $remaining 分鐘", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = teal)
                Spacer(Modifier.weight(1f))
                Text(times?.second.orEmpty(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // 教師 / 教室 / 時間
        Row(
            Modifier.padding(horizontal = 16.dp, vertical = 12.dp).height(IntrinsicSize.Min),
            verticalAlignment = Alignment.CenterVertically
        ) {
            InfoCell(Icons.Filled.Person, course.teacher, MaterialTheme.colorScheme.onSurfaceVariant, Modifier.weight(1f))
            VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            InfoCell(Icons.Filled.LocationOn, course.room, teal, Modifier.weight(1f))
            if (times != null) {
                VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                InfoCell(Icons.Filled.Schedule, "${times.first}–${times.second}", MaterialTheme.colorScheme.onSurfaceVariant, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun InfoCell(icon: ImageVector, label: String, color: Color, modifier: Modifier = Modifier) {
    Row(
        modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelMedium, color = color, maxLines = 1)
    }
}

@Composable
private fun UpcomingCourseRow(course: Course) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .shadow(2.dp, shape)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            Modifier
                .width(64.dp)
                .fillMaxHeight()
                .background(IosOrange.copy(alpha = 0.12f))
                .padding(vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(course.period, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = IosOrange)
            CourseParser.periodTimes[course.period]?.let { (start, end) ->
                Text(start, fontSize = 11.sp, lineHeight = 13.sp, color = IosOrange)
                Text("|", fontSize = 8.sp, lineHeight = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(end, fontSize = 11.sp, lineHeight = 13.sp, color = IosOrange)
            }
        }

        Column(
            Modifier.weight(1f).padding(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(course.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                IconText(Icons.Filled.Person, course.teacher, MaterialTheme.colorScheme.onSurfaceVariant)
                IconText(Icons.Filled.LocationOn, course.room, MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
private fun IconText(icon: ImageVector, text: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = color)
    }
}

@Composable
private fun EmptyStateCard(icon: ImageVector, message: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.6f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier.size(32.dp)
        )
        Text(message, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ═══════════════ 個人資料（Step 10 製作） ═══════════════

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfilePlaceholder(onBack: () -> Unit, modifier: Modifier = Modifier) {
    BackHandler(onBack = onBack)
    Scaffold(
        modifier = modifier,
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("個人資料") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        PlaceholderScreen("Step 10 製作", Modifier.padding(padding))
    }
}