package com.example.services

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.services.databinding.ActivityMainBinding
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * UNDERSTANDING ANDROID SERVICES
 *
 * What is a Service?
 * A Service is an application component that performs long-running tasks in the background without
 * providing a user interface (UI).
 *
 * There are two main patterns to use a Service:
 *  1. **Started Service (via startService)**:
 *     - "Fire and Forget". The Service runs indefinitely in the background, even if the calling
 *       Activity is completely destroyed.
 *     - It must be explicitly stopped via `stopService()` or `stopSelf()`.
 *
 *  2. **Bound Service (via bindService)**:
 *     - "Client-Server" relationship. The Service acts as a server, and the Activity acts as a client.
 *     - The Activity can directly call methods on the Service and get responses (using a `Binder`).
 *     - The Service stays alive only as long as there is an Activity bound to it. When all clients unbind,
 *       the Service is automatically destroyed by the system.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var notificationManager: NotificationManager
    private lateinit var rpl: ActivityResultLauncher<Array<String>>
    private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.POST_NOTIFICATIONS)

    private var downloadBinder: MyService.DownloadBinder? = null
    
    // Tracks whether our Activity is currently bound to the Service
    private var bound = false

    /**
     * ServiceConnection:
     * An interface that acts as the communication link.
     * When we request a service binding, Android binds them in the background and calls this
     * interface's methods once the connection is established or lost.
     */
    private val connection: ServiceConnection = object : ServiceConnection {

        // Triggered if the service connection is lost (e.g. service crashes or is killed by the OS)
        override fun onServiceDisconnected(name: ComponentName) {
            bound = false
        }

        // Triggered when we successfully connect to the Service
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
            // 1. Cast the generic IBinder parameter to our custom DownloadBinder subclass
            downloadBinder = service as MyService.DownloadBinder
            
            // 2. We can now directly invoke public methods on the Service!
            downloadBinder?.startDownload()
            downloadBinder?.getProgress()
            
            // 3. Set our connection state tracker to true
            bound = true
        }
    }

    /**
     * Notification Channels (Android O+ / API 26+ requirement):
     * Starting in Android Oreo, all notifications must be assigned to a specific "Channel".
     * This allows users to customize notification settings (e.g. block "Promotional" notifications
     * while allowing "Critical" service notifications).
     */
    private fun createNotificationChannel() {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val name: CharSequence = getString(R.string.channel_name)
        val description = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_HIGH

        val channel = NotificationChannel(id, name, importance)
        channel.description = description
        channel.enableLights(true)
        channel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
        channel.lightColor = Color.BLUE
        channel.enableVibration(true)
        channel.setShowBadge(true)
        channel.vibrationPattern = longArrayOf(100, 100, 100)

        // Register the channel with the system
        notificationManager.createNotificationChannel(channel)
    }

    private fun allPermissionsGranted(): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Android 13+ (API 33+) requires explicit permission from the user to display notifications!
        rpl = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { isGranted ->
            var granted = true
            for ((key, value) in isGranted) {
                Log.d("MainActivity", "$key is $value")
                if (!value) granted = false
            }
            if (granted) Log.d("MainActivity", "Permissions granted for api 33+")
        }

        createNotificationChannel()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!allPermissionsGranted()) {
                rpl.launch(REQUIRED_PERMISSIONS)
            }
        }

        // 1. START SERVICE BUTTON (Started Service model)
        binding.startService.setOnClickListener {
            val startIntent = Intent(this@MainActivity, MyService::class.java)
            startService(startIntent) // Tells the system to boot up the Service
            Toast.makeText(binding.root.context, "Service starting", Toast.LENGTH_SHORT).show()
        }

        // 2. STOP SERVICE BUTTON (Stops the Started Service)
        binding.stopService.setOnClickListener {
            val stopIntent = Intent(this@MainActivity, MyService::class.java)
            stopService(stopIntent) // Tells the system to terminate the Service
            makeNotification("Stopped Service", 2)
            Toast.makeText(binding.root.context, "Service stopping", Toast.LENGTH_SHORT).show()
        }

        // 3. BIND SERVICE BUTTON (Bound Service model)
        binding.bindService.setOnClickListener {
            val bindIntent = Intent(this@MainActivity, MyService::class.java)
            // 'BIND_AUTO_CREATE' tells Android: "If the service is not currently running, create it automatically."
            bindService(bindIntent, connection, BIND_AUTO_CREATE)
            Toast.makeText(binding.root.context, "Binding Service", Toast.LENGTH_SHORT).show()
        }

        // 4. UNBIND SERVICE BUTTON
        binding.unbindService.setOnClickListener {
            // Safety check: only unbind if a binding is currently active!
            if (bound) {
                unbindService(connection) // Safely unbind from the service
                bound = false
                Toast.makeText(binding.root.context, "Unbinding Service", Toast.LENGTH_SHORT).show()
            }
        }

        EventBus.getDefault().register(this)
    }

    /**
     * Reusable method to post a system notification from the main screen
     */
    fun makeNotification(message: String, msgCount: Int) {
        val notification = Notification.Builder(applicationContext, id)
            .setSmallIcon(R.mipmap.ic_launcher)     
            .setContentTitle("Service")             
            .setContentText(message)                
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.mipmap.ic_launcher))
            .setWhen(System.currentTimeMillis())    
            .setChannelId(id)                       
            .setAutoCancel(true)                    
            .build()

        notificationManager.notify(msgCount, notification)
    }

    /**
     * [onStop] is called when our Activity is no longer visible to the user.
     *
     * To prevent resource and memory leaks, we must unregister EventBus and unbind from our service.
     * We perform a safety check: `if (bound)`. We only call `unbindService()` if we have successfully
     * bound to the service, preventing unregister errors.
     */
    override fun onStop() {
        EventBus.getDefault().unregister(this)
        super.onStop()
        if (bound) {
            unbindService(connection)
            bound = false
        }
    }

    // Listens for communication messages sent from MyService via EventBus
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEventActivity(event: MessageEvent) {
        if (event.type == MessageEvent.SERVICE) {
            Log.d("MyService", "Service Message Content: " + event.message)
            // Reply back to the Service using EventBus
            EventBus.getDefault().post(MessageEvent(MessageEvent.ACTIVITY, "Hello from Activity!"))
        }
    }

    companion object {
        const val id = "channel_01"
    }
}
