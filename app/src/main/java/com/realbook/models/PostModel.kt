package com.realbook.models

class PostModel {
    var id: String? = null
    var content: String? = null
    var imageUrl: String? = null
    var createdByUser:  UserModel? = null
    var likes: MutableList<UserModel>? = null

    constructor() {}

    constructor(id: String?, content: String?, imageUrl: String?, createdByUser: UserModel?, likes: MutableList<UserModel>?) {
        this.id = id
        this.content = content
        this.imageUrl = imageUrl
        this.createdByUser = createdByUser
        this.likes = likes
    }

    override fun toString(): String {
        return "Post(id=$id, content=$content, imageUrl=$imageUrl, createdByUser=$createdByUser), likes=$likes"
    }
}