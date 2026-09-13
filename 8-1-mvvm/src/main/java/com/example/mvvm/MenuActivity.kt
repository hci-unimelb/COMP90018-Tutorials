package com.example.mvvm

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mvvm.databinding.ActivityMenuBinding
import com.example.mvvm.view.BrokenActivity
import com.example.mvvm.view.LoginActivity
import com.example.mvvm.view.MvvmActivity
import com.example.mvvm.view.SavedStateActivity

/**
 * WEEK 8 - MVVM AND SURVIVING A ROTATION
 *
 * This module is one tiny app built three different ways.
 * The screen looks identical every time. The only thing that changes is
 * WHERE the data is kept.
 *
 *   1. BrokenActivity      - data kept in the Activity itself
 *   2. MvvmActivity        - data kept in a ViewModel  (this is MVVM)
 *   3. SavedStateActivity  - ViewModel + SavedStateHandle
 *   4. LoginActivity       - a ViewModel that holds real logic, not just data
 *
 * Run each one, tap the counter, and rotate the emulator (Ctrl+F11 / Cmd+Left).
 */
class MenuActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMenuBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.openBroken.setOnClickListener {
            startActivity(Intent(this, BrokenActivity::class.java))
        }
        binding.openMvvm.setOnClickListener {
            startActivity(Intent(this, MvvmActivity::class.java))
        }
        binding.openSavedState.setOnClickListener {
            startActivity(Intent(this, SavedStateActivity::class.java))
        }
        binding.openLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
    }
}
