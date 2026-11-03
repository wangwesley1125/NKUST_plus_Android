package com.example.nkust_app.data.parking

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Looper
import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import com.example.nkust_app.data.campus.Campus
import com.example.nkust_app.ui.theme.IosBlue
import com.example.nkust_app.ui.theme.IosOrange
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import org.json.JSONObject
import java.util.Locale
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

/*
 * 記錄停車位置（機車 / 汽車各一筆，對應 iOS ParkingStore.swift）
 *   - 記錄：取得目前定位 → 存在本機 SharedPreferences（不會上傳）
 *   - GPS 校正：在車旁再定位一次，與舊點依準度加權平均（越按越準）
 *   - 手動調整：使用者在地圖上拖曳到正確停車格
 *   - 自動命名：依附近校區建築 / 路名產生備註建議
 */

private const val TAG = "NKUST"

// ═══════ 車種 ═══════

enum class VehicleType(val key: String, val title: String, val icon: ImageVector, val color: Color) {
    Scooter("scooter", "機車", Icons.Filled.TwoWheeler, IosOrange),
    Car("car", "汽車", Icons.Filled.DirectionsCar, IosBlue)
}

// ═══════ 停車位置 ═══════

data class ParkingSpot(
    val latitude: Double,
    val longitude: Double,
    /** 定位誤差（公尺）；手動調整過為 0 */
    val accuracy: Double,
    /** 停車時間（epoch 毫秒） */
    val savedAt: Long,
    /** 使用者備註，例如「燕巢 B 停車場第 3 排」 */
    val note: String,
    /** 是否由使用者手動拖曳調整過 */
    val isAdjusted: Boolean = false
) {
    fun toJson(): String = JSONObject()
        .put("latitude", latitude).put("longitude", longitude).put("accuracy", accuracy)
        .put("savedAt", savedAt).put("note", note).put("isAdjusted", isAdjusted)
        .toString()

    companion object {
        fun fromJson(text: String): ParkingSpot? = runCatching {
            val o = JSONObject(text)
            ParkingSpot(
                latitude = o.getDouble("latitude"),
                longitude = o.getDouble("longitude"),
                accuracy = o.optDouble("accuracy", 0.0),
                savedAt = o.optLong("savedAt"),
                note = o.optString("note"),
                isAdjusted = o.optBoolean("isAdjusted")
            )
        }.getOrNull()
    }
}

/** GPS 校正結果 */
sealed interface RefineResult {
    /** 已與舊點合併 */
    data class Refined(val oldAccuracy: Double, val newAccuracy: Double) : RefineResult
    /** 目前位置離停車點太遠（人可能已經離開），交給 UI 詢問 */
    data class FarAway(val distance: Double, val location: Location) : RefineResult
    /** 定位失敗（錯誤訊息已寫在 errorMessage） */
    data object Failed : RefineResult
}

/** 兩點距離（公尺） */
fun distanceMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
    val out = FloatArray(1)
    Location.distanceBetween(lat1, lng1, lat2, lng2, out)
    return out[0].toDouble()
}

/** 離這個座標最近的校區（以校門口為準） */
fun nearestCampus(latitude: Double, longitude: Double): Campus =
    Campus.entries.minBy { distanceMeters(it.latitude, it.longitude, latitude, longitude) }

// ═══════ 停車位置管理（定位 + 本機儲存） ═══════

object ParkingStore {

    private const val PREFS_NAME = "nkust_parking"
    private fun key(vehicle: VehicleType) = "savedParkingSpot.${vehicle.key}"

    private var prefs: SharedPreferences? = null
    private lateinit var appContext: Context

    private val spots = mutableStateMapOf<VehicleType, ParkingSpot>()

    /** 正在定位哪一台車（null = 沒在定位） */
    var locatingVehicle by mutableStateOf<VehicleType?>(null)
        private set
    val isLocating: Boolean get() = locatingVehicle != null

    /** 錯誤訊息（UI 顯示後設回 null） */
    var errorMessage by mutableStateOf<String?>(null)
    /** 定位權限被拒，UI 可據此顯示「前往設定」 */
    var isPermissionDenied by mutableStateOf(false)
        private set

