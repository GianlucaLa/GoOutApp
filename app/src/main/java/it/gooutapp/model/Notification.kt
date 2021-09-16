package it.gooutapp.model

data class Notification(
    var groupName: String ?= null,
    var message: String ?= null,
    var proposalCreationDate: String ?= null,
    var proposalId: String ?= null
)
