package com.realbook.models

import java.util.UUID

class UserModel {
    var id: String = ""
    var name: String = ""
    var email: String = ""
    var avatar: String = ""
    var shareLocation: Boolean = false
    var location: Coords? = null
    var friends: Array<UserModel>? = null

    constructor(name: String, email: String, avatar: String, shareLocation: Boolean, location: Coords, friends: Array<UserModel>?, id: String?) {
        if (id != null) this.id = id
        else this.id = UUID.randomUUID().toString()
        this.name = name
        this.email = email
        this.avatar = avatar
        this.shareLocation = shareLocation
        this.location = location
        this.friends = friends
    }

    override fun toString(): String {
        return "User(id='$id', name='$name', email='$email', avatar='$avatar', shareLocation=$shareLocation, location=$location, friends=${friends?.contentToString()})"
    }

    data class Coords(val latitude: Double? = 0.0, val longitude: Double? = 0.0) {}
}