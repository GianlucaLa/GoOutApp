package it.gooutapp.firebase

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class FireStore {
    private var db = FirebaseFirestore.getInstance()
    private val TAG = "FIRE_STORE"
    private var tmpname = ""
    private var tmpsurname = ""

    fun createUserData(name: String, surname: String, nickname: String, email: String, password: String) {
        val user = hashMapOf(
            "name" to name,
            "surname" to surname,
            "nickname" to nickname,
            "email" to email,
            "password" to password
        )

        db.collection("users").document(email)
            .set(user)
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
    }

    fun getUserData(email: String) : DocumentSnapshot? {
        val docRef = db.collection("users").document(email)
        var result: DocumentSnapshot? = null
        docRef.get()
            .addOnCompleteListener { document ->
                if (document != null) {
                    Log.d(TAG, "DocumentSnapshot data: ${document.result}")
                    result = document.result!!
                } else {
                    Log.d(TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
        return result
    }
}
