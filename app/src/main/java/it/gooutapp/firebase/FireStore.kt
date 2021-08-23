package it.gooutapp.firebase

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import it.gooutapp.model.Group
import it.gooutapp.model.Message
import it.gooutapp.model.Proposal
import it.gooutapp.model.User
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.coroutines.coroutineContext

class FireStore {
    private val TAG = "FIRE_STORE"
    private val db = FirebaseFirestore.getInstance()
    private val groupCollection = "groups"
    private val userCollection = "users"
    private val proposalCollection = "proposals"
    private val chatCollection = "chats"
    private val messageSubCollection = "messages"
    private val source: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')   // per generazione randomica di character

    //CREATE METHODS
    fun createUserData(name: String, surname: String, nickname: String, email: String) {
        val user = hashMapOf(
            "name" to name,
            "surname" to surname,
            "nickname" to nickname,
            "email" to email,
            "authId" to currentUserId()
        )
        db.collection(userCollection).document(email)
            .set(user)
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
    }

    fun createGroupData(groupName: String, email: String, callback: (Boolean) -> Unit) {
        val groupId: String = List(15) { source.random() }.joinToString("")
        val group = hashMapOf(
            "groupId" to groupId,
            "groupName" to groupName,
            "admin" to email
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun createProposalData(groupId: String, proposalName: String, dateTime: String, place: String, groupName: String, callback: (Boolean) -> Unit){
        val proposalId: String = List(15) { source.random() }.joinToString("")
        currentUserNickname { currNickname ->
            val proposal = hashMapOf(
                "groupId" to groupId,
                "dateTime" to dateTime,
                "organizator" to currNickname,
                "organizatorId" to currentUserId(),
                "place" to place,
                "groupName" to groupName,
                "proposalId" to proposalId,
                "proposalName" to proposalName
            )
            db.collection(proposalCollection).document("proposal_${LocalDateTime.now()}")
                .set(proposal)
                .addOnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot successfully written!")
                    createChatData(groupId, proposalId)
                    callback(true)
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error writing document", e)
                    callback(false)
                }
        }
    }

    private fun createChatData(groupId: String, proposalId: String){
        val chat = hashMapOf(
            "proposalId" to proposalId,
            "groupId" to groupId
        )
        db.collection(chatCollection).document(proposalId)
            .set(chat)
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully written!")
                db.collection(chatCollection).document(proposalId).collection(messageSubCollection)
                    .document("firstMessage").set(hashMapOf("firstMessage" to ""))
            }
            .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
    }

    //GET-SET METHODS
    fun getUserData(email: String, callback: (User) -> Unit) {
        db.collection(userCollection).document(email).get().addOnSuccessListener { document ->
            if (document.data != null) {
                callback(document.toObject(User::class.java)!!)
            } else {
                callback(User())
                Log.d(TAG, "No such document")
            }
        }.addOnFailureListener { exception ->
            Log.d(TAG, "get failed with ", exception)
        }
    }

    fun getUserGroupsData(callback: (ArrayList<Group>, ArrayList<Boolean>) -> Unit) {
        val userGroupsList = ArrayList<Group>()
        val adminFlagList = ArrayList<Boolean>()
        db.collection(groupCollection).addSnapshotListener { value, error ->
            if (error != null) {
                Log.e("Firestore Error", error.message.toString())
                return@addSnapshotListener
            }
            for (dc: DocumentChange in value?.documentChanges!!) {
                //cerco e aggiungo i gruppi che contengono l'email dell'utente
                if (dc.document.get("admin")== currentUserEmail()) {
                    adminFlagList.add(true)
                    if (dc.type == DocumentChange.Type.ADDED)
                        userGroupsList.add(dc.document.toObject(Group::class.java))
                    else if (dc.type == DocumentChange.Type.REMOVED)
                        userGroupsList.remove(dc.document.toObject(Group::class.java))
                } else if (dc.document.get("users").toString().contains(currentUserEmail())) {
                    adminFlagList.add(false)
                    if (dc.type == DocumentChange.Type.ADDED)
                        userGroupsList.add(dc.document.toObject(Group::class.java))
                    else if (dc.type == DocumentChange.Type.REMOVED)
                        userGroupsList.remove(dc.document.toObject(Group::class.java))
                }else{
                    if (dc.type == DocumentChange.Type.MODIFIED)
                        userGroupsList.remove(dc.document.toObject(Group::class.java))
                }
            }
            callback(userGroupsList, adminFlagList)
        }
    }

