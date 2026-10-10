package com.example.nkust_app.ui.profile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nkust_app.data.certificate.CertificateLanguage
import com.example.nkust_app.ui.components.BackTopBar
import com.example.nkust_app.ui.components.CircleIconButton
import com.example.nkust_app.ui.components.PdfPages
import com.example.nkust_app.ui.components.sharePdf
import com.example.nkust_app.ui.theme.IosOrange

@Composable
fun CertificateScreen(
    onBack: () -> Unit,
    onSessionExpired: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CertificateViewModel = viewModel()
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    var showEnglishTip by remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.sessionExpired) {
        if (viewModel.sessionExpired) onSessionExpired()
    }

    val file = viewModel.pdfFile
    val error = viewModel.error

    Scaffold(
        modifier = modifier,
        topBar = {
            BackTopBar("在學證明", onBack) {
                CircleIconButton(
                    icon = Icons.Filled.Download,
                    contentDescription = "下載",
                    enabled = file != null && error == null,
                    onClick = { file?.let { sharePdf(context, it, "在學證明") } }
                )
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            when {
                viewModel.isLoading -> LoadingView("載入在學證明中...")

                error != null -> Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
                ) {
                    Icon(Icons.Outlined.Warning, contentDescription = null, tint = IosOrange, modifier = Modifier.size(40.dp))
                    Text(error, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)

                    if (viewModel.language == CertificateLanguage.English) {
                        Button(onClick = { viewModel.selectLanguage(CertificateLanguage.Chinese) }) {
                            Text("切換回中文版")
                        }
                        OutlinedButton(
                            onClick = { showEnglishTip = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = IosOrange)
                        ) { Text("可能錯誤原因") }
                    } else {
                        Button(onClick = viewModel::load) { Text("重試") }
                    }
                }

                file != null -> Column(Modifier.fillMaxSize()) {
                    LanguagePicker(
                        selected = viewModel.language,
                        enabled = !viewModel.isSwitching,
                        onSelect = viewModel::selectLanguage,
                        modifier = Modifier.padding(16.dp)
                    )
                    if (viewModel.isSwitching) {
                        LoadingView("切換中...")
                    } else {
                        PdfPages(file, Modifier.fillMaxSize())
                    }
                }
            }
        }
    }

    if (showEnglishTip) {
        AlertDialog(
            onDismissRequest = { showEnglishTip = false },
            title = { Text("英文在學證明無法下載") },
            text = {
                Text(
                    "以下說明來自學校教務資訊系統：\n\n$ENGLISH_NAME_MISSING",
                    modifier = Modifier.verticalScroll(rememberScrollState())
                )
            },
            confirmButton = {
                TextButton(onClick = { showEnglishTip = false }) { Text("確認") }
            }
        )
    }
}

@Composable
private fun LoadingView(message: String) {
    Column(
        Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
    ) {
        CircularProgressIndicator()
        Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

/** 對應 iOS 的 .pickerStyle(.segmented) */
@Composable
private fun LanguagePicker(
    selected: CertificateLanguage,
    enabled: Boolean,
    onSelect: (CertificateLanguage) -> Unit,
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
        CertificateLanguage.entries.forEach { language ->
            val isSelected = language == selected
            Box(
                Modifier
                    .weight(1f)
                    .clip(CircleShape)
                    .background(if (isSelected) selectedColor else Color.Transparent)
                    .clickable(enabled = enabled) { onSelect(language) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    language.label,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}