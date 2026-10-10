package com.example.nkust_app.ui.home

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.nkust_app.data.graduation.CreditItem
import com.example.nkust_app.data.graduation.GraduationStatus
import com.example.nkust_app.data.graduation.OtherCourse
import com.example.nkust_app.ui.components.BackTopBar
import com.example.nkust_app.ui.theme.IosBlue
import com.example.nkust_app.ui.theme.IosGreen
import com.example.nkust_app.ui.theme.IosOrange
import com.example.nkust_app.ui.main.TabBarBottomSpace

private val IosPink = Color(0xFFFF2D55)

@Composable
fun CreditDetailScreen(
    status: GraduationStatus,
    onBack: () -> Unit,
    onOpenCoursePlan: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)

    Scaffold(
        modifier = modifier,
        topBar = {
            BackTopBar("修課學分狀況", onBack) {
                Surface(
                    onClick = onOpenCoursePlan,
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceContainerLow
                ) {
                    Row(
                        Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Filled.TableChart,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text("課程規劃", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, top = 16.dp, end = 16.dp, bottom = TabBarBottomSpace),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 尚未通過
            if (status.unmetItems.isNotEmpty()) {
                item {
                    GroupSection("尚未通過") {
                        status.unmetItems.forEachIndexed { i, item ->
                            if (i > 0) RowDivider()
                            CreditRow(item)
                        }
                    }
                }
            }

            // 學分總覽
            if (status.summary.isNotEmpty()) {
                item { GroupSection("修課學分結果預覽") { CreditTree(status.summary) } }
            }

            // 課群比對
            if (status.groups.isNotEmpty()) {
                item { GroupSection("課規課群比對結果") { CreditTree(status.groups) } }
            }

            // 其他課規外修習課程
            if (status.otherCourses.isNotEmpty()) {
                item {
                    GroupSection("其他課規外修習課程（${status.otherCourses.size}）") {
                        status.otherCourses.forEachIndexed { i, course ->
                            if (i > 0) RowDivider()
                            OtherCourseRow(course)
                        }
                    }
                }
            }

            // 校定英語畢業門檻
            status.englishThreshold?.let { english ->
                item {
                    GroupSection("校定英語畢業門檻") {
                        Row(
                            Modifier.fillMaxWidth().padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                if (english.isPassed) Icons.Filled.CheckCircle else Icons.Filled.Cancel,
                                contentDescription = null,
                                tint = if (english.isPassed) IosBlue else MaterialTheme.colorScheme.error
                            )
                            Text("校定英語畢業門檻", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            Text(
                                english.resultText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (english.isPassed) IosGreen else MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            item {
                GroupSection(null) {
                    Text(
                        "結果依學校最後審查為準，僅供參考。",
                        style = MaterialTheme.typography.bodySmall,
                        color = IosPink,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}

/** 對應 iOS 的 List(.insetGrouped) 的一個 Section */
@Composable
private fun GroupSection(title: String?, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        if (title != null) {
            Text(
                title,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow),
            content = content
        )
    }
}

@Composable
private fun RowDivider(indent: Int = 0) {
    HorizontalDivider(
        Modifier.padding(start = 16.dp + 20.dp * indent),
        color = MaterialTheme.colorScheme.outlineVariant
    )
}

// ═══════════════ 樹狀列表（對應 OutlineGroup） ═══════════════

@Composable
private fun CreditTree(items: List<CreditItem>, depth: Int = 0, dividerBeforeFirst: Boolean = false) {
    items.forEachIndexed { index, item ->
        if (index > 0 || dividerBeforeFirst) RowDivider(depth)
        CreditTreeNode(item, depth)
    }
}

@Composable
private fun CreditTreeNode(item: CreditItem, depth: Int) {
    var expanded by rememberSaveable(item.name, depth) { mutableStateOf(false) }
    val expandable = item.children.isNotEmpty()

    CreditRow(
        item = item,
        depth = depth,
        expandable = expandable,
        expanded = expanded,
        onToggle = { expanded = !expanded }
    )
    if (expandable && expanded) {
        CreditTree(item.children, depth + 1, dividerBeforeFirst = true)
    }
}

@Composable
private fun CreditRow(
    item: CreditItem,
    depth: Int = 0,
    expandable: Boolean = false,
    expanded: Boolean = false,
    onToggle: () -> Unit = {}
) {
    val (icon, color) = statusStyle(item.status)
    val detail = creditDetail(item)

    Row(
        Modifier
            .fillMaxWidth()
            .then(if (expandable) Modifier.clickable(onClick = onToggle) else Modifier)
            .padding(start = 16.dp + 20.dp * depth, end = 12.dp, top = 12.dp, bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
        Text(
            item.name,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        if (detail != null) {
            Text(
                detail,
                style = MaterialTheme.typography.bodyMedium,
                color = if (item.status == CreditItem.Status.Failed) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        if (expandable) {
            Icon(
                if (expanded) Icons.Filled.KeyboardArrowDown else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = if (expanded) "收合" else "展開",
                tint = MaterialTheme.colorScheme.primary
            )
        }
    }
}

/** 有學分要求就顯示學分；學分要求是 0 但有課程數（例如體育）就顯示課程數 */
private fun creditDetail(item: CreditItem): String? {
    val r = item.required
    val e = item.earned
    if (r != null && e != null && r > 0) return "$e / $r 學分"
    if (item.requiredCourses != null && item.passedCourses != null) {
        return "${item.passedCourses} / ${item.requiredCourses} 門"
    }
    if (e != null && (e > 0 || item.status == CreditItem.Status.Info)) return "$e 學分"
    return null
}

@Composable
private fun statusStyle(status: CreditItem.Status): Pair<ImageVector, Color> = when (status) {
    CreditItem.Status.Passed -> Icons.Filled.CheckCircle to IosBlue
    CreditItem.Status.Conditional -> Icons.Filled.CheckBox to IosOrange
    CreditItem.Status.Failed -> Icons.Filled.Cancel to MaterialTheme.colorScheme.error
    CreditItem.Status.Info -> Icons.Filled.Info to MaterialTheme.colorScheme.primary
    CreditItem.Status.Unknown -> Icons.Outlined.Circle to MaterialTheme.colorScheme.onSurfaceVariant
}

// ═══════════════ 其他課規外修習課程 ═══════════════

@Composable
private fun OtherCourseRow(course: OtherCourse) {
    val counted = course.isGraduCredit ?: false

    Column(
        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Text(
                course.courseName ?: "（未命名課程）",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                modifier = Modifier.weight(1f)
            )
            Text(
                "${course.creditText} 學分",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            listOf(course.termText, course.openDeptName, course.selectTypeText)
                .filterNot { it.isNullOrEmpty() }
                .joinToString(" · "),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Tag(
                course.gradeText,
                when {
                    course.isInProgress -> IosOrange
                    course.isTotallyPass == true -> IosGreen
                    else -> MaterialTheme.colorScheme.error
                }
            )
            Tag(
                course.reviewText,
                if (counted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun Tag(text: String, color: Color) {
    Text(
        text,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = color,
        modifier = Modifier
            .clip(CircleShape)
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    )
}