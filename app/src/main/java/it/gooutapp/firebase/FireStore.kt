package it.gooutapp.firebase

import android.content.ReceiverCallNotAllowedException
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.*
import it.gooutapp.models.Group

class FireStore {
    private var db = FirebaseFirestore.getInstance()
    private var email = ""
    private val TAG = "FIRE_STORE"
    private val grpdoc = "grpdoc"


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
        val group = hashMapOf(
            "groupName" to groupName,
            "user1" to email,
            "groupId" to "grpdoc1"
        )

        checkGroupCreation(groupName, email) { result ->
            if(result.equals("groupAlreadyIn")) {
                callback(result)
            } else {
                db.collection("groups").document()
                    .set(group)
                    .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                    .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
                callback("successful")
            }
        }
    }

    fun checkGroupCreation(groupName: String, email: String, callback: (String) -> Unit) {
        var i = 0
        var bool = true
        while (bool){
            i++
            if() {
                callback("$i")
                bool = false
            }
        }
    }

    fun addUserToGroup(email: String, groupId: String, callback: (String) -> Unit) {
        checkForNewUserId(email, groupId) { result ->
            /*inserisco il nuovo utente al gruppo su FireStore assegnando come id
            il ritorno della calback di checkForNewUserId
            */
            if(result.equals("userAlreadyIn") || result.equals("groupNotExists")){
                callback(result)
            } else {
                db.collection("groups").document(groupId)//nome gruppo
                    .update("user$result", email)
                    .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                    .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
                callback("successful")
            }
        }
    }

    fun checkForNewUserId(email: String, groupId: String, callback: (String) -> Unit){
        //cerco il primo nome del campo disponibile da dare al nuovo user su FireStore
        var i = 0
        db.collection("groups").document(groupId).get().addOnSuccessListener { document ->
            if (document.data != null) {
                //se l'utente non è già presente nel gruppo trovo nuovo id, altrimenti ritorno -1
                if(!document.toString().contains("$email")){
                    var bool = true;
                    while(bool){
                        i++
                        if(!document.contains("user$i")){
                            callback("$i")
                            bool = false
                        }
                    }
                }else{
                    callback("userAlreadyIn")
                }
            } else {
                Log.d(TAG, "No such document")
                callback("groupNotExists")
            }
        }.addOnFailureListener { exception ->
            Log.d(TAG, "get failed with ", exception)
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

    fun getGroupData(email: String, callback: (ArrayList<Group>) -> Unit) {
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
