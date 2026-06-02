package com.example.layoutdemo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.layoutdemo.databinding.RecyclerExampleBinding

/**
 * [RecyclerDemoAdapter] - The Controller that links lists of data to a RecyclerView widget.
 *
 * What is a RecyclerView?
 * An advanced widget designed to display thousands of items smoothly. Instead of inflating a separate view
 * for all 1,000 items (which would crash the phone's memory), it only creates enough cell containers to fit
 * the visible screen (e.g. 10 cells).
 *
 * As the user scrolls, cells that disappear at the top are "recycled" and moved to the bottom to display
 * the next item's data.
 */
class RecyclerDemoAdapter(
    // List of fruit data models to display
    private val fruits: List<Fruit>,
    // The XML layout resource defining the UI design of a single row
    private val resourceId: Int
) : RecyclerView.Adapter<RecyclerDemoAdapter.ViewHolder>() {

    /**
     * ViewHolder Pattern:
     * A ViewHolder is a container object that holds references to all the sub-widgets of a single row cell
     * (like images, texts).
     * By storing the ViewBinding reference directly inside each ViewHolder, each row cell has its own isolated
     * widgets, and the system does not need to search the view hierarchy using slow findViewById calls while scrolling!
     */
    class ViewHolder(val binding: RecyclerExampleBinding) : RecyclerView.ViewHolder(binding.root)

    /**
     * [onCreateViewHolder]:
     * Triggered by the RecyclerView when it needs to create a new cell container (ViewHolder).
     * This is only called a few times until there are enough row containers to fill the screen.
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        // Inflate the XML design file for a single row item
        val binding = RecyclerExampleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    /**
     * [onBindViewHolder]:
     * Triggered by the RecyclerView to populate (bind) data into an existing cell container.
     * This runs constantly as the user scrolls, so it must be fast!
     *
     * @holder The ViewHolder representing the individual cell row we are updating
     * @position The index of the item in our list that we want to display inside this cell
     */
    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val fruit = fruits[position]

        // Load the image resource ID and the name string into the cell's widgets
        holder.binding.recyclerExampleImage.setImageResource(fruit.fruitImage)
        holder.binding.recyclerExampleText.text = fruit.fruitName

        // Handle user taps on individual list rows
        holder.binding.recyclerExampleLayout.setOnClickListener { view ->
            // Use bindingAdapterPosition to retrieve the current, real-time list index of this cell
            val currentIndex = holder.bindingAdapterPosition
            if (currentIndex != RecyclerView.NO_POSITION) {
                val clickedFruit = fruits[currentIndex]
                Toast.makeText(view.context, clickedFruit.fruitName, Toast.LENGTH_SHORT).show()
            }
        }
    }

    /**
     * [getItemCount]:
     * Returns the size of our list so the RecyclerView knows how many items exist in total.
     */
    override fun getItemCount(): Int = fruits.size
}
