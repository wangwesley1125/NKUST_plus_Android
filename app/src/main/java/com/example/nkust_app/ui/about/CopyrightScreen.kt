package com.example.nkust_app.ui.about

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.nkust_app.ui.components.BackTopBar
import com.example.nkust_app.ui.theme.IosBlue
import com.example.nkust_app.ui.theme.IosOrange

@Composable
fun CopyrightScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    BackHandler(onBack = onBack)

    val disclaimers = listOf(
        Triple(Icons.Filled.Person, MaterialTheme.colorScheme.primary, "NKUST Plus 為 Wesley Wang 個人開發之非官方應用程式。"),
        Triple(Icons.Filled.AccountBalance, IosBlue, "本 App 與國立高雄科技大學無任何合作或隸屬關係。"),
        Triple(Icons.Filled.Description, IosOrange, "本 App 所提供之相關資訊僅供參考，實際內容仍以國立高雄科技大學官方公告為準。"),
        Triple(Icons.Filled.Warning, MaterialTheme.colorScheme.error, "本 App 不保證資訊之即時性或完整性，僅為提供使用便利之第三方工具。")
    )

    Scaffold(modifier = modifier, topBar = { BackTopBar("版權聲明", onBack) }) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            disclaimers.forEach { (icon, color, text) ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                    Text(text, style = MaterialTheme.typography.bodyMedium)
                }
            }

            Text(
                "Copyright © 2026 Wesley Wang",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp)
                    .wrapContentWidth(Alignment.CenterHorizontally)
            )
        }
    }
}