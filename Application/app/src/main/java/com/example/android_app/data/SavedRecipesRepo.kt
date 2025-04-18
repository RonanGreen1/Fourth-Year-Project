
import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class SavedRecipesRepo {

    private val db = FirebaseFirestore.getInstance()
    private val usersRef = db.collection("users")
    private val TAG = "SavedRecipeRepo" // Define a tag for logging

    fun saveRecipeId(
        userId: String,
        recipeId: Int,
        onComplete: (Boolean) -> Unit
    ) {
        val userDocRef = usersRef.document(userId)
        userDocRef.update("savedRecipeIds", FieldValue.arrayUnion(recipeId))
            .addOnSuccessListener {
                Log.d(TAG, "Successfully updated savedRecipeIds for user $userId with recipe ID $recipeId")
                onComplete(true)
            }
            .addOnFailureListener { e ->
                // Check if the document doesn't exist, try setting it if needed
                if (e is com.google.firebase.firestore.FirebaseFirestoreException && e.code == com.google.firebase.firestore.FirebaseFirestoreException.Code.NOT_FOUND) {
                    Log.w(TAG, "User document $userId not found for update. Attempting to set initial array.")

                    val initialData = mapOf("savedRecipeIds" to listOf(recipeId))
                    userDocRef.set(initialData, com.google.firebase.firestore.SetOptions.merge())
                        .addOnSuccessListener {
                            Log.d(TAG, "Successfully created document and added recipeId $recipeId for user $userId")
                            onComplete(true)
                        }
                        .addOnFailureListener { e2 ->
                            Log.e(TAG, "Failed to set initial savedRecipeIds for user $userId. Error: ${e2.message}", e2)
                            onComplete(false)
                        }
                } else {
                    Log.e(TAG, "Failed to update savedRecipeIds for user $userId. Error: ${e.message}", e)
                    onComplete(false)
                }
            }
    }

    fun getSavedRecipeIds(
        userId: String,
        onSuccess: (List<Long>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        usersRef.document(userId).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    @Suppress("UNCHECKED_CAST")
                    val raw = doc.get("savedRecipeIds") as? List<Any>
                    val ids = raw
                        ?.mapNotNull { (it as? Number)?.toLong() }
                        ?: emptyList()
                    Log.d(TAG, "Fetched ${ids.size} saved recipe IDs for user $userId")
                    onSuccess(ids)
                } else {
                    Log.w(TAG, "User document $userId does not exist when getting saved recipe IDs.")
                    onSuccess(emptyList()) // Document doesn't exist, so no saved IDs
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Failed to get saved recipe IDs for user $userId", e)
                onFailure(e)
            }
    }

}