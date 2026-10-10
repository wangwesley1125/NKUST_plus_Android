package com.example.nkust_app.ui.about

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GppMaybe
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nkust_app.ui.components.BackTopBar
import com.example.nkust_app.ui.theme.IosBlue
import com.example.nkust_app.ui.theme.IosGreen
import com.example.nkust_app.ui.theme.IosOrange
import com.example.nkust_app.ui.theme.IosPurple
import com.example.nkust_app.ui.main.TabBarBottomSpace

private val IosIndigo = Color(0xFF5856D6)

private data class PolicySection(
    val number: String,
    val title: String,
    val icon: ImageVector,
    val color: Color?,             // null = teal
    val paragraphs: List<String>,
    val bullets: List<String> = emptyList()
)

@Composable
private fun policySections(): List<PolicySection> = listOf(
    PolicySection(
        "一", "蒐集目的", Icons.Filled.TrackChanges, null,
        listOf("本 App 提供校務系統連結、課表查詢及相關校園資訊服務。")
    ),
    PolicySection(
        "二", "蒐集之個人資料類別", Icons.Filled.VerifiedUser, IosBlue,
        listOf(
            "使用者透過 App 內建 WebView 登入學校系統時，相關驗證資訊係由學校系統處理。本 App 僅透過系統提供之 CookieManager 機制，暫時儲存登入狀態（Cookie），以維持使用期間之登入狀態。",
            "若使用者選擇開啟「AutoFill 自動填入」功能，帳號密碼將以 Android Keystore 金鑰加密後儲存於裝置本地，不會傳輸至任何伺服器或第三方。"
        )
    ),
    PolicySection(
        "三", "個人資料利用方式", Icons.Filled.Lock, IosIndigo,
        listOf("本 App 不會讀取、分析或另行利用帳號相關敏感資訊，亦不會傳送至第三方。Cookie 及帳密（如啟用 AutoFill）僅用於："),
        listOf("維持登入狀態", "提供課表與校務資訊查詢功能", "AutoFill 自動填入登入表單（需使用者明確同意）")
    ),
    PolicySection(
        "四", "資料保存與安全", Icons.Filled.Storage, IosOrange,
        listOf("登入狀態（Cookie）暫時儲存於裝置本地，不會傳輸至任何伺服器或第三方。Cookie 將依學校系統設定於期限內自動失效，屆時 App 將自動登出，使用者須重新登入。使用者亦可透過登出或清除 App 資料方式主動移除登入狀態。AutoFill 帳密以加密方式儲存於裝置，使用者可隨時在 App 內刪除。")
    ),
    PolicySection(
        "五", "使用者權利", Icons.Filled.PanTool, IosPurple,
        listOf("使用者可隨時停止使用本 App，或清除資料以終止相關處理行為。")
    ),
    PolicySection(
        "六", "資料提供", Icons.Filled.GppMaybe, MaterialTheme.colorScheme.error,
        listOf("若使用者不進行登入，將無法使用部分需驗證之功能（如課表查詢）。")
    ),
    PolicySection(
        "七", "位置資訊", Icons.Filled.LocationOn, IosGreen,
        listOf(
            "校園地圖的「我的位置」功能需要使用裝置的位置資訊，僅在使用者按下定位按鈕並同意授權後使用，用於在地圖上顯示目前位置。本 App 不會儲存或傳送位置資訊。",
            "校園地圖由 Google Maps 提供，使用地圖時相關資料之處理適用 Google 隱私權政策。"
        )
    )
)

@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    BackHandler(onBack = onBack)
    val teal = MaterialTheme.colorScheme.primary

    Scaffold(modifier = modifier, topBar = { BackTopBar("隱私政策", onBack) }) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(start = 16.dp, top = 16.dp, end = 16.dp, bottom = TabBarBottomSpace),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 頂部說明橫幅
            Row(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(teal.copy(alpha = 0.08f))
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(Icons.Filled.Security, contentDescription = null, tint = teal, modifier = Modifier.size(36.dp))
                Column {
                    Text("NKUST plus", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("個人資料保護聲明", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            policySections().forEach { SectionCard(it) }

            Text(
                "本聲明如有修改，將於 App 更新時一併公告。",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth().wrapContentWidth(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
private fun SectionCard(section: PolicySection) {
    val color = section.color ?: MaterialTheme.colorScheme.primary

    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(section.icon, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
            Text(
                "${section.number}、${section.title}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            section.paragraphs.forEach {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            section.bullets.forEach { bullet ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp).padding(top = 2.dp)
                    )
                    Text(bullet, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}