    fun init(context: Context) {
        if (prefs != null) return
        appContext = context.applicationContext
        val p = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs = p
        VehicleType.entries.forEach { vehicle ->
            p.getString(key(vehicle), null)?.let(ParkingSpot::fromJson)?.let { spots[vehicle] = it }
        }
    }

    fun spot(vehicle: VehicleType): ParkingSpot? = spots[vehicle]

    // ─── 記錄目前位置 ───

    /** 取得目前位置並存成該車種的停車位置（覆蓋舊的）。成功回傳 true。 */
    suspend fun recordCurrentLocation(vehicle: VehicleType): Boolean {
        val loc = acquireLocation(vehicle) ?: return false
        replace(loc, vehicle)
        return true
    }

    /** 用指定的定位結果取代停車位置（新的一次停車，備註清空） */
    fun replace(location: Location, vehicle: VehicleType) {
        save(
            vehicle,
            ParkingSpot(
                latitude = location.latitude,
                longitude = location.longitude,
                accuracy = location.accuracy.toDouble(),
                savedAt = System.currentTimeMillis(),
                note = ""
            )
        )
    }

    // ─── GPS 校正 ───

    /**
     * 在車旁再定位一次，與舊點依準度加權平均。
     * 誤差越小的那筆權重越大（權重 = 1 / 誤差²），所以多校正幾次會越來越準。
     */
    suspend fun refineLocation(vehicle: VehicleType): RefineResult {
        val old = spots[vehicle] ?: return RefineResult.Failed
        val loc = acquireLocation(vehicle) ?: return RefineResult.Failed

        val distance = distanceMeters(old.latitude, old.longitude, loc.latitude, loc.longitude)
        // 兩個誤差圈有重疊（或 30 公尺內）才合併；否則人大概已經離開車子了
        val threshold = max(30.0, old.accuracy + loc.accuracy)
        if (distance > threshold) return RefineResult.FarAway(distance, loc)

        val w1 = 1 / max(old.accuracy, 1.0).pow(2)
        val w2 = 1 / max(loc.accuracy.toDouble(), 1.0).pow(2)
        val merged = old.copy(
            latitude = (old.latitude * w1 + loc.latitude * w2) / (w1 + w2),
            longitude = (old.longitude * w1 + loc.longitude * w2) / (w1 + w2),
            // 理論誤差會變小，但短時間內的 GPS 誤差彼此相關，不會真的無限變準，最低只顯示到 3 公尺
            accuracy = max(1 / sqrt(w1 + w2), 3.0)
        )
        save(vehicle, merged)
        return RefineResult.Refined(old.accuracy, merged.accuracy)
    }

    // ─── 手動調整 / 備註 / 清除 ───

    /** 使用者在地圖上拖曳後確認的位置。保留停車時間與備註。 */
    fun setManualLocation(latitude: Double, longitude: Double, vehicle: VehicleType) {
        val s = spots[vehicle] ?: return
        save(vehicle, s.copy(latitude = latitude, longitude = longitude, accuracy = 0.0, isAdjusted = true))
    }

    fun updateNote(note: String, vehicle: VehicleType) {
        val s = spots[vehicle] ?: return
        save(vehicle, s.copy(note = note.trim()))
    }

    fun clear(vehicle: VehicleType) {
        spots.remove(vehicle)
        prefs?.edit { remove(key(vehicle)) }
    }

    // ─── 用 Google 地圖步行導航 ───

