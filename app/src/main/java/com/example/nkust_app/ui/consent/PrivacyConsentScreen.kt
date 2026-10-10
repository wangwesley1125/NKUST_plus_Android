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

            PolicySection("一、", "蒐集目的") {
                PolicyText("本 App 提供校務系統連結、課表查詢及相關校園資訊服務。")
            }
            PolicySection("二、", "蒐集之個人資料類別") {
                PolicyText("使用者透過 App 內建 WebView 登入學校系統時，相關驗證資訊係由學校系統處理。本 App 僅透過系統提供之 CookieManager 機制，暫時儲存登入狀態（Cookie），以維持使用期間之登入狀態。")
                PolicyText("若使用者選擇開啟「AutoFill 自動填入」功能，帳號密碼將以加密方式儲存於裝置本地（Android Keystore），不會傳輸至任何伺服器或第三方。")
            }
            PolicySection("三、", "個人資料利用方式") {
                PolicyText("Cookie 及帳密（如啟用 AutoFill）僅用於：")
                BulletRow("維持登入狀態")
                BulletRow("提供課表與校務資訊查詢功能")
                BulletRow("AutoFill 自動填入登入表單（需使用者明確同意）")
                PolicyText("本 App 不會讀取、分析或另行利用帳號相關敏感資訊，亦不會傳送至第三方。")
            }
            PolicySection("四、", "資料保存與安全") {
                PolicyText("登入狀態（Cookie）暫時儲存於裝置本地，不會傳輸至任何伺服器或第三方。Cookie 將依學校系統設定於期限內自動失效，屆時 App 將自動登出，使用者須重新登入。使用者亦可透過登出或清除 App 資料方式主動移除登入狀態。AutoFill 帳密以加密方式儲存於裝置，使用者可隨時在 App 內刪除。")
            }
            PolicySection("五、", "使用者權利") {
                PolicyText("使用者可隨時停止使用本 App，或清除資料以終止相關處理行為。")
            }
            PolicySection("六、", "資料提供") {
                PolicyText("若使用者不進行登入，將無法使用部分需驗證之功能（如課表查詢）。")
            }
            PolicySection("七", "位置資訊") {
                PolicyText("校園地圖的「我的位置」功能需要使用裝置的位置資訊，僅在使用者按下定位按鈕並同意授權後使用，本 App 不會儲存或傳送位置資訊。校園地圖由 Google Maps 提供，相關資料處理適用 Google 隱私權政策。")
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