package com.example.mvvm.view

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.mvvm.databinding.ActivityCounterBinding
import com.example.mvvm.viewmodel.StepViewModel

/**
 * DEMO 2 - THE SAME APP, DONE WITH MVVM
 *
 * Compare this file to BrokenActivity line by line. The layout is identical,
 * the buttons are identical. What changed is that the Activity no longer
 * OWNS anything. It has exactly two jobs now:
 *
 *   1. Send user events DOWN to the ViewModel      (setOnClickListener -> viewModel.addStep())
 *   2. Render whatever comes back UP               (observe -> set text)
 *
 * That one-way loop is the whole idea:
 *
 *      View  --(events)-->  ViewModel  --(calls)-->  Model / Repository
 *      View  <--(state)---  ViewModel  <--(data)---  Model / Repository
 *
 * An Activity written this way is boring, short, and hard to get wrong -
 * which is exactly what you want from the class that Android keeps destroying.
 */
class MvvmActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCounterBinding

    // `by viewModels()` is the important line in this whole module.
    // On first creation it builds a StepViewModel. After a rotation it finds
    // the existing one and gives it back. You never call the constructor
    // yourself, and you never write `StepViewModel()`.
    private val viewModel: StepViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCounterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.screenLabel.text = "DEMO 2 - state lives in a ViewModel"
        binding.screenSubtitle.text = "Same buttons. Same layout. Rotate and compare."
        binding.hint.text =
            "The counter keeps its value, and a load started before the rotation still lands."

        // ----- events going DOWN -----
        // Notice there is no arithmetic here. The Activity does not know or
        // care that a step is +1. It just reports that a button was tapped.
        binding.addStep.setOnClickListener { viewModel.addStep() }
        binding.loadGoal.setOnClickListener { viewModel.loadGoal() }

        // ----- state coming UP -----
        // observe() is lifecycle-aware. It only delivers while this Activity
        // is at least STARTED, and it unsubscribes itself on destroy, so you
        // cannot leak and cannot get a crash from updating a dead view.
        //
        // It also REPLAYS the current value the moment we subscribe. That is
        // what repaints the screen correctly after a rotation - we did not
        // write a single line of restore code.
        viewModel.stepCount.observe(this) { count ->
            binding.stepCount.text = count.toString()
        }
        viewModel.goalStatus.observe(this) { status ->
            binding.goalStatus.text = status
        }
    }
}
