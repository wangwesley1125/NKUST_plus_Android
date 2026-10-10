package com.example.nkust_app.ui.about

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nkust_app.BuildConfig
import com.example.nkust_app.ui.components.openWebLink
import com.example.nkust_app.ui.components.sendFeedbackMail
import com.example.nkust_app.ui.components.shareApp
import com.example.nkust_app.ui.main.TabBarBottomSpace

private enum class AboutPage { Main, Privacy, Copyright }

private const val AUTHOR_URL = "https://github.com/wangwesley1125"
private const val REPO_URL = "https://github.com/wangwesley1125/NKUST_plus"
private const val INSTAGRAM_URL =
    "https://www.instagram.com/nkust_plus?igsh=MXR0NXR3MmJiczAybg%3D%3D&utm_source=qr"

/** 學生專區（同 iOS） */
private val STUDENT_LINKS = listOf(
    "新生專區" to "https://enterschool.nkust.edu.tw/",
    "Elearning 教學平台" to "https://elearning.nkust.edu.tw/moocs/#/home",
    "教學意見調查系統" to "https://ceq.nkust.edu.tw/Home",
    "活動訊息平台" to "https://ws1.nkust.edu.tw/Activity/"
)

/** 關於高雄科技大學（同 iOS） */
private val SCHOOL_LINKS = listOf(
    "國立高雄科技大學官網" to "https://www.nkust.edu.tw/",
    "教務處" to "https://acad.nkust.edu.tw/",
    "學務處" to "https://osa.nkust.edu.tw/",
    "總務處" to "https://gen.nkust.edu.tw/",
    "國際事務處" to "https://oia.nkust.edu.tw/",
    "學務資訊系統（請假 / 曠課 / 獎懲 / 幹部...）" to "https://oosaf.nkust.edu.tw/",
    "國立高雄科技大學 圖書館" to "https://www.lib.nkust.edu.tw/portal/",
    "其他行政單位" to "https://www.nkust.edu.tw/p/412-1000-85.php"
)

/** 關於頁的一列 */
private data class AboutItem(
    val title: String,
    val icon: ImageVector? = null,
    val value: String? = null,          // 右邊顯示的值（版本號）
    val showChevron: Boolean = false,
    val color: Color? = null,           // null = teal
    val onClick: (() -> Unit)? = null
)

@Composable
fun AboutScreen(
    hasSavedCredentials: Boolean,
    onClearCredentials: () -> Unit,
    modifier: Modifier = Modifier
) {
    var page by rememberSaveable { mutableStateOf(AboutPage.Main) }
    val backToMain = { page = AboutPage.Main }

    when (page) {
        AboutPage.Privacy -> PrivacyPolicyScreen(onBack = backToMain, modifier = modifier)
        AboutPage.Copyright -> CopyrightScreen(onBack = backToMain, modifier = modifier)
        AboutPage.Main -> AboutMain(
            hasSavedCredentials = hasSavedCredentials,
            onClearCredentials = onClearCredentials,
            onOpenPrivacy = { page = AboutPage.Privacy },
            onOpenCopyright = { page = AboutPage.Copyright },
            modifier = modifier
        )
    }
}

@Composable
private fun AboutMain(
    hasSavedCredentials: Boolean,
    onClearCredentials: () -> Unit,
    onOpenPrivacy: () -> Unit,
    onOpenCopyright: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val versionName = BuildConfig.VERSION_NAME
    var showClearDialog by remember { mutableStateOf(false) }
    val errorColor = MaterialTheme.colorScheme.error

    Scaffold(
        modifier = modifier,
        topBar = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("關於", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = TabBarBottomSpace),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            AboutSection(
                "關於開發者",
                listOf(AboutItem("作者", icon = Icons.Filled.Person) { openWebLink(context, AUTHOR_URL) })
            )

            AboutSection(
                "關於 NKUST plus",
                listOf(
                    AboutItem("版本資訊", value = versionName),
                    AboutItem("開源專案") { openWebLink(context, REPO_URL) },
                    AboutItem("隱私政策", showChevron = true, onClick = onOpenPrivacy),
                    AboutItem("版權聲明", showChevron = true, onClick = onOpenCopyright),
                    AboutItem("問題回報") { sendFeedbackMail(context, versionName) },
                    AboutItem("分享此 App") { shareApp(context) },
                    AboutItem("追蹤高科 Plus") { openWebLink(context, INSTAGRAM_URL) }
                )
            )

            AboutSection(
                "學生專區",
                STUDENT_LINKS.map { (title, url) -> AboutItem(title) { openWebLink(context, url) } }
            )

            AboutSection(
                "關於高雄科技大學",
                SCHOOL_LINKS.map { (title, url) -> AboutItem(title) { openWebLink(context, url) } }
            )

            // Android 版：清除 AutoFill 帳密（有存過才顯示）
            if (hasSavedCredentials) {
                AboutSection(
                    "帳號",
                    listOf(
                        AboutItem(
                            "清除已儲存的帳號密碼",
                            icon = Icons.Filled.Delete,
                            color = errorColor
                        ) { showClearDialog = true }
                    )
                )
            }
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("清除已儲存的帳號密碼？") },
            text = { Text("帳號密碼將從此裝置永久刪除，之後登入將無法使用自動填入。") },
            confirmButton = {
                TextButton(onClick = {
                    showClearDialog = false
                    onClearCredentials()
                }) { Text("清除", color = errorColor) }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) { Text("取消") }
            }
        )
    }
}

/** 對應 iOS List 的 Section（insetGrouped） */
@Composable
private fun AboutSection(title: String, items: List<AboutItem>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 16.dp)
        )
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
        ) {
            items.forEachIndexed { index, item ->
                if (index > 0) {
                    HorizontalDivider(
                        Modifier.padding(horizontal = 16.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                }
                AboutRow(item)
            }
        }
    }
}

@Composable
private fun AboutRow(item: AboutItem) {
    val color = item.color ?: MaterialTheme.colorScheme.primary

    Row(
        Modifier
            .fillMaxWidth()
            .then(if (item.onClick != null) Modifier.clickable(onClick = item.onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item.icon?.let { Icon(it, contentDescription = null, tint = color, modifier = Modifier.size(24.dp)) }
        Text(item.title, fontSize = 18.sp, color = color, modifier = Modifier.weight(1f))
        item.value?.let { Text(it, fontSize = 18.sp, color = color) }
        if (item.showChevron) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}