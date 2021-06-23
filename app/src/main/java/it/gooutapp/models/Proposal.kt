package it.gooutapp.models

class Proposal(
    var proposalName: String ?= null,
    var place: String ?= null,
    var date: String ?= null,
    var time: String?= null,
    var organizator: String?= null,
    var proposalCode: String ?= null
)