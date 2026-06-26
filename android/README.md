# Android Content Studio

A fully on-device, native Kotlin/Jetpack Compose Android app that ports the
backend/frontend system in `../backend` and `../frontend` to run with **no server at
all**. Everything happens locally on the phone:

- **Branding**: name, logo emoji, tagline, default CTA, hashtags, and colors are all
  configurable from an in-app settings screen (`ui/BrandSettingsSection.kt`), persisted
  via `data/BrandRepository.kt` (`brand.json` in app-internal storage). No brand is
  hardcoded — repurpose the app for any business by editing these settings.
- **Image/video rendering**: Android `Canvas`/`Bitmap` (replaces Sharp/SVG) and the
  byte-buffer `MediaCodec`/`MediaMuxer` APIs (replaces FFmpeg) for the Ken Burns-style
  reel, all in `app/src/main/java/com/thecompass/contentstudio/render/`, parameterized
  by the current `Brand`.
- **Storage**: a small JSON file in app-internal storage (replaces lowdb) — see
  `data/PostRepository.kt`.
- **Publishing**: there is no Instagram Graph API call (that requires a public media
  URL, which an offline app can't provide). Instead, "שיתוף באינסטגרם" hands the
  generated file to the real Instagram app via `Intent.ACTION_SEND`
  (`share/InstagramShare.kt`), and copies the caption to the clipboard since
  Instagram's feed composer doesn't reliably accept a pre-filled caption from share
  intents — paste it once inside Instagram.

## Building

```bash
cd android
./gradlew assembleDebug
```

or just open the `android/` folder directly in Android Studio (Iguana+) and run it —
that's the easiest path since Studio manages the SDK/build-tools versions for you.

**Note on this repo's build environment**: this project was written and reviewed for
correctness, but it could **not** be compiled or run here, because this sandbox's
network policy blocks Google's Maven repository (`dl.google.com`/`maven.google.com`
both return `403`), which is where the Android Gradle Plugin and every `androidx.*`/
Compose artifact are hosted — Maven Central alone isn't enough to resolve an Android
project. Build it on a machine/CI with normal internet access (or in Android Studio) to
get a real compile check; if anything doesn't line up, the most likely spots are
`app/build.gradle.kts` version numbers (AGP 8.5.2 / Kotlin 1.9.24 / Compose compiler
1.5.14 / Compose BOM 2024.06.00 — a known-compatible combo, but newer Studio versions
may suggest bumping them) and the `ReelEncoder` byte-buffer YUV fill, which is the one
piece of device-dependent code (encoder color-format support varies; it already falls
back across `YUV420Flexible` → `YUV420SemiPlanar` → `YUV420Planar`).

## Requirements

- minSdk 24, compileSdk/targetSdk 34
- The Instagram app installed on-device for the one-tap share step (falls back to the
  system share sheet if it isn't).

## What's different from the server-based version

- No `IG_ACCESS_TOKEN`/`IG_BUSINESS_ACCOUNT_ID`, no `PUBLIC_BASE_URL`, no ngrok — none
  of that is needed since there's no Graph API call.
- Publishing is a manual last step (tap "שיתוף באינסטגרם", then tap post/share inside
  Instagram) rather than fully automatic — the unavoidable tradeoff for running
  entirely offline.
- A post's status is `DRAFT` or `SHARED` (not `PUBLISHED`) since the app can't confirm
  Instagram-side that the post actually went live, only that the share intent was
  launched.
