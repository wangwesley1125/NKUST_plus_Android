<p align="center">
  <img src="https://raw.githubusercontent.com/wangwesley1125/NKUST_plus/main/Doc/NKUST_plus_light.svg" width="140" />
</p>

<h1 align="center">高科 Plus for Android</h1>

<p align="center">
  <a href="https://play.google.com/store/apps/details?id=com.wangwesley.nkust_app">
    <img src="https://play.google.com/intl/en_us/badges/static/images/badges/zh-tw_badge_web_generic.png" width="180">
  </a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/NKUST-Unofficial%20App-orange" />
  <img src="https://img.shields.io/badge/Android-9%2B-3DDC84" />
  <img src="https://img.shields.io/badge/Jetpack%20Compose-Kotlin-7F52FF" />
  <a href="https://github.com/wangwesley1125/NKUST_plus">
    <img src="https://img.shields.io/badge/iOS%20版-NKUST__plus-blue" />
  </a>
</p>

一款以 **Kotlin + Jetpack Compose** 打造的 Android App，專為高科大學生設計，整合校務系統與校園資訊。
介面設計沿用 [iOS 版](https://github.com/wangwesley1125/NKUST_plus)，讓 iPhone 與 Android 的同學都能有一致的使用體驗；並在使用前以清楚的隱私告知與同意流程保障使用者權益。

> 本 App 為學生獨立開發的**非官方** App，與國立高雄科技大學無隸屬關係。

## 核心特色

- **隱私優先設計**
  - 首次使用提供完整隱私告知與使用者同意流程
  - 登入狀態僅保存必要的 Cookie，所有資料都只存在使用者自己的手機

- **自動登入體驗**
  - 使用 `WebView` 串接校務系統，登入驗證由學校系統處理
  - 自動偵測登入狀態並持久化 Cookie，App 重開後維持登入
  - 可選擇儲存帳號密碼以便下次自動填入，並以 **Android Keystore（AES-256-GCM）加密**保存於本機

- **與 iOS 版一致的介面**
  - 深色 / 淺色模式、iOS 風格配色
  - 浮動膠囊分頁列

## 功能總覽

### 1. 首頁（Dashboard）
- 畢業學分進度與未通過項目
- 正在進行的課程（含剩餘時間進度條）
- 即將開始的課程

---

### 2. 個人課表
- 列表 / 格狀兩種顯示方式，可選擇顯示空堂
- 可為每堂課加上備註
- 支援切換學期

---

### 3. 成績
- 期中成績、當學期成績
- 歷年成績

---

### 4. 個人資料
- 學生資訊、學分明細與修課規劃
- 圖書館借書證
- 在學證明
- 行事曆

---

### 5. 校園地圖
- 整合高科各校區建築資訊（Google Maps）
- 提供快速查找與定位

---

### 6. 桌面小工具
- 不用打開 App 就能看今日課表
- 離線或登出後仍保留最後一次的課表

---

### 7. 關於
- 關於開發者、版本資訊
- 學生專區與常用外部連結
- 隱私政策與版權聲明
- 問題回報

## 架構總覽

```
app/src/main/java/com/example/nkust_app/
├── data/          資料層：網路請求、HTML/PDF 解析、本機儲存
│   ├── network/   NkustClient（OkHttp，帶入 Cookie 與 XSRF Token）
│   ├── course/ score/ graduation/ profile/ calendar/ campus/ certificate/
│   └── ConsentStorage / SessionStorage / CredentialStorage / SettingsStorage
├── ui/            Jetpack Compose 畫面與 ViewModel
│   ├── consent/   隱私告知與同意
│   ├── login/     WebView 登入、自動填入
│   ├── main/      主畫面與浮動分頁列
│   └── home/ course/ score/ profile/ calendar/ campus/ about/ components/ theme/
└── widget/        Jetpack Glance 今日課表小工具
```

- **Login Flow**
  - `PrivacyConsentScreen`：顯示隱私聲明，使用者需同意後才能使用
  - `LoginScreen` / `NkustWebView`：以 `WebView` 載入校務系統並監聽導覽事件，登入成功後保存 Cookie
- **Storage**
  - `SessionStorage`：登入狀態與訪客模式
  - `CredentialStorage`：使用者選擇儲存的帳號密碼（Android Keystore 加密）
  - `ConsentStorage`：是否已同意隱私聲明

## 主要技術

| 類別 | 使用 |
|---|---|
| UI | Jetpack Compose、Material 3 |
| 網路 | OkHttp、Jsoup、org.json |
| PDF | PdfBox-Android、PdfRenderer |
| 地圖 | Google Maps Compose、Play Services Location |
| 小工具 | Jetpack Glance |
| 其他 | ZXing（條碼）、Custom Tabs、Play In-App Updates |

## 隱私與資料處理
- 登入流程在 App 內建的 `WebView` 中完成，驗證由學校系統處理
- 登入狀態所需的 Cookie 僅儲存在本機
- 帳號密碼**只有在使用者選擇儲存時**才會保存，並以 Android Keystore 加密，不會上傳到任何伺服器；可隨時在「關於」頁清除
- 使用者可透過登出或清除 App 資料來移除所有資料

## 自行建置

1. 使用 Android Studio 開啟專案
2. 在專案根目錄建立 `local.properties`，加入你自己的 Google Maps API 金鑰：
   ```properties
   MAPS_API_KEY=你的金鑰
   ```
   （金鑰需在 Google Cloud 啟用 **Maps SDK for Android**）
3. Sync Gradle 後即可執行

> `local.properties` 與簽章金鑰已列入 `.gitignore`，請勿提交到版本控制。

## 系統需求
- Android 9（API 28）以上

## 相關專案
- 📱 iOS 版：[wangwesley1125/NKUST_plus](https://github.com/wangwesley1125/NKUST_plus)

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
