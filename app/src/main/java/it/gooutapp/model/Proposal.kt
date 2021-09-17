package it.gooutapp.model

data class Proposal(
    var groupId: String ?= null,
    var groupName: String ?= null,
    var proposalId: String ?= null,
    var proposalName: String ?= null,
    var place: String ?= null,
    var placeAddress: String ?= null,
    var dateTime: String ?= null,
    var organizator: String?= null,
    var organizatorId: String?= null,
    var accepters: ArrayList<String> ?= null,
    var decliners: ArrayList<String> ?= null,
    var archived: ArrayList<String> ?= null,
    var readCanceled: ArrayList<String> ?= null,
    var readModified: ArrayList<String> ?= null,
    var sendedNotification: ArrayList<String> ?= null,
    var sendedNotificationModified: ArrayList<String> ?= null,
    var sendedNotificationCanceled: ArrayList<String> ?= null,
    var read: ArrayList<String> ?= null,
    var canceled: String ?= null,
    var modified: String ?= null,
    var creationDate: String ?= null,
    var cancelCreationDate: String ?= null,
    var modifiedCreationDate: String ?= null
)