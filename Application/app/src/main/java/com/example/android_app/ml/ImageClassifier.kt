package com.example.android_app.ml

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

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
    fun classify(bitmap: Bitmap):  Pair<String, Float> {
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

        val confidence = if (maxIndex >= 0) probabilities[maxIndex] else 0f

        // Load class labels from the labels.txt file
        val labels = loadLabels()

        // Return the label with the highest probability, or "Unknown" if index is invalid
        val label = if (maxIndex != -1 && maxIndex < labels.size) {
            labels[maxIndex]
        } else {
            "Unknown"
        }

        return Pair(label, confidence)
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
