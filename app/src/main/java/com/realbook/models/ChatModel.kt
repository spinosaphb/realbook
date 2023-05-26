package com.realbook.models

import java.util.Date
import java.util.UUID

class ChatModel {
    var id: String = ""
    var messages: MutableList<MessageModel>? = null
    var score: Number = 0
    var lastInteraction: Date? = null

    var user1: UserModel? = null
    var user2: UserModel? = null

    constructor() {}

    constructor(messages: MutableList<MessageModel>?, score: Number, lastInteraction: Date?, id: String?, user1: UserModel?, user2: UserModel?) {
        this.messages = messages
        this.score = score
        this.lastInteraction = lastInteraction
        if (id != null) this.id = id
        else this.id = UUID.randomUUID().toString()
        this.user1 = user1
        this.user2 = user2
    }

    override fun toString(): String {
        return "ChatModel(id='$id', messages=$messages, score=$score, lastInteraction=$lastInteraction)"
    }


}