    fun getGroupMembers(groupId: String, callback: (ArrayList<User>) -> Unit){
        var groupMembers = ArrayList<User>()
        db.collection(groupCollection).whereEqualTo("groupId", "$groupId").get()
            .addOnSuccessListener { documents ->
                var groupDoc = documents.last()
                db.collection(userCollection).addSnapshotListener{ value, error ->
                    if (error != null) {
                        Log.e("Firestore Error", error.message.toString())
                        return@addSnapshotListener
                    }
                    for (dc: DocumentChange in value?.documentChanges!!) {
                        if (dc.type == DocumentChange.Type.ADDED &&
                            (groupDoc.get("users").toString().contains(dc.document.id) || groupDoc.get("admin").toString() == dc.document.id)) {
                            groupMembers.add(dc.document.toObject(User::class.java))
                        }
                    }
                    callback(groupMembers)
                }
            }
            .addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }

    fun getGroupAdmin(groupId: String, callback: (String) -> Unit){
        db.collection(groupCollection).whereEqualTo("groupId", "$groupId").get()
            .addOnSuccessListener { documents ->
                var groupDoc = documents.last()
                callback(groupDoc.get("admin").toString())
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getProposalData(groupId: String, callback: (ArrayList<Proposal>) -> Unit) {
        var proposalArrayList = ArrayList<Proposal>()
        db.collection(proposalCollection).whereEqualTo("groupId", "$groupId")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("Firestore Error", error.message.toString())
                    return@addSnapshotListener
                }
                for (dc: DocumentChange in value?.documentChanges!!) {
                    //cerco e aggiungo i gruppi che contengono l'email dell'utente
                    val currentDateTime = LocalDateTime.now()
                    val currDocDate = LocalDateTime.parse(dc.document.get("dateTime").toString(), DateTimeFormatter.ISO_DATE_TIME)
                    val canceled = dc.document.contains("canceled")
                    if (currDocDate.isAfter(currentDateTime))
                        if (dc.type == DocumentChange.Type.ADDED && !(dc.document.toString().contains(currentUserEmail()) || canceled))
                            proposalArrayList.add(dc.document.toObject(Proposal::class.java))
                        else if (dc.type == DocumentChange.Type.MODIFIED && (dc.document.toString().contains(currentUserEmail()) || canceled)){
                            proposalArrayList.removeIf{ p ->
                                p.proposalId == dc.document.get("proposalId").toString()
                            }
                        }
                }
                callback(proposalArrayList)
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getUserHistoryProposalData(callback: (ArrayList<Proposal>) -> Unit) {
        var proposalArrayList = ArrayList<Proposal>()
        getUserGroupsData { groupList, _ ->
            db.collection(proposalCollection).addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("Firestore Error", error.message.toString())
                    return@addSnapshotListener
                }
                for (dc: DocumentChange in value?.documentChanges!!) {
                    var stringDoc = dc.document.toString()
                    for (group in groupList) {
                        if (dc.document.get("groupId") == group.groupId) {
                            if (dc.type == DocumentChange.Type.ADDED
                                && (stringDoc.contains(currentUserEmail())
                                        || LocalDateTime.now().isAfter(
                                    LocalDateTime.parse(
                                        dc.document.get("dateTime").toString()
                                    )
                                )
                                        || dc.document.contains("canceled"))
                            ) {
                                proposalArrayList?.add(dc?.document?.toObject(Proposal::class.java))
                            } else if (dc.type == DocumentChange.Type.REMOVED) {
                                proposalArrayList.removeIf { p ->
                                    p.proposalId == dc.document.get("proposalId").toString()
                                }
                            }
                        }
                    }
                }
                callback(proposalArrayList)
            }
        }
    }

