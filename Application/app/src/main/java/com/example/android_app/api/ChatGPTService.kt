package com.example.android_app.api

import android.graphics.Bitmap  // Represents images in Android.
import android.util.Base64  // Used to encode images in Base64 format for API compatibility.
import android.util.Log  // Logging utility.
import okhttp3.*  // OkHttp library for handling HTTP requests.
import okhttp3.MediaType.Companion.toMediaTypeOrNull  // Converts a string to a MediaType object.
import org.json.JSONArray  // Provides JSON array manipulation.
import org.json.JSONObject  // Provides JSON object manipulation.
import java.io.ByteArrayOutputStream  // Converts Bitmap to a byte stream.
import java.io.IOException  // Handles exceptions during network calls.
import java.util.concurrent.TimeUnit  // Sets timeout for HTTP requests.

// Service for handling ChatGPT API calls
object ChatGPTService {

    private const val API_KEY = ""
    private const val ENDPOINT = "https://api.openai.com/v1/chat/completions"

    // Converts a Bitmap image into a Base64-encoded string for API submission.
    private fun convertBitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream() // Stream to hold compressed image data.
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream) // Compress image as PNG.
        val byteArray = outputStream.toByteArray() // Convert stream data to byte array.
        return Base64.encodeToString(byteArray, Base64.NO_WRAP) // Encode as Base64 string.
    }

    fun recognizeImage(bitmap: Bitmap, callback: (String) -> Unit) {
        val base64Image = convertBitmapToBase64(bitmap) // Convert image to Base64.

        // Construct the JSON request payload with a prompt for one-word answer.
        val messagesArray = JSONArray().apply {
            put(JSONObject().apply {
                put("role", "user")
                put("content", "Please provide exactly one word representing the dominant ingredient in this image. If no ingredient is found, respond with 'not recognised'.")
            })
            put(JSONObject().apply {
                put("role", "user")
                put("content", JSONArray().apply {
                    put(JSONObject().apply {
                        put("type", "image_url")
                        put("image_url", "data:image/png;base64,$base64Image") // Include the image in Base64.
                    })
                })
            })
        }

        // Build the full JSON request object.
        val json = JSONObject().apply {
            put("model", "gpt-4") // Specify the model.
            put("messages", messagesArray) // Attach the messages.
            put("max_tokens", 10) // Limit response length.
        }

        // Create HTTP client with proper timeouts.
        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        // Create request body with the JSON payload.
        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), json.toString())

        // Build the HTTP request.
        val request = Request.Builder()
            .url(ENDPOINT)
            .addHeader("Authorization", "Bearer $API_KEY")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        // Execute the API request asynchronously.
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()  // Log error.
                callback("not recognised") // Return "not recognised" on error.
            }

            override fun onResponse(call: Call, response: Response) {
                // Read API response as a string.
                val responseData = response.body?.string()
                // Log the raw API response for debugging.
                Log.d("ChatGPTService", "Raw API response: $responseData")
                // Process the response to extract one word.
                val result = extractIngredient(responseData)
                // Return the extracted ingredient via callback.
                callback(result)
            }
        })
    }

    private fun extractIngredient(responseData: String?): String {
        if (responseData.isNullOrEmpty()) return "not recognised"

        return try {
            val jsonResponse = JSONObject(responseData)
            if (!jsonResponse.has("choices")) return "not recognised"

            val choicesArray = jsonResponse.getJSONArray("choices")
            if (choicesArray.length() == 0) return "not recognised"

            // Extract the content message from the first choice object.
            val result = choicesArray.getJSONObject(0)
                .getJSONObject("message")
                .getString("content")
                .trim()

            // If the response contains multiple words, take only the first.
            val firstWord = result.split("\\s+".toRegex()).firstOrNull()?.trim() ?: ""
            if (firstWord.isEmpty()) "not recognised" else firstWord
        } catch (e: Exception) {
            "not recognised"
        }
    }
}
