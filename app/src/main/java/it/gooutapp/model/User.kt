package it.gooutapp.model

import java.io.Serializable

class User(
    var name: String ?= null,
    var surname: String ?= null,
    var nickname: String ?= null,
    var email: String ?= null,
    var authId: String ?= null
): Serializable