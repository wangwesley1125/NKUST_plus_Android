package com.example.nkust_app.ui.score

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nkust_app.data.score.ScoreRecord
import com.example.nkust_app.data.score.TranscriptData
import com.example.nkust_app.ui.theme.IosBlue
import com.example.nkust_app.ui.theme.IosGreen
import com.example.nkust_app.ui.theme.IosOrange
import com.example.nkust_app.ui.theme.IosPurple
import java.util.Locale
import com.example.nkust_app.ui.main.TabBarBottomSpace

@Composable
fun ScoreScreen(
    onSessionExpired: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ScoreViewModel = viewModel()
) {
    var tab by rememberSaveable { mutableStateOf(ScoreTab.Midterm) }
    var showHistory by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(viewModel.sessionExpired) {
        if (viewModel.sessionExpired) onSessionExpired()
    }
    LaunchedEffect(tab) { viewModel.loadIfNeeded(tab) }

    if (showHistory) {
        TranscriptHistoryScreen(
            onBack = { showHistory = false },
            onSessionExpired = onSessionExpired,
            modifier = modifier
        )
        return
    }

    // 防止載入中切換分頁互相干擾（同 iOS）
    val isAnyLoading = viewModel.midterm.isLoading || viewModel.semester.isLoading

    Scaffold(
        modifier = modifier,
        topBar = { ScoreTopBar(enabled = !isAnyLoading, onHistory = { showHistory = true }) }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {
            StudentInfoSection(viewModel.studentInfo, viewModel.isLoadingStudentInfo)

            SegmentedPicker(
                options = listOf("期中成績", "當學期"),
                selectedIndex = tab.ordinal,
                enabled = !isAnyLoading,
                onSelect = { tab = ScoreTab.entries[it] },
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(top = 12.dp, bottom = 4.dp)
            )

            key(tab) {
                ScoreList(
                    state = viewModel.stateOf(tab),
                    scoreColumnTitle = if (tab == ScoreTab.Midterm) "期中" else "學期",
                    emptyText = if (tab == ScoreTab.Midterm) "目前無期中成績" else "目前無當學期成績",
                    onRetry = { viewModel.load(tab) }
                )
            }
        }
    }
}

// ═══════════════ 標題列 ═══════════════

