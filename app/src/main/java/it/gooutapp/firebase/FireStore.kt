package it.gooutapp.firebase

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import it.gooutapp.R
import it.gooutapp.adapter.ProposalAdapter
import it.gooutapp.model.*
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

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
        val creationDate = LocalDateTime.now().toString()
        currentUserNickname { currNickname ->
            val proposal = hashMapOf(
                "groupId" to groupId,
                "dateTime" to dateTime,
                "organizator" to currNickname,
                "organizatorId" to currentUserId(),
                "place" to place,
                "groupName" to groupName,
                "proposalId" to proposalId,
                "proposalName" to proposalName,
                "read" to FieldValue.arrayUnion(currentUserEmail()),
                "creationDate" to creationDate
            )
            db.collection(proposalCollection).document("proposal_$creationDate")
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun getUserHomeData(context: Context, callback: (ArrayList<Group>, ArrayList<Boolean>, HashMap<String, Notification>, HashMap<String, MessagePreview>) -> Unit) {
        val userGroupsList = ArrayList<Group>()
        val adminFlagList = ArrayList<Boolean>()
        db.collection(groupCollection).addSnapshotListener { value, error ->
            if (error != null) {
                Log.e("Firestore Error", error.message.toString())
                return@addSnapshotListener
            }
            for (dc: DocumentChange in value?.documentChanges!!) {
                val thisGroup = dc.document.toObject(Group::class.java)
                //cerco e aggiungo i gruppi che contengono l'email dell'utente
                if (thisGroup.admin == currentUserEmail()) {
                    adminFlagList.add(true)
                    if (dc.type == DocumentChange.Type.ADDED)
                        userGroupsList.add(thisGroup)
                    else if (dc.type == DocumentChange.Type.REMOVED)
                        userGroupsList.remove(thisGroup)
                } else if (thisGroup.users?.contains(currentUserEmail()) == true) {
                    adminFlagList.add(false)
                    if (dc.type == DocumentChange.Type.ADDED)
                        userGroupsList.add(thisGroup)
                    else if (dc.type == DocumentChange.Type.REMOVED)
                        userGroupsList.remove(thisGroup)
                }else{
                    if (dc.type == DocumentChange.Type.MODIFIED)
                        userGroupsList.remove(thisGroup)
                }
            }
            getAllUserProposals{ proposalsList ->
                var notificationHM = HashMap<String, Notification>()
                var lastMessageHM = HashMap<String, MessagePreview>()
                var n: Notification
                var counter = 0
                for(proposal in proposalsList){
                    if(proposal.read?.contains(currentUserEmail()) == false){
                        if (notificationHM[proposal.groupId.toString()] != null) {
                            counter = notificationHM[proposal.groupId.toString()]?.numNotification!!
                            counter++
                        }else {
                            counter = 1
                        }
                        n = Notification(proposal.groupId, counter)
                        notificationHM[proposal.groupId.toString()] = n
                    }
                    if (currentUserId() == proposal.organizatorId)
                        lastMessageHM[proposal.groupId.toString()] = MessagePreview("${context.resources.getString(R.string.you)}: ${context.resources.getString(R.string.menu_new_proposal)} '${proposal.proposalName}'", proposal.creationDate)
                    else
                        lastMessageHM[proposal.groupId.toString()] = MessagePreview("${proposal.organizator}: ${context.resources.getString(R.string.menu_new_proposal)} '${proposal.proposalName}'", proposal.creationDate)
                }
                callback(userGroupsList, adminFlagList, notificationHM, lastMessageHM)
            }
        }
    }

    private fun getUserGroupData(callback: (ArrayList<Group>) -> Unit) {
        val userGroupsList = ArrayList<Group>()
        db.collection(groupCollection).addSnapshotListener { value, error ->
            if (error != null) {
                Log.e("Firestore Error", error.message.toString())
                return@addSnapshotListener
            }
            for (dc: DocumentChange in value?.documentChanges!!) {
                val thisGroup = dc.document.toObject(Group::class.java)
                //cerco e aggiungo i gruppi che contengono l'email dell'utente
                if (dc.type == DocumentChange.Type.ADDED)
                    userGroupsList.add(thisGroup)
                else if (dc.type == DocumentChange.Type.REMOVED)
                    userGroupsList.remove(thisGroup)
                else if (thisGroup.users?.contains(currentUserEmail()) == true) {
                    if (dc.type == DocumentChange.Type.ADDED)
                        userGroupsList.add(thisGroup)
                    else if (dc.type == DocumentChange.Type.REMOVED)
                        userGroupsList.remove(thisGroup)
                }else{
                    if (dc.type == DocumentChange.Type.MODIFIED)
                        userGroupsList.remove(thisGroup)
                }
            }
        }
        callback(userGroupsList)
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
    fun getGroupProposalData(groupId: String, callback: (ArrayList<Proposal>) -> Unit) {
        var proposalArrayList = ArrayList<Proposal>()
        db.collection(proposalCollection).whereEqualTo("groupId", "$groupId")
            .addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("Firestore Error", error.message.toString())
                    return@addSnapshotListener
                }
                for (dc: DocumentChange in value?.documentChanges!!) {
                    //cerco e aggiungo i gruppi che contengono l'email dell'utente
                    val thisProposal = dc.document.toObject(Proposal::class.java)
                    val currentDateTime = LocalDateTime.now()
                    val currDocDate = LocalDateTime.parse(thisProposal.dateTime, DateTimeFormatter.ISO_DATE_TIME)
                    val canceled = thisProposal.canceled == "canceled"
                    val isAccepted = thisProposal.accepters?.contains(currentUserEmail())
                    val isDeclined = thisProposal.decliners?.contains(currentUserEmail())
                    val isArchived = thisProposal.archived?.contains(currentUserEmail())
                    if (currDocDate.isAfter(currentDateTime))
                        if (dc.type == DocumentChange.Type.ADDED && !(isArchived == true || canceled))
                            proposalArrayList.add(thisProposal)
                        else if (dc.type == DocumentChange.Type.MODIFIED && (isArchived == true || canceled)){
                            proposalArrayList.removeIf{ p ->
                                p.proposalId == thisProposal.proposalId
                            }
                        }else if (dc.type == DocumentChange.Type.REMOVED){
                            proposalArrayList.removeIf { p ->
                                p.proposalId == thisProposal.proposalId
                            }
                        }
                }
                callback(proposalArrayList)
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getAllUserProposals(callback: (ArrayList<Proposal>) -> Unit) {
        var proposalArrayList = ArrayList<Proposal>()
        db.collection(proposalCollection).addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("Firestore Error", error.message.toString())
                    return@addSnapshotListener
                }
                for (dc: DocumentChange in value?.documentChanges!!) {
                    val thisProposal = dc.document.toObject(Proposal::class.java)
                    val currentDateTime = LocalDateTime.now()
                    val currDocDate = LocalDateTime.parse(thisProposal.dateTime, DateTimeFormatter.ISO_DATE_TIME)
                    val canceled = thisProposal.canceled == "canceled"
                    val alreadyAccepted = thisProposal.accepters?.contains(currentUserEmail())
                    val alreadyDeclined = thisProposal.decliners?.contains(currentUserEmail())
                    if (currDocDate.isAfter(currentDateTime))
                        if (dc.type == DocumentChange.Type.ADDED && !(alreadyAccepted == true || alreadyDeclined == true || canceled))
                            proposalArrayList.add(thisProposal)
                        else if (dc.type == DocumentChange.Type.MODIFIED && (alreadyAccepted == true || alreadyDeclined == true || canceled)){
                            proposalArrayList.removeIf{ p ->
                                p.proposalId == thisProposal.proposalId
                            }
                        } else if(dc.type == DocumentChange.Type.REMOVED && (alreadyAccepted == true || alreadyDeclined == true || canceled)){
                            proposalArrayList.removeIf{ p ->
                                p.proposalId == thisProposal.proposalId
                            }
                        }
                }
                callback(proposalArrayList)
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getUserHistoryProposalData(callback: (ArrayList<Proposal>) -> Unit) {
        var proposalArrayList = ArrayList<Proposal>()
        getUserGroupData{ groupList ->
            db.collection(proposalCollection).addSnapshotListener { value, error ->
                if (error != null) {
                    Log.e("Firestore Error", error.message.toString())
                    return@addSnapshotListener
                }
                for (dc: DocumentChange in value?.documentChanges!!) {
                    val thisProposal = dc.document.toObject(Proposal::class.java)
                    val canceled = thisProposal.canceled == "canceled"
                    val isArchived = thisProposal.archived?.contains(currentUserEmail())
                    for (group in groupList) {
                        if (thisProposal.groupId == group.groupId) {
                            if (dc.type == DocumentChange.Type.ADDED && (LocalDateTime.now().isAfter(LocalDateTime.parse(thisProposal.dateTime))) || isArchived == true || canceled) {
                                proposalArrayList?.add(thisProposal)
                            } else if (dc.type == DocumentChange.Type.MODIFIED && !(LocalDateTime.now().isAfter(LocalDateTime.parse(thisProposal.dateTime))) || isArchived == true || canceled) {
                                proposalArrayList.removeIf { p ->
                                    p.proposalId == thisProposal.proposalId
                                }
                            } else if (dc.type == DocumentChange.Type.REMOVED) {
                                proposalArrayList.removeIf { p ->
                                    p.proposalId == thisProposal.proposalId
                                }
                            }
                        }
                    }
                }
                callback(proposalArrayList)
            }
        }
    }

    fun getProposalPartecipants(proposalId: String, context: Context, callback: (ArrayList<String>) -> Unit){
        var partecipants = ArrayList<String>()
        db.collection(proposalCollection).whereEqualTo("proposalId", "$proposalId").get()
            .addOnSuccessListener { proposalDocs ->
                var proposalDoc = proposalDocs.last()
                var accepters = if(proposalDoc?.get("accepters") != null)
                                    proposalDoc?.get("accepters") as ArrayList<String>
                                else null

                val organizator = proposalDoc?.get("organizator") as String
                var stringOrg = if (proposalDoc?.get("organizatorId") == currentUserId())
                                    context.resources.getString(R.string.you)
                                else
                                    context.resources.getString(R.string.organizator)

                if (accepters != null && accepters.size > 0) {
                    getUsersNickname(accepters) { nicksArray ->
                        partecipants = nicksArray
                        partecipants.add("$organizator ($stringOrg)")
                        callback(partecipants)
                    }
                } else {
                    partecipants.add("$organizator ($stringOrg)")
                    callback(partecipants)
                }
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

    fun setProposalArchived(proposalId: String, callback: (Boolean) -> Unit){
        getProposalDocumentId(proposalId) { proposalDoc ->
            db.collection(proposalCollection).document(proposalDoc)
                .update("archived", FieldValue.arrayUnion(currentUserEmail()))
                .addOnSuccessListener {
                    Log.d(TAG, "Proposal archived for user ${currentUserId()}")
                    callback(true)
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error setting proposal as archived", e)
                    callback(false)
                }
        }
    }

    fun setProposalState(proposalId: String, state: String, callback: (Boolean) -> Unit){
        getProposalDocumentId(proposalId) { proposalDoc ->
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
                db.collection(proposalCollection).document(proposalDoc)
                    .update("decliners", FieldValue.arrayRemove(currentUserEmail()))
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
                db.collection(proposalCollection).document(proposalDoc)
                    .update("accepters", FieldValue.arrayRemove(currentUserEmail()))
            }
        }
    }

    fun setReadProposal(proposalToRead: ArrayList<Proposal>){
        for(proposal in proposalToRead) {
            db.collection(proposalCollection).document("proposal_${proposal.creationDate}")  //nome gruppo
                .update("read", FieldValue.arrayUnion(currentUserEmail()))
                .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
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

    private fun getUsersNickname(emailArray: ArrayList<String>, callback: (ArrayList<String>) -> Unit){
        val nicksArray = ArrayList<String>()
        for(email in emailArray) {
            db.collection(userCollection).document(email).get().addOnSuccessListener { document ->
                nicksArray.add(document.get("nickname").toString())
                callback(nicksArray)
            }.addOnFailureListener { exception ->
                Log.d(TAG, "get failed with ", exception)
            }
        }
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
