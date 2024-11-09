package com.example.android_app

import android.Manifest // Handles camera permissions
import android.content.pm.PackageManager // To check permission status
import android.os.Build // To handle version-specific functionality
import android.os.Bundle // Used for managing activity state
import android.util.Log // For logging
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity // Base class for activities
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture // For image capture functionality
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat // To check and manage permissions
import com.example.android_app.databinding.ActivityMainBinding
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.ExecutorService // For background threading
import java.util.concurrent.Executors // Creates thread pools

class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding // ViewBinding for accessing views

    private var imageCapture: ImageCapture? = null // Image capture instance

    private lateinit var cameraExecutor: ExecutorService // Executor for background tasks

    // ActivityResultLauncher for handling permission requests
    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value) {
                    permissionGranted = false
                }
            }
            if (!permissionGranted) {
                Toast.makeText(
                    baseContext,
                    "Permission request denied",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                startCamera()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewBinding = ActivityMainBinding.inflate(layoutInflater) // Inflate layout using ViewBinding
        setContentView(viewBinding.root) // Set the activity content to the inflated layout

        // Check and request camera permissions
        if (allPermissionsGranted()) {
            startCamera() // Initialize CameraX if permissions are granted
        } else {
            requestPermissions() // Request required permissions
        }

        // Set click listener for the photo capture button
        viewBinding.imageCaptureButton.setOnClickListener { takePhoto() } // Take photo on button click

        cameraExecutor = Executors.newSingleThreadExecutor() // Initialize a background thread executor
    }

    // Method to request permissions
    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    // Helper function to check if all required permissions are granted
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Implement the method to start CameraX
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder().build()

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    // Implement the method to capture a photo
    private fun takePhoto() {
        // Get a stable reference of the modifiable image capture use case
        val imageCapture = imageCapture ?: return

        // Create time-stamped output file to hold the image
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.US)
            .format(System.currentTimeMillis())
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            contentResolver,
            android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            android.content.ContentValues().apply {
                put(android.provider.MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(android.provider.MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            }
        ).build()

        // Set up image capture listener, which is triggered after photo has been taken
        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
                    Log.d(TAG, msg)
                }
            }
        )
    }

    // Clean up resources when the activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown() // Shut down the background thread executor
    }

    companion object {
        private const val TAG = "CameraXApp" // Tag for logging
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS" // Timestamp format for file names

        // Required permissions for camera
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA // Camera permission
            ).apply {
                // Add WRITE_EXTERNAL_STORAGE permission for devices running Android P or lower
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}
