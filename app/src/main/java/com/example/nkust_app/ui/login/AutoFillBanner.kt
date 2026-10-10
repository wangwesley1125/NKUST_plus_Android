package com.example.nkust_app.ui.login

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.nkust_app.ui.theme.IosBlue

@Composable
fun AutoFillBanner(
    username: String,
    onFill: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        // 對應 iOS 的 .regularMaterial
        color = MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.92f),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, end = 6.dp, top = 12.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 藍底鑰匙圖示
            Box(
                Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(IosBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.Key,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }

            Column(Modifier.weight(1f)) {
                Text(
                    "自動填入帳號密碼",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    username,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Button(
                onClick = onFill,
                colors = ButtonDefaults.buttonColors(containerColor = IosBlue, contentColor = Color.White),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                Text("填入", fontWeight = FontWeight.SemiBold)
            }

            IconButton(onClick = onDismiss) {
                Icon(Icons.Filled.Close, contentDescription = "關閉", tint = IosBlue)
            }
        }
    }
}