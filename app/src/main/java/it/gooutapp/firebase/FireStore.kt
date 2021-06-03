package it.gooutapp.firebase

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import it.gooutapp.models.Group

class FireStore {
    private var db = FirebaseFirestore.getInstance()
    private var email = ""
    private val TAG = "FIRE_STORE"
    private lateinit var user_id : String

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

    fun createGroupData(groupName: String, email: String, callback: (String) -> Unit) {
        user_id = Firebase.auth.currentUser?.uid.toString()
        val source: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        // generazione randomica di character
        val groupCode: String = List(8) { source.random() }.joinToString("")

        val group = hashMapOf(
            "groupCode" to groupCode,
            "groupName" to groupName,
            "user_$user_id" to email
        )
        db.collection("groups").document(groupName)
            .set(group)
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
        callback("successful")
    }

//    private fun getGroupCode(groupName: String, callback: (String) -> Unit) {
    //        db.collection("groups").get().addOnSuccessListener { documents ->
//            val currLastId = documents.last().id
//            val newId = currLastId.substring(6).toInt() + 1
//            Log.w(TAG, "$newId")
//            callback("$groupName$newId")
//        }.addOnFailureListener { exception ->
//            Log.d(TAG, "Error getting documents: ", exception)
//        }
//    }


    private fun isAlreadyMemberOf(email: String, groupId: String, callback: (Boolean, String) -> Unit) {
        user_id = Firebase.auth.currentUser?.uid.toString()
        db.collection("groups").whereEqualTo("groupCode", "$groupId").get().addOnSuccessListener { document ->

            if (document.last().contains("user_$user_id")) {
                callback(true, document.last().id)
            } else {
                callback(false, document.last().id)
            }
        }.addOnFailureListener { exception ->
            Log.d(TAG, "get failed with ", exception)
        }
    }

    fun addUserToGroup(email: String, groupId: String, callback: (Boolean) -> Unit) {
        isAlreadyMemberOf(email, groupId) { alreadyMember, groupName ->
            /*inserisco il nuovo utente al gruppo su FireStore assegnando come id
            il ritorno della calback di checkForNewUserId
            */
            if (alreadyMember) {
                callback(false)
            } else {
                db.collection("groups").document(groupName)  //nome gruppo
                    .update("user_$user_id", email)
                    .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                    .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
                callback(true)
            }
        }
    }

    fun getUserData(email: String, callback: (DocumentSnapshot) -> Unit) {
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

    fun getUserGroupData(email: String, callback: (ArrayList<Group>) -> Unit) {
        lateinit var document: DocumentSnapshot
        var groupArrayList = ArrayList<Group>()
        db = FirebaseFirestore.getInstance()
        db.collection("groups").addSnapshotListener(object : EventListener<QuerySnapshot> {
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                if (error != null) {
                    Log.e("Firestore Error", error.message.toString())
                    return
                }
                for (dc: DocumentChange in value?.documentChanges!!) {
                    Log.w(TAG, dc.toString())
                    document = dc.document
                    if (document.toString().contains(email)) {
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
