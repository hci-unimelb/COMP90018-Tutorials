package com.example.mvvm.view

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.mvvm.model.StepRepository
import com.example.mvvm.databinding.ActivityCounterBinding
import kotlinx.coroutines.launch

/**
 * DEMO 1 - THE WAY ALMOST EVERYONE WRITES IT FIRST
 *
 * Every piece of state lives in the Activity as a plain variable, and the
 * background work is launched from the Activity's own lifecycleScope.
 *
 * It works perfectly... until the device rotates.
 *
 * WHAT ACTUALLY HAPPENS ON ROTATION:
 * Rotating is a "configuration change". Android's answer to a configuration
 * change is brutal - it DESTROYS this Activity and builds a brand new one so
 * that it can load the landscape layout, landscape strings, landscape
 * dimensions, and so on.
 *
 *   onPause -> onStop -> onDestroy -> onCreate -> onStart -> onResume
 *
 * The new Activity object is a different object in memory. `stepCount` below
 * is a field on the OLD object, which is now garbage. The new one starts at 0.
 *
 * The same thing happens for: switching to dark mode, changing the system
 * font size, changing the device language, unfolding a foldable, plugging in
 * a keyboard, or resizing a window in split screen. Rotation is just the
 * easiest one to demo in class.
 */
class BrokenActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCounterBinding

    // PROBLEM 1: this lives on the Activity, so it dies with the Activity.
    private var stepCount = 0

    // PROBLEM 2: so does this.
    private var goalText = "Daily goal: not loaded"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCounterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.screenLabel.text = "DEMO 1 - state lives in the Activity"
        binding.screenSubtitle.text = "Tap +1 a few times, start the goal load, then rotate."
        binding.hint.text =
            "Watch: the counter resets to 0, and a load that was in flight never finishes."

        // The Activity is doing three jobs at once here:
        //   1. drawing the UI,
        //   2. holding the data,
        //   3. deciding what happens when data changes.
        // That is exactly the pile that MVVM splits apart.
        binding.addStep.setOnClickListener {
            stepCount++
            binding.stepCount.text = stepCount.toString()
        }

        binding.loadGoal.setOnClickListener {
            goalText = "Loading..."
            binding.goalStatus.text = goalText

            // PROBLEM 3: lifecycleScope is tied to THIS Activity.
            // Rotate while the 3 seconds are running and the coroutine is
            // cancelled halfway. The user taps the button, sees "Loading...",
            // rotates, and the app silently goes back to "not loaded".
            // On a real network call you have now burned the user's data
            // for a result nobody ever sees.
            lifecycleScope.launch {
                val goal = StepRepository.fetchDailyGoal()
                goalText = "Daily goal: $goal steps"
                binding.goalStatus.text = goalText
            }
        }

        // Restoring by hand from the view state does not help either - the
        // views are rebuilt from the XML, so they come back with their
        // default values too.
        binding.stepCount.text = stepCount.toString()
        binding.goalStatus.text = goalText
    }
}
