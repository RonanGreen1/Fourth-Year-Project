package com.example.android_app.api

import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

object GeminiService {

    private const val API_KEY = "AIzaSyDTZMz-QEPGqwnXi5svD8-py92ejxTlC2U"
    // Gemini Pro Vision endpoint
    private const val ENDPOINT = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-latest:generateContent?key=$API_KEY\""
    private const val TAG = "GeminiVisionService" // For logging

    // Helper to convert Bitmap to Base64 String (using JPEG for efficiency)
    private fun convertBitmapToBase64(bitmap: Bitmap): String {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream) // Compress as JPEG (quality 85)
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    /**
     * Sends the bitmap to Gemini Vision API for ingredient recognition.
     * Calls the callback with the identified ingredient name or "not recognised"/"blocked".
     */
    fun recognizeImageWithGemini(bitmap: Bitmap, callback: (String) -> Unit) {
        val base64Image = convertBitmapToBase64(bitmap)

        // Construct the JSON payload for Gemini Vision
        val textPart = JSONObject().apply {
            put("text", "Identify the single, most prominent food ingredient in this image. Respond with only the ingredient name. If no clear food ingredient is dominant, respond with 'not recognised'.")
        }

        val imagePart = JSONObject().apply {
            put("inline_data", JSONObject().apply {
                put("mime_type", "image/jpeg") // Must match the format used in convertBitmapToBase64
                put("data", base64Image)
            })
        }

        val contents = JSONObject().apply {
            put("parts", JSONArray().put(textPart).put(imagePart))
        }

        // Safety Settings
        val safetySettings = JSONArray().apply {
            put(JSONObject().apply { put("category", "HARM_CATEGORY_HARASSMENT"); put("threshold", "BLOCK_MEDIUM_AND_ABOVE") })
            put(JSONObject().apply { put("category", "HARM_CATEGORY_HATE_SPEECH"); put("threshold", "BLOCK_MEDIUM_AND_ABOVE") })
            put(JSONObject().apply { put("category", "HARM_CATEGORY_SEXUALLY_EXPLICIT"); put("threshold", "BLOCK_MEDIUM_AND_ABOVE") })
            put(JSONObject().apply { put("category", "HARM_CATEGORY_DANGEROUS_CONTENT"); put("threshold", "BLOCK_MEDIUM_AND_ABOVE") })
        }


        val requestJson = JSONObject().apply {
            put("contents", JSONArray().put(contents))
            put("safetySettings", safetySettings) // Add safety settings
        }

        Log.d(TAG, "Sending request to Gemini: ${requestJson.toString(2)}")

        // Setup OkHttp Client
        val client = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS) // Increase timeouts for vision models
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        // Create Request Body
        val requestBody = requestJson.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        // Build Request
        val request = Request.Builder()
            .url(ENDPOINT)
            .addHeader("Content-Type", "application/json")
            // Note: API key is in the URL for this specific Gemini endpoint format
            .post(requestBody)
            .build()

        // Make Asynchronous Call
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Gemini API Call Failed", e)
                callback("not recognised") // Network or other error
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string() // Read response only once
                if (!response.isSuccessful || responseBody == null) {
                    Log.e(TAG, "Gemini API Error: Code=${response.code}, Message=${response.message}, Body=$responseBody")
                    callback("not recognised") // API returned an error
                    response.close() // Close the response body resource
                    return
                }

                // Success - Parse the response
                Log.d(TAG, "Gemini Raw Response: $responseBody")
                val result = parseGeminiResponse(responseBody)
                callback(result)
                response.close() // Close the response body resource
            }
        })
    }

    // Parses the complex Gemini response to extract the text result
    private fun parseGeminiResponse(responseBody: String): String {
        return try {
            val jsonResponse = JSONObject(responseBody)

            // Check for promptFeedback first - indicates blocking
            if (jsonResponse.has("promptFeedback")) {
                val feedback = jsonResponse.optJSONObject("promptFeedback")
                val blockReason = feedback?.optString("blockReason", null)
                if (blockReason != null) {
                    Log.w(TAG, "Gemini request blocked: $blockReason")
                    return "blocked" // Return a specific indicator for blocked content
                }
            }

            // Check for candidates array
            val candidatesArray = jsonResponse.optJSONArray("candidates")
            if (candidatesArray == null || candidatesArray.length() == 0) {
                Log.w(TAG, "No 'candidates' found in Gemini response.")
                return "not recognised"
            }

            // Get content from the first candidate
            val content = candidatesArray.getJSONObject(0).optJSONObject("content")
            if (content == null) {
                Log.w(TAG, "No 'content' found in first candidate.")
                return "not recognised"
            }

            // Get parts from the content
            val partsArray = content.optJSONArray("parts")
            if (partsArray == null || partsArray.length() == 0) {
                Log.w(TAG, "No 'parts' found in content.")
                return "not recognised"
            }

            // Extract text from the first part
            val textResult = partsArray.getJSONObject(0).optString("text", "").trim()

            Log.d(TAG, "Extracted text from Gemini: '$textResult'")

            // Basic validation - return "not recognised" if empty or explicitly says so
            if (textResult.isEmpty() || textResult.equals("not recognised", ignoreCase = true)) {
                "not recognised"
            } else {
                textResult // Return the identified ingredient
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing Gemini JSON response", e)
            "not recognised" // Parsing error
        }
    }
}