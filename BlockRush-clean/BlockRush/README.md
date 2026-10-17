# BLOCK RUSH

A Java and Kotlin 3D endless-runner foundation. Java owns the LibGDX gameplay and rendering; Kotlin supplies the Android entry point. The same Java game also has a desktop launcher for quick playtesting. All art in this first slice is generated from simple 3D primitives; no paid assets are needed.

## Toolchain

- Android Studio Quail 4 (2026.1.4) or newer, Android SDK 36
- JDK 17
- Android Gradle Plugin 8.13.2 with Gradle 8.13
- Kotlin Android plugin 2.2.20
- LibGDX 1.14.2

LibGDX's setup guide lists 1.14.2 as its latest stable release and recommends JDK 17 or 21 for development. The Android launcher is Kotlin, while the engine module stays Java for straightforward interop.

## Open and run in Android Studio

1. Extract the ZIP, then open the extracted **BlockRush** folder in Android Studio. Select the top-level folder that contains `settings.gradle`; do not open the `android` subfolder by itself.
2. At the Android Studio welcome screen, choose **More Actions > SDK Manager**. Install **Android SDK Platform 36**, **Android SDK Build-Tools 35.0.0**, **Android SDK Platform-Tools**, and (if using an emulator) **Android Emulator**.
3. Open **Settings/Preferences > Build, Execution, Deployment > Build Tools > Gradle**. Set **Gradle JDK** to JDK 17. This project deliberately compiles Java and Kotlin to JVM 17.
4. This archive does not bundle a Gradle wrapper. Download and extract the Gradle 8.13 binary distribution, then set **Gradle distribution** to **Local Gradle distribution** and select the extracted `gradle-8.13` folder. The project uses Android Gradle Plugin 8.13.2, which requires Gradle 8.13.
5. Click **Sync Project with Gradle Files**. The first sync needs an internet connection so Android Studio can download LibGDX and Android build dependencies.
6. Create an emulator from **Tools > Device Manager > Create Device**, choosing an API 36 system image, or connect a physical Android device with Developer Options and USB debugging enabled.
7. In the toolbar, select the **android** run configuration and your device, then click **Run**. If no configuration appears, use **Run > Edit Configurations > + > Android App** and choose the `android` module.

To create a debug APK, choose **Build > Build Bundle(s) / APK(s) > Build APK(s)**. Android Studio writes it under `android/build/outputs/apk/debug/`.

### Quick fixes

- **SDK location not found:** open **File > Project Structure > SDK Location** and select the Android SDK installed by Android Studio.
- **Gradle version error:** return to the Gradle settings and choose the extracted Gradle 8.13 folder.
- **JDK error:** set the Gradle JDK to a JDK 17 installation, then sync again.
- **Dependency download error:** verify the network connection and retry Gradle sync; this game downloads LibGDX from Maven Central on the first build.

For desktop play, run `com.blockrush.desktop.DesktopLauncher` from the IDE. Desktop is useful for quick testing of the shared Java gameplay code.

## Controls

- Android: swipe left/right to change lanes, up to jump, down to slide; tap to begin, resume, or restart.
- Desktop: A/Left and D/Right to switch lanes, Space/Up to jump, S/Down to slide, Escape/P to pause.

## Included in this first playable slice

- Three-lane forward-running loop and follow camera
- Smooth lane switching, one jump at a time, timed slide
- Procedurally repeated track tiles and obstacle/coin patterns with one lane kept open per pattern
- Coin pickup, distance score, local personal best
- Ready, paused, and game-over states with tap/key restart
- Keyboard and touch input
- Kotlin Android launcher plus Java desktop launcher

## Structure

```text
BlockRush/
  android/     Kotlin Android launcher and manifest
  core/        Java game, procedural primitive scene, input and gameplay
  lwjgl3/      Java desktop launcher
  assets/      User-provided art/audio can be added here later
```

## Current limits / next phases

This is the requested playable foundation, not the complete feature list in the pasted brief. It does not yet include a shop, permanent coin wallet, power-ups, character selection, missions, audio, asset pipeline, advanced collapsing bridges, or mobile release signing. The scene uses generated block geometry and simple text HUD; replace or extend these pieces as the game grows.

## Sources

- [LibGDX project generation and stable version guidance](https://libgdx.com/wiki/start/project-generation)
- [LibGDX development environment setup](https://libgdx.com/wiki/start/setup)
- [Android Gradle Plugin and Gradle compatibility](https://developer.android.com/build/releases/about-agp)
