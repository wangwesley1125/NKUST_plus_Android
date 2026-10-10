package com.example.nkust_app.ui.profile

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nkust_app.ui.calendar.CalendarScreen
import com.example.nkust_app.ui.components.BackTopBar

private enum class ProfilePage { Main, LibraryCard, Certificate, Calendar }

@Composable
fun ProfileScreen(
    onBack: () -> Unit,
    onLogout: () -> Unit,
    onSessionExpired: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = viewModel()
) {
    var page by rememberSaveable { mutableStateOf(ProfilePage.Main) }
    val backToMain = { page = ProfilePage.Main }

    LaunchedEffect(viewModel.sessionExpired) {
        if (viewModel.sessionExpired) onSessionExpired()
    }

    when (page) {
        ProfilePage.LibraryCard -> LibraryCardScreen(
            studentId = viewModel.profile.studentId,
            name = viewModel.profile.name,
            onBack = backToMain,
            modifier = modifier
        )

        ProfilePage.Certificate -> CertificateScreen(
            onBack = backToMain,
            onSessionExpired = onSessionExpired,
            modifier = modifier
        )

        ProfilePage.Calendar -> CalendarScreen(
            modifier = modifier,
            onBack = backToMain
        )

        ProfilePage.Main -> {
            BackHandler(onBack = onBack)
            Scaffold(modifier = modifier, topBar = { BackTopBar("個人資料", onBack) }) { padding ->
                if (viewModel.isLoading) {
                    Box(Modifier.padding(padding).fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    return@Scaffold
                }

                Column(
                    Modifier
                        .padding(padding)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    ProfileHeader(viewModel, onShowLibraryCard = { page = ProfilePage.LibraryCard })

                    // 學籍資料
                    Section("學籍資料") {
                        InfoRow(Icons.Filled.AccountBalance, "科系", viewModel.profile.department)
                        RowDivider()
                        InfoRow(Icons.Filled.Groups, "班級", viewModel.profile.className)
                        RowDivider()
                        InfoRow(
                            Icons.Filled.Verified, "在學狀態", viewModel.profile.status,
                            onClick = { page = ProfilePage.Certificate }
                        )
                    }

                    // 行事曆
                    Section(null) {
                        LinkRow(
                            Icons.Filled.CalendarMonth, "行事曆",
                            color = MaterialTheme.colorScheme.primary,
                            showChevron = true,
                            onClick = { page = ProfilePage.Calendar }
                        )
                    }

                    // 登出
                    Section(null) {
                        LinkRow(
                            Icons.AutoMirrored.Filled.Logout, "登出",
                            color = MaterialTheme.colorScheme.error,
                            onClick = onLogout
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(viewModel: ProfileViewModel, onShowLibraryCard: () -> Unit) {
    val profile = viewModel.profile
    val teal = MaterialTheme.colorScheme.primary

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 照片
        Box(
            Modifier
                .size(84.dp)
                .clip(CircleShape)
                .border(2.dp, teal, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            val photo = viewModel.photo
            if (photo != null) {
                Image(photo, contentDescription = "大頭照", contentScale = ContentScale.Crop, modifier = Modifier.fillMaxSize())
            } else {
                Icon(Icons.Filled.AccountCircle, contentDescription = null, tint = teal, modifier = Modifier.fillMaxSize())
            }
        }

        // 姓名 + 學號
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(profile.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            if (profile.englishName.isNotEmpty()) {
                Text(profile.englishName, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(profile.studentId, style = MaterialTheme.typography.titleMedium, color = teal)
        }

        // QR Code（借閱證）
        Icon(
            Icons.Filled.QrCode2,
            contentDescription = "圖書館借閱證",
            tint = teal,
            modifier = Modifier
                .size(60.dp)
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onShowLibraryCard)
        )
    }
}

@Composable
private fun Section(title: String?, content: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (title != null) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp)
            )
        }
        Column(
            Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow),
            content = content
        )
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(Modifier.padding(start = 56.dp), color = MaterialTheme.colorScheme.outlineVariant)
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String, onClick: (() -> Unit)? = null) {
    Row(
        Modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .padding(horizontal = 16.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(26.dp))
        Text(label, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        if (onClick != null) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun LinkRow(
    icon: ImageVector,
    text: String,
    color: Color,
    showChevron: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
        Text(text, style = MaterialTheme.typography.titleLarge, color = color, modifier = Modifier.weight(1f))
        if (showChevron) {
            Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}