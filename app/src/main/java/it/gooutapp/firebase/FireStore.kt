package it.gooutapp.firebase

import android.util.Log
import android.widget.Toast
import com.google.firebase.firestore.*
import it.gooutapp.models.Group

class FireStore {
    private var db = FirebaseFirestore.getInstance()
    private var email = ""
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

/*    fun addUserToGroup(email: String, groupId: String) {
        db.collection("groups").document(groupId).get().addOnSuccessListener { document ->
            if (document.data != null) {
                for(document.)
            } else {
                Log.d(TAG, "No such document")
            }
        }.addOnFailureListener { exception ->
            Log.d(TAG, "get failed with ", exception)
        }
        db.collection("groups").document()//nome gruppo
            .update()
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
    }*/

    fun getUserData(email: String, callback:(DocumentSnapshot) -> Unit){
        this.email = email
        db.collection("users").document(email).get().addOnSuccessListener { document ->
            if (document.data != null) {
                callback(document)
            } else {
                Log.d(TAG, "No such document")
            }
        }.addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
        }
    }

    fun getGroupData(email: String, callback: (ArrayList<Group>) -> Unit) {
        lateinit var document: DocumentSnapshot
        var groupArrayList = ArrayList<Group>()
        db = FirebaseFirestore.getInstance()
        db.collection("groups").
        addSnapshotListener(object : EventListener<QuerySnapshot> {
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                if(error != null){
                    Log.e("Firestore Error", error.message.toString())
                    return
                }
                for(dc: DocumentChange in value?.documentChanges!!){
                    Log.w(TAG, dc.toString())
                    document = dc.document
                    if(document.toString().contains(email)) {
                        if (dc.type == DocumentChange.Type.ADDED) {
                            groupArrayList.add(dc.document.toObject(Group::class.java))
                        }
                    }
                }
                callback(groupArrayList)
            }
        })
    }
}
