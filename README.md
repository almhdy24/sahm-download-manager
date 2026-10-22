# Sahm Download Manager (سهم)

[![Android CI & Release Build](https://github.com/elmahdi-dev/sahm-download-manager/actions/workflows/android-build.yml/badge.svg)](https://github.com/elmahdi-dev/sahm-download-manager/actions)
[![Platform](https://img.shields.io/badge/Platform-Android%208.0%2B%20(API%2026%2B)-166534?logo=android&logoColor=white)](https://android.com)
[![License](https://img.shields.io/badge/License-Apache%202.0-111827.svg)](LICENSE)
[![No Ads / No Trackers](https://img.shields.io/badge/Privacy-100%25%20Ad--Free%20%7C%20Zero%20Tracking-22C55E.svg)](#privacy--security-constitution)

An independent, ad-free native Android download manager engineered by **Elmahdi Dev**. Inspired by the dependability of traditional desktop utilities (IDM, Free Download Manager) and modern Android engineering (Jetpack Compose, Room, OkHttp, Kotlin Coroutines).

---

## Brand Identity & Symbol

```
        ▲
       / \
      /   \
     /  ▲  \
    /  / \  \
   /  /   \  \
  ▼  /     \  ▼
    ▼       ▼
```

- **English Brand**: Sahm Download Manager
- **Arabic Brand**: سهم — مدير التنزيلات
- **Developer**: Elmahdi Dev
- **Identity Concept**: The Arabic word **سهم** (arrow) represents direct, precise, unhindered velocity. The geometric arrow silhouette incorporates a calibrated negative-space channel, optimized to remain instantly recognizable across Android Adaptive Icon masks (circle, squircle, teardrop) and in single-color Android 13+ Material You themed icons.

---

## Key Features

1. **Native HTTP Streaming Engine**:
   - Supports multithreaded buffered streaming with real-time throughput calculations.
   - Dynamic `Range: bytes=X-` resumption for broken connections.
   - Graceful fallback for single-stream direct servers that omit range headers.

2. **Pre-Download Inspection**:
   - Lightweight `Range: bytes=0-0` / `HEAD` probing detects file name, content type, exact size, and server resumption capability before transferring bytes.
   - Allows renaming target files and previewing local storage destination paths prior to download.

3. **Concurrency & Prioritized Queue Management**:
   - Configurable concurrent transfer slots (1 to 5, default 3).
   - Priority queue manipulation: **Move to Front**, **Move Up**, and **Move Down**.
   - Global **Pause All** and **Resume All** capabilities.

4. **Batch Download Parser**:
   - Multiline URL parser validating `http://` and `https://` schemas.
   - Real-time diagnostic breakdown separating valid links, intra-list/active duplicates, and invalid entries.
   - Enqueue all validated tasks in a single operation.

5. **Network Awareness & Speed Limiting**:
   - Bandwidth limiter options: Unlimited, 256 KB/s, 512 KB/s, 1 MB/s, 2 MB/s.
   - Exponential backoff retry policy for transient server errors (HTTP 429, 5xx, socket timeouts) with immediate rejection of permanent errors (401, 403, 404, 410).
   - **Wi-Fi Only Mode** automatically pauses transfers when moving to cellular data.

6. **Bilingual Arabic (RTL) & English (LTR)**:
   - Full native right-to-left layout alignment for Arabic.
   - Clean, technical terminology without machine translation artifacts.

7. **Zero-Tracking Privacy Constitution**:
   - No advertising SDKs.
   - No telemetry, analytics, or background tracking services.
   - No cloud account requirements; all metadata is persisted locally on-device in Room SQLite.

---

## Screenshots

> *Note: Placeholders for Google Play store listing captures.*

| Downloads & Active Transfers | Pre-Download Probe | Batch URL Parser | Queue Priority Manager |
| :---: | :---: | :---: | :---: |
| *[Screenshot: Flat, compact task list]* | *[Screenshot: Header probe & rename]* | *[Screenshot: Multiline URL breakdown]* | *[Screenshot: Move to Front / Up / Down]* |

| Download Details Sheet | File Browser | Settings & Speed Limiter | Arabic RTL Layout (سهم) |
| :---: | :---: | :---: | :---: |
| *[Screenshot: Diagnostics & speed graph]* | *[Screenshot: Local categories]* | *[Screenshot: Bandwidth & Wi-Fi only]* | *[Screenshot: Native Arabic RTL interface]* |

---

## Technology Stack

- **Language**: Kotlin 2.0.21
- **UI Toolkit**: Jetpack Compose with Material 3
- **Architecture**: Clean Architecture / MVVM with Kotlin Flow & StateFlow
- **Database**: Android Jetpack Room 2.6.1 with KSP code generation
- **Networking**: OkHttp 4.12.0 with custom streaming throttler
- **Background Execution**: Android Foreground Service with `dataSync` type and Android 13+ Notification Permissions
- **System Integration**: Storage Access Framework, AndroidX FileProvider, Android SplashScreen API (`androidx.core:core-splashscreen:1.0.1`)

---

## Project Specification

| Attribute | Value | Note |
| :--- | :--- | :--- |
| **Application ID** | `com.sahm.downloadmanager` | Production Package Name |
| **Namespace** | `com.sahm.downloadmanager` | Android Gradle Plugin Namespace |
| **Compile SDK** | `35` | Android 15 (Vanilla Ice Cream) |
| **Target SDK** | `35` | Compliant with Google Play requirements |
| **Min SDK** | `26` | Android 8.0 Oreo (Covers 95%+ of active devices) |
| **Version Code** | `1` | Increment sequentially for releases |
| **Version Name** | `1.0.0` | Semantic Versioning |

---

## Building Locally

### Prerequisites

- **Java Development Kit**: JDK 17 (Eclipse Temurin or OpenJDK 17 recommended)
- **Android SDK**: Build-Tools `35.0.0`, SDK Platform `35`
- **Gradle**: 8.9 (included via Gradle Wrapper `gradlew`)

### Build Steps

1. **Clone the repository**:
   ```bash
   git clone https://github.com/elmahdi-dev/sahm-download-manager.git
   cd sahm-download-manager/android
   ```

2. **Run automated unit tests**:
   ```bash
   ./gradlew testDebugUnitTest
   ```

3. **Build Debug APK**:
   ```bash
   ./gradlew assembleDebug
   ```
   Output artifact will be generated at:
   `android/app/build/outputs/apk/debug/app-debug.apk`

---

## Release Signing Configuration

For official production builds, signing credentials must be configured securely.

### Step 1: Generate Release Keystore (If you don't already have one)

```bash
keytool -genkey -v -keystore sahm-release.keystore \
  -alias sahm_release_key \
  -keyalg RSA -keysize 2048 -validity 10000
```

> **CRITICAL SECURITY NOTE**: Never commit `sahm-release.keystore` or password files to version control. Both are excluded by `.gitignore`.

### Step 2: Configure Local Properties

Copy the template from `android/keystore.properties.example`:
```bash
cp keystore.properties.example keystore.properties
```

Edit `keystore.properties`:
```properties
SAHM_KEYSTORE_PATH=sahm-release.keystore
SAHM_KEYSTORE_PASSWORD=your_keystore_password
SAHM_KEY_ALIAS=sahm_release_key
SAHM_KEY_PASSWORD=your_key_password
```

Alternatively, you can export these as environment variables in your local shell or CI:
```bash
export SAHM_KEYSTORE_PATH="/path/to/sahm-release.keystore"
export SAHM_KEYSTORE_PASSWORD="your_keystore_password"
export SAHM_KEY_ALIAS="sahm_release_key"
export SAHM_KEY_PASSWORD="your_key_password"
```

### Step 3: Build Production Android App Bundle (AAB)

Google Play Console requires an **Android App Bundle (.aab)** for publication:
```bash
./gradlew bundleRelease
```
The signed AAB file is generated at:
`android/app/build/outputs/bundle/release/app-release.aab`

### Step 4: Build Signed Release APK (For Direct Distribution)

```bash
./gradlew assembleRelease
```
The signed APK file is generated at:
`android/app/build/outputs/apk/release/app-release.apk`

---

## GitHub Actions CI/CD Pipeline

The repository includes an automated workflow at `.github/workflows/android-build.yml`:

- **Debug Builds**: Runs on pushes and pull requests, running unit tests and outputting `sahm-download-manager-debug.apk`.
- **Production Builds**: Triggered on version tags (`v1.0.0`) or manual dispatch.
- **Repository Secrets Required for Release Signing**:
  - `SAHM_KEYSTORE_BASE64`: Base64-encoded keystore (`base64 -w 0 sahm-release.keystore`)
  - `SAHM_KEYSTORE_PASSWORD`: Keystore password
  - `SAHM_KEY_ALIAS`: Key alias
  - `SAHM_KEY_PASSWORD`: Key private password

Artifacts are automatically uploaded to the GitHub Actions Run summary.

---

## Privacy & Security Summary

- **No Third-Party Trackers**: Verified zero inclusion of Firebase Analytics, Adjust, AppsFlyer, Facebook SDK, or AdMob.
- **Safe File Sharing**: Uses `androidx.core.content.FileProvider` (`content://` URIs). `file://` URIs are never exposed.
- **Strict Network Permissions**: Uses standard `INTERNET` and `ACCESS_NETWORK_STATE` with cleartext traffic disabled by default (`android:usesCleartextTraffic="false"`).

---

## Known Limitations

- **Content Delivery Networks with Transient Auth**: Some proprietary hosting services (e.g., Google Drive large file direct links, Mega, Rapidgator) require browser-driven cookie solving / captcha challenges before direct HTTP streaming is permitted.
- **Android Scoped Storage Restrictions**: Under Android 11+ storage policies, downloads are stored in standard public downloads directory (`/Download/Sahm`).

---

## Developer Attribution

- **Developer**: Elmahdi Dev
- **Contact**: almhdybdallh24@gmail.com
- **Product**: Sahm Download Manager (سهم)
