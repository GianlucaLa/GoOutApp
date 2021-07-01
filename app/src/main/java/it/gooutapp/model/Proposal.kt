package it.gooutapp.model

class Proposal(
    var groupId: String ?= null,
    var proposalId: String ?= null,
    var proposalName: String ?= null,
    var place: String ?= null,
    var dateTime: String ?= null,
    var organizator: String?= null,
    var organizatorId: String?= null
)