package com.example.nkust_app.ui.profile

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.graphics.Bitmap
import android.view.WindowManager
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nkust_app.ui.components.BackTopBar
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.common.BitMatrix
import com.google.zxing.oned.Code128Writer
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel

@Composable
fun LibraryCardScreen(
    studentId: String,
    name: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)
    KeepScreenBright()

    val qrCode = remember(studentId) { generateQrCode(studentId) }
    val barcode = remember(studentId) { generateBarcode(studentId) }

    Scaffold(
        modifier = modifier,
        topBar = {
            BackTopBar("圖書館借閱證", onBack) {
                TextButton(onClick = onBack) { Text("完成", fontWeight = FontWeight.Bold) }
            }
        }
    ) { padding ->
        Column(
            Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp, Alignment.CenterVertically)
        ) {
            // QR Code
            CodeBlock("QR Code") {
                qrCode?.let {
                    CodeImage(it, "QR Code", Modifier.size(220.dp))
                }
            }

            // Barcode
            CodeBlock("Barcode") {
                barcode?.let {
                    CodeImage(it, "Barcode", Modifier.width(300.dp).height(110.dp))
                }
                Text(
                    studentId,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = 2.sp
                )
            }

            Text(name, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun CodeBlock(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(title, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        content()
    }
}

/** 一律白底，深色模式下掃描器才讀得到 */
@Composable
private fun CodeImage(bitmap: ImageBitmap, description: String, modifier: Modifier) {
    Image(
        bitmap = bitmap,
        contentDescription = description,
        contentScale = ContentScale.FillBounds,
        filterQuality = FilterQuality.None,   // 對應 iOS 的 .interpolation(.none)，邊緣保持銳利
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .padding(8.dp)
    )
}

// ═══════ 亮度（對應 UIScreen.main.brightness） ═══════

@Composable
private fun KeepScreenBright() {
    val activity = LocalContext.current.findActivity()
    DisposableEffect(activity) {
        val window = activity?.window
        val original = window?.attributes?.screenBrightness
        if (window != null) {
            val params = window.attributes
            params.screenBrightness = 1f
            window.attributes = params
        }
        onDispose {
            if (window != null) {
                val params = window.attributes
                params.screenBrightness = original ?: WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
                window.attributes = params
            }
        }
    }
}

private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

// ═══════ 產生 QR Code / 條碼 ═══════

private fun generateQrCode(text: String): ImageBitmap? = try {
    val hints = mapOf(
        EncodeHintType.ERROR_CORRECTION to ErrorCorrectionLevel.M,   // 同 iOS 的 "M"（15% 容錯）
        EncodeHintType.MARGIN to 1
    )
    QRCodeWriter().encode(text, BarcodeFormat.QR_CODE, 512, 512, hints).toImageBitmap()
} catch (e: Exception) {
    null
}

private fun generateBarcode(text: String): ImageBitmap? = try {
    val hints = mapOf(EncodeHintType.MARGIN to 10)
    Code128Writer().encode(text, BarcodeFormat.CODE_128, 900, 300, hints).toImageBitmap()
} catch (e: Exception) {
    null
}

private fun BitMatrix.toImageBitmap(): ImageBitmap {
    val pixels = IntArray(width * height) { i ->
        if (get(i % width, i / width)) android.graphics.Color.BLACK else android.graphics.Color.WHITE
    }
    return Bitmap.createBitmap(pixels, width, height, Bitmap.Config.ARGB_8888).asImageBitmap()
}