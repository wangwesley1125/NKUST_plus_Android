package com.example.nkust_app.ui.campus

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.nkust_app.data.campus.Building
import com.example.nkust_app.data.campus.Campus
import com.example.nkust_app.data.campus.RoomResult
import com.example.nkust_app.data.campus.searchRooms
import com.example.nkust_app.ui.components.CircleIconButton
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority

private const val CAMPUS_ZOOM = 17.5f   // 對應 iOS span 0.003
private const val ROOM_ZOOM = 19.5f     // 搜尋到教室時拉近
private const val ME_ZOOM = 18f

private val LOCATION_PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

@Composable
fun CampusMapScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var campus by rememberSaveable { mutableStateOf(Campus.Jiangong) }
    var camera by remember { mutableStateOf(CameraTarget(campus.latitude, campus.longitude, CAMPUS_ZOOM)) }
    var query by rememberSaveable { mutableStateOf("") }
    val results = remember(query) { searchRooms(query) }
    var selectedBuilding by remember { mutableStateOf<Building?>(null) }
    var highlightedRoom by remember { mutableStateOf("") }
    var hasLocationPermission by remember { mutableStateOf(context.hasLocationPermission()) }

    val markerIcons = rememberMarkerIcons()

    fun moveCamera(latitude: Double, longitude: Double, zoom: Float) {
        camera = CameraTarget(latitude, longitude, zoom, camera.key + 1)
    }

    fun goToMyLocation() {
        fetchCurrentLocation(context) { lat, lng -> moveCamera(lat, lng, ME_ZOOM) }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        hasLocationPermission = grants.values.any { it }
        if (hasLocationPermission) goToMyLocation()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            Box(
                Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("校園地圖", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        Column(Modifier.padding(padding).fillMaxSize()) {

            // 搜尋框
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text("搜尋教室，例如：資001") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
            )

            // 搜尋結果
            if (results.isNotEmpty()) {
                LazyColumn(Modifier.heightIn(max = 250.dp)) {
                    items(results) { result ->
                        SearchResultRow(result) {
                            campus = result.campus
                            moveCamera(result.building.latitude, result.building.longitude, ROOM_ZOOM)
                            highlightedRoom = result.room
                            selectedBuilding = result.building
                            query = ""
                            focusManager.clearFocus()
                        }
                        HorizontalDivider(Modifier.padding(start = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }

            // 校區選擇列
            Row(
                Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Campus.entries.forEach { c ->
                    val selected = c == campus
                    Text(
                        c.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                campus = c
                                highlightedRoom = ""
                                moveCamera(c.latitude, c.longitude, CAMPUS_ZOOM)
                            }
                            .padding(horizontal = 18.dp, vertical = 8.dp)
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

            // 地圖
            Box(Modifier.fillMaxSize()) {
                CampusMapView(
                    buildings = campus.buildings,
                    camera = camera,
                    markerIcons = markerIcons,
                    myLocationEnabled = hasLocationPermission,
                    onBuildingClick = {
                        highlightedRoom = ""
                        selectedBuilding = it
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // 定位按鈕（對應 MapUserLocationButton）
                CircleIconButton(
                    icon = Icons.Filled.NearMe,
                    contentDescription = "我的位置",
                    onClick = {
                        if (context.hasLocationPermission()) goToMyLocation()
                        else permissionLauncher.launch(LOCATION_PERMISSIONS)
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                )
            }
        }
    }

    selectedBuilding?.let { building ->
        BuildingDetailSheet(
            building = building,
            highlightedRoom = highlightedRoom,
            onDismiss = { selectedBuilding = null }
        )
    }
}

private fun Context.hasLocationPermission(): Boolean =
    LOCATION_PERMISSIONS.any { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }

@SuppressLint("MissingPermission")   // 呼叫前已確認過權限
private fun fetchCurrentLocation(context: Context, onResult: (Double, Double) -> Unit) {
    LocationServices.getFusedLocationProviderClient(context)
        .getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
        .addOnSuccessListener { location ->
            location?.let { onResult(it.latitude, it.longitude) }
        }
}

@Composable
private fun SearchResultRow(result: RoomResult, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(result.room, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(
                "${result.campus.title}校區・${result.building.name}・${result.floor}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(Icons.Filled.LocationOn, contentDescription = null, tint = result.building.color)
    }
}