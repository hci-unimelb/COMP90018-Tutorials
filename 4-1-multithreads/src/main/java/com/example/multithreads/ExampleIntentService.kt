@file:Suppress("DEPRECATION", "OVERRIDE_DEPRECATION")

package com.example.multithreads

import android.app.IntentService
import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class ExampleIntentService : IntentService("ExampleIntentService") {

    // Called when receiving Intent from other components
    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            if (ACTION_EXAMPLE_START == action) {
                handleActionExample()
            }
        }
    }

    // the method to process the intent contains action of ACTION_EXAMPLE_START
    private fun handleActionExample() {
        try {
            Thread.sleep(2000)
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        //Communicate by broadcasting Intent
        val intent = Intent()
        intent.action = ACTION_EXAMPLE_END
        intent.putExtra(RESULT_PARAM, "IntentService created this message.")
        intent.putExtra(RESULT_DURATION, System.currentTimeMillis())
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    companion object {
        private const val ACTION_EXAMPLE_START = "example.start"
        const val ACTION_EXAMPLE_END = "example.end"

        const val RESULT_PARAM = "result"
        const val RESULT_DURATION = "duration"

        fun startExample(context: Context) {
            val intent = Intent(context, ExampleIntentService::class.java)
            intent.action = ACTION_EXAMPLE_START
            context.startService(intent)
        }
    }
}
