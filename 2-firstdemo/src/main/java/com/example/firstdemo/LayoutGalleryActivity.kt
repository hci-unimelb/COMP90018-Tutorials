package com.example.firstdemo

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.firstdemo.databinding.ActivityLayoutGalleryBinding

/**
 * [LayoutGalleryActivity] - Compares padding/margin, LinearLayout, RelativeLayout,
 * ConstraintLayout, GridLayout, and TableLayout side by side.
 *
 * A segmented control swaps between six pre-built mock screens, each with content
 * suited to that layout, plus a short explanation of when to use it and a UI/UX tip.
 */
class LayoutGalleryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLayoutGalleryBinding

    private enum class Demo { PADDING_MARGIN, LINEAR, RELATIVE, CONSTRAINT, GRID, TABLE }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLayoutGalleryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Fires whenever the user taps a different segment in the toggle group
        binding.layoutToggle.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (!isChecked) return@addOnButtonCheckedListener
            when (checkedId) {
                R.id.btn_padding_margin -> showDemo(Demo.PADDING_MARGIN)
                R.id.btn_linear -> showDemo(Demo.LINEAR)
                R.id.btn_relative -> showDemo(Demo.RELATIVE)
                R.id.btn_constraint -> showDemo(Demo.CONSTRAINT)
                R.id.btn_grid -> showDemo(Demo.GRID)
                R.id.btn_table -> showDemo(Demo.TABLE)
            }
        }

        // Start on Padding vs Margin; this also triggers the listener above via check()
        binding.layoutToggle.check(R.id.btn_padding_margin)

        // Prints the two boxes' ACTUAL laid-out pixel sizes to Logcat — padding leaves the
        // view's own width untouched (the background still fills the frame); margin shrinks it.
        binding.demoPaddingMargin.logDimensionsButton.setOnClickListener {
            val paddingBox = binding.demoPaddingMargin.paddingBox
            val marginBox = binding.demoPaddingMargin.marginBox
            Log.d(TAG, "Padding box: width=${paddingBox.width}px, height=${paddingBox.height}px (fills the frame — padding only pushes the TEXT inward)")
            Log.d(TAG, "Margin box: width=${marginBox.width}px, height=${marginBox.height}px (smaller than the frame — margin pushes the WHOLE VIEW inward)")
        }
    }

    /**
     * Shows the chosen mock layout (all six are already inflated, we just flip visibility)
     * and swaps the explanation card underneath it to match.
     */
    private fun showDemo(demo: Demo) {
        binding.demoPaddingMargin.root.visibility = if (demo == Demo.PADDING_MARGIN) View.VISIBLE else View.GONE
        binding.demoLinear.root.visibility = if (demo == Demo.LINEAR) View.VISIBLE else View.GONE
        binding.demoRelative.root.visibility = if (demo == Demo.RELATIVE) View.VISIBLE else View.GONE
        binding.demoConstraint.root.visibility = if (demo == Demo.CONSTRAINT) View.VISIBLE else View.GONE
        binding.demoGrid.root.visibility = if (demo == Demo.GRID) View.VISIBLE else View.GONE
        binding.demoTable.root.visibility = if (demo == Demo.TABLE) View.VISIBLE else View.GONE

        val (title, use, tip) = when (demo) {
            Demo.PADDING_MARGIN -> Triple(
                "Padding vs Margin",
                "Padding is space INSIDE a view — between its edge and its content; the view's own background still fills it. Margin is space OUTSIDE a view — between it and its parent or siblings.",
                "Tip: use padding to give a view's own content breathing room, margin to space two views apart. Prefer paddingStart/End and layout_marginStart/End over Left/Right — Start/End auto-flips for right-to-left languages."
            )
            Demo.LINEAR -> Triple(
                "LinearLayout",
                "Best for: simple, predictable lists — settings rows, toolbars, chat bubbles, forms that read top-to-bottom.",
                "Tip: great for accessibility (natural tab order) and fast prototyping, but avoid nesting many of these — every extra layer costs a measure/layout pass. A flat, wide hierarchy renders faster than a deep, narrow one."
            )
            Demo.RELATIVE -> Triple(
                "RelativeLayout",
                "Best for: overlapping or corner-pinned elements a single-axis stack can't express — badges, timestamps, cards with layered content.",
                "Tip: mostly superseded by ConstraintLayout today. Fine to read in legacy code, but reach for ConstraintLayout on new screens."
            )
            Demo.CONSTRAINT -> Triple(
                "ConstraintLayout",
                "Best for: complex, responsive screens — profile headers, dashboards, anything that must adapt across phone/tablet/orientation.",
                "Tip: Android Studio's default for new projects, and Google's recommended tool for keeping a hierarchy shallow. Use chains + 0dp \"match constraint\" widths so views share space proportionally instead of a fixed size."
            )
            Demo.GRID -> Triple(
                "GridLayout",
                "Best for: a fixed, non-scrolling grid of same-sized items — calculators, calendars, icon/emoji pickers, dial pads.",
                "Tip: layout_columnSpan lets one cell (like the \"0\" key here) take up more than one column. For a SCROLLING grid of dynamic data — a photo gallery, a product list — use RecyclerView with a GridLayoutManager instead."
            )
            Demo.TABLE -> Triple(
                "TableLayout",
                "Best for: static, column-aligned data — pricing/spec comparisons, label-value forms that need to line up across rows.",
                "Tip: stretchColumns (used here on the Price column) makes one column absorb the leftover width. TableLayout doesn't scroll or recycle views — for a long or dynamic dataset, use RecyclerView instead."
            )
        }
        binding.explanationTitle.text = title
        binding.explanationUse.text = use
        binding.explanationTip.text = tip
    }

    companion object {
        private const val TAG = "Layout Gallery"
    }
}
