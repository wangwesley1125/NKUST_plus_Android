package com.example.nkust_app.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.ui.graphics.vector.ImageVector

enum class MainTab(val title: String, val icon: ImageVector) {
    Home("首頁", Icons.Filled.Home),                    // house
    Course("個人課表", Icons.Filled.CalendarMonth),      // calendar
    Grades("歷年成績", Icons.Filled.BarChart),           // chart.bar.fill
    Calendar("行事曆", Icons.Filled.CalendarMonth),      // calendar
    Map("校園地圖", Icons.Filled.Map),                   // map
    About("關於", Icons.Filled.Info);                    // info.square

    companion object {
        /** 對應 iOS 的 ContentView / GuestContentView */
        fun available(isGuest: Boolean): List<MainTab> =
            if (isGuest) listOf(Calendar, Map, About)
            else listOf(Home, Course, Grades, Map, About)
    }
}