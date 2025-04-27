package com.example.android_app.ui


import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.android_app.R
import com.example.android_app.api.SpoonacularService
import SavedRecipesRepo // Use the correct repo
import android.annotation.SuppressLint
import android.content.Intent
import android.view.Menu
import android.view.MenuItem
import com.example.android_app.MainActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class SavedRecipeActivity : AppCompatActivity() {

    // Main container where buttons or details will be shown
    private lateinit var container: LinearLayout

    private val repo = SavedRecipesRepo()
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener
    private var currentUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_saved_recipes)

        container = findViewById(R.id.savedRecipesContainer)

        firebaseAuth = FirebaseAuth.getInstance()
        setupAuthStateListener()
        supportActionBar?.title = "Saved Recipes"
    }



    override fun onStart() { super.onStart(); firebaseAuth.addAuthStateListener(authStateListener); Log.d("SavedRecipeActivity", "AuthStateListener added.") }
    override fun onStop() { super.onStop(); firebaseAuth.removeAuthStateListener(authStateListener); Log.d("SavedRecipeActivity", "AuthStateListener removed.") }

    private fun setupAuthStateListener() {
        authStateListener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            if (user == null) {
                Log.w("SavedRecipeActivity", "User is signed out. Closing SavedRecipeActivity.")
                finish()
                return@AuthStateListener
            }

            val userIdToUse = user.uid
            Log.d("SavedRecipeActivity", "AuthStateListener: Signed in as $userIdToUse")

            // Only reload the list when UID actually changes
            if (currentUserId != userIdToUse) {
                currentUserId = userIdToUse
                Log.d("SavedRecipeActivity", "Loading saved recipes for user $userIdToUse")
                loadInitialRecipeList(userIdToUse)
            } else {
                Log.d("SavedRecipeActivity", "UID unchanged; no need to reload.")
            }
        }
    }


    // Fetches ONLY the saved recipe IDs and then triggers fetching
    // the necessary info (title) to display the initial list of buttons.

    private fun loadInitialRecipeList(userId: String) {
        showRecipeListView()
        container.removeAllViews() // Clear previous buttons/details
        Log.i("SavedRecipe_List", "Attempting to load initial recipe list for userId: $userId")

        repo.getSavedRecipeIds(userId,
            onSuccess = { ids ->
                Log.i("SavedRecipe_List", "Received ${ids.size} recipe IDs: $ids")
                if (ids.isEmpty()) {
                    showEmptyState() // Show empty message in the button container
                } else {
                    Log.i("SavedRecipe_List", "Starting loop to fetch titles/buttons for IDs.")
                    ids.forEach { id ->
                        fetchAndDisplayRecipeButton(id) // Fetch data needed for the button
                    }
                }
            },
            onFailure = { e ->
                Log.e("SavedRecipe_List", "Failed to get recipe IDs from Firestore for user $userId", e)
                showErrorState("Could not load saved recipe list.") // Show error in button container
            }
        )
    }


    // Fetches just enough info for a recipe ID to display a button.
    // Calls SpoonacularService.getRecipeDetails but only uses the title.

    private fun fetchAndDisplayRecipeButton(recipeId: Long) {
        Log.d("SavedRecipe_List", "--> Fetching title for button for ID: $recipeId")
        lifecycleScope.launch {
            val details = try {
                SpoonacularService.getRecipeDetails(recipeId.toInt())
            } catch (e: Exception) {
                Log.e("SavedRecipe_List", "--> Exception during Spoonacular API call for ID $recipeId", e)
                null
            }

            runOnUiThread {
                if (details != null) {
                    Log.d("SavedRecipe_List", "--> Title '${details.title}' received for ID: $recipeId. Creating button.")
                    // Create the button using the fetched details
                    val button = makeRecipeButton(details.id, details.title)
                    container.addView(button)
                } else {
                    Log.w("SavedRecipe_List", "--> Spoonacular returned NULL details for ID: $recipeId when fetching for button. Button not created.")
                }
            }
        }
    }


    // Creates a Button view for the initial recipe list.
    // Sets an OnClickListener to fetch and display full details when clicked.

    private fun makeRecipeButton(recipeApiId: Int, title: String): Button {
        return Button(this).apply {
            text = title
            // Style the button (e.g., blue background, white text)
            setBackgroundColor(ContextCompat.getColor(this@SavedRecipeActivity, R.color.primary_blue)) // Make sure primary_blue is defined in your colors.xml
            setTextColor(ContextCompat.getColor(this@SavedRecipeActivity, R.color.white)) // Make sure white is defined
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, // Make buttons fill width
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).also {
                it.setMargins(0, 8, 0, 8) // Add spacing between buttons
            }
            // Store the ID in the tag for easy retrieval on click
            tag = recipeApiId

            setOnClickListener {
                Log.d("SavedRecipe_Click", "Recipe button clicked for ID: $recipeApiId, Title: '$title'")
                // Fetch and display the full details when this button is clicked
                displayFullRecipeDetails(recipeApiId)
            }
        }
    }


     // Fetches the FULL details for a specific recipe ID and displays them,
     // replacing the initial button list.

    @SuppressLint("SetTextI18n")
    private fun displayFullRecipeDetails(recipeId: Int) {
        showRecipeDetailView()
        container.removeAllViews()
        Log.i("SavedRecipe_Detail", "Fetching full details for recipe ID: $recipeId")
        val loadingTv = TextView(this).apply { text = "Loading details..." }
        container.addView(loadingTv)


        lifecycleScope.launch {
            val recipeDetails = try {
                SpoonacularService.getRecipeDetails(recipeId)
            } catch (e: Exception) {
                Log.e("SavedRecipe_Detail", "Exception fetching full details for ID $recipeId", e)
                null
            }

            runOnUiThread {
                container.removeView(loadingTv) // Remove loading indicator

                if (recipeDetails != null) {
                    Log.i("SavedRecipe_Detail", "Full details fetched for '${recipeDetails.title}'. Displaying.")
                    // Create the detailed view (similar to MainActivity's fetchRecipeDetails)
                    val detailsViewLayout = LinearLayout(this@SavedRecipeActivity).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        setPadding(16, 16, 16, 16)
                    }

                    // Add Title
                    detailsViewLayout.addView(TextView(this@SavedRecipeActivity).apply {
                        text = recipeDetails.title
                        textSize = 24f
                        setTypeface(null, Typeface.BOLD)
                        setPadding(0, 0, 0, 16)
                    })

                    if (!recipeDetails.extendedIngredients.isNullOrEmpty()) {
                        detailsViewLayout.addView(TextView(this@SavedRecipeActivity).apply {
                            text = "Ingredients:"
                            textSize = 18f
                            setTypeface(null, Typeface.BOLD)
                            setPadding(0, 16, 0, 8)
                        })

                        recipeDetails.extendedIngredients.forEach { ingredient ->
                            detailsViewLayout.addView(TextView(this@SavedRecipeActivity).apply {
                                text = "• ${ingredient.original
                                    ?: ingredient.name
                                    ?: "Unknown Ingredient"}"
                                textSize = 16f
                                setPadding(8, 4, 0, 4)
                            })
                        }
                    }

                    // Add Instructions Header
                    detailsViewLayout.addView(TextView(this@SavedRecipeActivity).apply {
                        text = "Instructions:"
                        textSize = 18f
                        setTypeface(null, Typeface.BOLD)
                        setPadding(0, 16, 0, 8)
                    })

                    // Add Instructions Text
                    detailsViewLayout.addView(TextView(this@SavedRecipeActivity).apply {
                        text = recipeDetails.instructions ?: "No instructions available." // Handle null instructions
                        textSize = 16f
                        setPadding(0, 8, 0, 16)
                    })

                    if (!recipeDetails.nutrition?.nutrients.isNullOrEmpty()) {
                        detailsViewLayout.addView(TextView(this@SavedRecipeActivity).apply {
                            text = "Nutrition per serving:"
                            textSize = 18f
                            setPadding(0, 16, 0, 8)
                        })
                        recipeDetails.nutrition!!.nutrients.forEach { n ->
                            detailsViewLayout.addView(TextView(this@SavedRecipeActivity).apply {
                                text = "• ${n.name}: ${"%.1f".format(n.amount)} ${n.unit}"
                                textSize = 16f
                                setPadding(8, 4, 0, 4)
                            })
                        }
                    }

                    // Add Back Button 
                    detailsViewLayout.addView(Button(this@SavedRecipeActivity).apply {
                        text = "Back to Saved List"
                        setOnClickListener {
                            startActivity(Intent(this@SavedRecipeActivity, SavedRecipeActivity::class.java))
                            finish()
                        }
                        // Add layout params if needed
                        layoutParams = LinearLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                        ).also { it.setMargins(0,16,0,0)}
                    })


                    // Add the composed details view to the main container
                    container.addView(detailsViewLayout)

                } else {
                    Log.e("SavedRecipe_Detail", "Failed to load full recipe details for ID: $recipeId.")
                    // Show error message in the container
                    showErrorState("Failed to load recipe details.")
                    // Add Back Button even on error
                    container.addView(Button(this@SavedRecipeActivity).apply {
                        text = "Back to Saved List"
                        setOnClickListener { currentUserId }
                        layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).also { it.setMargins(0,16,0,0)}
                    })
                }
            }
        }
    }

    private fun showRecipeListView() {

    }

    private fun showRecipeDetailView() {
        container.removeAllViews()
    }



    private fun showEmptyState() {
        container.removeAllViews()
        val tv = TextView(this).apply { text = "You haven’t saved any recipes yet."; /*...*/ }
        container.addView(tv)
    }
    private fun showErrorState(message: String) {
        container.removeAllViews()
        val tv = TextView(this).apply { text = message; /*...*/ }
        container.addView(tv)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        menu?.findItem(R.id.nav_saved_recipes)?.isVisible = false
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
                val intent = Intent(this, ShoppingListActivity::class.java)
                intent.putExtra("USER_ID_KEY", currentUserId) // Pass determined or fallback ID
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}