# Running a Last Asylum Plague Bot on Android

## Important context
The [automationmacro.com blog](https://automationmacro.com/blog/last-asylum-plague-automation-bot) describes **Macro Automation Studio (MAS)** — a **Windows / Apple Silicon Mac desktop app** that automates an Android emulator. It is *not* an Android app, and it cannot run directly on your phone.

To run a bot **on the Android device itself**, you need on-device screen capture + OCR + touch injection.

## Your realistic options

| Tool | Works on-device? | OCR-based decisions? | Continuous loop? | Effort |
|------|------------------|----------------------|------------------|--------|
| **Tanker** (`tanker.app`) | ✅ Yes | ✅ On-device OCR + find-text-tap | ✅ Bot mode loops steps | Low |
| **Tasker + AutoInput** | ✅ Yes | ✅ OCR + UI elements | ✅ Full scripting | Medium |
| **MacroDroid** | ✅ Yes | ⚠️ Limited — screenshot + OCR exists but conditional logic is basic | ⚠️ Can loop, but fragile | Low |
| **Our custom Accessibility app** | ✅ Yes | ✅ ML Kit OCR + pixel analysis | ✅ Foreground service | Higher (code) |

### Recommended
- **Simplest on-device bot → Tanker.** It was built for exactly this: capture screen → OCR → tap text/coordinates → loop.
- **Most reliable / no vendor lock-in → our custom Android app** (AccessibilityService + MediaProjection + ML Kit OCR + click injection as a foreground service).

## Tanker — quick start
1. Install Tanker from the Play Store.
2. Enable its Accessibility + Overlay permissions.
3. Open Last Asylum, record/define steps:
   - Capture screen
   - "Find text" condition (e.g., "Scavenge", "Start", "Heal")
   - Tap action at the matched region
   - Wait N seconds
   - Loop
4. Start the bot from a floating button.

### Limitations
- OCR fails on stylized/low-contrast game fonts → test on your device.
- Battery optimization may kill the bot → whitelist Tanker in battery settings.
- Game may detect automation → use humanized pacing.

## MacroDroid — what works and what doesn't
- ✅ Triggers: time, app open, WiFi, notification.
- ✅ Actions: launch app, click at X/Y, type, wait, take screenshot.
- ⚠️ OCR-based branching is limited — decisions like "if 'DNA' < 100 then tap Upgrade" are unreliable on game UIs.
- Good for: simple "open game → tap start button every N minutes" scripts, not state-driven bots.

## Tasker + AutoInput — most powerful
- AutoInput gives OCR and UI-element tapping.
- Can build full state machines with variables.
- Steep learning curve and paid plugin.

## Our custom Android app (best for this game)
The project in this repo is building exactly this:
- **`AutomationService`** — foreground service holding MediaProjection + AccessibilityService
- **`ScreenAnalyzer`** — ML Kit Text Recognition (OCR) on captured frames
- **`GameBot`** module — scrape state, decide action, inject click
- Runs continuously without a desktop PC.

## Safety note
- No on-device bot is invisible to anti-cheat. Use spaced pacing, avoid impossible speed.
- Don't log into your main account until you've tested for a week.
- MACRODROID / TANKER / CUSTOM APP: all carry the same account-risk profile.