@Composable
private fun ScoreTopBar(enabled: Boolean, onHistory: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // 左：歷年成績（膠囊按鈕）
        Surface(
            onClick = onHistory,
            enabled = enabled,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Row(
                Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    Icons.Filled.History,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(22.dp)
                )
                Text(
                    "歷年成績",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Text(
            "我的成績",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

// ═══════════════ 學生資訊卡 ═══════════════

@Composable
private fun StudentInfoSection(info: TranscriptData?, isLoading: Boolean) {
    val cardModifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
        .padding(top = 8.dp)
        .clip(RoundedCornerShape(16.dp))
        .background(MaterialTheme.colorScheme.surfaceContainerLow)
        .padding(16.dp)

    when {
        info != null -> Column(cardModifier, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(
                        info.studentName,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        info.studentId,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    Icons.Filled.School,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                InfoChip(Icons.Outlined.Apartment, info.department, Modifier.weight(1f))
                InfoChip(Icons.Outlined.Groups, info.classInfo)
            }
        }

        isLoading -> Row(
            cardModifier,
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
            Spacer(Modifier.width(8.dp))
            Text(
                "載入學生資料…",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun InfoChip(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    Row(modifier, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ═══════════════ 期中 / 當學期 切換 ═══════════════

/** 對應 iOS 的 .pickerStyle(.segmented) */
@Composable
private fun SegmentedPicker(
    options: List<String>,
    selectedIndex: Int,
    enabled: Boolean,
    onSelect: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedColor =
        if (isSystemInDarkTheme()) MaterialTheme.colorScheme.surfaceContainerHighest else Color.White

    Row(
        modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(3.dp)
    ) {
        options.forEachIndexed { index, label ->
            val selected = index == selectedIndex
            Box(
                Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .background(if (selected) selectedColor else Color.Transparent)
                    .clickable(enabled = enabled) { onSelect(index) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = if (enabled) 1f else 0.5f)
                )
            }
        }
    }
}

// ═══════════════ 成績列表（ScoreListView） ═══════════════

@Composable
private fun ScoreList(
    state: ScoreListState,
    scoreColumnTitle: String,
    emptyText: String,
    onRetry: () -> Unit
) {
    when {
        state.isLoading || !state.loaded -> CenterBox {
            CircularProgressIndicator()
            Text("載入中…", color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        state.error != null -> CenterBox {
            Icon(Icons.Outlined.Warning, contentDescription = null, tint = IosOrange, modifier = Modifier.size(40.dp))
            Text(state.error, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
            OutlinedButton(onClick = onRetry) { Text("重試") }
        }

        state.records.isEmpty() -> CenterBox {
            Icon(
                Icons.Outlined.Inbox,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(56.dp)
            )
            Text(
                emptyText,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        else -> ScoreContent(state.records, scoreColumnTitle)
    }
}

@Composable
fun CenterBox(content: @Composable ColumnScope.() -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        content = content
    )
}

@Composable
private fun ScoreContent(records: List<ScoreRecord>, scoreColumnTitle: String) {
    var filterType by rememberSaveable { mutableStateOf("全部") }
    var sortByScore by rememberSaveable { mutableStateOf(false) }

    val filtered = remember(records, filterType, sortByScore) {
        var list = if (filterType == "全部") records else records.filter { it.type == filterType }
        if (sortByScore) list = list.sortedByDescending { it.score.toIntOrNull() ?: -1 }
        list
    }
    val totalCredits = filtered.sumOf { it.credits }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = TabBarBottomSpace),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 摘要卡
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SummaryItem("科目數", "${filtered.size}", MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                VerticalDivider(Modifier.height(44.dp), color = MaterialTheme.colorScheme.outlineVariant)
                SummaryItem("總學分", formatCredits(totalCredits), IosBlue, Modifier.weight(1f))
            }
        }

        // 篩選列
        item {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("全部", "必修", "選修").forEach { type ->
                    val selected = filterType == type
                    Text(
                        type,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (selected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { filterType = type }
                            .padding(horizontal = 16.dp, vertical = 7.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
                val sortColor =
                    if (sortByScore) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                Row(
                    Modifier
                        .clip(CircleShape)
                        .clickable { sortByScore = !sortByScore }
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(Icons.Filled.SwapVert, contentDescription = null, tint = sortColor, modifier = Modifier.size(18.dp))
                    Text(if (sortByScore) "成績" else "依序", style = MaterialTheme.typography.bodyMedium, color = sortColor)
                }
            }
        }

        // 表頭
        item {
            Row(
                Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HeaderText("課程名稱", Modifier.weight(1f), TextAlign.Start)
                HeaderText("修別", Modifier.width(46.dp))
                HeaderText("學分", Modifier.width(36.dp))
                HeaderText(scoreColumnTitle, Modifier.width(50.dp))
            }
        }

        // 課程列（insetGrouped 的圓角區塊）
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                filtered.forEachIndexed { index, record ->
                    RecordRow(record)
                    if (index < filtered.lastIndex) {
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

@Composable
fun SummaryItem(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun HeaderText(text: String, modifier: Modifier, align: TextAlign = TextAlign.Center) {
    Text(
        text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = align,
        modifier = modifier
    )
}

@Composable
private fun RecordRow(record: ScoreRecord) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(record.name, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Text(record.courseCode, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        val typeColor = if (record.type == "必修") IosBlue else IosPurple
        Box(Modifier.width(46.dp), contentAlignment = Alignment.Center) {
            Text(
                record.type,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = typeColor,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(typeColor.copy(alpha = 0.1f))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            )
        }

        Text(
            formatCredits(record.credits),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(36.dp)
        )

        Text(
            record.score.ifEmpty { "—" },
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = scoreColor(record.score),
            textAlign = TextAlign.Center,
            modifier = Modifier.width(50.dp)
        )
    }
}

/** 同 iOS 的 scoreColor */
@Composable
fun scoreColor(score: String): Color {
    val value = score.toIntOrNull() ?: return MaterialTheme.colorScheme.onSurfaceVariant
    return when {
        value >= 90 -> IosGreen
        value >= 80 -> IosBlue
        value >= 70 -> MaterialTheme.colorScheme.onSurface
        value >= 60 -> IosOrange
        else -> MaterialTheme.colorScheme.error
    }
}

fun formatCredits(value: Double) = String.format(Locale.US, "%.1f", value)