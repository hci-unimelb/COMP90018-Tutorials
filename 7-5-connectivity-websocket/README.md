# 7-5 Connectivity: WebSocket Demo

A tutorial Android app demonstrating real-time, full-duplex communication over WebSockets. The app subscribes to Supabase Realtime's Postgres Changes feed for the `messages` table (the same table used in 7-3-connectivity-supabase) -- a row inserted from **anywhere** (this app's Send button, the Supabase dashboard, the SQL editor, another device) shows up live in the log.

> Earlier versions of this demo connected to a public echo server (`wss://echo.websocket.org`) instead. Supabase Realtime is itself a WebSocket connection under the hood (supabase-kt's `Realtime` plugin) -- see "Architecture" below.

---

## What it Demonstrates

- **Full-duplex, real-time communication** -- a WebSocket connection that pushes database change events to the app as they happen
- **supabase-kt's Realtime plugin** -- `postgresChangeFlow<PostgresAction.Insert>` + `channel.subscribe()`, the SDK-level equivalent of OkHttp's `WebSocketListener` callbacks
- **Clean lifecycle management** -- subscribing on user action, unsubscribing in `onDestroy`
- **Thread safety** -- Realtime callbacks arrive on a background dispatcher; `runOnUiThread {}` is used before touching Views

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

These are the codes the underlying WebSocket protocol uses; supabase-kt's `channel.unsubscribe()` handles closing cleanly for you.

---

## Thread Safety

Realtime's callbacks (delivered via `postgresChangeFlow`) run on a background dispatcher -- **not** the Android Main Thread. Updating UI from a background thread causes crashes.

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
    +-- WebSocketManager              (owns the Supabase Realtime channel)
            |
            +-- RealtimeChannel       (supabase-kt -- a WebSocket connection under the hood)
            |       postgresChangeFlow<PostgresAction.Insert> / subscribe() / unsubscribe()
            |
            +-- WsEvent (sealed class)
                    Opened / MessageReceived / Error / Closed
```

`WebSocketManager` wraps all Supabase Realtime/Postgrest details. `MainActivity` only calls `connect()`, `send()`, `disconnect()`, and `shutdown()` -- it never touches `SupabaseClient` directly. This separation is what made it a drop-in swap from the original raw-OkHttp echo-server version: same event types, same method names, different transport underneath.

---

## Supabase Setup

This module shares the same Supabase project and `messages` table as **7-3-connectivity-supabase** -- if you've already done that tutorial's setup (project, table, `local.properties`), you only need one extra step:

**Enable Realtime on the `messages` table.** Realtime is off per table by default -- a table can have data and RLS policies fine and still deliver nothing over Realtime until it's added to the `supabase_realtime` publication. In the Supabase dashboard **SQL Editor**, run:

```sql
alter publication supabase_realtime add table messages;
```

(equivalently: **Database → Publications** → `supabase_realtime` → toggle on `messages`)

If you haven't done 7-3 yet, follow that module's README first (create project, create the `messages` table, add `SUPABASE_URL`/`SUPABASE_KEY` to `local.properties`), then run the SQL above.

---

## How to Run

1. Complete the Supabase setup above (shared with 7-3; only the publication step is new).
2. Open the project in Android Studio, sync Gradle.
3. Run on a device or emulator (API 33+).
4. Tap **Connect** -- status turns green once subscribed.
5. Type a message and tap **Send** -- you'll see `SENT` then `RECEIVED` in the log (the insert echoing back over the same channel).
6. While connected, insert a row via the Supabase Table Editor or SQL Editor -- it appears as `RECEIVED` with no matching `SENT` line, since it didn't come from this app.
7. Tap **Disconnect** to unsubscribe cleanly.

---

## How to Adapt

**Listen to a different table:**
```kotlin
wsManager.connect("your_table_name")
```

**Handle UPDATE/DELETE too, not just INSERT:**
```kotlin
ch.postgresChangeFlow<PostgresAction>(schema = "public") { table = tableName }
    .onEach { action ->
        when (action) {
            is PostgresAction.Insert -> { /* ... */ }
            is PostgresAction.Update -> { /* ... */ }
            is PostgresAction.Delete -> { /* ... */ }
            else -> {}
        }
    }
```

**Reconnect on failure** -- implement retry/backoff in the `WsEvent.Error` handler instead of just showing an error message.

**Raw WebSocket instead of a managed SDK** -- see this file's git history for the original OkHttp `WebSocketListener` version against a public echo server; useful if you want to teach the wire protocol directly rather than a client SDK's abstraction over it.

---

## Key Files

| File | Purpose |
|---|---|
| `WebSocketManager.kt` | Supabase Realtime wrapper -- connect/send/disconnect/shutdown |
| `SupabaseClientProvider.kt` | Singleton Supabase client (Postgrest + Realtime installed) |
| `Message.kt` | `@Serializable` row shape for the `messages` table |
| `MainActivity.kt` | UI logic, event handling, lifecycle |
| `activity_main.xml` | Layout -- status, message log, input, buttons |
| `AndroidManifest.xml` | INTERNET permission |
