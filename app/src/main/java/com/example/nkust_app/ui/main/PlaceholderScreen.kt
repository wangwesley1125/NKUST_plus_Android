package com.example.nkust_app.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/** 有置中標題列的分頁 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TitledScreen(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable (Modifier) -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = { CenterAlignedTopAppBar(title = { Text(title) }) }
    ) { padding ->
        content(Modifier.padding(padding))
    }
}

@Composable
fun PlaceholderScreen(text: String, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(24.dp)
        )
    }
}