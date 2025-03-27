package com.example.android_app.model

data class ShoppingItem(
    var ingredient: String = "",
    var quantity: Int = 1,
    var checked: Boolean = false
) {
    init {
        if (quantity < 0) {
            throw IllegalArgumentException("Quantity cannot be negative")
        }
    }
}