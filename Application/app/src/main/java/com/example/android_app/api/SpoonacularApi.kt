package com.example.android_app.api  // Defines the package location of the API interface

// Importing required data models from the model package
import com.example.android_app.model.Recipe  // Represents basic recipe details from API response
import com.example.android_app.model.RecipeDetails  // Represents detailed recipe information

// Retrofit imports for defining API endpoints
import retrofit2.http.GET  // Annotation for HTTP GET requests
import retrofit2.http.Path  // Annotation for inserting values into URL paths
import retrofit2.http.Query  // Annotation for adding query parameters to a request


interface SpoonacularApi {


    @GET("recipes/findByIngredients")  // Specifies the API endpoint for ingredient-based search
    suspend fun searchRecipesByIngredient(
        @Query("ingredients") ingredient: String,  // Adds ingredient(s) as a query parameter
        @Query("number") number: Int,  // Specifies the number of recipes to return
        @Query("apiKey") apiKey: String  // API key required for authentication
    ): List<Recipe>  // Returns a list of recipes matching the given ingredient(s)


    @GET("recipes/{id}/information")  // Specifies the API endpoint for fetching recipe details
    suspend fun getRecipeDetails(
        @Path("id") recipeId: Int,  // Inserts recipe ID dynamically into the request URL
        @Query("apiKey") apiKey: String  // API key required for authentication
    ): RecipeDetails  // Returns the detailed information of the requested recipe
}


