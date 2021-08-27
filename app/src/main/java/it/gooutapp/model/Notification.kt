package it.gooutapp.model

data class Notification(
    var groupId: String ?= null,
    var numNotification: Int ?= null,
    var lastMessage: String ?= null,
    var time: String ?= null
)
