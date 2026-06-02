package com.example.layoutdemo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.example.layoutdemo.databinding.ListExampleBinding

/**
 * [ListDemoAdapter] - The Controller that links lists of data to a ListView widget.
 *
 * What is a ListView?
 * An older, simpler list container widget. To display elements efficiently, it utilizes
 * a parameter named `convertView`.
 *
 * How does ListView view-recycling work?
 * As cells scroll off the screen, their visual containers are saved in `convertView` and passed
 * back to the Adapter. Instead of inflating new XML views repeatedly, the adapter can check if
 * a recycled `convertView` is available and reuse it!
 */
class ListDemoAdapter(
    context: Context,
    private val resourceId: Int,
    objects: List<Fruit>
) : ArrayAdapter<Fruit>(context, resourceId, objects) {

    /**
     * [getView]:
     * Returns the view representing a single row cell in our ListView.
     *
     * @position The index of the item to display in the list.
     * @convertView A recycled cell container that we can reuse (could be null).
     * @parent The parent layout container that this row belongs to.
     */
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val binding: ListExampleBinding

        // We check if a recycled view is available
        if (convertView == null) {
            // Case A: No recycled view exists! Inflate a brand-new XML layout cell.
            binding = ListExampleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            
            // We store the binding instance inside the root view's "tag" pocket so we can retrieve it later.
            binding.root.tag = binding
        } else {
            // Case B: A recycled view is available! Pull the old binding reference directly out of its tag.
            // This avoids slow XML layout inflation and keeps scrolling smooth!
            binding = convertView.tag as ListExampleBinding
        }

        // Fetch the data object for the current row index
        val fruit = getItem(position)

        // Bind the data values to the cell UI widgets
        if (fruit != null) {
            binding.listExampleImage.setImageResource(fruit.fruitImage)
            binding.listExampleText.text = fruit.fruitName
        }

        // Return the cell's root view to display it in the list
        return binding.root
    }
}
