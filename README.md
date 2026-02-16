# Toshiba TV WebView App

This is a simple Android application designed for Toshiba 43C350NP (and other Android/Google TVs) that displays a full-screen WebView.

## Features
- **Launch Popup**: Automatically asks for a URL when the app starts.
- **4K Support**: Optimized for high-resolution TV screens.
- **Remote Control Navigation**: Supports D-pad navigation for scrolling and clicking links.
- **Persistent URL**: Remembers the last entered URL.

## How to Build and Install
1. **Download/Clone** this project folder.
2. Open **Android Studio**.
3. Select **File > Open** and choose this folder (`ToshibaWebView`).
4. Wait for Gradle sync to complete.
5. Connect your TV via ADB or ensure developer options are enabled on the TV.
6. Click **Run** (Green Play Button) in Android Studio to install on the TV.
   - Alternatively, build the APK: **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
   - Copy the APK to a USB drive and install it on the TV using a File Manager app.

## Project Structure
- `app/src/main/java/com/example/toshibawebview/MainActivity.kt`: Main logic for the popup and WebView.
- `app/src/main/AndroidManifest.xml`: TV-specific configuration (fullscreen, no touchscreen required).
- `app/src/main/res/layout/activity_main.xml`: Layout file.

## Troubleshooting
- If the URL doesn't load, ensure the TV is connected to the internet.
- If navigation is tricky, use an air mouse or a USB mouse connected to the TV.
