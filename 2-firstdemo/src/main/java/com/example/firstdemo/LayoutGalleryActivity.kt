package com.example.firstdemo

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.firstdemo.databinding.ActivityLayoutGalleryBinding

/**
 * [LayoutGalleryActivity] - Compares LinearLayout, RelativeLayout, and ConstraintLayout side by side.
 *
 * A segmented control (MaterialButtonToggleGroup) swaps between three pre-built mock screens,
 * each showing the kind of content that layout is actually good at, plus a short explanation
 * of when to reach for it and a UI/UX tip.
 */
class LayoutGalleryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLayoutGalleryBinding

    private enum class Demo { LINEAR, RELATIVE, CONSTRAINT }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLayoutGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Fires whenever the user taps a different segment in the toggle group
        binding.layoutToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            when (checkedId) {
                R.id.btn_linear -> showDemo(Demo.LINEAR)
                R.id.btn_relative -> showDemo(Demo.RELATIVE)
                R.id.btn_constraint -> showDemo(Demo.CONSTRAINT)
            }
        }

        // Start on Linear; this also triggers the listener above via check()
        binding.layoutToggle.check(R.id.btn_linear)
    }

    /**
     * Shows the chosen mock layout (all three are already inflated, we just flip visibility)
     * and swaps the explanation card underneath it to match.
     */
    private fun showDemo(demo: Demo) {
        binding.demoLinear.root.visibility = if (demo == Demo.LINEAR) View.VISIBLE else View.GONE
        binding.demoRelative.root.visibility = if (demo == Demo.RELATIVE) View.VISIBLE else View.GONE
        binding.demoConstraint.root.visibility = if (demo == Demo.CONSTRAINT) View.VISIBLE else View.GONE

        val (title, use, tip) = when (demo) {
            Demo.LINEAR -> Triple(
                "LinearLayout",
                "Best for: simple, predictable lists — settings rows, toolbars, chat bubbles, forms that read top-to-bottom.",
                "Tip: great for accessibility (natural tab order) and fast prototyping, but avoid nesting many of these — each nested layout costs an extra measure/layout pass."
            )
            Demo.RELATIVE -> Triple(
                "RelativeLayout",
                "Best for: overlapping or corner-pinned elements a single-axis stack can't express — badges, timestamps, cards with layered content.",
                "Tip: mostly superseded by ConstraintLayout today. Fine to read in legacy code, but reach for ConstraintLayout on new screens."
            )
            Demo.CONSTRAINT -> Triple(
                "ConstraintLayout",
                "Best for: complex, responsive screens — profile headers, dashboards, anything that must adapt across phone/tablet/orientation.",
                "Tip: Android Studio's default for new projects. Use chains + 0dp \"match constraint\" widths so views share space proportionally instead of a fixed size."
            )
        }
        binding.explanationTitle.text = title
        binding.explanationUse.text = use
        binding.explanationTip.text = tip
    }
}
