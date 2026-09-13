package com.example.mvvm.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mvvm.model.StepRepository
import kotlinx.coroutines.launch

/**
 * THE "VIEWMODEL" LAYER (the VM in MVVM)
 *
 * A ViewModel is a plain class that holds the state of ONE screen and the
 * logic that changes it. Two rules make it work:
 *
 *   RULE 1 - It outlives the Activity.
 *            Android keeps the ViewModel alive across a configuration change
 *            and hands the SAME instance to the newly created Activity.
 *            It is only cleared when the screen is really finished
 *            (back pressed, finish() called) - that is onCleared().
 *
 *   RULE 2 - It must never touch a View, a Context, or an Activity.
 *            No findViewById, no binding, no `this@Activity`. If a ViewModel
 *            held a reference to a destroyed Activity, that Activity could
 *            never be garbage collected - a classic Android memory leak.
 *            This is also why it can be unit tested with no emulator.
 *
 * THE _underscore PATTERN
 * We expose two properties for each piece of state:
 *   _stepCount : MutableLiveData - private, only this class can write to it
 *    stepCount : LiveData        - public, read-only for the UI
 * The Activity can observe but cannot secretly change the data behind the
 * ViewModel's back. In a group project this is the difference between
 * "one place changes the score" and "four people change the score".
 */
class StepViewModel : ViewModel() {

    private val _stepCount = MutableLiveData(0)
    val stepCount: LiveData<Int> = _stepCount

    private val _goalStatus = MutableLiveData("Daily goal: not loaded")
    val goalStatus: LiveData<String> = _goalStatus

    // Guard so double-tapping the button does not fire two network calls.
    // Business rules like this belong here, not in the Activity.
    private var isLoading = false

    fun addStep() {
        _stepCount.value = (_stepCount.value ?: 0) + 1
    }

    fun loadGoal() {
        if (isLoading) return
        isLoading = true
        _goalStatus.value = "Loading..."

        // viewModelScope is tied to the VIEWMODEL, not the Activity.
        // Rotating does not cancel it, so the call finishes and the result
        // lands in LiveData. Whichever Activity is on screen at that moment
        // gets the update. If the user leaves the screen for good, the
        // ViewModel is cleared and the coroutine is cancelled automatically -
        // no leak, no work done for nothing.
        viewModelScope.launch {
            val goal = StepRepository.fetchDailyGoal()
            _goalStatus.value = "Daily goal: $goal steps"
            isLoading = false
        }
    }
}
