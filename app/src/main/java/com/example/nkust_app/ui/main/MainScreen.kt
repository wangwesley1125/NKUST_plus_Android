package com.example.nkust_app.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nkust_app.ui.about.AboutScreen
import com.example.nkust_app.ui.course.CourseScreen
import com.example.nkust_app.ui.score.ScoreScreen
import com.example.nkust_app.ui.home.HomeScreen
import com.example.nkust_app.ui.calendar.CalendarScreen
import com.example.nkust_app.ui.campus.CampusMapScreen
import com.example.nkust_app.ui.main.TabBarBottomSpace

/** 捲動內容底部要留給浮動膠囊的空間 */
val TabBarBottomSpace = 90.dp

@Composable
fun MainScreen(
    isGuest: Boolean,
    hasSavedCredentials: Boolean,
    onLogout: () -> Unit,
    onSessionExpired: () -> Unit,
    onClearCredentials: () -> Unit
) {
    val tabs = remember(isGuest) { MainTab.available(isGuest) }
    var selectedTab by rememberSaveable(isGuest) { mutableStateOf(tabs.first()) }

    // 不在第一個分頁時，返回鍵先回第一個分頁
    BackHandler(enabled = selectedTab != tabs.first()) {
        selectedTab = tabs.first()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 內容鋪滿整個畫面，延伸到膠囊下面
        val tabModifier = Modifier.fillMaxSize()

        when (selectedTab) {
            MainTab.Home -> HomeScreen(
                onSessionExpired = onSessionExpired,
                onLogout = onLogout,
                modifier = tabModifier
            )
            MainTab.Course -> CourseScreen(
                onSessionExpired = onSessionExpired,
                modifier = tabModifier
            )
            MainTab.Grades -> ScoreScreen(
                onSessionExpired = onSessionExpired,
                modifier = tabModifier
            )
            MainTab.Calendar -> CalendarScreen(
                modifier = tabModifier,
                onLogin = onLogout   // 訪客按「登入」= 離開訪客模式回到登入頁
            )
            MainTab.Map -> CampusMapScreen(modifier = tabModifier)
            MainTab.About -> AboutScreen(
                hasSavedCredentials = hasSavedCredentials,
                onClearCredentials = onClearCredentials,
                modifier = tabModifier
            )
        }

        // 膠囊浮在內容上面
        FloatingTabBar(
            tabs = tabs,
            selected = selectedTab,
            onSelect = { selectedTab = it },
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

/** iOS 26 風格的浮動膠囊分頁列 */
@Composable
private fun FloatingTabBar(
    tabs: List<MainTab>,
    selected: MainTab,
    onSelect: (MainTab) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(percent = 50),
            color = MaterialTheme.colorScheme.surfaceContainerLow.copy(alpha = 0.95f),
            border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
            shadowElevation = 10.dp
        ) {
            Row(Modifier.fillMaxWidth().padding(5.dp)) {
                tabs.forEach { tab ->
                    val isSelected = tab == selected
                    val contentColor =
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurface

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(percent = 50))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.surfaceVariant
                                else Color.Transparent
                            )
                            .clickable { onSelect(tab) }
                            .padding(vertical = 7.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            tab.icon,
                            contentDescription = tab.title,
                            tint = contentColor,
                            modifier = Modifier.size(26.dp)
                        )
                        Text(
                            tab.title,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = contentColor,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}