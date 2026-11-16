# HEXON Android

Dark Cyber starter app for Android 12 / ARM64.

## Included in this starter
- Dark cyber UI
- Target authorization checkbox
- Bounded TCP port connectivity check (maximum 10 ports)
- Basic HTTPS response-header snapshot
- Engine status cards for planned embedded tools

## Not included yet
Nuclei, HTTPX, Subfinder, Katana and FFUF are shown as integration placeholders. They are not bundled or executable in this first source drop. See `docs/EMBEDDED_ENGINES.md`.

## Build
Open the project in Android Studio with JDK 17 and Android SDK 35, sync Gradle, then Build APK.

Application ID: `com.hexon.security`
ABI: `arm64-v8a`
Min SDK: 26
Target SDK: 35

Only test systems you own or have explicit permission to assess.
