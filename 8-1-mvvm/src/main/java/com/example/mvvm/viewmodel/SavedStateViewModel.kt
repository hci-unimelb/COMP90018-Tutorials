package com.example.mvvm.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mvvm.model.StepRepository
import kotlinx.coroutines.launch

/**
 * BONUS - THE CASE A PLAIN VIEWMODEL STILL LOSES
 *
 * A ViewModel survives rotation because it lives in memory alongside your
 * process. But Android kills BACKGROUND PROCESSES whenever it needs the RAM.
 * If the user opens your app, switches to Instagram for ten minutes, then
 * comes back, the system may have quietly killed your process and will now
 * rebuild the screen from scratch. Your ViewModel is long gone.
 *
 * SavedStateHandle is a small key-value bundle that Android writes to disk
 * and hands back when the process is recreated. Anything you put in here
 * survives that. It is NOT a database - keep it to small, cheap values
 * (a search query, a selected tab, a scroll position, a draft message),
 * not a list of 500 items.
 *
 * HOW TO SEE IT IN CLASS WITHOUT WAITING FOR A REAL KILL:
 *   Settings -> System -> Developer options -> "Don't keep activities"
 * Turn it on, leave the app, come back. Demo 2 forgets. Demo 3 remembers.
 *
 * Note the honest limit: the saved VALUE comes back, but a coroutine that was
 * mid-flight does not. Work in progress is never restored, only data.
 */
class SavedStateViewModel(private val state: SavedStateHandle) : ViewModel() {

    companion object {
        private const val KEY_STEPS = "steps"
        private const val KEY_GOAL = "goal"
    }

    // getLiveData() reads the saved value if there is one, or uses the
    // default on a genuinely fresh start.
    val stepCount: LiveData<Int> = state.getLiveData(KEY_STEPS, 0)
    val goalStatus: LiveData<String> = state.getLiveData(KEY_GOAL, "Daily goal: not loaded")

    private var isLoading = false

    // Writing through `state[...]` both stores the value for later AND
    // pushes it to the LiveData above, so the UI updates as normal.
    fun addStep() {
        state[KEY_STEPS] = (state.get<Int>(KEY_STEPS) ?: 0) + 1
    }

    fun loadGoal() {
        if (isLoading) return
        isLoading = true
        state[KEY_GOAL] = "Loading..."

        viewModelScope.launch {
            val goal = StepRepository.fetchDailyGoal()
            state[KEY_GOAL] = "Daily goal: $goal steps"
            isLoading = false
        }
    }
}
