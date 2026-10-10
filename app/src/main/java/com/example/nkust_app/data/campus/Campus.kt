package com.example.nkust_app.data.campus

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

data class Floor(val floor: String, val rooms: List<String>)

/** 建築資料（對應 iOS 的 Building） */
data class Building(
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val description: String,
    val icon: ImageVector,
    val color: Color,
    val classrooms: List<Floor> = emptyList()
)

/** 各校區與校門口座標 */
enum class Campus(val title: String, val latitude: Double, val longitude: Double) {
    Jiangong("建工", 22.651611, 120.328853),
    Yanchao("燕巢", 22.772465, 120.400284),
    Diyi("第一", 22.751928, 120.331460),
    Qijin("旗津", 22.608805, 120.271993),
    Nanzi("楠梓", 22.724602, 120.314650);

    val buildings: List<Building>
        get() = when (this) {
            Jiangong -> JiangongBuildings
            Yanchao -> YanchaoBuildings
            Diyi -> DiyiBuildings
            Qijin -> QijinBuildings
            Nanzi -> NanziBuildings
        }
}

/** 搜尋教室的結果（對應 iOS 的 RoomResult） */
data class RoomResult(
    val campus: Campus,
    val building: Building,
    val floor: String,
    val room: String
)

/** 對應 iOS 的 performSearch：跨所有校區找教室 */
fun searchRooms(query: String): List<RoomResult> {
    val q = query.trim()
    if (q.isEmpty()) return emptyList()
    return Campus.entries.flatMap { campus ->
        campus.buildings.flatMap { building ->
            building.classrooms.flatMap { floor ->
                floor.rooms
                    .filter { it.contains(q, ignoreCase = true) }
                    .map { RoomResult(campus, building, floor.floor, it) }
            }
        }
    }
}