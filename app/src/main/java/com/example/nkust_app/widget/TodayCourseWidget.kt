package com.example.nkust_app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.LocalSize
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import com.example.nkust_app.MainActivity
import com.example.nkust_app.data.course.Course
import com.example.nkust_app.data.course.CourseParser
import java.util.Calendar

private val SMALL = DpSize(110.dp, 110.dp)
private val MEDIUM = DpSize(180.dp, 110.dp)

// 淺色 / 深色（對應 iOS 的 .teal、.secondary、.fill.tertiary）
private val Teal = ColorProvider(day = Color(0xFF30B0C7), night = Color(0xFF40C8E0))
private val Primary = ColorProvider(day = Color.Black, night = Color.White)
private val Secondary = ColorProvider(day = Color(0xFF8A8A8E), night = Color(0xFF98989F))
private val Background = ColorProvider(day = Color.White, night = Color(0xFF1C1C1E))
private val Divider = ColorProvider(day = Color(0xFFE5E5EA), night = Color(0xFF38383A))
private val White = ColorProvider(day = Color.White, night = Color.White)

class TodayCourseWidget : GlanceAppWidget() {

    override val sizeMode = SizeMode.Responsive(setOf(SMALL, MEDIUM))

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val courses = WidgetCourseStorage(context).load()
        provideContent { WidgetContent(courses) }
    }
}

class TodayCourseWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget = TodayCourseWidget()
}

@Composable
private fun WidgetContent(courses: List<Course>) {
    val isMedium = LocalSize.current.width >= MEDIUM.width
    val today = todayIndex()
    val todayCourses = courses
        .filter { it.weekday == today }
        .sortedBy { CourseParser.periods.indexOf(it.period) }
    // small 最多 4 堂（同 iOS），medium 最多 6 堂
    val limit = if (isMedium) 6 else 4

    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(Background)
            .cornerRadius(16.dp)
            .padding(10.dp)
            .clickable(actionStartActivity<MainActivity>())
    ) {
        // 標題
        Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text("今日課表", style = TextStyle(fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Teal))
            Spacer(GlanceModifier.defaultWeight())
            Text(dateLabel(), style = TextStyle(fontSize = 10.sp, color = Secondary))
        }
        Spacer(GlanceModifier.height(4.dp))
        Box(GlanceModifier.fillMaxWidth().height(1.dp).background(Divider)) {}
        Spacer(GlanceModifier.height(4.dp))

        if (todayCourses.isEmpty()) {
            Box(GlanceModifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("今天沒有課程了喔～🎉", style = TextStyle(fontSize = 12.sp, color = Secondary))
            }
        } else {
            todayCourses.take(limit).forEach { course ->
                Row(
                    GlanceModifier.fillMaxWidth().padding(vertical = 2.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 節次方塊
                    Box(
                        GlanceModifier.size(18.dp).background(Teal).cornerRadius(4.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(course.period, style = TextStyle(fontSize = 9.sp, fontWeight = FontWeight.Bold, color = White))
                    }
                    Spacer(GlanceModifier.width(6.dp))
                    Text(
                        course.name,
                        maxLines = 1,
                        style = TextStyle(
                            fontSize = if (isMedium) 12.sp else 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Primary
                        ),
                        modifier = GlanceModifier.defaultWeight()
                    )
                    // medium 才顯示教室
                    if (isMedium) {
                        Spacer(GlanceModifier.width(6.dp))
                        Text(course.room, maxLines = 1, style = TextStyle(fontSize = 10.sp, color = Secondary))
                    }
                }
            }
            if (todayCourses.size > limit) {
                Text(
                    "還有 ${todayCourses.size - limit} 堂...",
                    style = TextStyle(fontSize = 9.sp, color = Secondary)
                )
            }
        }
    }
}

/** 週一 = 0 … 週日 = 6 */
private fun todayIndex(): Int {
    val day = Calendar.getInstance().get(Calendar.DAY_OF_WEEK)
    return if (day == Calendar.SUNDAY) 6 else day - 2
}

/** 例如「10月8日 週四」 */
private fun dateLabel(): String {
    val now = Calendar.getInstance()
    val weekday = listOf("日", "一", "二", "三", "四", "五", "六")[now.get(Calendar.DAY_OF_WEEK) - 1]
    return "${now.get(Calendar.MONTH) + 1}月${now.get(Calendar.DAY_OF_MONTH)}日 週$weekday"
}