package com.example.mvvm.view

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.doAfterTextChanged
import com.example.mvvm.databinding.ActivityLoginBinding
import com.example.mvvm.viewmodel.LoginViewModel

/**
 * DEMO 4 - A VIEWMODEL WITH REAL LOGIC IN IT
 *
 * This is the classic textbook MVVM example (the one in the GeeksforGeeks
 * article): a login form where the ViewModel validates the input and the View
 * just shows whatever it is told.
 *
 * Read this file and notice what is NOT here. There is no `if`. There is no
 * rule about password length. There is no email pattern. The Activity cannot
 * tell you what makes a login valid, and that is correct - it is a renderer.
 *
 * The article does this with the Data Binding library, so the rules are wired
 * up inside the XML with @={viewModel.userEmail}. This version does the same
 * thing with view binding plus LiveData, which is the wiring used everywhere
 * else in this repo. Same architecture, different plumbing. See the slides.
 */
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // ----- events going DOWN to the ViewModel -----
        binding.email.doAfterTextChanged { viewModel.onEmailChanged(it.toString()) }
        binding.password.doAfterTextChanged { viewModel.onPasswordChanged(it.toString()) }
        binding.login.setOnClickListener { viewModel.onLoginClicked() }

        // ----- state coming UP from the ViewModel -----
        viewModel.message.observe(this) { message ->
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                // Tell the ViewModel we have shown it, so a rotation does not
                // replay the same toast at the user.
                viewModel.onMessageShown()
            }
        }

        viewModel.isLoggedIn.observe(this) { loggedIn ->
            binding.status.text = if (loggedIn) "Signed in" else "Not signed in"
        }
    }
}
