package com.example.android_app

import android.Manifest // Handles camera and audio permissions
import android.content.pm.PackageManager // To check permission status
import android.os.Build // To handle version-specific functionality
import android.os.Bundle // Used for managing activity state
import androidx.appcompat.app.AppCompatActivity // Base class for activities
import androidx.camera.core.ImageCapture // For image capture functionality
import androidx.camera.video.Recorder // Handles video recording setup
import androidx.camera.video.Recording // Manages ongoing video recordings
import androidx.camera.video.VideoCapture // Video capture functionality
import androidx.core.content.ContextCompat // To check and manage permissions
import com.example.android_app.databinding.ActivityMainBinding
import java.util.concurrent.ExecutorService // For background threading
import java.util.concurrent.Executors // Creates thread pools

// Custom alias for a lambda function that listens to image luminance
typealias LumaListener = (luma: Double) -> Unit

class MainActivity : AppCompatActivity() {
    private lateinit var viewBinding: ActivityMainBinding // ViewBinding for accessing views

    private var imageCapture: ImageCapture? = null // Image capture instance
    private var videoCapture: VideoCapture<Recorder>? = null // Video capture instance
    private var recording: Recording? = null // Current video recording instance

    private lateinit var cameraExecutor: ExecutorService // Executor for background tasks

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

        // Set click listeners for buttons
        viewBinding.imageCaptureButton.setOnClickListener { takePhoto() } // Take photo on button click
        viewBinding.videoCaptureButton.setOnClickListener { captureVideo() } // Start/stop video capture on button click

        cameraExecutor = Executors.newSingleThreadExecutor() // Initialize a background thread executor
    }

    // Placeholder function to capture a photo
    private fun takePhoto() {}

    // Placeholder function to capture/start video recording
    private fun captureVideo() {}

    // Placeholder function to start CameraX
    private fun startCamera() {}

    // Placeholder function to request permissions
    private fun requestPermissions() {}

    // Helper function to check if all required permissions are granted
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    // Clean up resources when the activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown() // Shut down the background thread executor
    }

    companion object {
        private const val TAG = "CameraXApp" // Tag for logging
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS" // Timestamp format for file names

        // Required permissions for camera and audio
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA, // Camera permission
                Manifest.permission.RECORD_AUDIO // Audio permission for video recording
            ).apply {
                // Add WRITE_EXTERNAL_STORAGE permission for devices running Android P or lower
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}
