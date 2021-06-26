package it.gooutapp.firebase

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import it.gooutapp.model.Group
import it.gooutapp.model.Proposal
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class FireStore {
    private val db = FirebaseFirestore.getInstance()
    private val groupCollection = "groups"
    private val userCollection = "users"
    private val proposalCollection = "proposals"
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

    fun createGroupData(groupName: String, email: String, callback: (Boolean) -> Unit) {
        // generazione randomica di character
        val source: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        val groupCode: String = List(15) { source.random() }.joinToString("")
        val group = hashMapOf(
            "groupCode" to groupCode,
            "groupName" to groupName,
            "admin_${currentUserId()}" to email
        )
        db.collection(groupCollection).document()
            .set(group)
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully written!")
                callback(true) }
            .addOnFailureListener {
                e -> Log.w(TAG, "Error writing document", e)
                callback(false) }
    }

    fun createProposalData(groupCode: String, proposalName: String, dateTime: String, place: String, callback: (Boolean) -> Unit){
        val source: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')
        val proposalCode: String = List(15) { source.random() }.joinToString("")
        currentUserNickname { currNickname ->
            val proposal = hashMapOf(
                "groupCode" to groupCode,
                "dateTime" to dateTime,
                "organizator" to currNickname,
                "place" to place,
                "proposalCode" to proposalCode,
                "proposalName" to proposalName
            )
            db.collection(proposalCollection).document()
                .set(proposal)
                .addOnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot successfully written!")
                    callback(true)
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error writing document", e)
                    callback(false)
                }
        }
    }

    fun leaveGroup(groupCode: String, callback: (Boolean) -> Unit) {
        getGroupDocumentId(groupCode) { groupDoc ->
            // Remove the 'user' field from the document
            val updates = hashMapOf<String, Any>(
                "user_${currentUserId()}" to FieldValue.delete()
            )
            db.collection(groupCollection).document(groupDoc).update(updates)
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully deleted!")
                callback(true)}
            .addOnFailureListener {
                e -> Log.w(TAG, "Error deleting document", e)
                callback(false)}
        }
    }

    //solo per ADMINISTRATORS
    fun deleteGroupData(groupCode: String, callback: (Boolean) -> Unit) {
        getGroupDocumentId(groupCode) { documentToDelete ->
            db.collection(groupCollection).document(documentToDelete)
                .delete()
                .addOnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot successfully deleted!")
                    callback(true)}
                .addOnFailureListener {
                        e -> Log.w(TAG, "Error deleting document", e)
                    callback(false)}
            db.collection(proposalCollection).document(documentToDelete).delete()
            db.collection(proposalCollection).whereEqualTo("groupCode", "$groupCode").get()
                .addOnSuccessListener { proposalDocs ->
                    for(dc in proposalDocs){
                        db.collection(proposalCollection).document(dc.id).delete()
                    }
                }
        }
    }

    private fun getGroupDocumentId(groupCode: String, callback: (String) -> Unit) {
        db.collection(groupCollection).whereEqualTo("groupCode", "$groupCode").get()
            .addOnSuccessListener { foundGroupCode ->
                callback(foundGroupCode.last().id)
            }.addOnFailureListener { exception ->
            Log.d(TAG, "get failed with ", exception)
        }
    }

    private fun getProposalDocumentId(proposalCode: String, callback: (String) -> Unit) {
        db.collection(proposalCollection).whereEqualTo("proposalCode", "$proposalCode").get()
            .addOnSuccessListener { foundProposalCode ->
                callback(foundProposalCode.last().id)
            }.addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }

    //RETURN INFO: AM=already member, NM=not member, ER=group code not valid
    private fun isAlreadyMemberOf(groupCode: String, callback: (String, String) -> Unit) {
        db.collection(groupCollection).whereEqualTo("groupCode", "$groupCode").get()
            .addOnSuccessListener { documents ->
                if (documents.size() == 0) {
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
            if (alreadyMember == "AM" || alreadyMember == "ER") {
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
    fun getUserGroupData(email: String, callback: (ArrayList<Group>, ArrayList<Boolean>) -> Unit) {
        lateinit var document: DocumentSnapshot
        val userGroupList = ArrayList<Group>()
        val adminFlagList = ArrayList<Boolean>()
        db.collection(groupCollection).addSnapshotListener(object : EventListener<QuerySnapshot> {
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                if (error != null) {
                    Log.e("Firestore Error", error.message.toString())
                    return
                }
                for (dc: DocumentChange in value?.documentChanges!!) {
                    document = dc.document
                    //cerco e aggiungo i gruppi che contengono l'email dell'utente
                    if (document.contains("admin_${currentUserId()}")) {
                        adminFlagList.add(true)
                        if (dc.type == DocumentChange.Type.ADDED)
                            userGroupList.add(dc.document.toObject(Group::class.java))

                    } else if (document.contains("user_${currentUserId()}")) {
                        adminFlagList.add(false)
                        if (dc.type == DocumentChange.Type.ADDED)
                            userGroupList.add(dc.document.toObject(Group::class.java))
                    }
                }
                callback(userGroupList, adminFlagList)
            }
        })
    }

    fun getProposalData(groupCode: String, callback: (ArrayList<Proposal>) -> Unit) {
        var proposalArrayList = ArrayList<Proposal>()
        db.collection(proposalCollection).addSnapshotListener(object : EventListener<QuerySnapshot> {
            @RequiresApi(Build.VERSION_CODES.O)
            override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                if (error != null) {
                    Log.e("Firestore Error", error.message.toString())
                    return
                }
                for (dc: DocumentChange in value?.documentChanges!!) {
                    //cerco e aggiungo i gruppi che contengono l'email dell'utente
                    val currentDateTime = LocalDateTime.now()
                    var stringDoc = dc.document.toString()
                    val currDocDate = LocalDateTime.parse(dc.document.get("dateTime").toString(), DateTimeFormatter.ISO_DATE_TIME)
                    if (currDocDate.isAfter(currentDateTime)) {
                        if (dc.type == DocumentChange.Type.ADDED && stringDoc.contains(groupCode))
                            if (!(stringDoc.contains("accepted") || stringDoc.contains("refused")))
                                proposalArrayList?.add(dc?.document?.toObject(Proposal::class.java))
                    }
                    callback(proposalArrayList)
                }
            }
            })
    }

    fun getUserHistoryProposalData(callback: (ArrayList<Proposal>) -> Unit){
        currentUserNickname { currNickname ->
            var proposalArrayList = ArrayList<Proposal>()
            db.collection(proposalCollection).addSnapshotListener(object : EventListener<QuerySnapshot> {
                @RequiresApi(Build.VERSION_CODES.O)
                override fun onEvent(value: QuerySnapshot?, error: FirebaseFirestoreException?) {
                    if (error != null) {
                        Log.e("Firestore Error", error.message.toString())
                        return
                    }
                    for (dc: DocumentChange in value?.documentChanges!!) {
                        //cerco e aggiungo i gruppi che contengono l'email dell'utente
                        if (dc.type == DocumentChange.Type.ADDED
                            && (dc.document.toString().contains("user_${currentUserId()}")
                                || dc.document.contains("$currNickname")
                                || LocalDateTime.now().isAfter(LocalDateTime.parse(dc.document.get("dateTime").toString()))))
                            proposalArrayList?.add(dc?.document?.toObject(Proposal::class.java))
                    }
                    callback(proposalArrayList)
                }
            })
        }
    }

    fun getUserProposalState(proposalCode: String, callback: (Any) -> Unit){
        db.collection(proposalCollection).whereEqualTo("proposalCode", "$proposalCode").get()
            .addOnSuccessListener { proposalDocs ->
                var state = proposalDocs.documents.last().get("user_${currentUserId()}")?.toString()
                if(state == null)
                    callback("")
                else
                    callback(state)
            }
    }

    fun setProposalState(proposalCode: String, state: String, callback: (Boolean) -> Unit){
        getProposalDocumentId(proposalCode) { proposalDoc ->
            // Remove the 'user' field from the document
            val updates = hashMapOf<String, Any>(
                "user_${currentUserId()}" to "$state"
            )
            db.collection(proposalCollection).document(proposalDoc).update(updates)
                .addOnSuccessListener {
                    Log.d(TAG, "Proposal state of user ${currentUserId()} setted")
                    callback(true)}
                .addOnFailureListener {
                    e -> Log.w(TAG, "Error setting proposal state", e)
                    callback(false)}
        }
    }

    private fun currentUserId(): String {
        return Firebase.auth.currentUser?.uid.toString()
    }

    private fun currentUserEmail(): String {
        return Firebase.auth.currentUser?.email.toString()
    }

    private fun currentUserNickname(callback: (String) -> Unit) {
        db.collection(userCollection).document(Firebase.auth.currentUser?.email.toString()).get().addOnSuccessListener { document ->
            if (document.data != null) {
                callback(document.get("nickname") as String)
            } else {
                Log.d(TAG, "No such document")
            }
        }.addOnFailureListener { exception ->
            Log.d(TAG, "get failed with ", exception)
        }
    }
}
