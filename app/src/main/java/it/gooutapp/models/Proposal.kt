package it.gooutapp.models

import com.google.firebase.Timestamp

class Proposal(var proposalName: String ?= null, var place: String ?= null, var date: Timestamp ?= null) {
}