    fun openWalkingDirections(context: Context, vehicle: VehicleType) {
        val s = spots[vehicle] ?: return
        val uri = Uri.parse(
            "https://www.google.com/maps/dir/?api=1&destination=${s.latitude},${s.longitude}&travelmode=walking"
        )
        try {
            context.startActivity(Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (e: ActivityNotFoundException) {
            errorMessage = "找不到可以導航的地圖 App"
        }
    }

    // ─── 定位（共用） ───

    private fun hasPermission(): Boolean = listOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION
    ).any { ContextCompat.checkSelfPermission(appContext, it) == PackageManager.PERMISSION_GRANTED }

    /** 使用者拒絕定位權限時由 UI 呼叫 */
    fun markPermissionDenied() {
        isPermissionDenied = true
        errorMessage = "需要定位權限才能記錄停車位置，請到「設定」開啟。"
    }

    fun dismissError() {
        errorMessage = null
        isPermissionDenied = false
    }

    /**
     * 取得一筆目前位置：最多等約 10 秒，精度到 15 公尺內就提早結束。
     * 失敗時回傳 null，並把原因寫進 errorMessage。
     */
    @SuppressLint("MissingPermission")   // 下面已確認過權限
    private suspend fun acquireLocation(vehicle: VehicleType): Location? {
        if (isLocating) return null   // 一次只定位一台
        errorMessage = null
        isPermissionDenied = false
        if (!hasPermission()) {
            markPermissionDenied()
            return null
        }

        locatingVehicle = vehicle
        var best: Location? = null
        val client = LocationServices.getFusedLocationProviderClient(appContext)
        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                for (loc in result.locations) {
                    if (!loc.hasAccuracy()) continue
                    // 只收最近 10 秒內的位置，避免拿到系統快取的舊位置
                    val ageMs = abs(System.currentTimeMillis() - loc.time)
                    if (ageMs > 10_000) continue
                    val current = best
                    if (current == null || loc.accuracy < current.accuracy) best = loc
                }
            }
        }
        val request = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 500L)
            .setMinUpdateIntervalMillis(250L)
            .setWaitForAccurateLocation(false)
            .build()

        try {
            client.requestLocationUpdates(request, callback, Looper.getMainLooper())
            // GPS 剛啟動的前幾筆通常誤差很大，多等一下會準很多
            for (i in 0 until 20) {
                delay(500)
                val b = best
                if (b != null && b.accuracy <= 15f) break
            }
        } catch (e: SecurityException) {
            markPermissionDenied()
        } catch (e: kotlinx.coroutines.CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.w(TAG, "停車定位失敗", e)
        } finally {
            client.removeLocationUpdates(callback)
            locatingVehicle = null
        }

        if (isPermissionDenied) return null
        val result = best
        if (result == null) {
            errorMessage = "目前無法取得位置，請移到較空曠的地方再試一次。"
        }
        return result
    }

    private fun save(vehicle: VehicleType, spot: ParkingSpot) {
        spots[vehicle] = spot
        prefs?.edit { putString(key(vehicle), spot.toJson()) }
    }
}

// ═══════ 停車地點名稱建議 ═══════

/**
 * 依序嘗試：
 *   1. 自己的校區建築資料：150 公尺內最近的建築 →「建工校區・電資大樓旁」
 *   2. 系統反向地理編碼（路名 / 地點名稱）→「建工校區・建工路附近」
 * 最多等 3 秒，找不到就回傳 null（使用者自己填）
 */
object ParkingNameSuggester {

    suspend fun suggest(context: Context, latitude: Double, longitude: Double): String? =
        withTimeoutOrNull(3_000) { compute(context, latitude, longitude) }

    private suspend fun compute(context: Context, latitude: Double, longitude: Double): String? {
        val campus = nearestCampus(latitude, longitude)
        val campusDistance = distanceMeters(campus.latitude, campus.longitude, latitude, longitude)
        // 離校門 1.5 公里內才加校區名稱
        val nearCampus = campusDistance < 1500
        val prefix = if (nearCampus) "${campus.title}校區・" else ""

        // 1. 最近的校區建築
        if (nearCampus) {
            val nearest = campus.buildings
                .map { it.name to distanceMeters(it.latitude, it.longitude, latitude, longitude) }
                .minByOrNull { it.second }
            if (nearest != null && nearest.second <= 150) return "$prefix${nearest.first}旁"
        }

        // 2. 路名 / 地點名稱
        return reverseGeocode(context, latitude, longitude)?.let { "$prefix${it}附近" }
    }

    @Suppress("DEPRECATION")   // 新版的非同步 API 需要 Android 13，minSdk 28 用同步版即可（在背景執行緒）
    private suspend fun reverseGeocode(context: Context, latitude: Double, longitude: Double): String? =
        withContext(Dispatchers.IO) {
            if (!Geocoder.isPresent()) return@withContext null
            runCatching {
                val address = Geocoder(context, Locale.TAIWAN).getFromLocation(latitude, longitude, 1)
                    ?.firstOrNull() ?: return@runCatching null
                address.thoroughfare?.takeIf { it.isNotBlank() }
                    ?: address.featureName?.takeIf { it.isNotBlank() && !it.any(Char::isDigit) }
            }.getOrNull()
        }
}