package com.example.android_app

import ShoppingListRepo
import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.android_app.api.GeminiService
import com.example.android_app.api.SpoonacularService
import com.example.android_app.databinding.ActivityMainBinding
import com.example.android_app.ml.ImageClassifier
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.google.firebase.firestore.FirebaseFirestore
import android.content.Intent // Used to switch between activities (screens)
import android.view.Menu // Required to create the options menu
import android.view.MenuItem // Handles menu item selection
import com.example.android_app.ui.SavedRecipeActivity
import com.example.android_app.ui.ShoppingListActivity
import SavedRecipesRepo
import android.graphics.Typeface
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {


    private lateinit var viewBinding: ActivityMainBinding
    private var imageCapture: ImageCapture? = null
    private lateinit var cameraExecutor: ExecutorService

    private lateinit var resultLayout: LinearLayout
    private lateinit var resultTextView: TextView
    private lateinit var retakeButton: Button

    private lateinit var modelSelector: Spinner  // Dropdown for selecting model
    private var selectedModel: String = "My Model"  // Default selection

    // Login overlay views
    private lateinit var loginOverlay: View
    private lateinit var loginUsername: EditText
    private lateinit var loginPassword: EditText
    private lateinit var loginButton: Button
    private lateinit var signupButton: Button
    private lateinit var logoutButton: Button



    // Our TFLite ImageClassifier
    private lateinit var classifier: ImageClassifier

    // Request camera permissions at runtime
    private val activityResultLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val permissionGranted = permissions.entries.all { it.value }
            if (!permissionGranted) {
                Toast.makeText(baseContext, "Camera permission denied", Toast.LENGTH_SHORT).show()
            } else {
                startCamera()
            }
        }

    private lateinit var manualIngredientInput: EditText
    private lateinit var addIngredientButton: Button
    private lateinit var currentIngredientsText: TextView
    private lateinit var searchRecipesButton: Button
    private lateinit var clearIngredientsButton: Button
    private lateinit var saveButton: Button

    private lateinit var dietSpinner: Spinner
    private lateinit var intoleranceSpinner: Spinner

    private lateinit var recommendRecipe: Button

    // List to store multiple ingredients
    private val ingredientsList = mutableListOf<String>()

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    // at least one digit, at least one lowercase, at least one uppercase, at least one special character, no whitespace, at least 8 characters
    private val PASSWORD_REGEX = Regex(
        "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate layout
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        supportActionBar?.hide()

        // Assign layout references for camera/recipe functionality
        resultLayout = findViewById(R.id.resultLayout)
        resultTextView = findViewById(R.id.resultTextView)
        retakeButton = findViewById(R.id.retakeButton)
        modelSelector = findViewById(R.id.model_selector)  // Spinner


        // Hide the result layout initially
        resultLayout.visibility = View.GONE

        // Assign login overlay views (make sure these IDs exist in your XML)
        loginOverlay = findViewById(R.id.login_overlay)
        loginUsername = findViewById(R.id.login_username)
        loginPassword = findViewById(R.id.login_password)
        loginButton = findViewById(R.id.login_button)
        signupButton = findViewById(R.id.signup_button)
        logoutButton = findViewById(R.id.logoutButton)

        manualIngredientInput = findViewById(R.id.manualIngredientInput)
        addIngredientButton = findViewById(R.id.addIngredientButton)
        currentIngredientsText = findViewById(R.id.currentIngredientsText)
        searchRecipesButton = findViewById(R.id.searchRecipesButton)

        dietSpinner = findViewById(R.id.dietSpinner)
        intoleranceSpinner = findViewById(R.id.intoleranceSpinner)

        saveButton = findViewById(R.id.buttonSaveRecipe)
        saveButton.visibility = View.GONE

        recommendRecipe = findViewById(R.id.recommendRecipe)

        clearIngredientsButton = findViewById(R.id.clearIngredientsButton)

        // Set click listener for login button
        loginButton.setOnClickListener {
            val email = loginUsername.text.toString().trim()
            val password = loginPassword.text.toString().trim()
            if (email.isNotEmpty() && password.isNotEmpty()) {
                signIn(email, password)
            } else {
                Toast.makeText(this, "Please enter both fields", Toast.LENGTH_SHORT).show()
            }
        }

        signupButton.setOnClickListener {
            val email = loginUsername.text.toString().trim()
            val pass  = loginPassword.text.toString().trim()
            if (email.isNotBlank() && pass.isNotBlank()) {
                when {
                    email.isBlank() || pass.isBlank() -> {
                        Toast.makeText(this, "Enter both email and password", Toast.LENGTH_SHORT)
                            .show()
                    }

                    pass.length < 6 -> {
                        // Firebase’s minimum is 6, but you can enforce 8 (or whatever) yourself
                        Toast.makeText(
                            this,
                            "Password must be at least 8 characters",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    !PASSWORD_REGEX.matches(pass) -> {
                        Toast.makeText(
                            this,
                            "Password must have uppercase, lowercase, digit, and special character",
                            Toast.LENGTH_LONG
                        ).show()
                    }

                    else -> {
                        signUp(email, pass)
                    }
                }

            }
        }

        logoutButton.setOnClickListener {
            performLogout()
        }

        // Create our classifier (loads TFLite model)
        classifier = ImageClassifier(this)

        // Check permissions and start camera if granted
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        recommendRecipe.setOnClickListener {
            fetchPreferencesAndRecommend()
        }

        // Handle model selection from Spinner
        modelSelector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedModel = parent?.getItemAtPosition(position).toString()
                Log.d(TAG, "Selected Model: $selectedModel")
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedModel = "My Model" // Default
            }
        }

        // When "Take Photo" button is clicked
        viewBinding.imageCaptureButton.setOnClickListener {
            takePhoto()
        }

        // If the user wants to retake the photo
        retakeButton.setOnClickListener {
            retakePhoto()
        }

        // Set click listener for add ingredient button
        addIngredientButton.setOnClickListener {
            val ingredient = manualIngredientInput.text.toString().trim()
            if (ingredient.isNotEmpty()) {
                addIngredient(ingredient)
                manualIngredientInput.text.clear()
            } else {
                Toast.makeText(this, "Please enter an ingredient", Toast.LENGTH_SHORT).show()
            }
        }

        // Set click listener for search recipes button
        searchRecipesButton.setOnClickListener {
            if (ingredientsList.isNotEmpty()) {
                fetchRecipesForIngredients()
            } else {
                Toast.makeText(this, "Please add at least one ingredient", Toast.LENGTH_SHORT).show()
            }
        }

        // Add click listener for clear button
        clearIngredientsButton.setOnClickListener {
            ingredientsList.clear()
            updateIngredientsDisplay()
            Toast.makeText(this, "All ingredients cleared", Toast.LENGTH_SHORT).show()
        }

        // Background executor for CameraX
        cameraExecutor = Executors.newSingleThreadExecutor()
    }




    // Request camera permission if not granted
    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    // Start the camera preview
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (e: Exception) {
                Log.e(TAG, "startCamera: Use case binding failed.", e)
            }
        }, ContextCompat.getMainExecutor(this))
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            enterApp()          // already logged in
        } else {
            loginOverlay.visibility = View.VISIBLE
            supportActionBar?.hide()
            logoutButton.visibility = View.GONE
        }
    }


    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    enterApp()             // hide overlay, show UI
                } else {
                    Toast.makeText(this,
                        task.exception?.localizedMessage ?: "Login failed",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun signUp(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {

                    enterApp()
                } else {
                    Toast.makeText(this,
                        task.exception?.localizedMessage ?: "Sign-up failed",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun enterApp() {
        loginOverlay.visibility = View.GONE
        supportActionBar?.show()
        logoutButton.visibility = View.VISIBLE
        recommendRecipe.visibility = View.VISIBLE

    }

    private fun performLogout() {
        auth.signOut()
        loginOverlay.visibility = View.VISIBLE
        supportActionBar?.hide()
        logoutButton.visibility = View.GONE
    }





    // Capture a photo
    private fun takePhoto() {
        val imageCapture = imageCapture ?: run {
            Log.e(TAG, "ImageCapture not ready.")
            return
        }

        // Save to MediaStore (gallery)
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues()
        ).build()

        // Actually take the picture
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                @SuppressLint("SetTextI18n")
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri: Uri? = output.savedUri
                    Log.d(TAG, "onImageSaved() called with URI: $savedUri")
                    if (savedUri != null) {
                        Log.d(TAG, "Image saved at: $savedUri")
                        val bitmap = loadBitmapFromUri(savedUri)
                        runOnUiThread { // Ensure model check runs on main thread safely
                            if (selectedModel == "Gemini") {
                                Log.d(TAG, "Using Gemini model for classification")
                                identifyIngredientWithGemini(bitmap) // Call new Gemini function
                            } else { // Default to "My Model"
                                Log.d(TAG, "Using local TF Lite model for classification")
                                classifyAndAddIngredient(bitmap) // Call existing TF Lite function
                            }
                        }

                    }
                }
            }
        )
    }

    private fun identifyIngredientWithGemini(bitmap: Bitmap) {
        Log.d(TAG, "Sending image to Gemini...")
        Toast.makeText(this, "Identifying with Gemini...", Toast.LENGTH_SHORT).show() // User feedback

        GeminiService.recognizeImageWithGemini(bitmap) { ingredientResult ->
            // Ensure UI updates happen on the main thread
            runOnUiThread {
                Log.d(TAG, "Received from Gemini: $ingredientResult")

                // Handle the result
                when (ingredientResult) {
                    "not recognised" -> {
                        Toast.makeText(this, "Gemini could not recognise an ingredient.", Toast.LENGTH_SHORT).show()
                        retakePhoto() // Allow user to try again
                    }
                    "blocked" -> {
                        Toast.makeText(this, "Request blocked by Gemini safety filters.", Toast.LENGTH_LONG).show()
                        retakePhoto() // Allow user to try again
                    }
                    else -> {
                        // Add the ingredient
                        Toast.makeText(this, "Gemini detected: $ingredientResult", Toast.LENGTH_SHORT).show()
                        addIngredient(ingredientResult)

                        // Hide camera, show ingredients list & prompt
                        viewBinding.viewFinder.visibility = View.GONE
                        viewBinding.imageCaptureButton.visibility = View.GONE
                        promptForNextActionAfterDetection(ingredientResult) // Call the refactored prompt
                    }
                }
            }
        }
    }



    // Load the captured image from Uri and return a Bitmap
    private fun loadBitmapFromUri(uri: Uri): Bitmap {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(contentResolver, uri)
            ImageDecoder.decodeBitmap(source) {
                    decoder, _, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = true
            }
        } else {
            // Fallback for older devices
            val inputStream = contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream)!!
        }
    }
    
    @SuppressLint("SetTextI18n")
    private fun fetchRecipeDetails(recipeId: Int) {

        lifecycleScope.launch {
            val recipeDetails = SpoonacularService.getRecipeDetails(recipeId)


            runOnUiThread {
                if (recipeDetails != null) {
                    // Create a ScrollView programmatically for the recipe details
                    val container = findViewById<LinearLayout>(R.id.recipeButtonContainer)
                    container.removeAllViews() // Clear previous content

                    // Create formatted content view
                    val detailsView = LinearLayout(this@MainActivity).apply {
                        orientation = LinearLayout.VERTICAL
                        layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                        )
                        setPadding(16, 16, 16, 16)
                    }

                    // Title
                    val titleView = TextView(this@MainActivity).apply {
                        text = recipeDetails.title
                        textSize = 24f
                        setTextColor(ContextCompat.getColor(this@MainActivity, android.R.color.black))
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                        setPadding(0, 0, 0, 16)
                    }
                    detailsView.addView(titleView)

                    if (!recipeDetails.extendedIngredients.isNullOrEmpty()) {
                        // Add Ingredients Header
                        val ingredientsHeader = TextView(this@MainActivity).apply {
                            text = "Ingredients:"
                            textSize = 18f
                            setTextColor(ContextCompat.getColor(this@MainActivity, android.R.color.black))
                            typeface = android.graphics.Typeface.DEFAULT_BOLD
                            setPadding(0, 16, 0, 8) // Add spacing
                        }
                        detailsView.addView(ingredientsHeader) // Add header to the layout

                        // Loop through each ingredient and add a TextView for it
                        recipeDetails.extendedIngredients.forEach { ingredient ->
                            val ingredientView = TextView(this@MainActivity).apply {
                                // Use the 'original' string from the API for full description
                                text = "• ${ingredient.original ?: ingredient.name ?: "Unknown Ingredient"}" // Add bullet point
                                textSize = 16f
                                setTextColor(ContextCompat.getColor(this@MainActivity, android.R.color.black))
                                setPadding(8, 4, 0, 4) // Indent ingredients slightly
                            }
                            detailsView.addView(ingredientView) // Add ingredient TextView to the layout
                        }
                    } else {
                        // Display a message if no ingredients are available
                        val noIngredientsView = TextView(this@MainActivity).apply {
                            text = "(No ingredient details provided)"
                            textSize = 14f
                            setTypeface(null, Typeface.ITALIC)
                            setTextColor(ContextCompat.getColor(this@MainActivity, android.R.color.darker_gray))
                            setPadding(0, 16, 0, 8)
                        }
                        detailsView.addView(noIngredientsView)
                    }

                    // Instructions header
                    val instructionsHeader = TextView(this@MainActivity).apply {
                        text = "Instructions:"
                        textSize = 18f
                        setTextColor(ContextCompat.getColor(this@MainActivity, android.R.color.black))
                        typeface = android.graphics.Typeface.DEFAULT_BOLD
                        setPadding(0, 16, 0, 8)
                    }
                    detailsView.addView(instructionsHeader)

                    // Instructions
                    val instructionsView = TextView(this@MainActivity).apply {
                        text = recipeDetails.instructions
                        textSize = 16f
                        setTextColor(ContextCompat.getColor(this@MainActivity, android.R.color.black))
                        setPadding(0, 8, 0, 16)
                    }
                    detailsView.addView(instructionsView)

                    if (!recipeDetails.nutrition?.nutrients.isNullOrEmpty()) {
                        val nutritionHeader = TextView(this@MainActivity).apply {
                            text = "Nutrition per serving:"
                            textSize = 18f
                            setTypeface(null, Typeface.NORMAL)
                            setPadding(0, 16, 0, 8)
                        }
                        detailsView.addView(nutritionHeader)


                        recipeDetails.nutrition!!.nutrients.forEach { n ->
                            val tv = TextView(this@MainActivity).apply {
                                text = "• ${n.name}: ${"%.1f".format(n.amount)} ${n.unit}"
                                textSize = 16f
                                setPadding(8, 4, 0, 4)
                            }
                            detailsView.addView(tv)
                        }
                    }




                    container.addView(detailsView)

                    saveButton.visibility = View.VISIBLE

                    saveButton.setOnClickListener {
                        val userId = auth.currentUser!!.uid

                        SavedRecipesRepo().saveRecipeId(userId, recipeDetails.id) { success ->
                            runOnUiThread {
                                if (success) {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Recipe saved!",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    // navigate to saved list
                                    startActivity(Intent(this@MainActivity, SavedRecipeActivity::class.java))
                                } else {
                                    Toast.makeText(
                                        this@MainActivity,
                                        "Couldn’t save recipe.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        }
                    }


                    // Update the result text to just show the heading
                    resultTextView.text = "Recipe Details"

                    // Show the result layout with scroll capability
                    resultLayout.visibility = View.VISIBLE
                } else {
                    resultTextView.text = "Failed to load recipe details."
                    resultLayout.visibility = View.VISIBLE
                }
            }
        }
    }


    private fun addIngredient(ingredient: String) {
        ingredientsList.add(ingredient)
        updateIngredientsDisplay()
        addPreference(ingredient)

        // Show the search button once we have at least one ingredient
        if (ingredientsList.size == 1) {
            searchRecipesButton.visibility = View.VISIBLE
        }
    }

    private fun addPreference(ingredient: String) {
        val uid = auth.currentUser!!.uid

        val prefRef = FirebaseFirestore
            .getInstance()
            .collection("users")
            .document(uid)
            .collection("preferences")
            .document(ingredient)

        // Atomically increment count + set updatedAt
        prefRef.set(mapOf(
            "count"     to FieldValue.increment(1),
            "updatedAt" to FieldValue.serverTimestamp()
        ), SetOptions.merge())
    }

    private fun fetchPreferencesAndRecommend() {
        val uid = auth.currentUser!!.uid
        val prefsRef = FirebaseFirestore
            .getInstance()
            .collection("users")
            .document(uid)
            .collection("preferences")

        // 30-day cutoff
        val cutoff = Date(System.currentTimeMillis() - 30L*24*60*60*1000)

        prefsRef
            .whereGreaterThan("updatedAt", cutoff)
            .get()
            .addOnSuccessListener { snap ->
                val items = snap.documents.mapNotNull { doc ->
                    val name  = doc.id
                    val count = (doc.getLong("count") ?: 0L).toInt()
                    name to count
                }
                if (items.isEmpty()) {
                    Toast.makeText(this, "No recent ingredients to recommend from", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // Build weighted list
                val weighted = items.flatMap { (name, count) -> List(count) { name } }

                // Pick 3–10 unique ingredients
                val pickCount = (3..10).random().coerceAtMost(weighted.size)
                val selected = mutableSetOf<String>()
                while (selected.size < pickCount) {
                    selected += weighted.random()
                }

                ingredientsList.clear()
                ingredientsList.addAll(selected)

                updateIngredientsDisplay()

                fetchRecipesForIngredients()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load preferences", Toast.LENGTH_SHORT).show()
            }
    }


    @SuppressLint("SetTextI18n")
    private fun updateIngredientsDisplay() {
        if (ingredientsList.isEmpty()) {
            currentIngredientsText.visibility = View.GONE
            searchRecipesButton.visibility = View.GONE
            clearIngredientsButton.visibility = View.GONE
        } else {
            currentIngredientsText.text = "Current ingredients: ${ingredientsList.joinToString(", ")}"
            currentIngredientsText.visibility = View.VISIBLE
            searchRecipesButton.visibility = View.VISIBLE
            clearIngredientsButton.visibility = View.VISIBLE
        }
    }
    @SuppressLint("SetTextI18n")
    private fun classifyAndAddIngredient(bitmap: Bitmap) {
        val (ingredient, confidence) = classifier.classify(bitmap)
        Log.d(TAG, "Detected ingredient: $ingredient (confidence=${"%.1f".format(confidence * 100)}%)")

        runOnUiThread { // Ensure UI updates are on the main thread
            if (confidence >= 0.6f && ingredient != "Not Food" && ingredient.isNotEmpty()) {
                Toast.makeText(this, "Detected: $ingredient", Toast.LENGTH_SHORT).show()
                addIngredient(ingredient)

                // Hide the camera view, show the ingredients list
                viewBinding.viewFinder.visibility = View.GONE
                viewBinding.imageCaptureButton.visibility = View.GONE

                // Call the common prompt function
                promptForNextActionAfterDetection(ingredient)
            } else {
                Toast.makeText(this, "Could not recognise an ingredient.", Toast.LENGTH_SHORT).show()
                retakePhoto()
            }
        }
    }

    private fun promptForNextActionAfterDetection(detectedIngredient: String) {
        // Ensure this runs on the main thread if called from a background callback
        runOnUiThread {
            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            builder.setTitle("Ingredient Added: $detectedIngredient")
                .setMessage("Add more ingredients or search recipes?")
                .setPositiveButton("Add more") { _, _ ->
                    retakePhoto() // Go back to camera view
                }
                .setNegativeButton("Search recipes") { _, _ ->
                    fetchRecipesForIngredients() // Search with current list
                }
                .setCancelable(false) // Prevent dismissing without choosing
                .show()
        }
    }

    private fun fetchRecipesForIngredients() {
        // Join all ingredients with a comma for the API
        val ingredientsString = ingredientsList.joinToString(",")

        val diet = dietSpinner.selectedItem.toString()
        val intol = intoleranceSpinner.selectedItem.toString()

        val dietParam = if (diet == "Any") null
            else diet.lowercase(Locale.ROOT)
        val intolerancesParam = if (intol == "None") null
            else intol.lowercase(Locale.ROOT)

        lifecycleScope.launch {
            val recipes = SpoonacularService.getRecipes(
                ingredients   = ingredientsString,
                diet          = dietParam,
                intolerances  = intolerancesParam
            )
            Log.d(TAG, "Fetched ${recipes.size} recipes for ingredients: $ingredientsString")

            runOnUiThread {
                val container = findViewById<LinearLayout>(R.id.recipeButtonContainer)
                val resultTextView = findViewById<TextView>(R.id.resultTextView)

                container.removeAllViews() // Clear previous results

                if (recipes.isNotEmpty()) {
                    resultTextView.text = "Ingredients: ${ingredientsList.joinToString(", ")}\n\nSelect a recipe:"
                    resultTextView.visibility = View.VISIBLE

                    recipes.forEach { recipe ->
                        Log.d(TAG, "Adding button for: ${recipe.title}")

                        val button = Button(this@MainActivity).apply {
                            text = recipe.title
                            setBackgroundColor(ContextCompat.getColor(this@MainActivity, R.color.primary_blue))
                            setTextColor(ContextCompat.getColor(this@MainActivity, R.color.white))
                            setOnClickListener { fetchRecipeDetails(recipe.id) }
                        }

                        container.addView(button)
                    }

                    resultLayout.visibility = View.VISIBLE
                    container.visibility = View.VISIBLE
                } else {
                    Log.d(TAG, "No recipes found.")

                    resultTextView.text = "No recipes found for ${ingredientsList.joinToString(", ")}"
                    resultTextView.visibility = View.VISIBLE

                    resultLayout.visibility = View.VISIBLE
                    container.visibility = View.VISIBLE
                }

                // Hide the camera preview
                viewBinding.viewFinder.visibility = View.GONE
                viewBinding.imageCaptureButton.visibility = View.GONE
            }
        }
    }


    // Called when user taps "Retake Photo" button
    private fun retakePhoto() {
        // Hide result layout, show camera preview
        resultLayout.visibility = View.GONE
        viewBinding.viewFinder.visibility = View.VISIBLE
        viewBinding.imageCaptureButton.visibility = View.VISIBLE
        saveButton.visibility = View.GONE
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "CameraXApp"
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }

    // Inflate the menu (loads res/menu/main_menu.xml)
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu) // Load the menu from XML
        menu?.findItem(R.id.nav_camera)?.isVisible = false
        return true // Return true to display the menu
    }

    // Handle menu item clicks
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            // When "Shopping List" is selected, open ShoppingListActivity
            R.id.nav_shopping_list -> {
                val intent = Intent(this, ShoppingListActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.nav_saved_recipes -> {
                startActivity(Intent(this, SavedRecipeActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item) // Handle other menu items normally
        }
    }
}
