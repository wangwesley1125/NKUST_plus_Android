package com.example.nkust_app.ui.score

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.UnfoldMore
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nkust_app.data.score.TranscriptCourse
import com.example.nkust_app.data.score.TranscriptData
import com.example.nkust_app.ui.theme.IosBlue
import com.example.nkust_app.ui.theme.IosGreen
import com.example.nkust_app.ui.theme.IosOrange
import com.example.nkust_app.ui.theme.IosPurple
import java.util.Locale

@Composable
fun TranscriptHistoryScreen(
    onBack: () -> Unit,
    onSessionExpired: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TranscriptHistoryViewModel = viewModel()
) {
    BackHandler(onBack = onBack)

    LaunchedEffect(viewModel.sessionExpired) {
        if (viewModel.sessionExpired) onSessionExpired()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TranscriptTopBar(
                semesters = viewModel.semesters,
                selectedYM = viewModel.selectedYM,
                showPicker = !viewModel.isLoadingSemesters && viewModel.semesters.isNotEmpty(),
                pickerEnabled = !viewModel.isLoadingData,
                onSelect = viewModel::selectSemester,
                onBack = onBack
            )
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            val data = viewModel.data
            val error = viewModel.error
            when {
                viewModel.isLoadingSemesters -> CenterBox {
                    CircularProgressIndicator()
                    Text("載入學期中…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                viewModel.isLoadingData -> CenterBox {
                    CircularProgressIndicator()
                    Text("取得成績中…", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                error != null -> CenterBox {
                    Icon(Icons.Outlined.Warning, contentDescription = null, tint = IosOrange, modifier = Modifier.size(40.dp))
                    Text(error, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    OutlinedButton(onClick = viewModel::load) { Text("重試") }
                }

                data != null -> TranscriptContent(data)

                else -> CenterBox {
                    Text("請選擇學期", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// ═══════════════ 標題列 ═══════════════

@Composable
private fun TranscriptTopBar(
    semesters: List<String>,
    selectedYM: String,
    showPicker: Boolean,
    pickerEnabled: Boolean,
    onSelect: (String) -> Unit,
    onBack: () -> Unit
) {
    Box(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        // 左：圓形返回按鈕
        FilledIconButton(
            onClick = onBack,
            modifier = Modifier.size(48.dp).align(Alignment.CenterStart),
            colors = IconButtonDefaults.filledIconButtonColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                contentDescription = "返回",
                modifier = Modifier.size(30.dp)
            )
        }

        // 中：標題
        Text(
            "歷年成績",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )

        // 右：學期選單
        if (showPicker) {
            SemesterPicker(
                semesters = semesters,
                selectedYM = selectedYM,
                enabled = pickerEnabled,
                onSelect = onSelect,
                modifier = Modifier.align(Alignment.CenterEnd)
            )
        }
    }
}

/** 對應 iOS 的 Menu + chevron.up.chevron.down */
@Composable
private fun SemesterPicker(
    semesters: List<String>,
    selectedYM: String,
    enabled: Boolean,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier) {
        Surface(
            onClick = { expanded = true },
            enabled = enabled,
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerLow
        ) {
            Row(
                Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    selectedYM.ifEmpty { "選擇學期" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Icon(
                    Icons.Filled.UnfoldMore,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            semesters.forEach { ym ->
                DropdownMenuItem(
                    text = { Text(ym) },
                    onClick = {
                        expanded = false
                        onSelect(ym)
                    },
                    trailingIcon = if (ym == selectedYM) {
                        { Icon(Icons.Filled.Check, contentDescription = null) }
                    } else null
                )
            }
        }
    }
}

// ═══════════════ 主要內容 ═══════════════

@Composable
private fun TranscriptContent(data: TranscriptData) {
    var filterType by rememberSaveable(data.semester) { mutableStateOf("全部") }
    var sortByScore by rememberSaveable(data.semester) { mutableStateOf(false) }

    val courses = remember(data, filterType, sortByScore) {
        var list = if (filterType == "全部") data.courses else data.courses.filter { it.type == filterType }
        if (sortByScore) list = list.sortedByDescending { it.score }
        list
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 成績摘要卡
        item {
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
                    .padding(vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SummaryItem("學業成績", format2(data.academicScore), IosBlue, Modifier.weight(1f))
                SummaryDivider()
                SummaryItem("操行成績", format2(data.conductScore), IosPurple, Modifier.weight(1f))
                SummaryDivider()
                SummaryItem("實得學分", String.format(Locale.US, "%.0f", data.earnedCredits), IosGreen, Modifier.weight(1f))
                SummaryDivider()
                SummaryItem("班級排名", data.classRank.ifEmpty { "—" }, IosOrange, Modifier.weight(1f))
            }
        }

        // 篩選列
        item {
            FilterBar(
                filterType = filterType,
                onFilterChange = { filterType = it },
                sortByScore = sortByScore,
                onToggleSort = { sortByScore = !sortByScore }
            )
        }

        // 表頭
        item {
            Row(
                Modifier.padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                HeaderText("科目", Modifier.weight(1f), TextAlign.Start)
                HeaderText("修別", Modifier.width(46.dp))
                HeaderText("學分", Modifier.width(36.dp))
                HeaderText("成績", Modifier.width(44.dp))
            }
        }

        // 科目列表
        item {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerLow)
            ) {
                courses.forEachIndexed { index, course ->
                    CourseRow(course)
                    if (index < courses.lastIndex) {
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
private fun SummaryDivider() {
    VerticalDivider(Modifier.height(44.dp), color = MaterialTheme.colorScheme.outlineVariant)
}

private fun format2(value: Double) = String.format(Locale.US, "%.2f", value)

@Composable
private fun FilterBar(
    filterType: String,
    onFilterChange: (String) -> Unit,
    sortByScore: Boolean,
    onToggleSort: () -> Unit
) {
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
                    .clickable { onFilterChange(type) }
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
                .clickable(onClick = onToggleSort)
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(Icons.Filled.SwapVert, contentDescription = null, tint = sortColor, modifier = Modifier.size(18.dp))
            Text(if (sortByScore) "成績" else "依序", style = MaterialTheme.typography.bodyMedium, color = sortColor)
        }
    }
}

@Composable
private fun CourseRow(course: TranscriptCourse) {
    Row(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            course.name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        val typeColor = if (course.type == "必修") IosBlue else IosPurple
        Box(Modifier.width(46.dp), contentAlignment = Alignment.Center) {
            Text(
                course.type,
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
            formatCredits(course.credits),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.width(36.dp)
        )

        Text(
            "${course.score}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = scoreColor(course.score.toString()),
            textAlign = TextAlign.Center,
            modifier = Modifier.width(44.dp)
        )
    }
}