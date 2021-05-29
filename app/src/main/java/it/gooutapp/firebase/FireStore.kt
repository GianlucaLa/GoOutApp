package it.gooutapp.firebase

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class FireStore {
    private var db = FirebaseFirestore.getInstance()
    private val TAG = "FIRE_STORE"

    fun createUserData(name: String, surname: String, nickname: String, email: String) {
        val user = hashMapOf(
            "name" to name,
            "surname" to surname,
            "nickname" to nickname,
            "email" to email,
        )
        db.collection("users").document(email)
            .set(user)
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
    }

    fun getUserData(email: String, callback:(DocumentSnapshot) -> Unit){
        db.collection("users").document(email).get().addOnSuccessListener { document ->
            if (document.data != null) {
                Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                callback(document)
            } else {
                Log.d(TAG, "No such document")
            }
        }.addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
        }
    }
}
