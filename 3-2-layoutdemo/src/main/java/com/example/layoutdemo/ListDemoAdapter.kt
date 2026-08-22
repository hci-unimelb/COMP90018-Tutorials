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
 *
 * On screen: each row comes from res/layout/list_example.xml — an 80x80dp fruit icon on the
 * left, the fruit name to its right, vertically centered. Twelve of these stacked make up the
 * scrolling list.
 *
 * Note: unlike [RecyclerDemoAdapter], this class does NOT set a click listener on the row.
 * ListView's tap handling is wired up in LayoutDemoFragment.kt instead, on the ListView itself
 * (`demoListView.setOnItemClickListener`) — one listener for the whole list. RecyclerView's
 * demo does the opposite: the listener lives inside the adapter, per row. Same end result
 * (a Toast with the fruit name), two different places to attach it.
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

        // getView() is called once per VISIBLE ROW SLOT, not once per fruit — a screen showing
        // 8 rows at a time only ever needs 8 real View objects, no matter how many fruits scroll
        // past. `convertView` is Android asking "here's a row that just scrolled off-screen and
        // isn't being used — want it back instead of building a new one?"
        if (convertView == null) {
            // Case A: no leftover row was offered (this only happens the first ~8-10 times, just
            // enough to fill the screen). Inflating from XML is the SLOW step — parsing XML and
            // constructing the ImageView/TextView objects — so we only want to pay that cost once
            // per row slot, ever.
            binding = ListExampleBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            // Stash the binding on the row's own `.tag`, like taping a note to the back of a
            // picture frame: "here's where your widgets are" — so future recycles skip inflation.
            binding.root.tag = binding
        } else {
            // Case B: this exact row View has been used before and just scrolled off-screen.
            // Reuse it — just read the note back off `.tag` instead of rebuilding anything.
            // This is the FAST path, and it's what runs on nearly every scroll after the first
            // screenful: the row (the "frame") is reused, only the data in it (the "photo") changes.
            binding = convertView.tag as ListExampleBinding
        }

        // From here on, both cases converge: `binding` now points at SOME row (fresh or
        // recycled) with valid widget references — fill it with this position's data.
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
