package com.example.nkust_app.ui.course

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteSheet(
    courseName: String,
    initialNote: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initialNote) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
                .imePadding(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(Modifier.fillMaxWidth()) {
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterStart)) {
                    Text("取消", color = MaterialTheme.colorScheme.primary)
                }
                Text(
                    "備注",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center)
                )
                TextButton(onClick = { onSave(text) }, modifier = Modifier.align(Alignment.CenterEnd)) {
                    Text("儲存", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }

            Text(courseName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            TextField(
                value = text,
                onValueChange = { text = it },
                modifier = Modifier.fillMaxWidth().height(200.dp),
                shape = RoundedCornerShape(20.dp),
                placeholder = { Text("輸入備注…") },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                )
            )
        }
    }
}