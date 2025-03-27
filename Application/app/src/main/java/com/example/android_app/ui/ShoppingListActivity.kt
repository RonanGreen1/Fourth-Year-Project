package com.example.android_app.ui

import ShoppingListRepo
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android_app.MainActivity
import com.example.android_app.R


class ShoppingListActivity : AppCompatActivity() {

    private lateinit var repo: ShoppingListRepo
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ShoppingListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_shopping_list)

        // Creates a repository instance for retrieving shopping list data
        repo = ShoppingListRepo()

        // Initializes the RecyclerView and attaches a linear layout manager
        recyclerView = findViewById(R.id.rvShoppingList)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Creates an adapter with an empty list at first, then assigns it to the RecyclerView
        adapter = ShoppingListAdapter(emptyList())
        recyclerView.adapter = adapter

        // Retrieves the user ID from the Intent; falls back to "testuser" if none is passed
        val userId = intent.getStringExtra("USER_ID_KEY") ?: "testuser"

        // Calls the repository to fetch data from Firestore, then updates the adapter
        repo.getShoppingList(
            userId,
            onSuccess = { items ->
                adapter.updateItems(items)
            },
            onFailure = { exception ->
                exception.printStackTrace()
            }
        )
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    // Handles menu item clicks; if the user selects "Camera," navigate accordingly
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



