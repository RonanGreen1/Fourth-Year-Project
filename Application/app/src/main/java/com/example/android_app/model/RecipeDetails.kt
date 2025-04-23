package com.example.android_app.model

data class RecipeDetails(
    val id: Int,
    val title: String,
    val instructions: String,
    val extendedIngredients: List<ExtendedIngredient>?,
    val nutrition: Nutrition?
)

data class Nutrition(
    val nutrients: List<Nutrient>
)

data class Nutrient(
    val name: String,
    val amount: Double,
    val unit: String
)
