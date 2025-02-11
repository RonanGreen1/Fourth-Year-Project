package com.example.android_app.api

import android.graphics.Bitmap
import android.util.Base64
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

// Service for handling ChatGPT API calls
object ChatGPTService {
    private const val API_KEY = ""
    private const val ENDPOINT = "https://api.openai.com/v1/chat/completions"

    // Convert bitmap image to Base64 format
    private fun convertBitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    // Function to send an image to ChatGPT API and get a response
    fun recognizeImage(bitmap: Bitmap, callback: (String) -> Unit) {
        val base64Image = convertBitmapToBase64(bitmap)

        // Constructing the correct JSON payload
        val messagesArray = JSONArray()
        messagesArray.put(JSONObject().apply {
            put("role", "user")
            put("content", "What ingredients are in this image?")
        })
        messagesArray.put(JSONObject().apply {
            put("role", "user")
            put("content", JSONArray().apply {
                put(JSONObject().apply {
                    put("type", "image_url")
                    put("image_url", "data:image/png;base64,$base64Image")
                })
            })
        })

        val json = JSONObject().apply {
            put("model", "gpt-4-vision-preview")
            put("messages", messagesArray)
            put("max_tokens", 50)
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()

        val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), json.toString())

        val request = Request.Builder()
            .url(ENDPOINT)
            .addHeader("Authorization", "Bearer $API_KEY")
            .addHeader("Content-Type", "application/json")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                callback("Error: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseData = response.body?.string()
                val result = extractIngredients(responseData)
                callback(result)
            }
        })
    }

    // Function to extract ingredients from the API response
    private fun extractIngredients(responseData: String?): String {
        if (responseData.isNullOrEmpty()) return "No ingredients found"

        return try {
            val jsonResponse = JSONObject(responseData)

            // ✅ Ensure "choices" exist before accessing
            if (!jsonResponse.has("choices")) return "No ingredients found"

            val choicesArray = jsonResponse.getJSONArray("choices")
            if (choicesArray.length() == 0) return "No ingredients found"

            val result = choicesArray.getJSONObject(0)
                .getJSONObject("message")
                .getString("content")

            result.ifEmpty { "No ingredients found" }
        } catch (e: Exception) {
            "Error parsing response: ${e.message}"
        }
    }
}
