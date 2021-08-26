package it.gooutapp.model

data class Group(var admin: String ?= null,
                 var groupName: String ?= null,
                 var groupId: String ?= null,
                 var users: ArrayList<String> ?= null)
