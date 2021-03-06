package it.gooutapp.firebase

import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.*
import com.google.firebase.ktx.Firebase
import it.gooutapp.R
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
    fun createUserData(name: String, surname: String, nickname: String, email: String, callback: (Boolean) -> Unit) {
        val user = hashMapOf(
            "name" to name,
            "surname" to surname,
            "nickname" to nickname,
            "email" to email,
            "authId" to currentUserId()
        )
        db.collection(userCollection).document(email)
            .set(user)
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully written!")
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error writing document", e)
                callback(false)
            }
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
    fun createProposalData(groupId: String, proposalName: String, dateTime: String, place: String, placeAddress: String,groupName: String, callback: (Boolean) -> Unit){
        val proposalId: String = List(15) { source.random() }.joinToString("")
        val creationDate = LocalDateTime.now().toString()
        currentUserNickname { currNickname ->
            val proposal = hashMapOf(
                "groupId" to groupId,
                "dateTime" to dateTime,
                "organizator" to currNickname,
                "organizatorId" to currentUserId(),
                "place" to place,
                "placeAddress" to placeAddress,
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
    fun getUserHomeData(context: Context, callback: (ArrayList<Group>, ArrayList<Boolean>, HashMap<String, NotificationCounter>, HashMap<String, MessagePreview>) -> Unit) {
        val userGroupsList = ArrayList<Group>()
        val adminFlagList = ArrayList<Boolean>()
        var groupIdList = ArrayList<String>()
        db.collection(groupCollection).get().addOnSuccessListener { documents ->
            for (dc in documents) {
                val thisGroup = dc.toObject(Group::class.java)
                //cerco e aggiungo i gruppi che contengono l'email dell'utente
                if (thisGroup.admin == currentUserEmail()) {
                    adminFlagList.add(true)
                    userGroupsList.add(thisGroup)
                    groupIdList.add(thisGroup.groupId.toString())
                } else if (thisGroup.users?.contains(currentUserEmail()) == true) {
                    adminFlagList.add(false)
                    userGroupsList.add(thisGroup)
                    groupIdList.add(thisGroup.groupId.toString())
                }
            }
            getAllUserProposals { proposalsList ->
                var notificationHM = HashMap<String, NotificationCounter>()
                var lastMessageHM = HashMap<String, MessagePreview>()
                var n: NotificationCounter
                var counter = 0
                for(proposal in proposalsList){
                    if(proposal.readModified?.contains(currentUserEmail()) == false){
                        if (notificationHM[proposal.groupId.toString()] != null) {
                            counter = notificationHM[proposal.groupId.toString()]?.numNotification!!
                            counter++
                        }else {
                            counter = 1
                        }
                        n = NotificationCounter(proposal.groupId, counter)
                        notificationHM[proposal.groupId.toString()] = n
                    }
                    if(proposal.readCanceled?.contains(currentUserEmail()) == false){
                        if (notificationHM[proposal.groupId.toString()] != null) {
                            counter = notificationHM[proposal.groupId.toString()]?.numNotification!!
                            counter++
                        }else {
                            counter = 1
                        }
                        n = NotificationCounter(proposal.groupId, counter)
                        notificationHM[proposal.groupId.toString()] = n
                    }
                    if(proposal.read?.contains(currentUserEmail()) == false){
                        if (notificationHM[proposal.groupId.toString()] != null) {
                            counter = notificationHM[proposal.groupId.toString()]?.numNotification!!
                            counter++
                        }else {
                            counter = 1
                        }
                        n = NotificationCounter(proposal.groupId, counter)
                        notificationHM[proposal.groupId.toString()] = n
                    }
                    if(proposal.canceled == "canceled"){
                        if (currentUserId() == proposal.organizatorId)
                            lastMessageHM[proposal.groupId.toString()] = MessagePreview("${context.resources.getString(R.string.you)}: ${context.resources.getString(R.string.canceled_proposal)} '${proposal.proposalName}'", proposal.cancelCreationDate)
                        else
                            lastMessageHM[proposal.groupId.toString()] = MessagePreview("${proposal.organizator}: ${context.resources.getString(R.string.canceled_proposal)} '${proposal.proposalName}'", proposal.cancelCreationDate)
                    }
                    else if(proposal.modified == "modified"){
                        if (currentUserId() == proposal.organizatorId)
                            lastMessageHM[proposal.groupId.toString()] = MessagePreview("${context.resources.getString(R.string.you)}: ${context.resources.getString(R.string.modifed_proposal)} '${proposal.proposalName}'", proposal.modifiedCreationDate)
                        else
                            lastMessageHM[proposal.groupId.toString()] = MessagePreview("${proposal.organizator}: ${context.resources.getString(R.string.modifed_proposal)} '${proposal.proposalName}'", proposal.modifiedCreationDate)
                    }
                    else{
                        if (currentUserId() == proposal.organizatorId)
                            lastMessageHM[proposal.groupId.toString()] = MessagePreview("${context.resources.getString(R.string.you)}: ${context.resources.getString(R.string.menu_new_proposal)} '${proposal.proposalName}'", proposal.creationDate)
                        else
                            lastMessageHM[proposal.groupId.toString()] = MessagePreview("${proposal.organizator}: ${context.resources.getString(R.string.menu_new_proposal)} '${proposal.proposalName}'", proposal.creationDate)
                    }
                    if(proposal.sendedNotification?.contains(currentUserEmail()) != true){
                        setSendedNotification(proposal.creationDate.toString()){}
                    }
                    if(proposal.modified == "modified" && proposal.sendedNotificationModified?.contains(currentUserEmail()) != true){
                        setSendedNotificationModified(proposal.creationDate.toString()){}
                    }
                    if(proposal.canceled == "canceled" && proposal.sendedNotificationCanceled?.contains(currentUserEmail()) != true){
                        setSendedNotificationCanceled(proposal.creationDate.toString()){}
                    }
                }
                callback(userGroupsList, adminFlagList, notificationHM, lastMessageHM)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getGroupPopupNotification(context: Context, groupId: String, callback: (ArrayList<String>) -> Unit){
        var proposalArrayList = ArrayList<Proposal>()
        db.collection(proposalCollection).whereEqualTo("groupId", "$groupId").get()
            .addOnSuccessListener { documents ->
            for (dc in documents) {
                val thisProposal = dc.toObject(Proposal::class.java)
                val currentDateTime = LocalDateTime.now()
                val currDocDate = LocalDateTime.parse(thisProposal.dateTime, DateTimeFormatter.ISO_DATE_TIME)
                if (currDocDate.isAfter(currentDateTime)){                         // se non scaduto
                    proposalArrayList.add(thisProposal)
                }
            }
            val notificationList = ArrayList<String>()
            var notification = ""
            for(proposal in proposalArrayList){
                if(proposal.read?.contains(currentUserEmail()) == false){
                    notification = "${proposal.organizator}: ${context.resources.getString(R.string.menu_new_proposal)} '${proposal.proposalName}'"
                    notificationList.add(notification)
                }
                if(proposal.readCanceled?.contains(currentUserEmail()) == false){
                    notification = "${proposal.organizator}: ${context.resources.getString(R.string.canceled_proposal)} '${proposal.proposalName}'"
                    notificationList.add(notification)
                }
                if(proposal.readModified?.contains(currentUserEmail()) == false){
                    notification = "${proposal.organizator}: ${context.resources.getString(R.string.modifed_proposal)} '${proposal.proposalName}'"
                    notificationList.add(notification)
                }
            }
            callback(notificationList)
        }
    }

    private fun getUserGroupData(callback: (ArrayList<Group>) -> Unit) {
        val userGroupsList = ArrayList<Group>()
        db.collection(groupCollection).get().addOnSuccessListener { documents ->
            for (dc in documents) {
                val thisGroup = dc.toObject(Group::class.java)
                //cerco e aggiungo i gruppi che contengono l'email dell'utente
                if (thisGroup.users?.contains(currentUserEmail()) == true || thisGroup.admin == currentUserEmail()) {
                    userGroupsList.add(thisGroup)
                }
            }
            callback(userGroupsList)
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
    fun getGroupProposalData(groupId: String, callback: (ArrayList<Proposal>) -> Unit) {
        var proposalArrayList = ArrayList<Proposal>()
        db.collection(proposalCollection).whereEqualTo("groupId", "$groupId").get()
            .addOnSuccessListener { documents ->
                for (dc in documents) {
                    //cerco e aggiungo i gruppi che contengono l'email dell'utente
                    val thisProposal = dc.toObject(Proposal::class.java)
                    val currentDateTime = LocalDateTime.now()
                    val currDocDate = LocalDateTime.parse(thisProposal.dateTime, DateTimeFormatter.ISO_DATE_TIME)
                    val isArchived = thisProposal.archived?.contains(currentUserEmail())
                    if (currDocDate.isAfter(currentDateTime))               //se non ?? scaduta
                        if (isArchived != true)                             //se non ?? archiviata
                            proposalArrayList.add(thisProposal)
                        else{
                            proposalArrayList.removeIf { p ->
                                p.proposalId == thisProposal.proposalId
                            }
                        }
                }
                callback(proposalArrayList)
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getGroupFragmentProposalData(groupId: String, callback: (ArrayList<Proposal>) -> Unit) {
        var proposalArrayList = ArrayList<Proposal>()
        db.collection(proposalCollection).whereEqualTo("groupId", "$groupId").get()
            .addOnSuccessListener { documents ->
                for (dc in documents) {
                    //cerco e aggiungo i gruppi che contengono l'email dell'utente
                    val thisProposal = dc.toObject(Proposal::class.java)
                    val currentDateTime = LocalDateTime.now()
                    val currDocDate = LocalDateTime.parse(thisProposal.dateTime, DateTimeFormatter.ISO_DATE_TIME)
                    val canceled = thisProposal.canceled == "canceled"
                    val isArchived = thisProposal.archived?.contains(currentUserEmail())
                    if (currDocDate.isAfter(currentDateTime))
                        if (!(isArchived == true || canceled))
                            proposalArrayList.add(thisProposal)
                        else if ((isArchived == true || canceled)) {
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
        db.collection(proposalCollection).get().addOnSuccessListener { documents ->
            for (dc in documents) {
                val thisProposal = dc.toObject(Proposal::class.java)
                val currentDateTime = LocalDateTime.now()
                val currDocDate = LocalDateTime.parse(thisProposal.dateTime)
                if (currDocDate.isAfter(currentDateTime))
                    proposalArrayList.add(thisProposal)
            }
            callback(proposalArrayList)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getAllUserProposalsLive(callback: (ArrayList<Proposal>) -> Unit) {
        var userGroupsList = ArrayList<String>()
        db.collection(groupCollection).get().addOnSuccessListener { documents ->
            for (dc in documents) {
                val thisGroup = dc.toObject(Group::class.java)
                //cerco e aggiungo i gruppi che contengono l'email dell'utente
                if (thisGroup.admin == currentUserEmail()) {
                    userGroupsList.add(thisGroup.groupId.toString())
                } else if (thisGroup.users?.contains(currentUserEmail()) == true) {
                    userGroupsList.add(thisGroup.groupId.toString())
                }
            }
            var proposalArrayList = ArrayList<Proposal>()
            db.collection(proposalCollection).whereIn("groupId", userGroupsList)
                .addSnapshotListener { value, error ->
                    if (error != null) {
                        Log.e("Firestore Error", error.message.toString())
                        return@addSnapshotListener
                    }
                    for (dc: DocumentChange in value?.documentChanges!!) {
                        val thisProposal = dc.document.toObject(Proposal::class.java)
                        val currentDateTime = LocalDateTime.now()
                        if(thisProposal.dateTime != null) {
                            val currDocDate = LocalDateTime.parse(
                                thisProposal.dateTime,
                                DateTimeFormatter.ISO_DATE_TIME
                            )
                            if (currDocDate.isAfter(currentDateTime))
                                when (dc.type) {
                                    DocumentChange.Type.ADDED -> proposalArrayList.add(thisProposal)
                                    DocumentChange.Type.MODIFIED -> proposalArrayList.add(thisProposal)
                                    DocumentChange.Type.REMOVED -> {
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun getUserHistoryProposalData(callback: (ArrayList<Proposal>) -> Unit) {
        var proposalArrayList = ArrayList<Proposal>()
        getUserGroupData{ groupList ->
            db.collection(proposalCollection).get().addOnSuccessListener { documents ->
                for (dc in documents) {
                    val thisProposal = dc.toObject(Proposal::class.java)
                    val canceled = thisProposal.canceled == "canceled"
                    val isArchived = thisProposal.archived?.contains(currentUserEmail())
                    for (group in groupList) {
                        if (thisProposal.groupId == group.groupId) {
                            if ((LocalDateTime.now().isAfter(LocalDateTime.parse(thisProposal.dateTime))) || isArchived == true || canceled) {
                                proposalArrayList?.add(thisProposal)
                            } else if (!(LocalDateTime.now().isAfter(LocalDateTime.parse(thisProposal.dateTime))) || isArchived == true || canceled) {
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

    fun getProposalPartecipants(proposalCreationDate: String, context: Context, callback: (ArrayList<String>) -> Unit){
        var partecipants = ArrayList<String>()
        db.collection(proposalCollection).document("proposal_$proposalCreationDate").get()
            .addOnSuccessListener{ document ->
                var proposal = document.toObject(Proposal::class.java)
                val organizator = proposal?.organizator
                var stringOrg = if ((proposal?.organizatorId) == currentUserId())
                                    context.resources.getString(R.string.you)
                                else
                                    context.resources.getString(R.string.organizator)
                if (proposal?.accepters != null && proposal.accepters!!.size > 0) {
                    Log.e(TAG, proposal.accepters.toString())
                    getUsersNickname(proposal.accepters!!) { nicksArray ->
                        Log.e(TAG, nicksArray.toString())
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

    fun setReadCanceledProposal(proposalToRead: ArrayList<Proposal>){
        for(proposal in proposalToRead) {
            db.collection(proposalCollection).document("proposal_${proposal.creationDate}")  //nome gruppo
                .update("readCanceled", FieldValue.arrayUnion(currentUserEmail()))
                .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
        }
    }

    fun setReadModifiedProposal(proposalToRead: ArrayList<Proposal>){
        for(proposal in proposalToRead) {
            db.collection(proposalCollection).document("proposal_${proposal.creationDate}")  //nome gruppo
                .update("readModified", FieldValue.arrayUnion(currentUserEmail()))
                .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully written!") }
                .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e) }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun addMessageToChat(msgText: String, proposalId: String){
        currentUserNickname { nickname ->
            val message = hashMapOf(
                "user_id" to currentUserId(),
                "nickname" to nickname,
                "text" to msgText
            )
            db.collection(chatCollection).document(proposalId).collection(messageSubCollection)
                .document("message_${LocalDateTime.now()}").set(message)
        }
    }

    fun addReadGroupNotifications(groupId: String, email: String, notificationType: String, callback: (Boolean) -> Unit){
        getGroupDocumentId(groupId) { groupDoc ->
            val arrayName = "read_${notificationType}_${email}"
            db.collection(groupCollection).document(groupDoc)
                .update(arrayName, FieldValue.arrayUnion(currentUserEmail()))
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
                    .addOnSuccessListener {
                        Log.d(TAG, "DocumentSnapshot successfully written!")
                        callback(alreadyMember)
                    }
                    .addOnFailureListener { e -> Log.w(TAG, "Error writing document", e)
                        callback("ERROR")
                    }
            }
        }
    }

    //DELETE DATA METHODS
    fun leaveGroup(groupId: String, callback: (Boolean) -> Unit) {
        getGroupDocumentId(groupId) { groupDoc ->
            // Remove the 'user' field from the document
            db.collection(groupCollection).document(groupDoc).update("users", FieldValue.arrayRemove(currentUserEmail()))
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot successfully deleted!")
                callback(true)
            }
            .addOnFailureListener {
                e -> Log.w(TAG, "Error deleting document", e)
                callback(false)
            }
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
            db.collection(proposalCollection).whereEqualTo("groupId", groupId).get()
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
                .update("users", FieldValue.arrayRemove(email))
                .addOnSuccessListener {
                    //addReadGroupNotifications(groupId, email, "remove") {
                        Log.d(TAG, "DocumentSnapshot successfully updated!")
                        callback(true)
                    //}
                }
                .addOnFailureListener { e ->
                    Log.w(TAG, "Error deleting document", e)
                    callback(false)
                }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun cancelProposal(proposalId: String, callback: (Boolean) -> Unit) {
        db.collection(proposalCollection).whereEqualTo("proposalId", proposalId).get()
            .addOnSuccessListener { documents ->
            val docId = documents.last().id
            val proposal = hashMapOf(
                "canceled" to "canceled",
                "cancelCreationDate" to LocalDateTime.now().toString(),
                "readCanceled" to FieldValue.arrayUnion(currentUserEmail())
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun modifyProposalData(place: String, placeAddress: String, dateTime: String, creationDate: String,callback: (Boolean) -> Unit){
        val deletes = hashMapOf<String, Any>(
            "place" to FieldValue.delete(),
            "placeAddress" to FieldValue.delete(),
            "dateTime" to FieldValue.delete(),
            "modified" to FieldValue.delete(),
            "modifiedCreationDate" to FieldValue.delete(),
            "sendedNotificationModified" to FieldValue.delete(),
            "readModified" to FieldValue.delete()
        )
        db.collection(proposalCollection).document("proposal_${creationDate}")
            .update(deletes).addOnSuccessListener {
                val updates = hashMapOf(
                    "place" to place,
                    "placeAddress" to placeAddress,
                    "dateTime" to dateTime,
                    "modified" to "modified",
                    "modifiedCreationDate" to LocalDateTime.now().toString(),
                    "readModified" to FieldValue.arrayUnion(currentUserEmail())
                )
                db.collection(proposalCollection).document("proposal_${creationDate}")
                    .update(updates).addOnSuccessListener {
                        Log.d(TAG, "DocumentSnapshot successfully updated!")
                        callback(true)
                    }
                    .addOnFailureListener { e ->
                        Log.w(TAG, "Error writing document", e)
                        callback(false)
                    }
            }
            .addOnFailureListener{ e ->
                Log.w(TAG, "Error writing document", e)
                callback(false)
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
        db.collection(groupCollection).whereEqualTo("groupId", groupId).get()
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
        db.collection(userCollection).whereIn("email", emailArray).get().addOnSuccessListener { documents ->
            for(dc in documents){
                nicksArray.add(dc.get("nickname").toString())
            }
            callback(nicksArray)
        }.addOnFailureListener { exception ->
            Log.d(TAG, "get failed with ", exception)
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

    fun setSendedNotification(proposalCreationDate: String, callback: (Boolean) -> Unit){
        db.collection(proposalCollection).document("proposal_${proposalCreationDate}")
            .update("sendedNotification", FieldValue.arrayUnion(currentUserEmail()))
            .addOnSuccessListener {
                Log.d(TAG, "user ${currentUserId()} added to sendedNotification array")
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error setting proposal as archived", e)
                callback(false)
            }
    }

    fun setSendedNotificationModified(proposalCreationDate: String, callback: (Boolean) -> Unit){
        db.collection(proposalCollection).document("proposal_${proposalCreationDate}")
            .update("sendedNotificationModified", FieldValue.arrayUnion(currentUserEmail()))
            .addOnSuccessListener {
                Log.d(TAG, "user ${currentUserId()} added to sendedNotification array")
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error setting proposal as archived", e)
                callback(false)
            }
    }

    fun setSendedNotificationCanceled(proposalCreationDate: String, callback: (Boolean) -> Unit){
        db.collection(proposalCollection).document("proposal_${proposalCreationDate}")
            .update("sendedNotificationCanceled", FieldValue.arrayUnion(currentUserEmail()))
            .addOnSuccessListener {
                Log.d(TAG, "user ${currentUserId()} added to sendedNotification array")
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Error setting proposal as archived", e)
                callback(false)
            }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun getNotification(context: Context, callback: (ArrayList<Notification>) -> Unit){
        getAllUserProposalsLive{ proposalsList ->
            val messageList = ArrayList<Notification>()
            val alreadySendedNewProposal = ArrayList<String>()
            val alreadySendedModified = ArrayList<String>()
            val alreadySendedCanceled = ArrayList<String>()
            for(proposal in proposalsList){
                if(proposal.read?.contains(currentUserEmail()) == false) {
                    if (!alreadySendedNewProposal.contains(proposal.proposalId.toString())) {
                        Log.e(TAG, "MESSAGGIO AGGIUNTO IN proposal")
                        if (proposal.sendedNotification?.contains(currentUserEmail()) != true) {
                            val n = Notification(
                                proposal.groupName,
                                "${proposal.organizator}: ${context.resources.getString(R.string.menu_new_proposal)} '${proposal.proposalName}'",
                                proposal.creationDate, proposal.proposalId
                            )
                            alreadySendedNewProposal.add(proposal.proposalId.toString())
                            messageList.add(n)
                        }
                    }
                }
                if(proposal.readModified?.contains(currentUserEmail()) == false) {
                    if (!alreadySendedModified.contains(proposal.proposalId.toString())) {
                        if (proposal.sendedNotificationModified?.contains(currentUserEmail()) != true) {
                            val n = Notification(
                                proposal.groupName,
                                "${proposal.organizator}: ${context.resources.getString(R.string.modifed_proposal)} '${proposal.proposalName}'",
                                proposal.creationDate, proposal.proposalId
                            )
                            alreadySendedModified.add(proposal.proposalId.toString())
                            messageList.add(n)
                        }
                    }
                }
                if(proposal.readCanceled?.contains(currentUserEmail()) == false && proposal.canceled == "canceled") {
                    if (!alreadySendedCanceled.contains(proposal.proposalId.toString())) {
                        if (proposal.sendedNotificationCanceled?.contains(currentUserEmail()) != true) {
                            val n = Notification(
                                proposal.groupName,
                                "${proposal.organizator}: ${context.resources.getString(R.string.canceled_proposal)} '${proposal.proposalName}'",
                                proposal.creationDate, proposal.proposalId
                            )
                            Log.e(TAG, "MESSAGGIO AGGIUNTO IN CANCELED")
                            alreadySendedCanceled.add(proposal.proposalId.toString())
                            messageList.add(n)
                        }
                    }
                }
            }
            callback(messageList)
        }
    }
}