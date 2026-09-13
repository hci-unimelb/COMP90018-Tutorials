package com.example.mvvm.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mvvm.model.User

/**
 * A VIEWMODEL THAT ACTUALLY DECIDES SOMETHING
 *
 * The step counter ViewModel only held a number. This one holds the rules.
 * Every judgement in this file - what counts as an email, how long a password
 * has to be, what message the user sees - lives here and NOT in the Activity.
 *
 * Why that matters: this class has no Android imports at all except the
 * lifecycle ones, so you can unit test every rule below with plain JUnit on
 * your laptop in milliseconds. No emulator, no device, no UI.
 *
 * WHY NOT android.util.Patterns.EMAIL_ADDRESS?
 * It is the obvious way to validate an email, and it is a trap here: it is an
 * Android framework class, so the moment you use it your ViewModel can only be
 * tested on a device or with Robolectric. A hand-rolled check keeps this class
 * pure. In a real app you would put the pattern behind an interface in the
 * model layer and hand in a fake for tests.
 */
class LoginViewModel : ViewModel() {

    // What the user has typed. The View pushes these in as the text changes.
    private var email = ""
    private var password = ""

    // The result of the last login attempt, for the View to render.
    private val _message = MutableLiveData<String?>(null)
    val message: LiveData<String?> = _message

    private val _isLoggedIn = MutableLiveData(false)
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    fun onEmailChanged(value: String) { email = value }
    fun onPasswordChanged(value: String) { password = value }

    /**
     * The View calls this on a button tap and passes nothing. It does not know
     * what the rules are, only that the user pressed Login.
     */
    fun onLoginClicked() {
        _message.value = when {
            email.isBlank() -> "Email cannot be empty"
            !isValidEmail(email) -> "That is not a valid email address"
            password.length < 5 -> "Password must be at least 5 characters"
            else -> {
                val user = User(email, password)
                _isLoggedIn.value = true
                "Welcome, ${user.email}"
            }
        }
    }

    /**
     * ONE-TIME EVENTS ARE A REAL GOTCHA
     *
     * LiveData replays its current value to any new observer. That is exactly
     * what we wanted for the counter - it is how the screen repaints itself
     * after a rotation. But for a message it is wrong: rotate the phone and
     * the same error toast pops up again, out of nowhere.
     *
     * The fix is to mark the message as consumed once the View has shown it.
     * (Larger apps use a SingleLiveEvent wrapper, or a Channel / SharedFlow.)
     */
    fun onMessageShown() {
        _message.value = null
    }

    private fun isValidEmail(value: String): Boolean {
        val at = value.indexOf('@')
        val dot = value.lastIndexOf('.')
        return at > 0 && dot > at + 1 && dot < value.length - 1 && !value.contains(' ')
    }
}
