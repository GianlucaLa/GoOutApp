package it.gooutapp.firebase

import android.util.Log
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import it.gooutapp.models.Group

class FireStore {
    private val db = FirebaseFirestore.getInstance()
    private val groupCollection = "groups"
    private val userCollection = "users"
    private val TAG = "FIRE_STORE"

    fun createUserData(name: String, surname: String, nickname: String, email: String) {
        val user = hashMapOf(
            "name" to name,
            "surname" to surname,
            "nickname" to nickname,
            "email" to email,
        )
        db.collection(userCollection).document(email)
            .set(user)
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
    }

    fun createGroupData(groupName: String, email: String, callback: (String) -> Unit) {
        // generazione randomica di character
        val source: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        val groupCode: String = List(8) { source.random() }.joinToString("")

        val group = hashMapOf(
            "groupCode" to groupCode,
            "groupName" to groupName,
            "Admin_${currentUserId()}" to email
        )
        db.collection(groupCollection).document()
            .set(group)
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
        callback("successful")
    }

    fun leaveGroup(groupCode: String){
        getGroupDocumentId(groupCode) { groupDoc ->
            val docRef = db.collection(groupCollection).document(groupDoc)

            // Remove the 'user' field from the document
            val updates = hashMapOf<String, Any>(
                "user_${currentUserId()}" to FieldValue.delete()
            )
            docRef.update(updates).addOnCompleteListener { }
        }
    }

    //solo per ADMINISTRATORS
    fun deleteGroupData(groupCode: String){
        getGroupDocumentId(groupCode) { documentToDelete ->
            db.collection(groupCollection).document(documentToDelete)
                .delete()
                .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!") }
                .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
        }
    }

    private fun getGroupDocumentId(groupCode: String, callback: (String) -> Unit){
        db.collection(groupCollection).whereEqualTo("groupCode", "$groupCode").get().addOnSuccessListener { foundGroupCode ->
           callback(foundGroupCode.last().id)
        }.addOnFailureListener { exception ->
            Log.d(TAG, "get failed with ", exception)
        }
    }

    //RETURN INFO: AM=already member, NM=not member, ER=group code not valid
    private fun isAlreadyMemberOf(groupCode: String, callback: (String, String) -> Unit) {
        db.collection(groupCollection).whereEqualTo("groupCode", "$groupCode").get().addOnSuccessListener { documents ->
            if (documents.size()==0) {
                callback("ER", "")
            } else if (documents.last().contains("user_${currentUserId()}")) {
                callback("AM", "")
            } else {
                callback("NM", documents.last().id)
            }
        }.addOnFailureListener { exception ->
            Log.d(TAG, "get failed with ", exception)
        }
    }

    fun addUserToGroup(email: String, groupId: String, callback: (String) -> Unit) {
        isAlreadyMemberOf(groupId) { alreadyMember, groupDocId ->
            /*inserisco il nuovo utente al gruppo su FireStore assegnando come id
            il ritorno della calback di checkForNewUserId
            */
            if (alreadyMember=="AM" || alreadyMember=="ER") {
                callback(alreadyMember)
            } else {
                db.collection(groupCollection).document(groupDocId)  //nome gruppo
                    .update("user_${currentUserId()}", email)
                    .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                    .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
                callback(alreadyMember)
            }
        }
    }

    fun getUserData(email: String, callback: (DocumentSnapshot) -> Unit) {
        db.collection(userCollection).document(email).get().addOnSuccessListener { document ->
            if (document.data != null) {
                callback(document)
            } else {
                Log.d(TAG, "No such document")
            }
        }.addOnFailureListener { exception ->
            Log.d(TAG, "get failed with ", exception)
        }
    }
    //Restituisce i dati dei gruppi, utilizzata da Adapter per popolare la RecycleView
    fun getUserGroupData(email: String, callback: (ArrayList<Group>) -> Unit) {
        lateinit var document: DocumentSnapshot
        var groupArrayList = ArrayList<Group>()
        db.collection(groupCollection).addSnapshotListener(object : EventListener<QuerySnapshot> {
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                if (error != null) {
                    Log.e("Firestore Error", error.message.toString())
                    return
                }
                for (dc: DocumentChange in value?.documentChanges!!) {
                    Log.w(TAG, dc.toString())
                    document = dc.document
                    //cerco e aggiungo i gruppi che contengono l'email dell'utente
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

    private fun currentUserId(): String{
        return Firebase.auth.currentUser?.uid.toString()
    }
}
