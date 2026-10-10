package com.example.nkust_app.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nkust_app.ui.theme.IosBlue
import com.example.nkust_app.ui.theme.IosGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavePasswordSheet(
    username: String,
    isUpdate: Boolean,
    onSave: () -> Unit,
    onSkip: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onSkip) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    Icons.Filled.Security,
                    contentDescription = null,
                    tint = IosBlue,
                    modifier = Modifier.size(52.dp)
                )
                Text(
                    if (isUpdate) "更新已儲存的密碼？" else "儲存帳號密碼？",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    "帳密將以加密方式儲存於裝置\n下次登入時可快速自動填入",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            // 帳號預覽列
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    Icons.Filled.AccountCircle,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(username, modifier = Modifier.weight(1f))
                Icon(Icons.Filled.VerifiedUser, contentDescription = null, tint = IosGreen)
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Button(
                    onClick = onSave,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IosBlue, contentColor = Color.White)
                ) {
                    Text(if (isUpdate) "更新密碼" else "儲存密碼", fontWeight = FontWeight.SemiBold)
                }
                TextButton(
                    onClick = onSkip,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("不要儲存", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}