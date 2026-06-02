# 7-5 Connectivity: WebSocket Demo

A tutorial Android app demonstrating real-time, full-duplex communication using WebSockets via OkHttp. The app connects to a public echo server (`wss://echo.websocket.org`) that reflects every message back to the sender.

---

## What it Demonstrates

- **Full-duplex WebSocket communication** -- send and receive messages simultaneously over a single persistent connection
- **OkHttp WebSocketListener** -- handling `onOpen`, `onMessage`, `onClosing`, `onClosed`, and `onFailure` callbacks
- **Sending text frames** -- calling `WebSocket.send(text)` to transmit data
- **Clean lifecycle management** -- connecting on user action, shutting down OkHttp's thread pool in `onDestroy`
- **Thread safety** -- OkHttp callbacks arrive on background threads; `runOnUiThread {}` is used before touching Views

---

## WebSocket vs HTTP

| Feature | HTTP | WebSocket |
|---|---|---|
| Connection | Request/response, closes after | Persistent, stays open |
| Direction | Client initiates only | Bidirectional (server can push) |
| Overhead | Headers on every request | Minimal framing after handshake |
| Use case | Fetching data on demand | Chat, live feeds, gaming, IoT |

WebSocket starts as an HTTP upgrade handshake (`Upgrade: websocket` header), then the connection is "promoted" to the WebSocket protocol. After that, either side can send frames at any time.

---

## WebSocket Close Codes (RFC 6455)

| Code | Meaning |
|---|---|
| 1000 | Normal Closure -- intentional, clean close |
| 1001 | Going Away -- endpoint going offline (e.g. server restart) |
| 1006 | Abnormal Closure -- connection lost without close frame |
| 1011 | Internal Error -- server-side error |

This app uses **1000** when the user taps Disconnect, signalling a clean, intentional close.

---

## Thread Safety

OkHttp's `WebSocketListener` callbacks (`onOpen`, `onMessage`, etc.) are invoked on OkHttp's internal thread pool -- **not** the Android Main Thread. Updating UI from a background thread causes crashes.

Solution used in this app:

```kotlin
private val wsManager = WebSocketManager { event ->
    runOnUiThread {       // <-- marshal back to Main Thread
        when (event) { ... }
    }
}
```

---

## Architecture

```
MainActivity
    |
    +-- WebSocketManager          (owns OkHttpClient + WebSocket)
            |
            +-- WebSocketListener (OkHttp callbacks)
            |       onOpen / onMessage / onClosing / onClosed / onFailure
            |
            +-- WsEvent (sealed class)
                    Opened / MessageReceived / Error / Closed
```

`WebSocketManager` wraps all OkHttp details. `MainActivity` only calls `connect()`, `send()`, `disconnect()`, and `shutdown()` -- it never touches `OkHttpClient` directly. This separation makes it easy to swap the transport layer (e.g. replace OkHttp with Ktor) without touching UI code.

---

## How to Run

No setup needed. The app uses `wss://echo.websocket.org`, a free public echo server.

1. Open the project in Android Studio
2. Run on a device or emulator (API 33+)
3. Tap **Connect** -- status turns green when the handshake succeeds
4. Type a message and tap **Send** -- you will see `SENT` then `RECEIVED` in the log
5. Tap **Disconnect** to close cleanly

---

## How to Adapt to a Real Server

**Change the server URL:**
```kotlin
private val SERVER_URL = "wss://your-server.example.com/ws"
```

**Add authentication headers** (e.g. Bearer token):
```kotlin
val request = Request.Builder()
    .url(url)
    .addHeader("Authorization", "Bearer $token")
    .build()
```

**Handle binary frames** (images, protobufs):
```kotlin
override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
    // process binary payload
}
```

**Reconnect on failure** -- implement exponential backoff in `WsEvent.Error` handler instead of just showing an error message.

---

## Relation to Supabase Realtime

Supabase Realtime (covered in Tutorial 7-3) uses WebSocket internally. When you subscribe to a Supabase channel, the Supabase client library opens a WebSocket connection to Supabase's Realtime server and listens for database change events pushed over that connection.

This tutorial shows the raw WebSocket layer that Supabase (and many other real-time services) are built on top of. Understanding `onOpen`, `onMessage`, and `onFailure` directly helps you debug connectivity issues in higher-level libraries.

---

## Key Files

| File | Purpose |
|---|---|
| `WebSocketManager.kt` | OkHttp wrapper -- connect/send/disconnect/shutdown |
| `MainActivity.kt` | UI logic, event handling, lifecycle |
| `activity_main.xml` | Layout -- status, message log, input, buttons |
| `AndroidManifest.xml` | INTERNET permission |
