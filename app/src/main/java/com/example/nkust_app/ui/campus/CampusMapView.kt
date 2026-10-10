package com.example.nkust_app.ui.campus

import android.graphics.Bitmap
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.nkust_app.data.campus.Building
import com.example.nkust_app.data.campus.Campus
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState

/** 地圖要移動到的位置；key 改變時才移動，避免把使用者拖動的地圖拉回來 */
data class CameraTarget(
    val latitude: Double,
    val longitude: Double,
    val zoom: Float,
    val key: Int = 0
)

typealias MarkerKey = Pair<ImageVector, Color>

@Composable
fun CampusMapView(
    buildings: List<Building>,
    camera: CameraTarget,
    markerIcons: Map<MarkerKey, Bitmap>,
    myLocationEnabled: Boolean,
    onBuildingClick: (Building) -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark = isSystemInDarkTheme()
    val currentOnClick by rememberUpdatedState(onBuildingClick)

    val cameraState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(camera.latitude, camera.longitude), camera.zoom)
    }

    // 換校區、搜尋到教室、定位時移動鏡頭
    LaunchedEffect(camera.key) {
        if (camera.key > 0) {
            cameraState.animate(
                CameraUpdateFactory.newLatLngZoom(LatLng(camera.latitude, camera.longitude), camera.zoom),
                600
            )
        }
    }

    val properties = remember(isDark, myLocationEnabled) {
        MapProperties(
            mapStyleOptions = MapStyleOptions(if (isDark) DARK_STYLE else LIGHT_STYLE),
            isMyLocationEnabled = myLocationEnabled,   // 只有拿到定位權限才會是 true
            minZoomPreference = 12f,
            maxZoomPreference = 21f
        )
    }
    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,   // 用我們自己的圓形定位按鈕
            mapToolbarEnabled = false,
            compassEnabled = true
        )
    }

    GoogleMap(
        modifier = modifier,
        cameraPositionState = cameraState,
        properties = properties,
        uiSettings = uiSettings
    ) {
        buildings.forEachIndexed { index, building ->
            key(building.name, building.latitude, building.longitude, index) {
                val bitmap = markerIcons[building.icon to building.color]
                Marker(
                    state = remember { MarkerState(LatLng(building.latitude, building.longitude)) },
                    title = building.name,
                    icon = bitmap?.let { remember(it) { BitmapDescriptorFactory.fromBitmap(it) } },
                    anchor = Offset(0.5f, 0.5f),
                    onClick = {
                        currentOnClick(building)
                        true   // 自己處理點擊，不顯示 Google 預設的資訊框
                    }
                )
            }
        }
    }
}

/** 為所有校區用到的「圖示 + 顏色」組合各畫一張標記圖 */
@Composable
fun rememberMarkerIcons(): Map<MarkerKey, Bitmap> {
    val density = LocalDensity.current
    val keys = remember {
        Campus.entries.flatMap { it.buildings }.map { it.icon to it.color }.distinct()
    }
    val painters = keys.map { rememberVectorPainter(it.first) }
    return remember(density) {
        keys.zip(painters).associate { (key, painter) -> key to drawMarker(painter, key.second, density) }
    }
}

/** 彩色圓形 + 白色圖示 + 淡淡的陰影（對應 iOS 的 Annotation） */
private fun drawMarker(painter: Painter, color: Color, density: Density): Bitmap {
    val sizePx = with(density) { 42.dp.roundToPx() }
    val image = ImageBitmap(sizePx, sizePx)
    CanvasDrawScope().draw(density, LayoutDirection.Ltr, Canvas(image), Size(sizePx.toFloat(), sizePx.toFloat())) {
        val radius = 18.dp.toPx()
        drawCircle(Color.Black.copy(alpha = 0.25f), radius, center = center + Offset(0f, 1.5.dp.toPx()))
        drawCircle(color, radius)
        val iconPx = 22.dp.toPx()
        val inset = (size.width - iconPx) / 2
        translate(inset, inset) {
            with(painter) { draw(Size(iconPx, iconPx), colorFilter = ColorFilter.tint(Color.White)) }
        }
    }
    return image.asAndroidBitmap()
}

// ═══════ 地圖樣式（本機 JSON，不使用 Map ID，維持免費） ═══════

/** 淺色：隱藏商家、店家等標記 */
private const val LIGHT_STYLE = """
[
  {"featureType":"poi","stylers":[{"visibility":"off"}]},
  {"featureType":"transit","elementType":"labels.icon","stylers":[{"visibility":"off"}]}
]
"""

/** 深色：Google 官方 Night 樣式 + 隱藏商家標記 */
private const val DARK_STYLE = """
[
  {"elementType":"geometry","stylers":[{"color":"#242f3e"}]},
  {"elementType":"labels.text.stroke","stylers":[{"color":"#242f3e"}]},
  {"elementType":"labels.text.fill","stylers":[{"color":"#746855"}]},
  {"featureType":"administrative.locality","elementType":"labels.text.fill","stylers":[{"color":"#d59563"}]},
  {"featureType":"poi","stylers":[{"visibility":"off"}]},
  {"featureType":"road","elementType":"geometry","stylers":[{"color":"#38414e"}]},
  {"featureType":"road","elementType":"geometry.stroke","stylers":[{"color":"#212a37"}]},
  {"featureType":"road","elementType":"labels.text.fill","stylers":[{"color":"#9ca5b3"}]},
  {"featureType":"road.highway","elementType":"geometry","stylers":[{"color":"#746855"}]},
  {"featureType":"road.highway","elementType":"geometry.stroke","stylers":[{"color":"#1f2835"}]},
  {"featureType":"road.highway","elementType":"labels.text.fill","stylers":[{"color":"#f3d19c"}]},
  {"featureType":"transit","stylers":[{"visibility":"off"}]},
  {"featureType":"water","elementType":"geometry","stylers":[{"color":"#17263c"}]},
  {"featureType":"water","elementType":"labels.text.fill","stylers":[{"color":"#515c6d"}]},
  {"featureType":"water","elementType":"labels.text.stroke","stylers":[{"color":"#17263c"}]}
]
"""