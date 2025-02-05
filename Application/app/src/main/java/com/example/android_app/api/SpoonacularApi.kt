package com.example.android_app.api

import com.example.android_app.model.Recipe
import retrofit2.http.GET
import retrofit2.http.Query

// API interface defining the Spoonacular endpoints
interface SpoonacularApi {
    @GET("recipes/findByIngredients")
    suspend fun searchRecipesByIngredient(
        @Query("ingredients") ingredient: String,
        @Query("number") number: Int,
        @Query("apiKey") apiKey: String
    ): List<Recipe>
}

