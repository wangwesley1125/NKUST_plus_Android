package com.example.nkust_app.ui.login

import android.webkit.JavascriptInterface
import android.webkit.WebView
import com.example.nkust_app.data.Credentials
import org.json.JSONObject

private const val ALLOWED_HOST = "stdsys.nkust.edu.tw"

/** 找帳號、密碼欄位：依序嘗試 id、name，再 fallback 到 type（同 iOS 版） */
private const val FIND_FIELDS_JS = """
    var uField = document.querySelector('#UserName')
        || document.querySelector('input[name="UserName"]')
        || document.querySelector('input[type="text"]');
    var pField = document.querySelector('#Password')
        || document.querySelector('input[name="Password"]')
        || document.querySelector('input[type="password"]');
"""

/** 注入登入頁：使用者送出表單時，把帳密交給 NkustBridge */
const val CAPTURE_CREDENTIALS_JS = """
(function() {
    if (window.__nkustHooked) return;
    window.__nkustHooked = true;

    function capture() {
        $FIND_FIELDS_JS
        if (uField && pField && uField.value && pField.value) {
            NkustBridge.onCredentials(uField.value, pField.value);
        }
    }
    // 表單送出、按下登入按鈕、在欄位按 Enter，三種情況都抓
    document.addEventListener('submit', capture, true);
    document.addEventListener('click', function(e) {
        if (e.target.closest('button, input[type="submit"]')) capture();
    }, true);
    document.addEventListener('keydown', function(e) {
        if (e.key === 'Enter') capture();
    }, true);
})();
"""

/** JS → Kotlin 的橋接。注意：這個方法是在背景執行緒被呼叫的 */
class CredentialBridge {
    @Volatile var currentHost: String? = null
    @Volatile var captured: Credentials? = null
        private set

    @JavascriptInterface
    fun onCredentials(username: String, password: String) {
        // 只接受學校網站傳來的資料
        if (currentHost != ALLOWED_HOST) return
        captured = Credentials(username, password)
    }

    fun reset() {
        captured = null
    }
}

/** 把帳密填進登入表單（對應 iOS 的 injectCredentials） */
fun WebView.fillCredentials(credentials: Credentials) {
    // JSONObject.quote 會正確跳脫引號、反斜線、換行，比手動 replace 安全
    val u = JSONObject.quote(credentials.username)
    val p = JSONObject.quote(credentials.password)
    val js = """
        (function() {
            $FIND_FIELDS_JS
            function setValue(field, value) {
                if (!field) return;
                field.value = value;
                field.dispatchEvent(new Event('input',  { bubbles: true }));
                field.dispatchEvent(new Event('change', { bubbles: true }));
            }
            setValue(uField, $u);
            setValue(pField, $p);
        })();
    """.trimIndent()
    evaluateJavascript(js, null)
}