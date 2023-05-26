package com.realbook

import java.util.Date
import java.util.UUID

class Message {
    var id: String = ""
    var content: String = ""
    var from: User = User()
    var to: User = User()
    var sentAt: Long? = null

    constructor() {}

    constructor(content: String, from: User, to: User, sentAt: Long?, id: String?) {
        this.content = content
        this.from = from
        this.to = to
        val now = Date()
        this.sentAt = sentAt ?: (now.time / 1000)
        if (id != null) this.id = id
        else this.id = UUID.randomUUID().toString()
    }
}