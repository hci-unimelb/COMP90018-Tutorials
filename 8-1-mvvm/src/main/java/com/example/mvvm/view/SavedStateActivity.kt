package com.example.mvvm.view

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.mvvm.databinding.ActivityCounterBinding
import com.example.mvvm.viewmodel.SavedStateViewModel

/**
 * DEMO 3 - VIEWMODEL + SAVEDSTATEHANDLE
 *
 * The Activity is byte for byte the same shape as Demo 2. Only the ViewModel
 * changed. That is the point worth saying out loud: once the UI only renders
 * state, you can change how the state is stored without touching the UI.
 *
 * `by viewModels()` builds the SavedStateHandle for you - there is no factory
 * to write, because the default factory already knows how to supply one.
 */
class SavedStateActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCounterBinding

    private val viewModel: SavedStateViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCounterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.screenLabel.text = "DEMO 3 - ViewModel + SavedStateHandle"
        binding.screenSubtitle.text = "Survives rotation AND the system killing your process."
        binding.hint.text =
            "Turn on Developer options -> \"Don't keep activities\", leave the app, come back."

        binding.addStep.setOnClickListener { viewModel.addStep() }
        binding.loadGoal.setOnClickListener { viewModel.loadGoal() }

        viewModel.stepCount.observe(this) { count ->
            binding.stepCount.text = count.toString()
        }
        viewModel.goalStatus.observe(this) { status ->
            binding.goalStatus.text = status
        }
    }
}
