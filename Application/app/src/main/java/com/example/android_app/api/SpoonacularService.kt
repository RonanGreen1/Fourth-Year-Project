package com.example.android_app.api

import com.example.android_app.model.Recipe
import com.example.android_app.model.RecipeDetails
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Singleton object to handle API service creation
object SpoonacularService {
    private const val BASE_URL = "https://api.spoonacular.com/"
    private const val API_KEY = "53177bc8405b40d984f15882f17f6518"

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val api: SpoonacularApi by lazy {
        retrofit.create(SpoonacularApi::class.java)
    }



    // Function to fetch recipes from Spoonacular with multiple ingredients
    suspend fun getRecipes(ingredients: String, ranking: Int = 1): List<Recipe> {
        return try {
            api.searchRecipesByIngredient(
                ingredient = ingredients,
                number = 10,
                ranking = ranking,
                apiKey = API_KEY
            )
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    suspend fun getRecipeDetails(recipeId: Int): RecipeDetails? {
        return try {
            api.getRecipeDetails(recipeId, API_KEY)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

}
