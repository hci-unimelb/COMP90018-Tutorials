package com.example.mvvm.model

import kotlinx.coroutines.delay

/**
 * THE "MODEL" LAYER (the M in MVVM)
 *
 * A Repository is the single place that knows HOW to get a piece of data.
 * Room database? Firebase? A REST call with Retrofit? SharedPreferences?
 * The rest of the app does not care - it just calls fetchDailyGoal().
 *
 * Why bother?
 *   - You can swap Firebase for Supabase by editing ONE file.
 *   - Your ViewModel becomes testable, because you can hand it a fake repository.
 *   - In a group project, one person owns the repository and everyone else
 *     just calls the function. No merge conflicts in the Activity.
 *
 * Here we fake a slow network call with delay(). Note that `delay` is a
 * SUSPEND function: it pauses this coroutine for 3 seconds without blocking
 * the main thread, so the UI stays responsive the whole time.
 */
object StepRepository {

    suspend fun fetchDailyGoal(): Int {
        delay(3000)      // pretend this is Retrofit / Firebase / Supabase
        return 8000
    }
}
