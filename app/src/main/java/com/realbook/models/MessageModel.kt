package com.realbook.models

import java.util.Date
import java.util.UUID

class MessageModel {
    var id: String = ""
    var content: String = ""
    var from: UserModel = UserModel()
    var to: UserModel = UserModel()
    var sentAt: Long? = null

    constructor() {}

    constructor(content: String, from: UserModel, to: UserModel, sentAt: Long?, id: String?) {
        this.content = content
        this.from = from
        this.to = to
        val now = Date()
        this.sentAt = sentAt ?: (now.time / 1000)
        if (id != null) this.id = id
        else this.id = UUID.randomUUID().toString()
    }
}