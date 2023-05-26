package com.realbook

import android.content.Intent
import android.graphics.Typeface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.realbook.models.MessageModel
import com.realbook.models.UserModel
import java.text.SimpleDateFormat
import java.util.Date

class ChatActivity : AppCompatActivity() {
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth
    var userChatting: UserModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_chat)

        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
        loadChats()
    }

    private fun loadChats() {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid

        database.child("chats")
            .addListenerForSingleValueEvent(object: ValueEventListener {
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) {
                        val key = child.key.toString()

                        if (key.endsWith(userId)) {
                            val chattingWithUser = child.child("user1").getValue(UserModel::class.java)
                            userChatting = chattingWithUser
                            val score = child.child("score").getValue(Int::class.java)
                            val lastMessageChild = child.child("messages").children.last()
                            if(lastMessageChild != null) {
                                val lastMessage = lastMessageChild.getValue(MessageModel::class.java)
                                updateUI(chattingWithUser, lastMessage, score)
                            }

                        } else if (key.contains(userId)) {
                            val chattingWithUser = child.child("user2").getValue(UserModel::class.java)
                            userChatting = chattingWithUser
                            val score = child.child("score").getValue(Int::class.java)
                            val lastMessageChild = child.child("messages").children.last()

                            if(lastMessageChild != null) {
                                val lastMessage = lastMessageChild.getValue(MessageModel::class.java)
                                updateUI(chattingWithUser, lastMessage, score)
                            }
                        }
                    }
                }
            })
    }

    private fun updateUI(chattingWithUser: UserModel?, lastMessage: MessageModel?, score: Int?) {
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        )

        layoutParams.setMargins(0,0,0,48)

        val boxLayout = LinearLayout(baseContext)
        val contentLayout = LinearLayout(baseContext)
        contentLayout.orientation = LinearLayout.VERTICAL

        val layout = findViewById<LinearLayout>(R.id.container)
        val nameTextView = TextView(baseContext)
        nameTextView.text = chattingWithUser?.name
        nameTextView.textSize = 16.0F

        val lastMessageTextView = TextView(baseContext)
        lastMessageTextView.text = "Última mensagem: ${lastMessage?.content}"
        lastMessageTextView.setTypeface(null, Typeface.BOLD)

        val scoreMessageTextView = TextView(baseContext)
        scoreMessageTextView.text = "Pontuação: ${score}"

        val lastInteractionTextView = TextView(baseContext)
        lastInteractionTextView.text = getDateTime(lastMessage?.sentAt)

        boxLayout.elevation = 2f

        boxLayout.background = ContextCompat.getDrawable(this, R.drawable.header_drawable)

        contentLayout.addView(nameTextView)
        contentLayout.addView(lastMessageTextView)
        contentLayout.addView(scoreMessageTextView)
        contentLayout.addView(lastInteractionTextView)

        boxLayout.layoutParams = layoutParams
        boxLayout.setPadding(48,48,48,48)

        boxLayout.addView(contentLayout)
        boxLayout.setOnClickListener {
            openChat(chattingWithUser)
        }

        layout.addView(boxLayout)

    }

    private fun getDateTime(s: Long?): String? {
        if(s == null) return null

        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy")
            val netDate = Date(s * 1000)
            return sdf.format(netDate)
        } catch (e: Exception) {
            return e.toString()
        }
    }

    private fun openChat(chattingWithUser: UserModel?) {
        val intent = Intent(this, ChatMessagesActivity::class.java)
        intent.putExtra("user_id", chattingWithUser?.id)
        startActivity(intent)
        finish()
    }
}