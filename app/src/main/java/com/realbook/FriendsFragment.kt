package com.realbook

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.realbook.models.UserModel
import com.squareup.picasso.Picasso
import org.w3c.dom.Text

class FriendsFragment : Fragment() {
    private lateinit var layout: LinearLayout
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friends, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        layout = view.findViewById(R.id.friends_container)
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid

        database
            .child("users")
            .child(userId)
            .child("friends")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val snapshot = task.result

                    if (snapshot.exists()) {
                        for (friend in snapshot.children) {
                            val userName = friend.child("name").getValue(String::class.java)!!
                            val userId = friend.child("id").getValue(String::class.java)!!
                            val email = friend.child("email").getValue(String::class.java)!!
                            val userAvatar = friend.child("avatar").getValue(String::class.java)!!
                            val shareLocation = friend.child("shareLocation").getValue(Boolean::class.java)!!
                            val location = friend.child("location").getValue(UserModel.Coords::class.java)!!

                            val userFriend = UserModel(
                                id = userId,
                                name = userName,
                                avatar = userAvatar,
                                location = location,
                                shareLocation = shareLocation,
                                email = email,
                                friends = null
                            )

                            updateUI(userFriend, false)
                        }
                    }
                }
            }

        database
            .child("users")
            .child(userId)
            .child("friendRequests")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {


                    for (child in snapshot.children) {
                        if (child == null) continue

                        val userIdRequested = child.key.toString()

                        database
                            .child("users")
                            .child(userIdRequested)
                            .get()
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val snapshot = task.result

                                    if (snapshot.exists()) {
                                        val userName = snapshot.child("name").getValue(String::class.java)!!
                                        val userId = snapshot.child("id").getValue(String::class.java)!!
                                        val email = snapshot.child("email").getValue(String::class.java)!!
                                        val userAvatar = snapshot.child("avatar").getValue(String::class.java)!!
                                        val shareLocation = snapshot.child("shareLocation").getValue(Boolean::class.java)!!
                                        val location = snapshot.child("location").getValue(UserModel.Coords::class.java)!!

                                        val userFriend = UserModel(
                                            id = userId,
                                            name = userName,
                                            avatar = userAvatar,
                                            location = location,
                                            shareLocation = shareLocation,
                                            email = email,
                                            friends = null
                                        )
                                        val requestFriendsTextParams = LinearLayout.LayoutParams(
                                            LinearLayout.LayoutParams.MATCH_PARENT,
                                            LinearLayout.LayoutParams.WRAP_CONTENT,
                                        )
                                        updateUI(userFriend, true)
                                    }
                                }
                            }

                    }
                }
            })
    }

    private fun updateUI(friend: UserModel, isFriendRequest: Boolean) {
        val circleImageView = de.hdodenhof.circleimageview.CircleImageView(context)

        Picasso
            .get()
            .load(friend.avatar)
            .placeholder(R.drawable.post_image_default)
            .into(circleImageView)

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        )

        layoutParams.setMargins(48,48,48,16)

        val boxLayout = LinearLayout(context)
        val contentLayout = LinearLayout(context)
        contentLayout.orientation = LinearLayout.VERTICAL
        contentLayout.setPadding(48, 0,0,0)

        val nameTextView = TextView(context)
        nameTextView.text = friend?.name
        nameTextView.textSize = 16.0F


        boxLayout.elevation = 2f
        boxLayout.background = ContextCompat.getDrawable(requireContext(), R.drawable.header_drawable)

        contentLayout.addView(nameTextView)
        boxLayout.layoutParams = layoutParams
        boxLayout.setPadding(48,48,48,48)

        boxLayout.addView(circleImageView)
        boxLayout.addView(contentLayout)

        if (isFriendRequest) {
            val buttonsLayout = actionButtons(friend, boxLayout)
            boxLayout.addView(buttonsLayout)
        }

        boxLayout.setOnClickListener{
            val intent = Intent(context, ChatMessagesActivity::class.java)
            intent.putExtra("user_id", friend.id)
            startActivity(intent)
        }


        layout.gravity = Gravity.CENTER_VERTICAL
        layout.addView(boxLayout)


    }

    private fun actionButtons(friendRequest: UserModel, layout: LinearLayout): LinearLayout {
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        )

        layoutParams.gravity = Gravity.END
        val buttonsLayout = LinearLayout(requireContext())

        val acceptBtn = Button(requireContext())
        acceptBtn.setOnClickListener {
            onAcceptFriendRequest(friendRequest, layout)
        }
        buttonsLayout.layoutParams = layoutParams
        buttonsLayout.addView(customButton(acceptBtn, R.drawable.accept_friend))

        return buttonsLayout;
    }

    private fun onAcceptFriendRequest(friend: UserModel, layout: LinearLayout) {
        val currentUser = auth.currentUser ?: return

        val usersRef = database
            .child("users")
            .child(currentUser.uid)

        usersRef
            .child("friendRequests")
            .child(friend.id)
            .setValue(null)

        usersRef.child("friends").child(friend.id).setValue(friend)
        this.layout.removeView(layout)
        updateUI(friend, false)

    }

    private fun customButton(button: Button, drawable: Int): Button {
        button.text = "Adicionar"
        button.textSize = 11f
        button.setCompoundDrawablesRelativeWithIntrinsicBounds(
            null,
            null,
            null,
            ContextCompat.getDrawable(requireContext(), drawable),
        )

        button.setBackgroundColor(Color.TRANSPARENT)
        button.width = LayoutParams.WRAP_CONTENT
        return button
    }


    companion object {
        @JvmStatic
        fun newInstance() =
            FriendsFragment().apply {
                arguments = Bundle().apply {}
            }
    }
}