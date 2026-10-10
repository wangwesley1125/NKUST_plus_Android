package com.example.nkust_app.ui.calendar

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nkust_app.ui.components.BackTopBar
import com.example.nkust_app.ui.components.PdfPages
import com.example.nkust_app.ui.components.SegmentedPicker
import com.example.nkust_app.ui.theme.IosBlue
import com.example.nkust_app.ui.theme.IosOrange
import com.example.nkust_app.ui.main.TabBarBottomSpace

/**
 * @param onBack 從個人資料頁進來時有返回按鈕；當分頁時傳 null
 * @param onLogin 訪客模式時右上角顯示「登入」按鈕
 */
@Composable
fun CalendarScreen(
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    onLogin: (() -> Unit)? = null,
    viewModel: CalendarViewModel = viewModel()
) {
    if (onBack != null) BackHandler(onBack = onBack)

    Scaffold(
        modifier = modifier,
        topBar = { CalendarTopBar(onBack, onLogin) }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            val error = viewModel.error
            when {
                viewModel.isLoading -> Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
                ) {
                    CircularProgressIndicator()
                    Text("載入行事曆中...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                error != null -> Column(
                    Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
                ) {
                    Icon(Icons.Outlined.Warning, contentDescription = null, tint = IosOrange, modifier = Modifier.size(40.dp))
                    Text(error, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                    Button(onClick = viewModel::load) { Text("重試") }
                }

                else -> Column(Modifier.fillMaxSize()) {
                    SegmentedPicker(
                        options = viewModel.semesters.map { it.label },
                        selectedIndex = viewModel.selectedIndex,
                        onSelect = viewModel::select,
                        modifier = Modifier.padding(16.dp)
                    )
                    viewModel.semesters.getOrNull(viewModel.selectedIndex)?.let { semester ->
                        PdfPages(semester.file, Modifier.fillMaxSize(), bottomPadding = TabBarBottomSpace)
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarTopBar(onBack: (() -> Unit)?, onLogin: (() -> Unit)?) {
    if (onBack != null) {
        BackTopBar("行事曆", onBack)
        return
    }

    Box(
        Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .heightIn(min = 48.dp)
    ) {
        Text(
            "行事曆",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )
        // 訪客才有的「登入」按鈕（同 iOS）
        if (onLogin != null) {
            Button(
                onClick = onLogin,
                colors = ButtonDefaults.buttonColors(containerColor = IosBlue, contentColor = Color.White),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("登入", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}