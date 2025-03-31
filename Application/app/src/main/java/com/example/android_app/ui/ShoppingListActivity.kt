package com.example.android_app.ui

import ShoppingListRepo
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android_app.MainActivity
import com.example.android_app.R
import com.example.android_app.model.ShoppingItem
import com.google.android.material.floatingactionbutton.FloatingActionButton


class ShoppingListActivity : AppCompatActivity() {

    private lateinit var repo: ShoppingListRepo
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ShoppingListAdapter
    private lateinit var userId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping_list)

        // Creates a repository instance for retrieving shopping list data
        repo = ShoppingListRepo()

        // Initializes the RecyclerView and attaches a linear layout manager
        recyclerView = findViewById(R.id.rvShoppingList)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Creates an adapter with an empty list at first, then assigns it to the RecyclerView
        adapter = ShoppingListAdapter(emptyList(), this)
        recyclerView.adapter = adapter

        userId = intent.getStringExtra("USER_ID_KEY") ?: "testuser"
        Log.d("ShoppingListActivity", "Initialized userId: $userId") // Add log to confirm

        fetchShoppingList()

        val fabAddItem = findViewById<FloatingActionButton>(R.id.fabAddItem)
        fabAddItem.setOnClickListener {
            // This block runs when the FAB is tapped
            showAddItemDialog(userId)  // Example call to open a dialog
        }
    }

    private fun fetchShoppingList() {
        repo.getShoppingList(
            userId,
            onSuccess = { items ->
                adapter.updateItems(items)
            },
            onFailure = { exception ->
                exception.printStackTrace()
                Toast.makeText(this, "Failed to load list", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun showAddItemDialog(userId: String) {
        // Create an AlertDialog builder
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Add Shopping Item")

        // Inflate a custom layout with input fields
        val view = layoutInflater.inflate(R.layout.dialog_add_item, null)
        val etIngredient = view.findViewById<EditText>(R.id.etIngredient)
        val etQuantity = view.findViewById<EditText>(R.id.etQuantity)

        builder.setView(view)

        //Handle "Add" button
        builder.setPositiveButton("Add") { dialog, _ ->
            val ingredientText = etIngredient.text.toString().trim()
            val quantityText = etQuantity.text.toString().trim()

            val quantityValue = quantityText.toIntOrNull() ?: 1

            // Create a new ShoppingItem
            val newItem = ShoppingItem(
                ingredient = ingredientText,
                quantity = quantityValue,
                checked = false
            )

            // Use repo.addItem to append it to Firestore
            repo.addItem(userId, newItem) { success ->
                if (success) {
                    // 5) Refresh the RecyclerView by fetching the updated list
                    repo.getShoppingList(
                        userId,
                        onSuccess = { updatedItems ->
                            adapter.updateItems(updatedItems)
                        },
                        onFailure = { e ->
                            e.printStackTrace()
                        }
                    )
                }
            }

            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    fun onEditClicked(item: ShoppingItem, position: Int) {
        showEditItemDialog(item, position) // Show edit dialog
    }

    fun onDeleteClicked(item: ShoppingItem, position: Int) {
        // Optional: Show confirmation dialog
        AlertDialog.Builder(this)
            .setTitle("Delete Item")
            .setMessage("Are you sure you want to delete '${item.ingredient}'?")
            .setPositiveButton("Delete") { dialog, _ ->
                // Call repo to delete the item
                repo.deleteItem(userId, item) { success ->
                    if (success) {
                        fetchShoppingList() // Refresh list on success
                        Toast.makeText(this, "'${item.ingredient}' deleted", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Failed to delete item", Toast.LENGTH_SHORT).show()
                    }
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel", null) // No action on cancel
            .show()
    }

    // Edit Dialog
    private fun showEditItemDialog(itemToEdit: ShoppingItem, position: Int) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Edit Shopping Item")

        // Reuse or create a similar layout to dialog_add_item
        val view = layoutInflater.inflate(R.layout.dialog_add_item, null) // Can reuse layout
        val etIngredient = view.findViewById<EditText>(R.id.etIngredient)
        val etQuantity = view.findViewById<EditText>(R.id.etQuantity)

        // Pre-fill the dialog with existing data
        etIngredient.setText(itemToEdit.ingredient)
        etQuantity.setText(itemToEdit.quantity.toString())

        builder.setView(view)

        builder.setPositiveButton("Save") { dialog, _ ->
            val updatedIngredientText = etIngredient.text.toString().trim()
            val updatedQuantityText = etQuantity.text.toString().trim()

            if (updatedIngredientText.isEmpty()) {
                Toast.makeText(this, "Ingredient name cannot be empty", Toast.LENGTH_SHORT).show()
                return@setPositiveButton // Don't proceed if empty
            }

            val updatedQuantityValue = updatedQuantityText.toIntOrNull() ?: 1 // Default or handle error

            // Create the updated item object (keeping original 'checked' state)
            val updatedItem = itemToEdit.copy(
                ingredient = updatedIngredientText,
                quantity = updatedQuantityValue
            )

            // Call repo to update the item
            repo.updateItem(userId, itemToEdit, updatedItem) { success ->
                if (success) {
                    fetchShoppingList() // Refresh list on success
                    Toast.makeText(this, "Item updated", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Failed to update item", Toast.LENGTH_SHORT).show()
                }
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
        builder.show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    // Handles menu item clicks
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_camera -> {

                val intent = Intent(this, MainActivity::class.java) // Replace with your camera activity
                startActivity(intent)
                true
            }
            R.id.nav_shopping_list -> {
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}



