package it.gooutapp.model

data class Message(var owner: String ?= null, var ownerNickname: String ?= null, var text: String ?= null)
