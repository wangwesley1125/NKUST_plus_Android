package com.example.nkust_app.ui.login

import android.webkit.WebView
import androidx.activity.compose.BackHandler
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.nkust_app.data.Credentials
import com.example.nkust_app.data.SessionStorage
import com.example.nkust_app.ui.components.AppIconLoading
import com.example.nkust_app.ui.theme.IosBlue
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.seconds

private val TIMEOUT = 20.seconds

@Composable
fun LoginScreen(
    savedCredentials: Credentials?,
    onLoginSuccess: (Credentials?) -> Unit,
    onGuest: () -> Unit
) {
    var webView by remember { mutableStateOf<WebView?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var isTransitioning by remember { mutableStateOf(false) }
    var canGoBack by remember { mutableStateOf(false) }
    var loadError by remember { mutableStateOf<String?>(null) }
    var showAutoFillBanner by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    var topBarHeight by remember { mutableStateOf(0.dp) }

    fun reload() {
        loadError = null
        isTransitioning = false
        webView?.loadUrl(SessionStorage.LOGIN_URL)
    }

    BackHandler(enabled = canGoBack && !isTransitioning) {
        webView?.goBack()
    }

    LaunchedEffect(isLoading) {
        if (isLoading) {
            delay(TIMEOUT)
            webView?.stopLoading()
            isTransitioning = false
            showAutoFillBanner = false
            loadError = "連線逾時，請檢查網路後重試"
        }
    }

    Box(Modifier.fillMaxSize().background(Color.White)) {

        // 最底層：WebView（跟 iOS 一樣只避開狀態列，頂部列疊在網頁上）
        NkustWebView(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = topBarHeight)
                .navigationBarsPadding(),
            onWebViewCreated = { webView = it },
            onLoadingChange = { isLoading = it },
            onLoginPageLoaded = {
                if (savedCredentials != null) showAutoFillBanner = true
            },
            onLoginStarted = {
                isTransitioning = true
                showAutoFillBanner = false
            },
            onLoginSuccess = onLoginSuccess,
            onError = {
                isTransitioning = false
                showAutoFillBanner = false
                loadError = it
            },
            onCanGoBackChange = { canGoBack = it }
        )

        // 連線錯誤卡片（置中）
        loadError?.let { message ->
            Box(
                Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                ConnectionErrorCard(message = message, onRetry = ::reload)
            }
        }

        // 頂部列：疊在網頁上方，不隨網頁載入
        if (!isTransitioning) {
            LoginTopBar(
                showProgress = isLoading && loadError == null,
                onReload = ::reload,
                onGuest = onGuest,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .onSizeChanged { topBarHeight = with(density) { it.height.toDp() } }
            )
        }

        // AutoFill 提示列（底部滑出）
        androidx.compose.animation.AnimatedVisibility(
            visible = showAutoFillBanner && loadError == null && savedCredentials != null,
            enter = slideInVertically { it } + fadeIn(),
            exit = slideOutVertically { it } + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 20.dp)
        ) {
            savedCredentials?.let { cred ->
                AutoFillBanner(
                    username = cred.username,
                    onFill = {
                        showAutoFillBanner = false
                        webView?.fillCredentials(cred)
                    },
                    onDismiss = { showAutoFillBanner = false }
                )
            }
        }

        // 登入過渡畫面
        if (isTransitioning) {
            AppIconLoading()
        }
    }
}

/** 對應 iOS 的頂部列：.thinMaterial 半透明底，背景延伸到狀態列 */
@Composable
private fun LoginTopBar(
    showProgress: Boolean,
    onReload: () -> Unit,
    onGuest: () -> Unit,
    modifier: Modifier = Modifier
) {
    val blueButton = ButtonDefaults.buttonColors(
        containerColor = IosBlue,
        contentColor = Color.White
    )

    // 對應 iOS 的 .thinMaterial 疊在白色網頁上的顏色
    val isDark = isSystemInDarkTheme()
    val barColor = if (isDark) Color(0xFF7A7A7C) else Color(0xFFF2F2F7)
    val titleColor = if (isDark) Color.White else Color.Black

    Column(
        modifier
            .fillMaxWidth()
            .background(barColor)
            .statusBarsPadding()
    ) {
        Box(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Button(
                onClick = onReload,
                colors = blueButton,
                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 8.dp),
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = "重新整理", modifier = Modifier.size(22.dp))
            }

            Text(
                "歡迎使用高科 Plus",
                style = MaterialTheme.typography.titleMedium,
                color = titleColor,
                modifier = Modifier.align(Alignment.Center)
            )

            Button(
                onClick = onGuest,
                colors = blueButton,
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.align(Alignment.CenterEnd)
            ) {
                Icon(Icons.Filled.Person, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(4.dp))
                Text("訪客", fontWeight = FontWeight.SemiBold)
            }
        }

        // 載入進度條貼在頂部列下緣（高度固定，避免出現/消失時頂部列跳動）
        Box(Modifier.fillMaxWidth().height(2.dp)) {
            if (showProgress) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxSize(),
                    color = IosBlue,
                    trackColor = Color.Transparent
                )
            }
        }
    }
}