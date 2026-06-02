@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package com.example.multithreads

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.Looper
import android.os.Message
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.example.multithreads.databinding.ActivityMainBinding
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask
import java.util.concurrent.ScheduledThreadPoolExecutor

/**
 * UNDERSTANDING MULTITHREADING IN ANDROID
 *
 * Imagine your phone's processor as a busy restaurant:
 * - **The Main Thread (UI Thread)**: This is the head waiter. The waiter's sole job is to welcome
 *   customers, take orders, and update the menu (draw the UI, handle button clicks, run smooth animations).
 *   If the head waiter leaves the dining room to cook a 10-minute steak in the kitchen, the entire
 *   dining room freezes! In Android, if you block the Main Thread for more than 5 seconds, the OS
 *   displays an "Application Not Responding" (ANR) crash pop-up.
 *
 * - **Background Threads (Worker Threads)**: These are the kitchen chefs. They handle heavy, time-consuming
 *   work in the background (downloading files from the internet, reading from a database, calculating math).
 *
 * - **The Golden Rule of Android Development**:
 *   1. **NEVER** do long-running tasks (network, database, file read/write, sleep) on the Main Thread.
 *   2. **NEVER** update UI elements (like setting text or colors) from a Background Thread. Only the
 *      Main Thread is allowed to modify the UI.
 *
 * This demo explores different ways to execute background tasks and safely ship the results back to the Main Thread.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private lateinit var text: TextView
    private lateinit var buttonPressed: TextView
    private var future: FutureTask<String>? = null
    private var exampleHandlerThread: HandlerThread? = null
    private var broadcastReceiver: BroadcastReceiver? = null

    private var actionedTime: Long = 0
    private var elapsedTime: Long = 0

    private var localBroadcastManager: LocalBroadcastManager? = null

    /**
     * THE HANDLER:
     * A "Handler" is a communication channel. It acts like a dumbwaiter (a small food elevator)
     * that carries messages or orders from the background kitchen (Background Thread) back up to the
     * dining room waiter (Main Thread/UI Thread).
     *
     * We initialize this Handler with `Looper.getMainLooper()`. This tells it:
     * "Deliver all received messages directly onto the Main Thread so it can safely update the UI."
     */
    @SuppressLint("DefaultLocale")
    private val handler = Handler(Looper.getMainLooper()) { msg ->
        // This block runs on the MAIN THREAD when a background thread sends a message
        when (msg.what) {
            UPDATE_TEXT_RUNNABLE -> {
                elapsedTime = (System.currentTimeMillis() - actionedTime) / 1000
                text.text = String.format("Message Received from Runnable Interface after %d seconds", elapsedTime)
            }
            UPDATE_TEXT_THREAD -> {
                elapsedTime = (System.currentTimeMillis() - actionedTime) / 1000
                text.text = String.format("Message Received from Thread Class after %d seconds", elapsedTime)
            }
            UPDATE_TEXT_FUTURETASK -> {
                // msg.obj contains the custom data object shipped from the background
                text.text = String.format("%s Message received after %d seconds", msg.obj, elapsedTime)
            }
            UPDATE_TEXT_HANDLER_RECEIVE -> {
                text.text = String.format("Message Received from HandlerThread Class after %d seconds", elapsedTime)
                // Shut down the background HandlerThread to save system resources
                exampleHandlerThread?.quit()
            }
        }
        true // Message processed successfully
    }

    private fun updateAfterClick(v: String) {
        // Reset labels on button clicks
        buttonPressed.text = resources.getString(R.string.button_press) + v
        actionedTime = System.currentTimeMillis()
        text.text = ""
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        text = binding.text
        buttonPressed = binding.buttonPress

        /**
         * 1. THE CRASH BUTTON (Deliberate Bug Demonstration)
         *
         * What happens here?
         * We start a background thread via `Thread { ... }.start()`. Inside it, we attempt to modify
         * the UI directly: `text.text = ...`.
         *
         * Result:
         * The app will CRASH instantly! Look at the console logs and you will see:
         * `android.view.ViewRootImpl$CalledFromWrongThreadException: Only the original thread that created a view hierarchy can touch its views.`
         * This illustrates that Android strictly prevents background threads from modifying the UI!
         */
        binding.crashButton.setOnClickListener {
            updateAfterClick(" A")
            Thread {
                Log.d("Future Tasks", "Thread(Done method): " + Thread.currentThread().id)
                Log.d("Future Tasks", "TextView Object Reference(Done method): $text")
                
                // CRASH TRIGGER: Attempting to modify UI text directly from a background thread!
                elapsedTime = (System.currentTimeMillis() - actionedTime) / 1000
                text.text = "Crash Happens after $elapsedTime seconds"
            }.start()
        }

        /**
         * 2. THE RUNNABLE INTERFACE (Safe way 1)
         *
         * Here, we start a new background thread. Inside it, we sleep for 2 seconds (simulating heavy math or download).
         * To safely update the UI, we construct a `Message` object and send it to our UI `handler`
         * using `handler.sendMessage(message)`. The handler receives this and runs it on the Main Thread!
         */
        binding.changeTextRunnable.setOnClickListener {
            updateAfterClick(" B")
            Thread {
                try {
                    Thread.sleep(2000) // Sleep background thread (Safe!)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
                
                // Pack up a message indicating task completion
                val message = Message()
                message.what = UPDATE_TEXT_RUNNABLE
                
                // Ship it to the Main Thread handler
                handler.sendMessage(message)
            }.start()
        }

        /**
         * 3. SUBCLASSING THREAD (Safe way 2)
         *
         * Instead of passing a block of code, we create an anonymous subclass of `Thread` and override
         * the `run()` method. The execution flow and Handler message-passing is exactly the same as above.
         */
        binding.changeTextThreads.setOnClickListener {
            updateAfterClick(" C")
            object : Thread() {
                override fun run() {
                    try {
                        sleep(2000)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                    val message = Message()
                    message.what = UPDATE_TEXT_THREAD
                    handler.sendMessage(message)
                }
            }.start()
        }

        /**
         * 4. FUTURETASK & EXECUTORS (Advanced Multi-Threading)
         *
         * Unlike a standard Thread which is a "fire-and-forget" model, a `FutureTask` wraps a `Callable`.
         * A `Callable` is like a Runnable, but it can actually RETURN a value or throw exceptions when done!
         *
         * We execute this task using an Executor (a manager that controls a pool of reusable threads).
         */
        binding.changeTextFutureTask.setOnClickListener {
            updateAfterClick(" D")
            val executor = ScheduledThreadPoolExecutor(2) // A pool of 2 reusable background threads
            
            // Create a FutureTask that executes a background job returning a String
            future = object : FutureTask<String>(Callable {
                Thread.sleep(2000)
                "This message was created in the FutureTask thread!" // Returns this string
            }) {
                // The 'done()' method automatically runs when the background execution completes
                override fun done() {
                    var textContent: String? = null
                    try {
                        textContent = get() // Retrieves the returned string from the Callable
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    } catch (e: ExecutionException) {
                        e.printStackTrace()
                    }
                    
                    // Ship the returned string back to the UI thread using our Handler
                    val message = Message()
                    message.what = UPDATE_TEXT_FUTURETASK
                    message.obj = textContent
                    handler.sendMessage(message)
                }
            }
            executor.execute(future)
        }

        /**
         * 5. ASYNCTASK (DEPRECATED - HISTORICAL IMPORTANCE ONLY)
         *
         * AsyncTask was once Android's most popular tool. It split tasks into three stages:
         *  - `doInBackground`: Runs on a background thread (heavy calculations).
         *  - `onProgressUpdate`: Runs on the main thread (allows publishing percentage progress).
         *  - `onPostExecute`: Runs on the main thread (allows updating final layout).
         *
         * Why was it deprecated in API 30?
         * 1. **Memory Leaks**: It keeps a hidden reference to the MainActivity. If the user rotates
         *    the screen, the Activity is destroyed, but the background AsyncTask keeps running in memory.
         *    This prevents the garbage collector from reclaiming the old Activity, leading to massive memory leaks!
         * 2. **Fragile Lifecycles**: If the Activity is destroyed, onPostExecute will crash when modifying non-existent views.
         *
         * *Modern replacement*: Kotlin Coroutines (recommended for production).
         */
        binding.changeTextAsynctask.setOnClickListener {
            updateAfterClick(" E")
            object : AsyncTask<Int, Int, String>() {
                
                // Background execution (Cannot touch UI)
                override fun doInBackground(vararg integers: Int?): String {
                    val count = integers[0] ?: 0
                    for (i in 0 until count) {
                        try {
                            Thread.sleep(200)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                        publishProgress(i) // Triggers onProgressUpdate on the UI thread
                    }
                    elapsedTime = (System.currentTimeMillis() - actionedTime) / 1000
                    return "Message Received from AsyncTask after $elapsedTime seconds"
                }

                // UI Thread updates (Safe!)
                override fun onProgressUpdate(vararg progress: Int?) {
                    text.text = "Progress: ${(progress[0] ?: 0) * 10}%"
                    super.onProgressUpdate(*progress)
                }

                // UI Thread final results (Safe!)
                override fun onPostExecute(result: String) {
                    text.text = result
                }
            }.execute(10) // Input parameter passed to doInBackground
        }

        /**
         * 6. HANDLERTHREAD (Thread with its own Looper)
         *
         * A normal background thread terminates once its run() block finishes.
         * A `HandlerThread` is a persistent background thread that stays alive and has its own
         * Message Queue ("Looper"). You can continuously send tasks to it using a Handler!
         */
        binding.changeTextHandlerThread.setOnClickListener {
            updateAfterClick(" F")
            val handlerThread = HandlerThread("example")
            exampleHandlerThread = handlerThread
            handlerThread.start() // Start the background thread

            val msg = Message()
            msg.what = UPDATE_TEXT_HANDLER_SEND

            // Create a Handler bound to the background thread's Looper
            Handler(handlerThread.looper) { message ->
                // This block runs entirely on the BACKGROUND thread
                when (message.what) {
                    UPDATE_TEXT_HANDLER_SEND -> {
                        try {
                            Thread.sleep(2000)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }
                        // Send a reply back to the main UI thread Handler
                        val reply = Message()
                        reply.what = UPDATE_TEXT_HANDLER_RECEIVE
                        handler.sendMessage(reply)
                    }
                }
                false
            }.sendMessage(msg)
        }

        /**
         * 7. INTENTSERVICE & LOCALBROADCAST (Service-Based Background Tasks)
         *
         * An `IntentService` runs background tasks inside an Android Service.
         * To return results to the Activity, it broadcasts an "Intent" (message) locally.
         * Our Activity registers a `BroadcastReceiver` to listen for this broadcast and update the UI.
         */
        binding.changeTextIntentService.setOnClickListener {
            updateAfterClick(" G")
            val intentFilter = IntentFilter()
            intentFilter.addAction(ExampleIntentService.ACTION_EXAMPLE_END)
            val manager = LocalBroadcastManager.getInstance(binding.root.context)
            localBroadcastManager = manager
            
            // Define a receiver to handle the returned broadcast
            val receiver = object : BroadcastReceiver() {
                @SuppressLint("DefaultLocale")
                override fun onReceive(context: Context, intent: Intent) {
                    val message = intent.getStringExtra(ExampleIntentService.RESULT_PARAM)
                    elapsedTime = (intent.getLongExtra(ExampleIntentService.RESULT_DURATION, System.currentTimeMillis()) - actionedTime) / 1000
                    text.text = String.format("%s Received in %d seconds.", message, elapsedTime)
                    
                    // Always unregister broadcast receivers when done to prevent memory leaks!
                    broadcastReceiver?.let { manager.unregisterReceiver(it) }
                }
            }
            broadcastReceiver = receiver
            manager.registerReceiver(receiver, intentFilter)
            
            // Start the IntentService
            ExampleIntentService.startExample(binding.root.context)
        }

        /**
         * 8. EVENTBUS (Publish-Subscribe Architecture)
         *
         * EventBus is an external library that allows decoupled components to communicate easily.
         * Instead of setting up messy callbacks, intents, or handlers:
         *  - One component simply posts an event: `EventBus.getDefault().post(myEvent)`
         *  - Any registered subscriber automatically receives it if they have the `@Subscribe` annotation.
         */
        binding.changeTextEventBus.setOnClickListener {
            updateAfterClick(" H")
            object : Thread() {
                override fun run() {
                    try {
                        sleep(2000)
                    } catch (e: InterruptedException) {
                        e.printStackTrace()
                    }
                    // Publish the event object to all active subscribers
                    EventBus.getDefault().post(MessageEvent(
                        "Message Received from Thread through EventBus after $elapsedTime seconds"))
                }
            }.start()
        }

        // Print thread logs in the console
        Log.d("Future Tasks", "Thread Number: " + Thread.currentThread().id)
        Log.d("Future Tasks", "TextView Object Reference: $text")
    }

    override fun onStart() {
        super.onStart()
        // Register EventBus when the Activity becomes visible
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        // Unregister EventBus when hidden to prevent memory leaks (OOM)
        EventBus.getDefault().unregister(this)
        super.onStop()
    }

    /**
     * EVENTBUS SUBSCRIBER callback:
     * The annotation tells EventBus to run this method when a MessageEvent is posted.
     * `threadMode = ThreadMode.MAIN` guarantees that EventBus will deliver this event on the
     * main UI thread so we can safely update our text components!
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(event: MessageEvent) {
        text.text = event.message
    }

    companion object {
        const val UPDATE_TEXT_RUNNABLE = 1
        const val UPDATE_TEXT_THREAD = 2
        const val UPDATE_TEXT_FUTURETASK = 3
        const val UPDATE_TEXT_HANDLER_SEND = 4
        const val UPDATE_TEXT_HANDLER_RECEIVE = 5
    }
}
