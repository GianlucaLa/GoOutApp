package it.gooutapp.firebase.firestore

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore

class FireStore {
    private var db = FirebaseFirestore.getInstance()
    private val TAG = "FIRE_STORE"

    fun createUserData(name: String, surname: String, nickname: String, email: String, password: String) {
        val user = hashMapOf(
            "name" to name,
            "surname" to surname,
            "nickname" to nickname,
            "email" to email,
            "password" to password
        )

        db.collection("users").document(nickname)
            .set(user)
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
    }
}
