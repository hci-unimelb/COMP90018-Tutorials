# COMP90018 — Mobile Computing Systems Programming

Tutorial source code for **Mobile Computing Systems Programming** (2024) at the University of Melbourne.
Use this repo as a hands-on companion to your weekly tutorials — clone it once, pull each week, and follow along.

---

## Quick Start

### 1. Prerequisites

| Tool | Purpose | Download |
|------|---------|----------|
| Android Studio (Hedgehog or later) | IDE + emulator | [developer.android.com/studio](https://developer.android.com/studio) |
| Git | Clone & sync this repo | [git-scm.com](https://git-scm.com) |
| JDK 17+ | Required by Gradle | bundled with Android Studio |

> All projects target **Android 13 / API 33** and are written in **Kotlin**.

### 2. Clone the repo

```bash
git clone https://github.com/kevicebryan/COMP90018-Tutorials.git
cd COMP90018-Tutorials
```

### 3. Stay up to date each week

```bash
git pull origin master
```

Open the week's module folder in Android Studio (`File → Open` → select the module folder), let Gradle sync, then run on the emulator.

---

## Tutorial Modules

| Week | Module | Topics |
|------|--------|--------|
| 1 | [Introduction](./1-introduction/) | Setup: Android Studio, Git, first project |
| 2 | [First Demo](./2-firstdemo/) | Project structure, Activities, layouts, Kotlin basics |
| 3a | [Activity Lifecycle](./3-1-activitylifecycle/) | `onCreate` → `onDestroy`, state management |
| 3b | [Layout Demo](./3-2-layoutdemo/) | XML layouts, ConstraintLayout, View binding |
| 3c | [Watch](./3-3-watch/) | Wear OS / watch face basics |
| 4a | [Multithreading](./4-1-multithreads/) | Coroutines, background work, `Handler` |
| 4b | [Services](./4-2-services/) | Foreground & background services, `BroadcastReceiver` |
| 5a | [Sensor — Barometer](./5-1-sensor-barometer/) | `SensorManager`, pressure sensor |
| 5b | [Sensor — GPS](./5-2-sensor-gps/) | Location API, permissions, Maps |
| 6a | [Storage — SharedPreferences](./6-1-storage-sharedpreferences/) | Key-value persistence |
| 6b | [Storage — Database](./6-2-storage-database/) | Room, SQLite, DAOs |
| 6c | [Storage — ContentProvider](./6-4-storage-contentprovider/) | Sharing data between apps |
| 6d | [Storage — Internal Storage](./6-5-storage-internalstorage/) | Reading & writing files |
| 7 | [Connectivity — Firebase](./7-2-connectivity-firebase/) | Realtime Database, Authentication |

---

## Opening a Module

1. Launch Android Studio.
2. `File → Open` → navigate to the module folder (e.g., `2-firstdemo`).
3. Wait for Gradle to sync (bottom status bar).
4. Click **Run** (▶) or `Shift+F10` to launch on the emulator.

> Each module is a standalone Android project — open the module folder itself, not the root of this repo.

---

## Troubleshooting

**Gradle sync fails** — check that your Android Studio and SDK are up to date (`SDK Manager → SDK Tools`).

**Emulator won't start** — ensure hardware acceleration is enabled in your BIOS / Hyper-V settings.

**`google-services.json` missing** (Firebase module) — follow the setup instructions in [`7-2-connectivity-firebase/`](./7-2-connectivity-firebase/) and add your own Firebase config file.

---

## License

[MIT](./LICENSE) — free to use for study and personal projects.
