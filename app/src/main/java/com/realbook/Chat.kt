package com.realbook

import java.util.Date
import java.util.UUID

class Chat {
    var id: String = ""
    var messages: MutableList<Message>? = null
    var score: Number = 0
    var lastInteraction: Date? = null

    var user1: User? = null
    var user2: User? = null

    constructor() {}

    constructor(messages: MutableList<Message>?, score: Number, lastInteraction: Date?, id: String?, user1: User?, user2: User?) {
        this.messages = messages
        this.score = score
        this.lastInteraction = lastInteraction
        if (id != null) this.id = id
        else this.id = UUID.randomUUID().toString()
        this.user1 = user1
        this.user2 = user2
    }

    override fun toString(): String {
        return "Chat(id='$id', messages=$messages, score=$score, lastInteraction=$lastInteraction)"
    }


}