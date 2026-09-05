# Tutorial 7-2: Cloud Firestore

Demonstrates the [Firebase Android SDK](https://firebase.google.com/docs/android/setup) for real-time data sync using **Cloud Firestore**:

- **Write** a message to a Firestore collection when the user taps Send
- **Listen** for changes with `addSnapshotListener` — the list of messages updates automatically whenever any client writes to the collection

---

## Before you build: required setup

This module will **not compile** until you add your own `google-services.json`. This file is intentionally excluded from version control because it contains project-specific credentials.

### Step 1 — Create a Firebase project

1. Go to [console.firebase.google.com](https://console.firebase.google.com) and sign in with a Google account.
2. Click **Add project**, give it any name (e.g. `comp90018-tutorial`), and follow the prompts. You can disable Google Analytics if you want — it is not needed for this tutorial.

### Step 2 — Register your Android app

1. On the Firebase project overview, click the **Android icon** (Add app → Android).
2. Enter the package name exactly: `com.example.connectivity_firebase`
3. The **App nickname** and **Debug signing certificate SHA-1** are optional — leave them blank.
4. Click **Register app**.

### Step 3 — Download `google-services.json`

1. Firebase will prompt you to download `google-services.json`.
2. Place the file at:
   ```
   7-2-connectivity-firebase/google-services.json
   ```
   (Same folder as `build.gradle.kts`, **not** inside `src/`.)

   > A template showing the expected structure is at `google-services.json.template` in this folder.

3. Click **Next** through the remaining Firebase setup steps — you do not need to modify `build.gradle` manually, it is already configured.

### Step 4 — Enable Firestore

1. In the Firebase console sidebar, go to **Build → Firestore Database**.
2. Click **Create database**.
3. Choose the region closest to you and click **Next**.
4. Select **Start in test mode** (allows all reads/writes for 30 days — fine for a tutorial).
5. Click **Enable**.

### Step 5 — Open the security rules

Even in test mode, rules expire after 30 days or may already be locked down. Go to **Firestore Database → Rules** and set:

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /{document=**} {
      allow read, write: if true;
    }
  }
}
```

Click **Publish**. Never use these rules in a real app.

### Step 6 — Sync and run

1. In Android Studio: **File → Sync Project with Gradle Files**.
2. Run on a device or emulator (API 33+).

---

## What the app does

Each tap of **Send Message** adds a new document to the `messages` collection — it does not overwrite previous messages. A snapshot listener on the whole collection (ordered by `timestamp`) keeps the on-screen list in sync in real time.

```
Firestore
└── messages (collection)
      ├── <auto-id>: { text: "hi", timestamp: ... }
      ├── <auto-id>: { text: "hello", timestamp: ... }
      └── <auto-id>: { text: "hey", timestamp: ... }   ← any client can add here
```

- **Send** — adds a new document with the EditText value and a server timestamp
- **Message list** — `addSnapshotListener` fires whenever any document in `messages` changes, rebuilding the displayed list (oldest to newest) in the scrollable TextView

Because `addSnapshotListener` sets up a persistent subscription, opening the app on two devices and sending from one immediately updates the other.

---

## Architecture

```
MainActivity
  └── FirebaseFirestore.getInstance()
        └── CollectionReference ("messages")
              ├── add(text, timestamp)                 ← write (new doc per message)
              └── orderBy("timestamp").addSnapshotListener  ← real-time read of the whole list
```

All Firebase I/O is asynchronous and callback-based — the SDK handles threading internally.

---

## Key files

| File | Purpose |
|---|---|
| `MainActivity.kt` | All Firestore logic — init, add message, listen to list |
| `activity_main.xml` | EditText (input) + Button (send) + scrollable TextView (message list) |
| `build.gradle.kts` | Firebase BOM + `google-services` plugin applied |
| `google-services.json` | **Your credentials** — not in repo, download from Firebase Console |
| `google-services.json.template` | Shows the expected JSON structure |

---

## Key dependencies

| Library | Purpose |
|---|---|
| `com.google.gms:google-services` (plugin) | Reads `google-services.json` and configures the SDK |
| `com.google.firebase:firebase-bom:33.3.0` | BOM that pins all Firebase library versions |
| `com.google.firebase:firebase-firestore` | Cloud Firestore client |
| `com.google.firebase:firebase-analytics` | Required by the google-services plugin |

---

## Troubleshooting

**`File google-services.json is missing`**
You have not placed the file yet. Follow Step 3 above. Make sure the file is in `7-2-connectivity-firebase/` (next to `build.gradle.kts`), not deeper inside `src/`.

**`PERMISSION_DENIED` when writing or reading**
Your Firestore rules have expired or were set to locked mode. Repeat Step 5 above.

**The message list never updates**
Make sure you are connected to the internet. Firestore requires a live connection for real-time updates on a fresh install.
