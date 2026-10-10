package com.example.nkust_app.ui.campus

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.nkust_app.data.campus.Building

private val HighlightYellow = Color(0xFFFFCC00)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildingDetailSheet(
    building: Building,
    highlightedRoom: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    // 有搜尋的教室就跳到那層，不然選第一層
    var selectedFloor by remember(building) {
        mutableStateOf(
            building.classrooms.firstOrNull { highlightedRoom in it.rooms }?.floor
                ?: building.classrooms.firstOrNull()?.floor.orEmpty()
        )
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 標題
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Icon(building.icon, contentDescription = null, tint = building.color, modifier = Modifier.size(28.dp))
                Text(building.name, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }

            if (building.description.isNotEmpty()) {
                Text(building.description, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            if (building.classrooms.isNotEmpty()) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                // 樓層選擇列
                Row(
                    Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    building.classrooms.forEach { floor ->
                        val selected = floor.floor == selectedFloor
                        Text(
                            floor.floor,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (selected) building.color else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { selectedFloor = floor.floor }
                                .padding(horizontal = 14.dp, vertical = 7.dp)
                        )
                    }
                }

                // 選定樓層的教室
                building.classrooms.firstOrNull { it.floor == selectedFloor }?.let { current ->
                    Text("教室列表", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Column(
                        Modifier
                            .fillMaxWidth()
                            .heightIn(max = 220.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .verticalScroll(rememberScrollState())
                            .padding(10.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        current.rooms.chunked(4).forEach { row ->
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                row.forEach { room -> RoomTag(room, room == highlightedRoom, Modifier.weight(1f)) }
                                repeat(4 - row.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            // 開啟 Google Maps
            Button(onClick = { openInMaps(context, building) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Map, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text("在 Google Maps 開啟")
            }
        }
    }
}

@Composable
private fun RoomTag(room: String, highlighted: Boolean, modifier: Modifier) {
    Text(
        room,
        style = MaterialTheme.typography.labelSmall,
        textAlign = TextAlign.Center,
        color = if (highlighted) Color.Black else MaterialTheme.colorScheme.onSurface,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (highlighted) HighlightYellow else MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(horizontal = 6.dp, vertical = 4.dp)
    )
}

/** 優先開 Google Maps App，沒安裝就用瀏覽器（同 iOS） */
private fun openInMaps(context: Context, building: Building) {
    val lat = building.latitude
    val lng = building.longitude
    val geoUri = Uri.parse("geo:$lat,$lng?q=$lat,$lng(${Uri.encode(building.name)})")
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, geoUri).setPackage("com.google.android.apps.maps"))
    } catch (e: ActivityNotFoundException) {
        context.startActivity(
            Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=$lat,$lng"))
        )
    }
}