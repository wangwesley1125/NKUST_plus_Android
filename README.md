<p align="center">
  <img src="https://raw.githubusercontent.com/wangwesley1125/NKUST_plus/main/Doc/NKUST_plus_light.svg" width="140" />
</p>

<h1 align="center">高科 Plus</h1>

<p align="center">
  <a href="https://play.google.com/store/apps/details?id=com.wangwesley.nkust_app">
    <img src="https://play.google.com/intl/en_us/badges/static/images/badges/zh-tw_badge_web_generic.png" width="180">
  </a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/NKUST-Unofficial%20App-orange" />
  <img src="https://img.shields.io/badge/Android-9%2B-3DDC84" />
  <img src="https://img.shields.io/badge/Jetpack%20Compose-Based-7F52FF" />
  <a href="https://github.com/wangwesley1125/NKUST_plus">
    <img src="https://img.shields.io/badge/iOS%20版-NKUST__plus-blue" />
  </a>
</p>

一款以 **Kotlin + Jetpack Compose** 打造的 Android App，專為高科大學生設計，整合校務系統與校園資訊，提供更直覺、快速且安全的使用體驗，並在使用前以清楚的隱私告知與同意流程保障使用者權益。

> 📱 iOS 版請見 [NKUST_plus](https://github.com/wangwesley1125/NKUST_plus)

## 核心特色

- **隱私優先設計**
  - 首次使用提供完整隱私告知與使用者同意流程
  - 登入狀態僅保存必要 Cookie，所有資料只存在使用者自己的手機

- **自動登入體驗**
  - 使用 `WebView` 串接校務系統
  - 自動偵測登入狀態並持久化 Cookie
  - App 重開後維持登入狀態
  - 可選擇開啟 AutoFill，帳號密碼以 **Android Keystore** 加密儲存於本機

- **與 iOS 版一致的介面**
  - 沿用 iOS 版的配色與排版，支援深色 / 淺色模式
  - 浮動膠囊分頁列，簡潔、流暢、現代化 UI

## 功能總覽

### 1. 首頁（Dashboard）
- 畢業學分進度與未通過項目
- 正在進行的課程顯示
- 即將開始的課程顯示

---

### 2. 個人課表
- 清楚顯示每日課程，可自行選擇顯示空堂
- 支援列表與週課表（格狀）兩種顯示方式
- 快速掌握上課時間與教室

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
- 整合高科各校區資訊
- 提供快速查找與定位

---

### 6. 桌面小工具
- 不用打開 App 就能看今日課表

---

### 7. 關於
- 關於開發者
- 版本資訊
- 學生專區
- 常用的外部連結
- 隱私政策與版權聲明
- 問題回報

## 架構總覽
- Login Flow
  - `MainActivity`：進入點，決定顯示隱私告知、登入頁或主內容
  - `PrivacyConsentScreen`：顯示隱私聲明，使用者需同意後才能使用
  - `NkustWebView`：以 `WebView` 載入校務系統並監聽導覽事件
- Storage
  - `SessionStorage`：記錄登入狀態與訪客模式，Cookie 由 `CookieManager` 保存於本機
  - `ConsentStorage`：記錄是否已同意隱私聲明（`SharedPreferences`）
  - `CredentialStorage`：使用者選擇開啟 AutoFill 時，將帳密以 Android Keystore 加密儲存

## 隱私與資料處理
- 登入流程在 App 內建 `WebView` 中完成，驗證由學校系統處理
- 僅將登入狀態所需的 Cookie 儲存在本機以維持登入
- 帳號密碼**只有在使用者開啟 AutoFill 時**才會保存，並以 Android Keystore 加密，不會傳輸至任何伺服器或第三方；可隨時在 App 內刪除
- 使用者可透過登出或清除 App 資料來清除 Cookie 與狀態

## 自行建置
1. 使用 Android Studio 開啟專案
2. 在專案根目錄的 `local.properties` 加入自己的 Google Maps API 金鑰：
   ```properties
   MAPS_API_KEY=你的金鑰
   ```
3. Sync Gradle 後即可執行

## 系統需求
- Android 9（API 28）以上

## 版本紀錄

請參閱 [Releases](https://github.com/wangwesley1125/NKUST_plus_Android/releases) 頁面

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