    fun getProposalPartecipants(proposalId: String, callback: (ArrayList<String>) -> Unit){
        var partecipants = ArrayList<String>()
        db.collection(proposalCollection).whereEqualTo("proposalId", "$proposalId").get()
            .addOnSuccessListener { proposalDocs ->
                val organizator = proposalDocs.last()?.get("organizator") as String
                partecipants.add(organizator)
                if(proposalDocs.last()?.get("accepters") != null) {
                    var partecipants: ArrayList<String> = proposalDocs?.last()?.get("accepters")!! as ArrayList<String>
                    partecipants.add(organizator)
                    Log.e(TAG, partecipants.toString())
                }
                callback(partecipants)
            }
    }

    private fun getGroupDocumentId(groupId: String, callback: (String) -> Unit) {
        db.collection(groupCollection).whereEqualTo("groupId", "$groupId").get()
            .addOnSuccessListener { foundGroupId ->
                callback(foundGroupId.last().id)
            }.addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }

    private fun getProposalDocumentId(proposalId: String, callback: (String) -> Unit) {
        db.collection(proposalCollection).whereEqualTo("proposalId", "$proposalId").get()
            .addOnSuccessListener { foundProposalDocId ->
                callback(foundProposalDocId.last().id)
            }.addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
    }

    private fun getProposalId(groupId: String, callback: (String) -> Unit) {
        db.collection(proposalCollection).whereEqualTo("groupId", groupId).get()
            .addOnSuccessListener { foundProposalId ->
                for (dc in foundProposalId) {
                    callback(dc.data["proposalId"].toString())
                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getChatData(proposalId: String, callback: (ArrayList<Message>) -> Unit){
        val chatArray = ArrayList<Message>()
        db.collection(chatCollection).document(proposalId).collection(messageSubCollection).addSnapshotListener { value, error ->
            if (error != null) {
                Log.e("Firestore Error", error.message.toString())
                return@addSnapshotListener
            }
            for (dc: DocumentChange in value?.documentChanges!!) {
                if (dc.type == DocumentChange.Type.ADDED && dc.document.id != "firstMessage")
                    chatArray.add(dc.document.toObject(Message::class.java))
            }
            callback(chatArray)
        }
    }

    fun setProposalState(proposalId: String, state: String, callback: (Boolean) -> Unit){
        getProposalDocumentId(proposalId) { proposalDoc ->
            // Remove the 'user' field from the document
            if(state == "accepted") {
                db.collection(proposalCollection).document(proposalDoc)
                    .update("accepters", FieldValue.arrayUnion(currentUserEmail()))
                    .addOnSuccessListener {
                        Log.d(TAG, "Proposal state of user ${currentUserId()} setted")
                        callback(true)
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error setting proposal state", e)
                        callback(false)
                    }
            }else{
                db.collection(proposalCollection).document(proposalDoc)
                    .update("decliners", FieldValue.arrayUnion(currentUserEmail()))
                    .addOnSuccessListener {
                        Log.d(TAG, "Proposal state of user ${currentUserId()} setted")
                        callback(true)
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error setting proposal state", e)
                        callback(false)
                    }
            }
        }
    }

    fun addUserToGroup(groupId: String, callback: (String) -> Unit) {
        isAlreadyMemberOf(groupId) { alreadyMember, groupDocId ->
            /*inserisco il nuovo utente al gruppo su FireStore assegnando come id
            il return della callback di checkForNewUserId
            */
            if (alreadyMember == "AM" || alreadyMember == "ER") {
                callback(alreadyMember)
            } else {
                db.collection(groupCollection).document(groupDocId)  //nome gruppo
                    .update("users", FieldValue.arrayUnion(currentUserEmail()))
                    .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                    .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
                callback(alreadyMember)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addMessageToChat(msgText: String, proposalId: String){
        currentUserNickname { nickname ->
            val message = hashMapOf(
                "owner" to currentUserId(),
                "ownerNickname" to nickname,
                "text" to msgText
            )
            db.collection(chatCollection).document(proposalId).collection(messageSubCollection)
                .document("message_${LocalDateTime.now()}").set(message)
        }
    }

    //DELETE DATA METHODS
    fun leaveGroup(groupId: String, callback: (Boolean) -> Unit) {
        getGroupDocumentId(groupId) { groupDoc ->
            // Remove the 'user' field from the document
            db.collection(groupCollection).document(groupDoc).update("users", FieldValue.arrayRemove(currentUserEmail()))
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully deleted!")
                callback(true)}
            .addOnFailureListener {
                e -> Log.w(TAG, "Error deleting document", e)
                callback(false)}
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun deleteGroupData(groupId: String) {
        //solo per ADMINISTRATORS
        getGroupDocumentId(groupId) { documentToDelete ->
            //ELIMINO IL GRUPPO
            db.collection(groupCollection).document(documentToDelete).delete()
                .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!") }
                .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
            //ELIMINO LE PROPOSTE
            db.collection(proposalCollection).whereEqualTo("groupId", "$groupId").get()
                .addOnSuccessListener { proposalDocs ->
                    for (dc in proposalDocs)
                        db.collection(proposalCollection).document(dc.id).delete()
                }
            getProposalId(groupId) { proposalId ->
                db.collection(chatCollection).document(proposalId).collection(messageSubCollection).get()
                    .addOnSuccessListener { chatToDelete ->
                        for (dc in chatToDelete) {
                            db.collection(chatCollection).document(proposalId).collection(messageSubCollection).document(dc.id).delete()
                        }
                        db.collection(chatCollection).document(proposalId).delete()
                    }
            }
        }
    }

    fun removeMemberGroup(groupId: String, email: String, callback: (Boolean) -> Unit){
        getGroupDocumentId(groupId) { groupDoc ->
            Log.e(TAG, "$groupDoc")
            db.collection(groupCollection).document(groupDoc)
                .update("users", FieldValue.arrayRemove("$email"))
                .addOnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot successfully deleted!")
                    callback(true)
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error deleting document", e)
                    callback(false)
                }
        }
    }

    fun cancelProposal(proposalId: String, callback: (Boolean) -> Unit) {
        db.collection(proposalCollection).whereEqualTo("proposalId", "$proposalId").get().addOnSuccessListener { documents ->
            val docId = documents.last().id
            val proposal = hashMapOf<String, Any>(
                "canceled" to "canceled"
            )
            db.collection(proposalCollection).document(docId).update(proposal)
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

    fun modifyProposalData(proposalId: String, proposalName: String, dateTime: String, place: String, groupId: String, organizator: String, organizatorId: String, groupName: String, callback: (Boolean) -> Unit){
        db.collection(proposalCollection).whereEqualTo("proposalId", "$proposalId").get().addOnSuccessListener { documents ->
            val docId = documents.last().id
            val proposal = hashMapOf<String, Any>(
                "dateTime" to dateTime,
                "place" to place,
                "proposalName" to proposalName,
                "groupId" to groupId,
                "groupName" to groupName,
                "organizator" to organizator,
                "organizatorId" to organizatorId,
                "proposalId" to proposalId
            )
            db.collection(proposalCollection).document(docId)
                .delete()
                .addOnSuccessListener {
                    Log.d(TAG, "DocumentSnapshot successfully deleted!")
                    db.collection(proposalCollection).document(docId)
                        .set(proposal).addOnSuccessListener {
                            callback(true)
                        }
                        .addOnFailureListener { e ->
                            Log.w(TAG, "Error writing document", e)
                            callback(false)
                        }
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error deleting document", e)
                }
        }
    }

    //OTHER METHODS
    fun checkDuplicateNickname(nickname: String, callback: (Boolean) -> Unit) {
        var duplicateNick = false
        db.collection(userCollection).get().addOnSuccessListener { collectionUser ->
            for(dc in collectionUser){
                if(dc.get("nickname") == nickname)
                    duplicateNick = true
            }
            callback(duplicateNick)
        }
    }

    private fun isAlreadyMemberOf(groupId: String, callback: (String, String) -> Unit) {
        //RETURN INFO: AM=already member, NM=not member, ER=group code not valid
        db.collection(groupCollection).whereEqualTo("groupId", "$groupId").get()
            .addOnSuccessListener { documents ->
                when {
                    documents.size() == 0 -> {
                        callback("ER", "")
                    }
                    documents.size() == 1 -> {
                        when {
                            documents.last().get("users").toString().contains(currentUserEmail()) ->
                                callback("AM", "")
                            documents.last().get("admin").toString() == currentUserEmail() ->
                                callback("AM", "")
                            else ->
                                callback("NM", documents.last().id)
                        }
                    }
                }
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
