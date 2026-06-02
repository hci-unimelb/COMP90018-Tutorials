package com.example.services

import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.IBinder
import android.util.Log
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

/**
 * MYSERVICE CLASS:
 * This class inherits from Android's base `Service` class.
 * It runs in the background of our application.
 */
class MyService : Service() {

    private lateinit var notificationManager: NotificationManager

    // Create an instance of our binder (used when activities bind to this service)
    private val mBinder = DownloadBinder()

    /**
     * THE BINDER CLASS (DownloadBinder):
     * A "Binder" is the interface that allows communication between our Activity and the Service.
     * Think of it as a remote control. When the Activity binds to the Service, the system passes this
     * binder object to the Activity, which can then press "buttons" (call methods like startDownload)
     * on the service!
     */
    inner class DownloadBinder : Binder() {
        fun startDownload() {
            Log.d("MyService", "startDownload executed on the background service")
        }

        fun getProgress(): Int {
            Log.d("MyService", "getProgress executed on the background service")
            return 0
        }
    }

    /**
     * ONBIND METHOD:
     * This is the mandatory callback for bound services.
     * When an Activity calls `bindService()`, the system invokes this method and returns our
     * custom `mBinder` object.
     *
     * If you are writing a started-only service, you would return null here.
     */
    override fun onBind(intent: Intent): IBinder = mBinder

    /**
     * ONCREATE METHOD:
     * Called when the Service is being created for the first time.
     * This is only executed ONCE, before the service starts running.
     *
     * It is used for one-time initialization, such as setting up database connections or registering EventBus.
     */
    override fun onCreate() {
        super.onCreate()
        Log.d("MyService", "onCreate executed")

        // Register EventBus so this service can listen to message events
        EventBus.getDefault().register(this)
        
        // Broadcast a welcome message to any active subscribers (like our MainActivity)
        EventBus.getDefault().post(MessageEvent(MessageEvent.SERVICE, "Hello from Service using EventBus"))

        /**
         * NOTIFICATIONS FROM INSIDE A SERVICE:
         * To inform the user that a background service is running, we show a notification.
         * For "Foreground Services" (services that the user is actively aware of, like a download or map navigation),
         * showing a persistent notification is a strict Android requirement!
         * If a service runs in the foreground without a notification, Android will kill it.
         */
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Build a notification and hook it to the channel 'MainActivity.id'
        val notification = Notification.Builder(applicationContext, MainActivity.id)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
            .setChannelId(MainActivity.id)
            .setContentTitle("MyService")
            .setWhen(System.currentTimeMillis())
            .setAutoCancel(true)
            .setContentText("Notification from inside MyService")
            .setNumber(5)
            .build()

        // Display the notification immediately to the user
        notificationManager.notify(1, notification)
    }

    /**
     * ONSTARTCOMMAND METHOD:
     * Triggered every time an Activity calls `startService(intent)`.
     *
     * Unlike onCreate(), this can be called multiple times if started multiple times.
     * The return integer tells the Android OS how to handle this service if the OS runs out
     * of memory and has to kill the service in the background:
     *  - `START_STICKY`: Tells the system to recreate the service when memory is freed up (highly persistent).
     *  - `START_NOT_STICKY`: Tells the system NOT to recreate the service unless there are pending launch intents.
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("MyService", "onStartCommand executed")
        return super.onStartCommand(intent, flags, startId)
    }

    /**
     * ONDESTROY METHOD:
     * Triggered when the service is stopping and being removed from memory.
     * Always unregister listeners, EventBus, and stop active threads here to prevent memory leaks!
     */
    override fun onDestroy() {
        EventBus.getDefault().unregister(this) // Unregister EventBus to avoid Out-Of-Memory leaks
        super.onDestroy()
        Log.d("MyService", "onDestroy executed")
    }

    // Process event messages sent from the MainActivity via EventBus
    @Subscribe
    fun onMessageEventService(event: MessageEvent) {
        if (event.type == MessageEvent.ACTIVITY)
            Log.d("MyService", "Activity Message Content: " + event.message)
    }
}
