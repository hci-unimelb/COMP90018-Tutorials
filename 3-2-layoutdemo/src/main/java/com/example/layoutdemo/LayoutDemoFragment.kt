package com.example.layoutdemo

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.example.layoutdemo.databinding.FragmentLayoutDemoListBinding
import com.example.layoutdemo.databinding.FragmentLayoutDemoRecyclerBinding

/**
 * One Fragment class, four demos. Instead of writing a separate Fragment for Linear, Relative,
 * List and Recycler, this single class is reused for all four — MainActivity just creates a new
 * instance via [newInstance] and tells it which layout resource to inflate.
 *
 * That "which layout" value has to travel through a Bundle (see [LAYOUT_TYPE] below), not a
 * constructor parameter or a plain property set after construction. Android sometimes recreates
 * a Fragment from scratch using only its class name + its arguments Bundle (e.g. after a
 * rotation), so the Bundle is the only place that's guaranteed to survive that process.
 */
class LayoutDemoFragment : Fragment() {

    // Falls back to the Linear demo if this Fragment is ever created without arguments.
    private var layout = R.layout.fragment_layout_demo_linear

    // Bindings are nulled out in onDestroyView() below — holding onto a view binding after its
    // view has been destroyed is a classic Fragment memory leak.
    private var listBinding: FragmentLayoutDemoListBinding? = null
    private var recyclerBinding: FragmentLayoutDemoRecyclerBinding? = null

    // Called by the FragmentManager every time this Fragment needs its view (re)built — i.e. once
    // per beginTransaction().replace() call in MainActivity.kt.
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // Read back whichever layout MainActivity asked for via newInstance().
        arguments?.let { layout = it.getInt(LAYOUT_TYPE) }

        return when (layout) {
            R.layout.fragment_layout_demo_list -> {
                val binding = FragmentLayoutDemoListBinding.inflate(inflater, container, false)
                listBinding = binding

                // The Adapter is the middleman: it turns each Fruit into a row View using
                // list_example.xml (an 80x80dp icon + the fruit name, side by side) and hands
                // ListView back whichever row is needed as the user scrolls.
                val adapter = ListDemoAdapter(requireActivity(), R.layout.list_example, getFruits())
                binding.demoListView.adapter = adapter

                // Method 1: listen on the ListView itself — one listener for the whole list;
                // Android tells you which row index (i) was tapped.
                binding.demoListView.setOnItemClickListener { adapterView, _, i, _ ->
                    val fruit = adapterView.getItemAtPosition(i) as Fruit
                    Toast.makeText(context, fruit.fruitName, Toast.LENGTH_SHORT).show()
                }

                binding.root
            }
            R.layout.fragment_layout_demo_recycler -> {
                val binding = FragmentLayoutDemoRecyclerBinding.inflate(inflater, container, false)
                recyclerBinding = binding

                // Same idea as the ListView adapter above, but each row (recycler_example.xml)
                // stacks its icon above its name instead of side by side — a better shape for a grid.
                val adapter = RecyclerDemoAdapter(getFruits(), R.layout.recycler_example)

                // RecyclerView doesn't know how to arrange its rows on its own — a LayoutManager
                // decides that. A staggered 2-column grid lets each card settle into whichever
                // column is currently shorter, instead of forcing every card into a rigid table.
                binding.demoRecycler.layoutManager =
                    StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
                binding.demoRecycler.adapter = adapter

                binding.root
            }
            else -> inflater.inflate(layout, container, false)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        listBinding = null
        recyclerBinding = null
    }

    // To generate an array of fruit example for ListView and RecyclerView Demonstration
    private fun getFruits(): ArrayList<Fruit> {
        val fruits = ArrayList<Fruit>()
        fruits.add(Fruit(R.drawable.apple, "Apple"))
        fruits.add(Fruit(R.drawable.bananas, "Bananas"))
        fruits.add(Fruit(R.drawable.cherry, "Cherry"))
        fruits.add(Fruit(R.drawable.grapes, "Grapes"))
        fruits.add(Fruit(R.drawable.lemon, "Lemon"))
        fruits.add(Fruit(R.drawable.orange, "Orange"))
        fruits.add(Fruit(R.drawable.melon, "Melon"))
        fruits.add(Fruit(R.drawable.peach, "Peach"))
        fruits.add(Fruit(R.drawable.pear, "Pear"))
        fruits.add(Fruit(R.drawable.pomegranate, "Pomegranate"))
        fruits.add(Fruit(R.drawable.strawberry, "Strawberry"))
        fruits.add(Fruit(R.drawable.watermelon, "Watermelon"))
        return fruits
    }

    companion object {
        // The four layout resources this one Fragment class can show.
        val LINEAR_DEMO = R.layout.fragment_layout_demo_linear
        val RELATIVE_DEMO = R.layout.fragment_layout_demo_relative
        val LIST_DEMO = R.layout.fragment_layout_demo_list
        val RECYCLER_DEMO = R.layout.fragment_layout_demo_recycler

        // The key used to store/retrieve the chosen layout inside the arguments Bundle.
        const val LAYOUT_TYPE = "type"

        // The recommended way to create a new LayoutDemoFragment — never call the constructor
        // directly and set fields on it afterwards, because those fields wouldn't survive Android
        // recreating the Fragment later. Packing everything into `arguments` here is what makes
        // that recreation possible.
        fun newInstance(layout: Int): Fragment {
            val fragment: Fragment = LayoutDemoFragment()
            val bundle = Bundle()
            bundle.putInt(LAYOUT_TYPE, layout)
            fragment.arguments = bundle
            return fragment
        }
    }
}
