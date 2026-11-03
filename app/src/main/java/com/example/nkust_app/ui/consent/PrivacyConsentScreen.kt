package com.example.nkust_app.ui.consent

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nkust_app.ui.about.policySections
import com.example.nkust_app.ui.theme.Nkust_AppTheme
import com.example.nkust_app.ui.theme.IosBlue

private val WarningOrange = Color(0xFFFF9500)

@Composable
fun PrivacyConsentScreen(
    onAgree: () -> Unit,
    onDisagree: () -> Unit
) {
    var hasRead by remember { mutableStateOf(false) }
    val groupedBg = MaterialTheme.colorScheme.surfaceContainer

    Column(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surface)) {

        // ── 標題 ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(groupedBg)
                .statusBarsPadding()
                .padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("使用前請詳閱", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(
                "個人資料蒐集告知聲明",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        HorizontalDivider()

        // ── 條文（可捲動，佔滿中間）──
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // 非官方警告
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(WarningOrange.copy(alpha = 0.08f))
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(Icons.Filled.Warning, null, tint = WarningOrange, modifier = Modifier.size(18.dp))
                Text(
                    "本 App 為非官方應用程式，與國立高雄科技大學無任何隸屬或合作關係。使用者登入功能將導向國立高雄科技大學學生教務系統（stdsys.nkust.edu.tw）進行驗證，帳號密碼之輸入與真人驗證均由學校系統處理，本 App 不涉入其過程。",
                    style = MaterialTheme.typography.bodySmall,
                    color = WarningOrange
                )
            }

            // 條文與「關於 → 隱私政策」共用同一份，兩邊永遠一致
            policySections().forEach { section ->
                PolicySection("${section.number}、", section.title) {
                    section.paragraphs.forEach { PolicyText(it) }
                    section.bullets.forEach { BulletRow(it) }
                }
            }
        }
        HorizontalDivider()

        // ── 底部勾選 + 按鈕 ──
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(groupedBg)
                .navigationBarsPadding()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { hasRead = !hasRead },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = hasRead,
                    onCheckedChange = { hasRead = it },
                    colors = CheckboxDefaults.colors(checkedColor = IosBlue)
                )
                Text("我已閱讀並了解上述個人資料告知聲明", style = MaterialTheme.typography.bodyMedium)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = onDisagree,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = Color.Red
                    )
                ) { Text("不同意", fontWeight = FontWeight.SemiBold) }

                Button(
                    onClick = onAgree,
                    enabled = hasRead,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IosBlue,
                        contentColor = Color.White,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        disabledContentColor = Color.White
                    )
                ) { Text("同意並繼續", fontWeight = FontWeight.SemiBold) }
            }
        }
    }
}

@Composable
private fun PolicySection(
    number: String,
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
//            Box(
//                modifier = Modifier
//                    .size(20.dp)
//                    .background(MaterialTheme.colorScheme.primary),
//                contentAlignment = Alignment.Center
//            ) {
//                Text(number, color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold)
//            }
            Text(number, color = MaterialTheme.colorScheme.onSurface, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            Text(title, style = MaterialTheme.typography.titleSmall)
        }
        Column(
            modifier = Modifier.padding(start = 26.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
            content = content
        )
    }
}

@Composable
private fun PolicyText(text: String) {
    Text(
        text,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        lineHeight = 20.sp
    )
}

@Composable
private fun BulletRow(text: String) {
    Row(Modifier.padding(start = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PolicyText("•")
        PolicyText(text)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PrivacyConsentPreview() {
    Nkust_AppTheme { PrivacyConsentScreen(onAgree = {}, onDisagree = {}) }
}