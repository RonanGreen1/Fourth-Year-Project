import com.google.firebase.firestore.FirebaseFirestore
import com.example.android_app.model.ShoppingItem

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
}

