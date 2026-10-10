package com.example.nkust_app

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.core.net.toUri
import com.example.nkust_app.data.ConsentStorage
import com.example.nkust_app.data.CredentialStorage
import com.example.nkust_app.data.Credentials
import com.example.nkust_app.data.SessionStorage
import com.example.nkust_app.ui.consent.PrivacyConsentScreen
import com.example.nkust_app.ui.login.LoginScreen
import com.example.nkust_app.ui.login.SavePasswordSheet
import com.example.nkust_app.ui.main.MainScreen
import com.example.nkust_app.ui.theme.Nkust_AppTheme
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.UpdateAvailability

class MainActivity : ComponentActivity() {

    private val appUpdateManager by lazy { AppUpdateManagerFactory.create(this) }
    private var showUpdateDialog by mutableStateOf(false)
    private var updateDismissed = false     // 按過「稍後再說」，這次開啟就不再跳
    private var isCheckingVersion = false   // 避免重複檢查

    override fun onResume() {
        super.onResume()
        checkForUpdate()   // 冷啟動、從背景回來都會檢查（同 iOS 的 scenePhase）
    }

    private fun checkForUpdate() {
        if (isCheckingVersion || updateDismissed) return
        isCheckingVersion = true
        appUpdateManager.appUpdateInfo
            .addOnSuccessListener { info ->
                showUpdateDialog = info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
            }
            // 不是從 Play 商店安裝的（例如 Android Studio 直接執行）會失敗，忽略即可
            .addOnCompleteListener { isCheckingVersion = false }
    }

    private fun openPlayStore() {
        val market = Intent(Intent.ACTION_VIEW, "market://details?id=$packageName".toUri())
        try {
            startActivity(market)
        } catch (_: ActivityNotFoundException) {
            startActivity(Intent(Intent.ACTION_VIEW, "https://play.google.com/store/apps/details?id=$packageName".toUri()))
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val consentStorage = ConsentStorage(this)
        val session = SessionStorage(this)
        val credentialStorage = CredentialStorage(this)

        setContent {
            Nkust_AppTheme {
                var showConsent by rememberSaveable { mutableStateOf(!consentStorage.hasAgreed) }
                var isLoggedIn by rememberSaveable { mutableStateOf(session.isLoggedIn) }
                var isGuest by rememberSaveable { mutableStateOf(session.isGuest) }

                // AutoFill 相關
                var savedCredentials by remember { mutableStateOf(credentialStorage.load()) }
                var pendingCredentials by remember { mutableStateOf<Credentials?>(null) }

                when {
                    showConsent -> PrivacyConsentScreen(
                        onAgree = {
                            consentStorage.hasAgreed = true
                            showConsent = false
                        },
                        onDisagree = { finish() }
                    )

                    isLoggedIn || isGuest -> MainScreen(
                        isGuest = !isLoggedIn,
                        hasSavedCredentials = savedCredentials != null,
                        onLogout = {
                            if (isLoggedIn) {
                                session.logout()
                                viewModelStore.clear()   // 清掉課表等資料，避免換帳號登入看到別人的課表
                                isLoggedIn = false
                            } else {
                                session.isGuest = false
                                isGuest = false
                            }
                        },
                        onSessionExpired = {
                            Toast.makeText(this@MainActivity, "登入已過期，請重新登入", Toast.LENGTH_SHORT).show()
                            session.logout()
                            viewModelStore.clear()
                            isLoggedIn = false
                        },
                        onClearCredentials = {
                            credentialStorage.clear()
                            savedCredentials = null
                        }
                    )

                    else -> LoginScreen(
                        savedCredentials = savedCredentials,
                        onLoginSuccess = { detected ->
                            session.isLoggedIn = true
                            session.isGuest = false
                            isLoggedIn = true
                            // 新帳密，或跟已儲存的不同 → 詢問是否儲存
                            if (detected != null && detected != savedCredentials) {
                                pendingCredentials = detected
                            }
                        },
                        onGuest = {
                            session.isGuest = true
                            isGuest = true
                        }
                    )
                }

                pendingCredentials?.let { pending ->
                    SavePasswordSheet(
                        username = pending.username,
                        isUpdate = savedCredentials != null,
                        onSave = {
                            credentialStorage.save(pending)
                            savedCredentials = pending
                            pendingCredentials = null
                        },
                        onSkip = { pendingCredentials = null }
                    )
                }

                if (showUpdateDialog) {
                    AlertDialog(
                        onDismissRequest = {
                            showUpdateDialog = false
                            updateDismissed = true
                        },
                        title = { Text("發現新版本 🎉") },
                        text = { Text("新版本已上架，建議更新以獲得最佳體驗。") },
                        confirmButton = {
                            TextButton(onClick = {
                                showUpdateDialog = false
                                openPlayStore()
                            }) { Text("前往更新") }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showUpdateDialog = false
                                updateDismissed = true
                            }) { Text("稍後再說") }
                        }
                    )
                }
            }
        }
    }
}