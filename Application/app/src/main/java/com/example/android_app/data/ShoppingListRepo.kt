import android.content.ContentValues.TAG
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.example.android_app.model.ShoppingItem
import com.google.firebase.firestore.FieldValue

class ShoppingListRepo {

    private val db = FirebaseFirestore.getInstance()
    private val usersRef = db.collection("users")

    fun getShoppingList(
        userId: String,
        onSuccess: (List<ShoppingItem>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        usersRef.document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Retrieve the shoppingList field as a List of Maps
                    val shoppingListData = document.get("shoppingList") as? List<Map<String, Any>>

                    val shoppingList: List<ShoppingItem> = shoppingListData?.map { itemMap ->
                        // Safely extract each field with fallback defaults
                        val ingredient = itemMap["Ingredient"] as? String ?: "None"
                        val quantity = (itemMap["Quantity"] as? Long)?.toInt() ?: 0
                        val checked = itemMap["Checked"] as? Boolean ?: false
                        ShoppingItem(
                            ingredient = ingredient,
                            quantity = quantity,
                            checked = checked
                        )
                    } ?: emptyList()

                    onSuccess(shoppingList)
                } else {
                    // If no document exists for the user, return an empty list.
                    onSuccess(emptyList())
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception)
            }
    }
    fun addItem(
        userId: String,
        newItem: ShoppingItem,
        onComplete: (Boolean) -> Unit
    ) {
        usersRef.document(userId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Get existing shoppingList array
                    val currentListData = document.get("shoppingList") as? List<Map<String, Any>> ?: emptyList()

                    // Convert the existing list to a mutable list of maps
                    val mutableList = currentListData.toMutableList()

                    // Create a map from the new item’s fields
                    val newItemMap = mapOf(
                        "Ingredient" to newItem.ingredient,
                        "Quantity" to newItem.quantity,
                        "Checked" to newItem.checked
                    )

                    // Add the new item to the in-memory list
                    mutableList.add(newItemMap)

                    // Push the updated list back to Firestore
                    usersRef.document(userId)
                        .update("shoppingList", mutableList)
                        .addOnSuccessListener {
                            onComplete(true)
                        }
                        .addOnFailureListener { e ->
                            e.printStackTrace()
                            onComplete(false)
                        }
                } else {
                    // If no document exists, create one with an initial shoppingList array
                    val initialList = listOf(
                        mapOf(
                            "Ingredient" to newItem.ingredient,
                            "Quantity" to newItem.quantity,
                            "Checked" to newItem.checked
                        )
                    )
                    usersRef.document(userId)
                        .set(mapOf("shoppingList" to initialList))
                        .addOnSuccessListener {
                            onComplete(true)
                        }
                        .addOnFailureListener { e ->
                            e.printStackTrace()
                            onComplete(false)
                        }
                }
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                onComplete(false)
            }
    }


    fun deleteItem(
        userId: String,
        itemToDelete: ShoppingItem,
        onComplete: (Boolean) -> Unit
    ) {
        // Convert the item to delete into the map format stored in Firestore
        val itemToDeleteMap = mapOf(
            "Ingredient" to itemToDelete.ingredient,
            "Quantity" to itemToDelete.quantity,
            "Checked" to itemToDelete.checked
        )

        // FieldValue.arrayRemove removes all instances matching the map
        usersRef.document(userId)
            .update("shoppingList", FieldValue.arrayRemove(itemToDeleteMap))
            .addOnSuccessListener {
                Log.d(TAG, "Item removed successfully using arrayRemove for $userId")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error removing item using arrayRemove for $userId", e)

                onComplete(false)
            }
    }


    // Fetches the list, finds the item, replaces it, and saves the list.
    fun updateItem(
        userId: String,
        originalItem: ShoppingItem, // The item before editing
        updatedItem: ShoppingItem, // The item after editing
        onComplete: (Boolean) -> Unit
    ) {
        usersRef.document(userId).get()
            .addOnSuccessListener { document ->
                if (!document.exists()) {
                    Log.e(TAG, "Document not found for update for user $userId")
                    onComplete(false)
                    return@addOnSuccessListener
                }

                val currentListData = document.get("shoppingList") as? List<Map<String, Any>>
                if (currentListData == null) {
                    Log.e(TAG, "Shopping list field not found or not a list for user $userId")
                    onComplete(false)
                    return@addOnSuccessListener
                }

                // Convert original and updated items to maps
                val originalItemMap = mapOf(
                    "Ingredient" to originalItem.ingredient,
                    "Quantity" to originalItem.quantity,
                    "Checked" to originalItem.checked
                )
                val updatedItemMap = mapOf(
                    "Ingredient" to updatedItem.ingredient,
                    "Quantity" to updatedItem.quantity,
                    "Checked" to updatedItem.checked
                )

                // Find the index of the original item in the list of maps
                val itemIndex = currentListData.indexOfFirst { itemMap ->
                    // Compare map contents carefully
                    itemMap["Ingredient"] == originalItemMap["Ingredient"] &&
                            itemMap["Quantity"] == (originalItemMap["Quantity"] as? Number)?.toLong() && // Firestore stores numbers as Long
                            itemMap["Checked"] == originalItemMap["Checked"]
                }

                if (itemIndex != -1) {
                    // Item found, create a mutable copy and update it
                    val mutableList = currentListData.toMutableList()
                    mutableList[itemIndex] = updatedItemMap // Replace with the updated map

                    // Write the entire modified list back
                    usersRef.document(userId)
                        .update("shoppingList", mutableList)
                        .addOnSuccessListener {
                            Log.d(TAG,"Item updated successfully for $userId at index $itemIndex")
                            onComplete(true)
                        }
                        .addOnFailureListener { e ->
                            Log.e(TAG, "Failed to write updated list for $userId", e)
                            onComplete(false)
                        }
                } else {
                    // Item not found in the list
                    Log.w(TAG,"Original item not found for update for user $userId: $originalItemMap")
                    onComplete(false) // Indicate failure as the item wasn't found
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to get document for update for $userId", e)
                onComplete(false)
            }
    }



}

