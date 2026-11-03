package com.example.nkust_app.ui.main

import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.nkust_app.ui.about.AboutScreen
import com.example.nkust_app.ui.course.CourseScreen
import com.example.nkust_app.ui.score.ScoreScreen
import com.example.nkust_app.ui.home.HomeScreen
import com.example.nkust_app.ui.calendar.CalendarScreen
import com.example.nkust_app.ui.campus.CampusMapScreen

/** 分頁列實際佔用的高度（含系統導覽列），由 MainScreen 量出來後提供給各頁 */
val LocalTabBarHeight = compositionLocalOf { 0.dp }

/**
 * 捲動內容底部要留給浮動分頁列的空間。
 * 依實際量到的高度計算：三鍵導覽、手勢導覽、字體放大都不會被擋到。
 */
val TabBarBottomSpace: Dp
    @Composable get() {
        val bar = LocalTabBarHeight.current
        return if (bar > 0.dp) bar + 12.dp
        else 90.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
    }

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
    var tabBarHeight by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    // 不在第一個分頁時，返回鍵先回第一個分頁
    BackHandler(enabled = selectedTab != tabs.first()) {
        selectedTab = tabs.first()
    }

    Box(
        Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // 內容鋪滿整個畫面，延伸到分頁列下面
        val tabModifier = Modifier.fillMaxSize()

        CompositionLocalProvider(LocalTabBarHeight provides tabBarHeight) {
            // 系統導覽列的高度已算進 TabBarBottomSpace，所以這裡把它「用掉」，
            // 避免各頁的 Scaffold 底部再多留一次空白
            Box(
                Modifier
                    .fillMaxSize()
                    .consumeWindowInsets(WindowInsets.navigationBars)
            ) {
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
            }
        }

        // 分頁列浮在內容上面
        FloatingTabBar(
            tabs = tabs,
            selected = selectedTab,
            onSelect = { selectedTab = it },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .onSizeChanged { tabBarHeight = with(density) { it.height.toDp() } }
        )
    }
}

/** IG 風格的浮動分頁列：只有圖示，選中的那格有圓角底色 */
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
            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(6.dp)
            ) {
                tabs.forEach { tab ->
                    TabItem(
                        tab = tab,
                        isSelected = tab == selected,
                        onClick = { onSelect(tab) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun TabItem(
    tab: MainTab,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val background by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent,
        label = "tabBackground"
    )
    val tint by animateColorAsState(
        if (isSelected) MaterialTheme.colorScheme.onSurface
        else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "tabTint"
    )

    Box(
        modifier
            .height(48.dp)
            .clip(RoundedCornerShape(percent = 50))
            .background(background)
            .clickable(role = Role.Tab, onClick = onClick)
            .semantics {
                contentDescription = tab.title
                selected = isSelected
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            if (isSelected) tab.icon else tab.outlinedIcon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(28.dp)
        )
    }
}