package com.example.android_app.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.android_app.R
import com.example.android_app.model.ShoppingItem


class ShoppingListAdapter(
    private var items: List<ShoppingItem>,
    private val listener: ShoppingListActivity
) : RecyclerView.Adapter<ShoppingListAdapter.ShoppingViewHolder>() {

    // Holds references to the views for each item
    class ShoppingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvIngredient: TextView = itemView.findViewById(R.id.tvIngredient)
        val tvQuantity: TextView = itemView.findViewById(R.id.tvQuantity)
        val chkChecked: CheckBox = itemView.findViewById(R.id.chkChecked)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btnEditItem)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btnDeleteItem)
    }

    // Inflates the layout for each shopping item row
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ShoppingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shopping, parent, false)
        return ShoppingViewHolder(view)
    }

    // Binds each ShoppingItem to its corresponding views
    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: ShoppingViewHolder, position: Int) {
        val item = items[position]
        holder.tvIngredient.text = item.ingredient
        holder.tvQuantity.text = item.quantity.toString()
        holder.chkChecked.isChecked = item.checked
        holder.btnEdit.setOnClickListener {
            // Safely get position in case item was removed quickly
            val currentPosition = holder.bindingAdapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                listener.onEditClicked(items[currentPosition], currentPosition)
            }
        }
        holder.btnDelete.setOnClickListener {
            val currentPosition = holder.bindingAdapterPosition
            if (currentPosition != RecyclerView.NO_POSITION) {
                listener.onDeleteClicked(items[currentPosition], currentPosition)
            }
        }
    }



    override fun getItemCount(): Int = items.size

    // Updates the list of items and refreshes the RecyclerView
    @SuppressLint("NotifyDataSetChanged")
    fun updateItems(newItems: List<ShoppingItem>) {
        items = newItems
        notifyDataSetChanged()
    }
}
