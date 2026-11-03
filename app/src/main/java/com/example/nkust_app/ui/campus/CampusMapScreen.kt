package com.example.nkust_app.ui.campus

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.CenterFocusStrong
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.nkust_app.data.campus.Building
import com.example.nkust_app.data.campus.Campus
import com.example.nkust_app.data.campus.RoomResult
import com.example.nkust_app.data.campus.searchRooms
import com.example.nkust_app.data.parking.ParkingNameSuggester
import com.example.nkust_app.data.parking.ParkingSpot
import com.example.nkust_app.data.parking.ParkingStore
import com.example.nkust_app.data.parking.RefineResult
import com.example.nkust_app.data.parking.VehicleType
import com.example.nkust_app.data.parking.nearestCampus
import com.example.nkust_app.ui.components.CircleIconButton
import com.example.nkust_app.ui.main.TabBarBottomSpace
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val CAMPUS_ZOOM = 17.5f   // 對應 iOS span 0.003
private const val ROOM_ZOOM = 19.5f     // 搜尋到教室時拉近
private const val ME_ZOOM = 18f
private const val PARKING_ZOOM = 18.5f  // 前往停車位置
private const val ADJUST_ZOOM = 20f     // 拖曳調整時拉近，方便對準停車格

private val LOCATION_PERMISSIONS = arrayOf(
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
)

/** GPS 校正時發現人離車太遠，詢問是否移動 */
private data class MovePrompt(
    val vehicle: VehicleType,
    val distance: Double,
    val location: android.location.Location
)

/** 拿到定位權限後要繼續做的事 */
private sealed interface PendingAction {
    data object GoToMe : PendingAction
    data class Record(val vehicle: VehicleType) : PendingAction
    data class Refine(val vehicle: VehicleType) : PendingAction
}

