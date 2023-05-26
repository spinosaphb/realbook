package com.realbook

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.realbook.models.ChatModel
import com.realbook.models.MessageModel
import com.realbook.models.UserModel

class ChatMessagesActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    private var messageList = mutableListOf<MessageModel>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_chat_messages)

        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()

        val buttonSendMessage = findViewById<Button>(R.id.send_message_btn)
        val layout = findViewById<LinearLayout>(R.id.messages_view_linear_layout)
        buttonSendMessage.setOnClickListener {
            createNewMessage(layout)
        }

        val fromUser = auth.currentUser ?: return;
        val fromUserId = fromUser.uid
        val userToId = intent.getStringExtra("user_id")


        val userRef = database.child("users")
        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val to = generateUser(snapshot.child(userToId!!))
                val from = generateUser(snapshot.child(fromUserId))
                updateUI(layout, from, to)
            }

            override fun onCancelled(error: DatabaseError) {

            }
        })

        val childId = "${fromUserId}${userToId}"
        val invertedChildId = "${userToId}${fromUserId}"
        var chatsRefList = mutableListOf<DatabaseReference>()

        val chatRef = database
            .child("chats")
            .child(childId)

        val invertedChatRef = database
            .child("chats")
            .child(invertedChildId)

        chatsRefList.add(chatRef)
        chatsRefList.add(invertedChatRef)

        chatsRefList.forEach { chatRef ->
            chatRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messages = snapshot.child("messages").children
                    for (child in messages) {
                        val message = child.getValue(MessageModel::class.java)
                        messageList.add(message!!)

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
        }

    }

    private fun createNewMessage(layout: LinearLayout) {
        val fromUser = auth.currentUser ?: return;
        val fromUserId = fromUser.uid

        val userToId = intent.getStringExtra("user_id")
        val userRef = database.child("users")

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val to = generateUser(snapshot.child(userToId!!))
                val from = generateUser(snapshot.child(fromUserId))

                val newMessageEditText = findViewById<EditText>(R.id.new_message_edit_text)
                val messageContent = newMessageEditText.text.toString()

                val message = MessageModel(
                    messageContent,
                    from,
                    to,
                    null,
                    null
                )

                messageList.add(message)

                val chatId = "${fromUserId}${userToId}"
                val chatIdInverse = "${userToId}${fromUserId}"

                val chatRef = database.child("chats")
                chatRef.child(chatId).get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val snapshot = task.result
                        if (snapshot.exists()) {
                            val chat = ChatModel(
                                messages = null,
                                score = messageList.size,
                                lastInteraction = null,
                                id = chatId,
                                user1 = from,
                                user2 = to
                            )

                            chatRef.child(chatId).setValue(chat)
                            chatRef
                                .child(chatId)
                                .child("messages")
                                .setValue(messageList)

                        } else {
                            chatRef.child(chatIdInverse).get().addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val snapshot = task.result
                                    if (snapshot.exists()) {
                                        val chat = ChatModel(
                                            messages = null,
                                            score = messageList.size,
                                            lastInteraction = null,
                                            id = chatId,
                                            user1 = to,
                                            user2 = from
                                        )

                                        chatRef.child(chatIdInverse).setValue(chat)
                                        chatRef
                                            .child(chatIdInverse)
                                            .child("messages")
                                            .setValue(messageList)

                                    } else {
                                        val chat = ChatModel(
                                            messages = null,
                                            score = messageList.size,
                                            lastInteraction = null,
                                            id = chatId,
                                            user1 = from,
                                            user2 = to
                                        )

                                        chatRef.child(chatId).setValue(chat)
                                        chatRef
                                            .child(chatId)
                                            .child("messages")
                                            .setValue(messageList)


                                    }
                                }
                            }
                        }
                    }
                }

                layout.removeAllViews()
                newMessageEditText.setText("")
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })

        sendNotification()
    }
    private fun updateUI(layout: LinearLayout, from: UserModel, to: UserModel) {
        val scrollView = findViewById<ScrollView>(R.id.messages_view_scrollview)

        val chattingWith = findViewById<TextView>(R.id.chatting_with_text_view)
        chattingWith.text = to.name
        val childId = "${from.id}${to.id}"
        val invertedChildId = "${to.id}${from.id}"
        var chatsRefList = mutableListOf<DatabaseReference>()

        val chatRef = database
            .child("chats")
            .child(childId)

        val invertedChatRef = database
            .child("chats")
            .child(invertedChildId)

        chatsRefList.add(chatRef)
        chatsRefList.add(invertedChatRef)

        chatsRefList.forEach { chatRef ->
            chatRef.addValueEventListener(object: ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val messages = snapshot.child("messages").children.sortedBy { it.child("sentAt").value as Long }
                    for (child in messages) {
                        val message = child.getValue(MessageModel::class.java) ?: return
                        val textView = TextView(baseContext)
                        textView.text = message?.content

                        val layoutParams = LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                        )
                        textView.layoutParams = layoutParams
                        textView.setPadding(26,26,26,26)

                        if(from.id == message.from.id) {
                            textView.background = ContextCompat.getDrawable(baseContext, R.drawable.purple_gradient)
                            layoutParams.gravity = Gravity.END
                            textView.setTextColor(Color.WHITE)

                        } else {
                            textView.background = ContextCompat.getDrawable(baseContext, R.drawable.gray_gradient)

                        }
                        layoutParams.setMargins(0,0,0,16)
                        layout.addView(textView)
                        scrollView.postDelayed({
                            scrollView.scrollTo(0, scrollView.bottom)
                        }, 100)

                    }
                }

                override fun onCancelled(error: DatabaseError) {
                }
            })

        }
    }

    private fun generateUser(snapshot: DataSnapshot): UserModel {
        val userName = snapshot.child("name").getValue(String::class.java)!!
        val userId = snapshot.child("id").getValue(String::class.java)!!
        val email = snapshot.child("email").getValue(String::class.java)!!
        val userAvatar = snapshot.child("avatar").getValue(String::class.java)!!
        val shareLocation = snapshot.child("shareLocation").getValue(Boolean::class.java)!!
        val location = snapshot.child("location").getValue(UserModel.Coords::class.java)!!

        return UserModel(
            id = userId,
            name = userName,
            avatar = userAvatar,
            location = location,
            shareLocation = shareLocation,
            email = email,
            friends = null
        )
    }
    private fun sendNotification() {
        var builder = NotificationCompat.Builder(this, NotificationChannelCompat.DEFAULT_CHANNEL_ID)
            .setSmallIcon(R.drawable.messages)
            .setContentTitle("Uma nova mensagem foi enviada a você")
            .setContentText("Essa é uma nova mensagem")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    baseContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(1, builder.build())
        }
    }


}