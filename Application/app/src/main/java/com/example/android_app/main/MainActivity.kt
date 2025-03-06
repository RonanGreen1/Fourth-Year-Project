package com.example.android_app

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.ContentValues.TAG
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
import com.example.android_app.api.ChatGPTService
import com.example.android_app.api.SpoonacularService
import com.example.android_app.databinding.ActivityMainBinding
import com.example.android_app.ml.ImageClassifier
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.google.firebase.firestore.FirebaseFirestore

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

    // Firestore instance for login
    private val db = FirebaseFirestore.getInstance()

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate layout
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

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

        // Set click listener for login button
        loginButton.setOnClickListener {
            val username = loginUsername.text.toString().trim()
            val password = loginPassword.text.toString().trim()
            if (username.isNotEmpty() && password.isNotEmpty()) {
                performLogin(username, password)
            } else {
                Toast.makeText(this, "Please enter both fields", Toast.LENGTH_SHORT).show()
            }
        }

        // Create our classifier (loads TFLite model)
        classifier = ImageClassifier(this)

        // Check permissions and start camera if granted
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
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

    // Perform login by checking Firestore for matching username and password
    private fun performLogin(username: String, password: String) {
        db.collection("users")
            .whereEqualTo("username", username)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val doc = documents.documents[0]
                    val storedPassword = doc.getString("password")
                    if (storedPassword == password) {
                        // Login successful: hide the login overlay
                        loginOverlay.visibility = View.GONE
                        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Login error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
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

                        // Check which model is selected
                        if (selectedModel == "ChatGPT API") {
                            sendToChatGPT(bitmap)
                        } else {
                            classifyAndFetchRecipes(bitmap)
                        }
                    }
                }
            }
        )
    }

    @SuppressLint("SetTextI18n")
    private fun sendToChatGPT(bitmap: Bitmap) {
        ChatGPTService.recognizeImage(bitmap) { result ->
            runOnUiThread {
                Log.d(TAG, "ChatGPT Result: $result")
                resultTextView.text = "ChatGPT Result: $result"
                resultLayout.visibility = View.VISIBLE
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

    // Classify the image using TFLite, then fetch recipes from Spoonacular
    @SuppressLint("SetTextI18n")
    private fun classifyAndFetchRecipes(bitmap: Bitmap) {
        val ingredient = classifier.classify(bitmap)
        Log.d(TAG, "Detected ingredient: $ingredient")

        lifecycleScope.launch {
            val recipes = SpoonacularService.getRecipes(ingredient)
            Log.d(TAG, "Fetched ${recipes.size} recipes")

            runOnUiThread {
                val container = findViewById<LinearLayout>(R.id.recipeButtonContainer)
                val resultTextView = findViewById<TextView>(R.id.resultTextView)

                container.removeAllViews() // Clear previous results

                if (recipes.isNotEmpty()) {
                    resultTextView.text = "Detected: $ingredient\n\nSelect a recipe:"
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

                    resultTextView.text = "No recipes found for $ingredient"
                    resultTextView.visibility = View.VISIBLE

                    resultLayout.visibility = View.VISIBLE
                    container.visibility = View.VISIBLE
                }

                // 🔹 Ensure the camera preview is hidden
                viewBinding.viewFinder.visibility = View.GONE
                viewBinding.imageCaptureButton.visibility = View.GONE
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun fetchRecipeDetails(recipeId: Int) {
        lifecycleScope.launch {
            val recipeDetails = SpoonacularService.getRecipeDetails(recipeId)
            runOnUiThread {
                if (recipeDetails != null) {
                    resultTextView.text = "Recipe: ${recipeDetails.title}\n\nInstructions:\n${recipeDetails.instructions}"
                } else {
                    resultTextView.text = "Failed to load recipe details."
                }
                resultLayout.visibility = View.VISIBLE
            }
        }
    }




    // Called when user taps "Retake Photo" button
    private fun retakePhoto() {
        // Hide result layout, show camera preview
        resultLayout.visibility = View.GONE
        viewBinding.viewFinder.visibility = View.VISIBLE
        viewBinding.imageCaptureButton.visibility = View.VISIBLE
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
}
