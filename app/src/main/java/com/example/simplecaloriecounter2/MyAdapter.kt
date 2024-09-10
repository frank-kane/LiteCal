package com.example.simplecaloriecounter2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyAdapter(
    private val dataset: MutableList<Item>,
    private val onTotalUpdated: () -> Unit  // Callback to update total
) : RecyclerView.Adapter<MyAdapter.MyViewHolder>() {

    // Variable to track the position of the last added item
    var lastAddedPosition = -1

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.itemNameTextView)
        val totalTextView: TextView = view.findViewById(R.id.itemTotalTextView)
        val deleteButton: Button = view.findViewById(R.id.deleteItemButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val currentItem = dataset[position]
        holder.nameTextView.text = currentItem.name
        holder.totalTextView.text = currentItem.total.toString()

        // Only animate the newly added item
        if (position > lastAddedPosition) {
            holder.itemView.translationX = holder.itemView.width.toFloat()
            holder.itemView.alpha = 0f
            holder.itemView.animate()
                .translationX(0f)
                .alpha(1f)
                .setDuration(500)
                .start()

            // Update lastAddedPosition to prevent the animation from replaying
            lastAddedPosition = position
        }

        // Handle delete button click event
        holder.deleteButton.setOnClickListener {
            dataset.removeAt(position)
            notifyItemRemoved(position)
            notifyItemRangeChanged(position, dataset.size)

            // Call the callback function to update the total
            onTotalUpdated()
        }
    }

    override fun getItemCount() = dataset.size
}
