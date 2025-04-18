
package com.example.android_app.model

data class ExtendedIngredient(
    val id: Int?, // Ingredient ID
    val name: String?, // e.g., "Flour"
    val amount: Double?, // e.g., 2.0
    val unit: String?, // e.g., "cup"
    val original: String?, // The original string,
    val image: String? // URL path for ingredient image (needs base URL added)

)