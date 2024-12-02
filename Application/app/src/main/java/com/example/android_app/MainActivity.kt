package com.example.android_app

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.android_app.databinding.ActivityMainBinding
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.content.ContentValues
import android.graphics.BitmapFactory

// Main activity class that handles the camera and image classification
class MainActivity : AppCompatActivity() {

    // View binding for activity_main.xml layout
    private lateinit var viewBinding: ActivityMainBinding

    // Variable to hold the image capture use case
    private var imageCapture: ImageCapture? = null

    // Executor for running camera tasks in the background
    private lateinit var cameraExecutor: ExecutorService

    // UI elements to display the classification result
    private lateinit var resultLayout: LinearLayout
    private lateinit var resultTextView: TextView

    // Launcher for requesting permissions at runtime
    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            var permissionGranted = true
            // Check if all required permissions are granted
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value) {
                    permissionGranted = false
                }
            }
            if (!permissionGranted) {
                // If permissions are denied, show a toast message
                Toast.makeText(
                    baseContext,
                    "Permission request denied",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                // If permissions are granted, start the camera
                startCamera()
            }
        }

    // Called when the activity is first created
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout using view binding
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        // Initialize the result layout and text view from the layout
        resultLayout = findViewById(R.id.resultLayout)
        resultTextView = findViewById(R.id.resultTextView)

        // Check for camera permissions and start the camera
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestPermissions()
        }

        // Set up the listener for the capture button
        viewBinding.imageCaptureButton.setOnClickListener { takePhoto() }

        // Initialize the camera executor for background tasks
        cameraExecutor = Executors.newSingleThreadExecutor()
    }

    // Request the necessary permissions
    private fun requestPermissions() {
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    // Check if all required permissions are granted
    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            baseContext, it
        ) == PackageManager.PERMISSION_GRANTED
    }

    // Start the camera and bind the use cases
    private fun startCamera() {
        // Get a CameraProvider
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider = cameraProviderFuture.get()

            // Preview use case to display the camera feed
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewBinding.viewFinder.surfaceProvider)
                }

            // ImageCapture use case to take pictures
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

    // Capture a photo and classify the image
    private fun takePhoto() {
        val imageCapture = imageCapture ?: return

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            contentResolver,
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            ContentValues()
        ).build()

        // Set up image capture listener, which is triggered after photo has been taken
        imageCapture.takePicture(
            outputOptions, ContextCompat.getMainExecutor(this), object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    // Log error if photo capture fails
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    output.savedUri?.let { uri ->
                        // Load the captured image as a Bitmap
                        val imageBitmap = loadBitmapFromUri(uri)
                        // Initialize the ImageClassifier
                        val classifier = ImageClassifier(this@MainActivity)
                        // Classify the image
                        val result = classifier.classify(imageBitmap)

                        runOnUiThread {
                            // Hide the camera preview and capture button
                            viewBinding.viewFinder.visibility = View.GONE
                            viewBinding.imageCaptureButton.visibility = View.GONE

                            // Display the classification result
                            resultTextView.text = result
                            resultLayout.visibility = View.VISIBLE
                        }
                    }
                }
            }
        )
    }

    // Load a Bitmap from a given Uri with correct configuration
    private fun loadBitmapFromUri(uri: Uri): Bitmap {
        val resolver = contentResolver
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            // For Android P and above, use ImageDecoder
            val source = ImageDecoder.createSource(resolver, uri)
            ImageDecoder.decodeBitmap(source) { decoder, info, source ->
                // Ensure the bitmap is in ARGB_8888 format and mutable
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                decoder.isMutableRequired = true
            }
        } else {
            // For older versions, use BitmapFactory with options
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }
            BitmapFactory.decodeStream(resolver.openInputStream(uri), null, options)!!
        }
    }

    // Called when the activity is destroyed
    override fun onDestroy() {
        super.onDestroy()
        // Shut down the camera executor when the activity is destroyed
        cameraExecutor.shutdown()
    }

    // Companion object for constants
    companion object {
        private const val TAG = "CameraXApp"

        // Required permissions for the app
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    // For Android versions <= P, add WRITE_EXTERNAL_STORAGE permission
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

    // Function to retake the photo and return to the camera view
    fun retakePhoto(view: View) {
        // Hide the result layout
        resultLayout.visibility = View.GONE
        // Show the camera preview and capture button
        viewBinding.viewFinder.visibility = View.VISIBLE
        viewBinding.imageCaptureButton.visibility = View.VISIBLE
    }
}

// Class for image classification using TensorFlow Lite
class ImageClassifier(private val context: Context) {

    // TensorFlow Lite interpreter for running the model
    private lateinit var interpreter: Interpreter

    // Model input dimensions
    private val inputImageWidth = 224
    private val inputImageHeight = 224

    // Size of the input buffer for the model (4 bytes per float, 3 channels)
    private val modelInputSize = 4 * inputImageWidth * inputImageHeight * 3

    // Initialize the interpreter when the class is instantiated
    init {
        loadModel()
    }

    // Loads the TensorFlow Lite model
    private fun loadModel() {
        try {
            // Load the model file from assets and initialize the interpreter
            val modelFile = loadModelFile()
            interpreter = Interpreter(modelFile)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Reads the TensorFlow Lite model file as a MappedByteBuffer
    private fun loadModelFile(): MappedByteBuffer {
        // Access the model file from the assets folder
        val fileDescriptor = context.assets.openFd("mobilenet_model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength

        // Map the model file into memory for efficient reading
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    // Classifies an input image and returns the most likely label
    fun classify(bitmap: Bitmap): String {
        // Resize the bitmap to match the model's input dimensions
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, inputImageWidth, inputImageHeight, true)

        // Convert the scaled bitmap into a byte buffer for the model
        val byteBuffer = convertBitmapToByteBuffer(scaledBitmap)

        // Prepare the output buffer to hold model predictions
        val output = Array(1) { FloatArray(1000) }

        // Run inference using the model
        interpreter.run(byteBuffer, output)

        // Extract probabilities and find the index of the highest probability
        val probabilities = output[0]
        val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: -1

        // Load class labels from the labels.txt file
        val labels = loadLabels()

        // Return the label with the highest probability, or "Unknown" if index is invalid
        return if (maxIndex != -1 && maxIndex < labels.size) {
            labels[maxIndex]
        } else {
            "Unknown"
        }
    }

    // Converts a bitmap to a ByteBuffer for model input
    private fun convertBitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        // Allocate a direct byte buffer with the required size
        val byteBuffer = ByteBuffer.allocateDirect(modelInputSize)
        byteBuffer.order(ByteOrder.nativeOrder())

        // Extract pixel values from the bitmap
        val intValues = IntArray(inputImageWidth * inputImageHeight)
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)

        // Normalize pixel values to the [0, 1] range and add them to the buffer
        for (pixelValue in intValues) {
            val r = ((pixelValue shr 16) and 0xFF) / 255.0f
            val g = ((pixelValue shr 8) and 0xFF) / 255.0f
            val b = (pixelValue and 0xFF) / 255.0f

            byteBuffer.putFloat(r)
            byteBuffer.putFloat(g)
            byteBuffer.putFloat(b)
        }

        return byteBuffer
    }

    // Loads the class labels from the labels.txt file in the assets folder
    private fun loadLabels(): List<String> {
        // Read the file line by line and return the labels as a list
        return context.assets.open("labels.txt").bufferedReader().readLines()
    }
}
