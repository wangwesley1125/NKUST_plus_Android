package com.example.nkust_app.ui.home

import android.app.Application
import android.util.Log
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nkust_app.data.SessionStorage
import com.example.nkust_app.data.graduation.GraduationService
import com.example.nkust_app.data.network.NkustClient
import com.example.nkust_app.data.network.SessionExpiredException
import com.example.nkust_app.ui.components.BackTopBar
import com.example.nkust_app.ui.components.HtmlWebView
import com.example.nkust_app.ui.theme.IosOrange
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch

class CoursePlanViewModel(application: Application) : AndroidViewModel(application) {

    private val service = GraduationService(NkustClient(application))

    var html by mutableStateOf<String?>(null)
        private set
    var error by mutableStateOf<String?>(null)
        private set
    var sessionExpired by mutableStateOf(false)
        private set

    init {
        viewModelScope.launch {
            try {
                html = wrap(service.fetchCourseStructure())
            } catch (e: CancellationException) {
                throw e
            } catch (e: SessionExpiredException) {
                sessionExpired = true
            } catch (e: Exception) {
                Log.w("NKUST", "課程規劃載入失敗", e)
                error = "請稍後再試"
            }
        }
    }

    /** 分頁內容只是一段 HTML 片段，包上完整的頁面與樣式（同 iOS） */
    private fun wrap(fragment: String) = """
        <!DOCTYPE html>
        <html lang="zh-tw">
        <head>
          <meta charset="utf-8">
          <meta name="viewport" content="width=1100">
          <link rel="stylesheet" href="/student/_content/Nkust.Web.Core/libs/bootstrap/dist/css/bootstrap.min.css">
          <style>
            :root { color-scheme: light; }
            body { margin: 0; padding: 16px 16px 260px; background: #ffffff; font-family: sans-serif; }
            table { border-collapse: collapse; width: 100%; }
            td, th { border: 1px solid #333; padding: 6px; vertical-align: middle; text-align: center; }
            .btn, button { display: none !important; }
            html, body, div, section, article, fieldset, .card, .card-body,
            .k-content, .k-tabstrip-content, .table-responsive {
              height: auto !important; max-height: none !important; overflow: visible !important;
            }
          </style>
        </head>
        <body>
        $fragment
        </body>
        </html>
    """.trimIndent()
}

@Composable
fun CoursePlanScreen(
    onBack: () -> Unit,
    onSessionExpired: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CoursePlanViewModel = viewModel()
) {
    BackHandler(onBack = onBack)
    LaunchedEffect(viewModel.sessionExpired) {
        if (viewModel.sessionExpired) onSessionExpired()
    }

    Scaffold(modifier = modifier, topBar = { BackTopBar("課程規劃", onBack) }) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            val html = viewModel.html
            val error = viewModel.error
            when {
                html != null -> HtmlWebView(
                    html = html,
                    baseUrl = SessionStorage.BASE_URL + "/student/Graduation/StudyStatusPreview/",
                    modifier = Modifier.fillMaxSize()
                )

                error != null -> Column(
                    Modifier.fillMaxSize().padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
                ) {
                    Icon(Icons.Outlined.Warning, contentDescription = null, tint = IosOrange, modifier = Modifier.size(44.dp))
                    Text("無法載入課程規劃", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(error, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
                }

                else -> Column(
                    Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically)
                ) {
                    CircularProgressIndicator()
                    Text("載入中...", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}