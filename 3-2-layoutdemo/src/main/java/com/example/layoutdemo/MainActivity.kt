package com.example.layoutdemo

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.example.layoutdemo.databinding.ActivityMainBinding

/**
 * UNDERSTANDING FRAGMENTS:
 * This Activity's own layout (activity_main.xml) is almost empty — just a FragmentContainerView
 * ("layout_fragment") sitting above a bottom navigation bar. It's an empty picture frame.
 *
 * All four demos (Linear, Relative, List, Recycler) are the SAME Fragment class,
 * [LayoutDemoFragment], reused four times with a different argument telling it which layout to
 * inflate. Swapping what's inside the frame is done with a FragmentTransaction: begin it,
 * describe the change with replace(), then commit it.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Show the Linear demo by default when the Activity first opens.
        // newInstance() builds a fresh LayoutDemoFragment and stores which layout to show inside
        // a Bundle (fragment.arguments) rather than a constructor parameter, because Android needs
        // to be able to recreate this Fragment from scratch later using only its class name +
        // its arguments (e.g. after a config change or process death).
        val initial = LayoutDemoFragment.newInstance(LayoutDemoFragment.LINEAR_DEMO)
        supportFragmentManager
            .beginTransaction()                        // 1. open a transaction (a batch of pending changes)
            .replace(R.id.layout_fragment, initial)     // 2. remove whatever's in the frame, put this fragment there instead
            .addToBackStack(null)                       // 3. let the Back button undo this swap instead of closing the app
            .commit()                                   // 4. nothing above actually runs until commit() is called

        // Setting for Navigation Bar
        // Every tap repeats the exact same beginTransaction().replace().commit() pattern above —
        // only the fragment type (i.e. which layout gets inflated) changes.
        binding.navView.setOnItemSelectedListener { item ->
            val fragmentType = when (item.itemId) {
                // To show Linear layout demonstration
                R.id.navigation_linear -> LayoutDemoFragment.LINEAR_DEMO
                // To show Relative layout demonstration
                R.id.navigation_relative -> LayoutDemoFragment.RELATIVE_DEMO
                // To show List view demonstration
                R.id.navigation_list -> LayoutDemoFragment.LIST_DEMO
                // To show Recycler demonstration
                R.id.navigation_recycler -> LayoutDemoFragment.RECYCLER_DEMO
                else -> return@setOnItemSelectedListener false
            }

            // A brand-new Fragment instance is created on every tap — even switching back to a
            // tab you've already visited builds a new one; the old instance is gone for good
            // (replace() tore down its view in onDestroyView() the moment we last navigated away).
            val fragment = LayoutDemoFragment.newInstance(fragmentType)
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.layout_fragment, fragment)
                .addToBackStack(null)
                .commit()
            true
        }
    }
}
