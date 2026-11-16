# HEXON embedded ARM64 engine plan

This starter project builds the Android UI and two deliberately low-impact checks:
- bounded TCP connect checks (up to 10 user-selected ports)
- one HTTPS GET to inspect common response security headers

The Nuclei, HTTPX, Subfinder, Katana and FFUF cards are placeholders. They are NOT bundled binaries and do not run in this starter build.

## Recommended production architecture

1. Build each engine for Android `arm64-v8a` (Android/Bionic target), not generic Linux `aarch64`.
2. Prefer a JNI shared-library adapter (`.so`) with a narrow interface over shipping arbitrary executable files.
3. Place signed native libraries under `app/src/main/jniLibs/arm64-v8a/`.
4. Implement one adapter per engine behind a common Kotlin/Java interface.
5. Keep scan profiles conservative: concurrency and request rate caps, timeouts, cancellation, output size limits, and explicit authorized-target confirmation.
6. Store logs in app-private storage; provide user-initiated export and delete controls.
7. Verify licenses and redistribution terms for every engine and template set before bundling them.

## Important compatibility note

Upstream Go tools commonly target Linux, not Android. A Linux ARM64 binary is not automatically an Android ARM64 binary. Nuclei and other tools may require Android-specific build patches/adapters, and their full functionality is not included by this scaffold.

## Build

Use Android Studio with JDK 17 and Android SDK Platform 35:
- Open this folder as a project.
- Allow Gradle sync.
- Build > Build Bundle(s) / APK(s) > Build APK(s).

The resulting APK should target ARM64 only and support Android 8+ (including Android 12). The project has not been built or device-tested in this environment.
