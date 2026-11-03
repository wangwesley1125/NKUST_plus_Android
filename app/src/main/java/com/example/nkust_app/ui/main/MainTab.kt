package com.example.nkust_app.ui.main

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Map
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 分頁列只顯示圖示（IG 風格）：選中時用實心圖示，沒選中用線條圖示。
 * title 仍保留，給無障礙朗讀（TalkBack）和長按提示用。
 */
enum class MainTab(val title: String, val icon: ImageVector, val outlinedIcon: ImageVector) {
    Home("首頁", Icons.Filled.Home, Icons.Outlined.Home),
    Course("個人課表", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    Grades("歷年成績", Icons.Filled.BarChart, Icons.Outlined.BarChart),
    Calendar("行事曆", Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth),
    Map("校園地圖", Icons.Filled.Map, Icons.Outlined.Map),
    About("關於", Icons.Filled.Info, Icons.Outlined.Info);

    companion object {
        /** 對應 iOS 的 ContentView / GuestContentView */
        fun available(isGuest: Boolean): List<MainTab> =
            if (isGuest) listOf(Calendar, Map, About)
            else listOf(Home, Course, Grades, Map, About)
    }
}