@Composable
fun CampusMapScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()
    val parking = ParkingStore
    parking.init(context)   // 只有第一次會真的讀取，之後直接略過

    var campus by rememberSaveable { mutableStateOf(Campus.Jiangong) }
    var camera by remember { mutableStateOf(CameraTarget(campus.latitude, campus.longitude, CAMPUS_ZOOM)) }
    var query by rememberSaveable { mutableStateOf("") }
    val results = remember(query) { searchRooms(query) }
    var selectedBuilding by remember { mutableStateOf<Building?>(null) }
    var highlightedRoom by remember { mutableStateOf("") }
    var hasLocationPermission by remember { mutableStateOf(context.hasLocationPermission()) }

    // ─── 停車位置 ───
    var noteVehicle by remember { mutableStateOf<VehicleType?>(null) }
    var noteDraft by remember { mutableStateOf("") }
    /** 備註是不是系統依附近地點自動填的（用來改提示文字） */
    var noteIsSuggested by remember { mutableStateOf(false) }
    var overwriteVehicle by remember { mutableStateOf<VehicleType?>(null) }
    var clearVehicle by remember { mutableStateOf<VehicleType?>(null) }
    var movePrompt by remember { mutableStateOf<MovePrompt?>(null) }
    var menuVehicle by remember { mutableStateOf<VehicleType?>(null) }
    // 手動拖曳調整（Uber 式：地圖中央固定圖釘，拖地圖對準）
    var adjustingVehicle by remember { mutableStateOf<VehicleType?>(null) }
    var adjustCenter by remember { mutableStateOf<LatLng?>(null) }
    var pendingAction by remember { mutableStateOf<PendingAction?>(null) }
    // 上方小提示
    var toastMessage by remember { mutableStateOf<String?>(null) }
    var toastJob by remember { mutableStateOf<Job?>(null) }

    val markerIcons = rememberMarkerIcons()
    val parkingIcons = rememberParkingIcons()
    val bottomSpace = TabBarBottomSpace

    fun moveCamera(latitude: Double, longitude: Double, zoom: Float) {
        camera = CameraTarget(latitude, longitude, zoom, camera.key + 1)
    }

    fun showToast(message: String, autoHide: Boolean = true) {
        toastJob?.cancel()
        toastMessage = message
        if (autoHide) {
            toastJob = scope.launch {
                delay(2500)
                toastMessage = null
            }
        }
    }

    fun hideToast() {
        toastJob?.cancel()
        toastMessage = null
    }

    fun goToMyLocation() {
        fetchCurrentLocation(context) { lat, lng -> moveCamera(lat, lng, ME_ZOOM) }
    }

    fun goToParking(vehicle: VehicleType) {
        val spot = parking.spot(vehicle) ?: return
        campus = nearestCampus(spot.latitude, spot.longitude)
        moveCamera(spot.latitude, spot.longitude, PARKING_ZOOM)
    }

    /** 開備註框；suggest = true 時先依附近地點預填（使用者仍需按「儲存」） */
    suspend fun promptNote(vehicle: VehicleType, suggest: Boolean) {
        val spot = parking.spot(vehicle) ?: return
        noteDraft = spot.note
        noteIsSuggested = false
        if (suggest) {
            showToast("正在找附近地點…", autoHide = false)
            ParkingNameSuggester.suggest(context, spot.latitude, spot.longitude)?.let {
                noteDraft = it
                noteIsSuggested = true
            }
            hideToast()
        }
        noteVehicle = vehicle
    }

    fun recordParking(vehicle: VehicleType) {
        scope.launch {
            if (!parking.recordCurrentLocation(vehicle)) return@launch
            goToParking(vehicle)
            promptNote(vehicle, suggest = true)
        }
    }

    fun refineParking(vehicle: VehicleType) {
        scope.launch {
            when (val result = parking.refineLocation(vehicle)) {
                is RefineResult.Refined -> {
                    goToParking(vehicle)
                    val old = Math.round(result.oldAccuracy)
                    val new = Math.round(result.newAccuracy)
                    showToast(
                        if (result.newAccuracy < result.oldAccuracy - 0.5) "已校正，誤差約 $old → $new 公尺"
                        else "已校正，位置已經很準了"
                    )
                }
                is RefineResult.FarAway -> movePrompt = MovePrompt(vehicle, result.distance, result.location)
                RefineResult.Failed -> Unit   // 錯誤訊息由「無法取得位置」對話框顯示
            }
        }
    }

    fun perform(action: PendingAction) {
        when (action) {
            PendingAction.GoToMe -> goToMyLocation()
            is PendingAction.Record -> recordParking(action.vehicle)
            is PendingAction.Refine -> refineParking(action.vehicle)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        hasLocationPermission = grants.values.any { it }
        val action = pendingAction
        pendingAction = null
        when {
            hasLocationPermission -> action?.let { perform(it) }
            action != null && action != PendingAction.GoToMe -> parking.markPermissionDenied()
        }
    }

    /** 有權限就直接做，沒有就先要權限 */
    fun withLocation(action: PendingAction) {
        if (context.hasLocationPermission()) {
            hasLocationPermission = true
            perform(action)
        } else {
            pendingAction = action
            permissionLauncher.launch(LOCATION_PERMISSIONS)
        }
    }

    fun startAdjusting(vehicle: VehicleType) {
        val spot = parking.spot(vehicle) ?: return
        adjustCenter = LatLng(spot.latitude, spot.longitude)
        campus = nearestCampus(spot.latitude, spot.longitude)
        adjustingVehicle = vehicle
        moveCamera(spot.latitude, spot.longitude, ADJUST_ZOOM)
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
                enabled = adjustingVehicle == null,
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
                            .clickable(enabled = adjustingVehicle == null) {
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
                // 正在拖曳調整的那台先藏起來，改用畫面中央的圖釘
                val visibleSpots = VehicleType.entries
                    .filter { it != adjustingVehicle }
                    .mapNotNull { v -> parking.spot(v)?.let { v to it } }
                    .toMap()

                CampusMapView(
                    buildings = campus.buildings,
                    camera = camera,
                    markerIcons = markerIcons,
                    myLocationEnabled = hasLocationPermission,
                    onBuildingClick = {
                        if (adjustingVehicle == null) {
                            highlightedRoom = ""
                            selectedBuilding = it
                        }
                    },
                    parkingSpots = visibleSpots,
                    parkingIcons = parkingIcons,
                    // 地圖下方被浮動分頁列擋住：Google 標誌和鏡頭中心都往上移
                    contentPadding = PaddingValues(bottom = bottomSpace),
                    onCameraCenterChange = { if (adjustingVehicle != null) adjustCenter = it },
                    modifier = Modifier.fillMaxSize()
                )

                // 定位按鈕（對應 MapUserLocationButton）
                CircleIconButton(
                    icon = Icons.Filled.NearMe,
                    contentDescription = "我的位置",
                    onClick = { withLocation(PendingAction.GoToMe) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                )

                // 拖曳調整：圖釘固定在「可見地圖」的正中央（扣掉下方分頁列）
                adjustingVehicle?.let { vehicle ->
                    Box(
                        Modifier
                            .fillMaxSize()
                            .padding(bottom = bottomSpace),
                        contentAlignment = Alignment.Center
                    ) {
                        CenterPin(vehicle)
                    }
                }

                // 右下角：機車 / 汽車停車按鈕（調整時隱藏）
                if (adjustingVehicle == null) {
                    Column(
                        Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 12.dp, bottom = bottomSpace + 4.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        VehicleType.entries.forEach { vehicle ->
                            val spot = parking.spot(vehicle)
                            Box {
                                VehicleTile(
                                    vehicle = vehicle,
                                    recorded = spot != null,
                                    locating = parking.locatingVehicle == vehicle,
                                    enabled = !parking.isLocating,
                                    onClick = {
                                        // 沒記錄：點一下直接記錄目前位置；已記錄：點一下開選單
                                        if (spot == null) withLocation(PendingAction.Record(vehicle))
                                        else menuVehicle = vehicle
                                    }
                                )
                                if (spot != null) {
                                    VehicleMenu(
                                        expanded = menuVehicle == vehicle,
                                        vehicle = vehicle,
                                        spot = spot,
                                        onDismiss = { menuVehicle = null },
                                        onGoTo = { goToParking(vehicle) },
                                        onWalk = { parking.openWalkingDirections(context, vehicle) },
                                        onRefine = { withLocation(PendingAction.Refine(vehicle)) },
                                        onAdjust = { startAdjusting(vehicle) },
                                        onOverwrite = { overwriteVehicle = vehicle },
                                        onEditNote = { scope.launch { promptNote(vehicle, suggest = spot.note.isEmpty()) } },
                                        onClear = { clearVehicle = vehicle }
                                    )
                                }
                            }
                        }
                    }
                }

                // 拖曳調整：底部確認列
                adjustingVehicle?.let { vehicle ->
                    AdjustBar(
                        vehicle = vehicle,
                        onCancel = { adjustingVehicle = null },
                        onConfirm = {
                            adjustCenter?.let {
                                parking.setManualLocation(it.latitude, it.longitude, vehicle)
                                showToast("已更新${vehicle.title}位置")
                            }
                            adjustingVehicle = null
                        },
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(start = 16.dp, end = 16.dp, bottom = bottomSpace + 4.dp)
                    )
                }

                // 上方小提示
                androidx.compose.animation.AnimatedVisibility(
                    visible = toastMessage != null,
                    enter = slideInVertically { -it } + fadeIn(),
                    exit = slideOutVertically { -it } + fadeOut(),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = 12.dp)
                ) {
                    Text(
                        toastMessage.orEmpty(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .shadow(6.dp, CircleShape)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
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

    // ─── 對話框 ───

    // 備註（記錄後 / 編輯時）：可能已預填附近地點，使用者按「儲存」才會存
    noteVehicle?.let { vehicle ->
        AlertDialog(
            onDismissRequest = { noteVehicle = null },
            title = { Text("停車備註") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        if (noteIsSuggested) "已依附近地點幫你的${vehicle.title}填好名稱，可以修改後按「儲存」"
                        else "幫你的${vehicle.title}加個備註，下次比較好找（可略過）"
                    )
                    OutlinedTextField(
                        value = noteDraft,
                        onValueChange = { noteDraft = it },
                        singleLine = true,
                        placeholder = { Text("例如：燕巢 B 停車場第 3 排") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    parking.updateNote(noteDraft, vehicle)
                    noteVehicle = null
                }) { Text("儲存") }
            },
            dismissButton = {
                TextButton(onClick = { noteVehicle = null }) { Text("略過") }
            }
        )
    }

    // 重新記錄前確認
    overwriteVehicle?.let { vehicle ->
        ConfirmDialog(
            title = "要重新記錄停車位置嗎？",
            message = "會用你現在站的位置取代原本的停車點。",
            confirmText = "把${vehicle.title}位置移到目前位置",
            onConfirm = { withLocation(PendingAction.Record(vehicle)) },
            onDismiss = { overwriteVehicle = null }
        )
    }

    // GPS 校正時離車太遠
    movePrompt?.let { prompt ->
        ConfirmDialog(
            title = "你離停車位置約 ${prompt.distance.toInt()} 公尺",
            message = "GPS 校正要站在車旁邊才有效。如果你已經離開，請保留原位置。",
            confirmText = "把${prompt.vehicle.title}移到目前位置",
            dismissText = "保留原位置",
            onConfirm = {
                parking.replace(prompt.location, prompt.vehicle)
                goToParking(prompt.vehicle)
                scope.launch { promptNote(prompt.vehicle, suggest = true) }
            },
            onDismiss = { movePrompt = null }
        )
    }

    // 清除
    clearVehicle?.let { vehicle ->
        ConfirmDialog(
            title = "要清除停車位置嗎？",
            message = null,
            confirmText = "清除${vehicle.title}停車位置",
            destructive = true,
            onConfirm = { parking.clear(vehicle) },
            onDismiss = { clearVehicle = null }
        )
    }

    // 定位失敗 / 權限被拒
    parking.errorMessage?.let { message ->
        AlertDialog(
            onDismissRequest = { parking.dismissError() },
            title = { Text("無法取得位置") },
            text = { Text(message) },
            confirmButton = {
                if (parking.isPermissionDenied) {
                    TextButton(onClick = {
                        parking.dismissError()
                        context.startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null)
                            ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }) { Text("前往設定") }
                } else {
                    TextButton(onClick = { parking.dismissError() }) { Text("好") }
                }
            },
            dismissButton = if (parking.isPermissionDenied) {
                { TextButton(onClick = { parking.dismissError() }) { Text("好") } }
            } else null
        )
    }
}

// ═══════ 右下角車輛按鈕 ═══════

/** 小圓角正方形：未記錄 = 白底 + 彩色圖示；已記錄 = 彩色底 + 白色圖示 + 綠勾勾 */
@Composable
private fun VehicleTile(
    vehicle: VehicleType,
    recorded: Boolean,
    locating: Boolean,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val shape = RoundedCornerShape(12.dp)
    val content = if (recorded) Color.White else vehicle.color

    Box {
        Box(
            Modifier
                .size(50.dp)
                .shadow(6.dp, shape)
                .clip(shape)
                .background(if (recorded) vehicle.color else MaterialTheme.colorScheme.surfaceContainerLow)
                .clickable(enabled = enabled, onClick = onClick)
                .semantics {
                    contentDescription =
                        if (recorded) "${vehicle.title}停車位置選單" else "記錄${vehicle.title}停車位置"
                },
            contentAlignment = Alignment.Center
        ) {
            if (locating) {
                CircularProgressIndicator(Modifier.size(22.dp), color = content, strokeWidth = 2.dp)
            } else {
                Icon(vehicle.icon, contentDescription = null, tint = content, modifier = Modifier.size(26.dp))
            }
        }
        if (recorded) {
            Icon(
                Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = Color(0xFF34C759),
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 5.dp, y = (-5).dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}

private val timeFormat = SimpleDateFormat("M/d HH:mm", Locale.TAIWAN)

private fun menuTitle(vehicle: VehicleType, spot: ParkingSpot): String {
    val title = spot.note.ifEmpty { "我的${vehicle.title}" }
    val time = timeFormat.format(Date(spot.savedAt))
    val precision = if (spot.isAdjusted) "已手動調整" else "誤差約 ${Math.round(spot.accuracy)} 公尺"
    return "$title\n$time 停・$precision"
}

@Composable
private fun VehicleMenu(
    expanded: Boolean,
    vehicle: VehicleType,
    spot: ParkingSpot,
    onDismiss: () -> Unit,
    onGoTo: () -> Unit,
    onWalk: () -> Unit,
    onRefine: () -> Unit,
    onAdjust: () -> Unit,
    onOverwrite: () -> Unit,
    onEditNote: () -> Unit,
    onClear: () -> Unit
) {
    DropdownMenu(expanded = expanded, onDismissRequest = onDismiss) {
        Text(
            menuTitle(vehicle, spot),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
        MenuItem("前往停車位置", Icons.Filled.CenterFocusStrong, onDismiss = onDismiss, action = onGoTo)
        MenuItem("步行導航", Icons.AutoMirrored.Filled.DirectionsWalk, onDismiss = onDismiss, action = onWalk)
        HorizontalDivider()
        if (!spot.isAdjusted) {
            MenuItem("GPS 校正（站在車旁按）", Icons.Filled.GpsFixed, onDismiss = onDismiss, action = onRefine)
        }
        MenuItem("在地圖上拖曳調整", Icons.Filled.PanTool, onDismiss = onDismiss, action = onAdjust)
        MenuItem("重新記錄（移到目前位置）", Icons.Filled.Refresh, onDismiss = onDismiss, action = onOverwrite)
        HorizontalDivider()
        MenuItem("編輯備註", Icons.Filled.Edit, onDismiss = onDismiss, action = onEditNote)
        MenuItem("清除停車位置", Icons.Filled.Delete, danger = true, onDismiss = onDismiss, action = onClear)
    }
}

@Composable
private fun MenuItem(
    text: String,
    icon: ImageVector,
    danger: Boolean = false,
    onDismiss: () -> Unit,
    action: () -> Unit
) {
    val color = if (danger) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
    DropdownMenuItem(
        text = { Text(text, color = color) },
        leadingIcon = { Icon(icon, contentDescription = null, tint = color) },
        onClick = {
            onDismiss()
            action()
        }
    )
}

// ═══════ 拖曳調整 UI ═══════

/** 圖釘尖端剛好對準地圖中心 */
@Composable
private fun CenterPin(vehicle: VehicleType) {
    Box(contentAlignment = Alignment.Center) {
        // 地上的小影子，標示真正的落點
        Box(
            Modifier
                .size(width = 12.dp, height = 6.dp)
                .clip(CircleShape)
                .background(Color.Black.copy(alpha = 0.3f))
        )
        Column(
            Modifier.offset(y = (-29).dp),   // (44 + 14) / 2：讓圖釘底部落在中心點
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier
                    .size(44.dp)
                    .shadow(3.dp, CircleShape)
                    .clip(CircleShape)
                    .background(Color.White)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(vehicle.color),
                contentAlignment = Alignment.Center
            ) {
                Icon(vehicle.icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(22.dp))
            }
            Box(
                Modifier
                    .size(width = 3.dp, height = 14.dp)
                    .background(vehicle.color)
            )
        }
    }
}

@Composable
private fun AdjustBar(
    vehicle: VehicleType,
    onCancel: () -> Unit,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier
            .fillMaxWidth()
            .shadow(10.dp, RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            "拖曳地圖，讓${vehicle.title}圖釘對準你的停車格",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(
                onClick = onCancel,
                modifier = Modifier.weight(1f).height(48.dp)
            ) { Text("取消") }
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(1f).height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = vehicle.color, contentColor = Color.White)
            ) { Text("確認位置") }
        }
    }
}

@Composable
private fun ConfirmDialog(
    title: String,
    message: String?,
    confirmText: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    dismissText: String = "取消",
    destructive: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = if (message != null) {
            { Text(message) }
        } else null,
        confirmButton = {
            TextButton(onClick = {
                onDismiss()
                onConfirm()
            }) {
                Text(
                    confirmText,
                    color = if (destructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(dismissText) }
        }
    )
}

// ═══════ 其他 ═══════

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