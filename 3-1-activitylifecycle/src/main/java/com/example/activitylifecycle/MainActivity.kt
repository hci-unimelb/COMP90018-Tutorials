package com.example.activitylifecycle

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.activitylifecycle.databinding.ActivityMainBinding

/**
 * UNDERSTANDING THE ACTIVITY LIFECYCLE:
 * Unlike desktop applications where execution starts at `main()` and runs sequentially,
 * Android components are driven by "Lifecycle Callbacks" triggered by the operating system.
 *
 * As the user navigates, opens, minimizes, and closes apps, an Activity moves through different
 * "states" (e.g., Created, Started, Resumed, Paused, Stopped, Destroyed).
 * The OS notifies us of these state transitions by executing specific override methods.
 *
 * This demo prints logs at each lifecycle step. To see it in action:
 *  1. Open the "Logcat" tab in Android Studio.
 *  2. Search for "MainActivity" to filter out other logs.
 *  3. Watch what logs appear as you open the app, click buttons, minimize the app, and close it!
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    /**
     * STATE 1: ONCREATE (Created)
     * Triggered when the Activity is first created in memory.
     * This happens only ONCE per screen launch.
     * Use this method to do basic startup setup: inflate layout, set click listeners, and initialize variables.
     *
     * The Activity is NOT yet visible to the user.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Launch a normal, full-screen activity
        binding.startNormalActivity.setOnClickListener {
            val intent = Intent(this@MainActivity, NormalActivity::class.java)
            startActivity(intent)
        }

        // Launch a transparent, dialog-themed activity (doesn't cover the entire screen)
        // This is useful for observing the difference between onPause (partially visible) and onStop (completely hidden)!
        binding.startDialogActivity.setOnClickListener {
            val intent = Intent(this@MainActivity, DialogActivity::class.java)
            startActivity(intent)
        }
    }

    /**
     * STATE 2: ONSTART (Started)
     * Triggered when the Activity becomes VISIBLE to the user on the screen.
     * However, the user cannot interact with it yet (it's not in the foreground).
     *
     * Runs right after onCreate() or when returning to this screen from a stopped state.
     */
    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: The app is now visible, but not yet interactive.")
    }

    /**
     * STATE 3: ONRESUME (Resumed)
     * Triggered when the Activity moves into the FOREGROUND.
     * The app is now fully active, visible, and the user can tap buttons, type text, and interact with it.
     *
     * This is the state where the app sits during active usage.
     * Put setup for interactive features here (e.g., resuming camera preview, starting animations).
     */
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: The app is now in the foreground and fully interactive!")
    }

    /**
     * STATE 4: ONPAUSE (Paused)
     * Triggered when the Activity is PARTIALLY hidden or loses focus.
     * The screen is still visible, but the user is not actively interacting with it.
     *
     * Examples of when this is triggered:
     *  - A dialog or a pop-up window (like our DialogActivity) covers part of the screen.
     *  - The device enters split-screen multitasking.
     *
     * Crucial: Keep operations inside onPause() fast! If it takes too long, it will delay the next screen from opening.
     */
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: The app has lost focus (e.g., covered by a dialog or popup). It is still partially visible.")
    }

    /**
     * STATE 5: ONSTOP (Stopped)
     * Triggered when the Activity becomes COMPLETELY HIDDEN from the user.
     *
     * Examples:
     *  - The user presses the Home button, minimizing the app.
     *  - A new full-screen Activity (like NormalActivity) is launched and completely covers this screen.
     *
     * This is the perfect place to release heavy resources (like pausing video playback, stopping location updates,
     * or saving draft data to the database) so they don't waste battery!
     */
    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: The app is now completely hidden from the user. Good place to save work.")
    }

    /**
     * STATE 6: ONDESTROY (Destroyed)
     * Triggered when the Activity is being completely removed from memory.
     *
     * Why does an Activity get destroyed?
     *  1. The user manually swiped the app away to close it, or called finish().
     *  2. The system is low on memory and kills the background activity to free up RAM.
     *  3. **Configuration Change**: By default, when you rotate the screen (portrait to landscape),
     *     Android completely destroys the current Activity instance and creates a brand-new one in the new orientation!
     */
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: The app is being removed from memory. Cleaning up final resources.")
    }

    /**
     * EXTRA STATE: ONRESTART (Restarting)
     * Triggered when an Activity in the STOPPED state is being started again by the user.
     *
     * For example, if you press the Home button (stops the app) and then reopen the app from the launcher,
     * the sequence will be: onRestart() -> onStart() -> onResume().
     */
    override fun onRestart() {
        super.onRestart()
        Log.d(TAG, "onRestart: The app was stopped and is now being restarted by the user.")
    }

    companion object {
        const val TAG = "MainActivity"
    }
}
