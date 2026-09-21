# Lost Asylum — On-Device Automation Bot

A custom Android app that automates **Last Asylum** plague runs directly on your phone — no PC, no emulator. It uses the phone's own camera and screen capture, OCR to read the game UI, and AccessibilityService to inject taps — all running as a foreground service you control from your home screen.

## What it does

- Scans the game screen with **ML Kit Text Recognition (OCR)** — reads DNA count, timers, buttons
- Decides what to tap next based on the current game state
- Injects touches via **AccessibilityService** (no root)
- Captures screen with **MediaProjection** (no root)
- Runs 24/7 as a **foreground service** with a persistent notification
- Survives screen-off and app-switching
- Tracks run stats (success rate, actions/min, errors) — survives app restarts via SharedPreferences
- Built for AGP 8.11.0 / Kotlin 1.9.22 / compileSdk 36 — uses the offline Android SDK on PocketDev

## Features by module

| Module | What it automates |
|--------|-------------------|
| **PlagueModule** | Full plague cycle — Aqueous I → Inoculation → Cure Manufacture → Upgrade DNA → Repeat |
| **AutoScavengeModule** | Auto-loot scavenge runs |
| **ResourceFarmModule** | Farm gold/XP efficiently |
| **CureHealingModule** | Craft cures & heal survivors |
| **ReconModule** | Run recon missions & collect intel |
| **RaidsRalliesModule** | Raids + rally allies |
| **DailyQuestsModule** | Complete daily quests |

## Build info

```
AGP        : 8.11.0
Kotlin     : 1.9.22
compileSdk : 36
targetSdk  : 36
minSdk     : 26 (Android 8.0)
Java       : 17
Gradle     : 8.14.3 (bundled wrapper)
```

Build the release APK:

```bash
./gradlew assembleRelease
```

APK output: `app/build/outputs/apk/release/app-release-unsigned.apk`

> This is an **unsigned** release build (no signing key configured). For personal sideloading it works as-is — you'll just need to allow "Install from unknown sources" on Android 8+. For Play Store distribution you must sign it with your own key.

## Install & setup (step-by-step)

1. **Install the APK**
   ```bash
   adb install app/build/outputs/apk/release/app-release-unsigned.apk
   ```
   Or transfer it to your phone and install manually (allow "Install unknown apps" for your file manager).

2. **Enable Accessibility Service**
   - Settings → Accessibility → Lost Asylum Automation → toggle ON
   - Grant the permission when prompted

3. **Grant Screen Capture (MediaProjection)**
   - Open the app → tap **Start Automation**
   - A system dialog will ask for screen-recording permission — allow it
   - A persistent notification appears while the bot is running

4. **Open Last Asylum** and start a plague run

5. **Tap the floating bot button** to start / stop automation

6. **Configure pacing** (Settings → Pacing) — higher = slower / safer, lower = faster

## Architecture

```
com.lastasylum.automation/
├── LostAsylumAccessibilityService.kt   ← Touch injection + OCR entry point
├── ScreenAnalyzer.kt                   ← ML Kit OCR on captured frames
├── AutomationService.kt                ← Foreground service + MediaProjection
├── Module.kt                           ← Base interface for all modules
├── ModuleRepository.kt                 ← Holds & switches active module
├── TouchAction.kt                      ← Click, long-press, swipe, multi-finger
├── GameElement.kt                      ← Scanned UI element (text + bounding box)
├── analytics/
│   └── StatsTracker.kt                 ← Runs, actions, success rate, APM
├── state/
│   └── StateManager.kt                 ← Persists run state across restarts
├── utils/
│   └── ScreenshotLogger.kt             ← Debug screenshot dumps
├── ui/
│   ├── MainActivity.kt                 ← Dashboard + start/stop
│   └── SettingsActivity.kt             ← Pacing, module selection, OCR threshold
└── automation/
    ├── modules/
    │   ├── PlagueModule.kt             ← Plague cycle automation
    │   ├── AutoScavengeModule.kt
    │   ├── ResourceFarmModule.kt
    │   ├── CureHealingModule.kt
    │   ├── ReconModule.kt
    │   ├── RaidsRalliesModule.kt
    │   └── DailyQuestsModule.kt
    └── automation/
        ├── AutomationReceiver.kt       ← Boot-completed / scheduled triggers
        └── NotificationChannel.kt
```

## How the plague bot works (high level)

1. Capture screen frame via MediaProjection
2. OCR the frame with ML Kit Text Recognition
3. Parse text into GameElement list (text + position)
4. LostAsylumAccessibilityService matches elements against GameElement rules
5. Decides next tap based on current plague step (Aqueous → Inoculation → Cure → Upgrade → Repeat)
6. Injects touch via AccessibilityService
7. Paces between actions (configurable delay)
8. Logs every step to StatsTracker + ScreenshotLogger

## OCR text it looks for

The ScreenAnalyzer parses these patterns from the game UI:
- DNA: (\d+) — current DNA level
- Scavenge, Inoculate, Cure, Upgrade — action buttons
- Plague / Stage — current plague phase
- Timer / countdown text

## Safety & anti-cheat notes

- The bot paces taps — it doesn't spam — but no on-device bot is invisible to anti-cheat.
- **Use a burner account** for testing; don't risk your main account until you've run it for a week and confirmed it behaves like a human.
- Set pacing >= 800 ms for plague steps to avoid detection.
- The bot stops automatically if the screen shows an error / popup it can't parse.

## Dependencies

| Library | Purpose |
|---------|---------|
| ML Kit Text Recognition | OCR on captured frames |
| AndroidX Core / AppCompat / Material | UI & permissions |
| Kotlin Coroutines | Async task scheduling |
| Lifecycle Service | Foreground service lifecycle |
| CameraX | Screen capture pipeline |
| WorkManager | Scheduled / boot-started tasks |
| DataStore | User settings persistence |
| TensorFlow Lite | Optional on-device image classification |

## License

This project is for personal / educational use. Automating online games may violate their Terms of Service — use at your own risk. The authors are not responsible for account bans or other consequences.

## Credits

- Inspired by the [automationmacro.com Last Asylum plague bot](https://automationmacro.com/blog/last-asylum-plague-automation-bot) (desktop MAS version)
- Built as a pure on-device Android alternative using AccessibilityService + OCR + MediaProjection
