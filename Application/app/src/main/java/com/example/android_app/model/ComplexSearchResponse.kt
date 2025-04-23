package com.example.android_app.model


data class ComplexSearchResponse(
    val results: List<Recipe>,
    val offset: Int,
    val number: Int,
    val totalResults: Int
)