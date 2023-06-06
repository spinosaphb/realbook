package com.realbook

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.media.Image
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat.OrientationMode
import androidx.appcompat.widget.LinearLayoutCompat.VERTICAL
import androidx.core.content.ContextCompat
import androidx.core.view.setMargins
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.realbook.models.PostModel
import com.realbook.models.UserModel
import com.squareup.picasso.Picasso
import java.util.*
import javax.sql.DataSource

class FeedFragment : Fragment() {
    private lateinit var firebaseDatabase: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var layout: LinearLayout

    private var userLikes = mutableListOf<UserModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}

        firebaseDatabase = FirebaseDatabase.getInstance().reference
        firebaseAuth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        layout = view.findViewById(R.id.feed)
        val currentUser = firebaseAuth.currentUser ?: return
        firebaseDatabase.child("posts")
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    layout.removeAllViews()

                    for (child in snapshot.children) {
                        val content = child.child("content").getValue(String::class.java)
                        val createdByUser = generateUser(child.child("createdByUser"))
                        val postId = child.child("id").getValue(String::class.java)
                        val imageUrl = child.child("imageUrl").getValue(String::class.java)

                        val likes = child.child("likes").children
                        var userHasLikedPost = false
                        for (childLike in likes) {
                            val like = generateUser(childLike)
                            if (like.id == currentUser.uid)
                                userHasLikedPost = true
                        }
                        val postC = PostModel(
                            content = content,
                            createdByUser = createdByUser,
                            id = postId,
                            imageUrl = imageUrl,
                            likes = null
                        )

                        updateUI(postC, userHasLikedPost)
                        userLikes.forEach { like -> userLikes.remove(like) }
                    }
                }
            })

    }

    private fun updateUI(post: PostModel?, isPostLiked: Boolean) {
        val currentUser = firebaseAuth.currentUser ?: return

        val boxPostLayout = LinearLayout(context)
        boxPostLayout.orientation = LinearLayout.VERTICAL

        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        )
        layoutParams.setMargins(0,0,0,32)

        val headerLayout = LinearLayout(context)

        val createdByTextView = TextView(context)
        createdByTextView.text = post?.createdByUser?.name
        createdByTextView.setTypeface(null, Typeface.BOLD)
        val headerLayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT,
        )

        headerLayoutParams.gravity = Gravity.CENTER_VERTICAL

        headerLayout.layoutParams = headerLayoutParams
        headerLayout.addView(createdByTextView)
        headerLayout.setPadding(32, 32,32,32)

        if (post !== null && currentUser.uid != post.createdByUser?.id) {
            val itemsHeaderParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )

            itemsHeaderParams.gravity = Gravity.END

            val actionButtonsLayout = LinearLayout(requireContext())

            var buttonLikeDrawable = R.drawable.like_outline

            if (isPostLiked)
                buttonLikeDrawable =  R.drawable.like_filled

            val buttonLike = createButton(buttonLikeDrawable, Color.TRANSPARENT)

            buttonLike.setOnClickListener {
                handleLikePost(currentUser, post)
            }

            actionButtonsLayout.layoutParams = itemsHeaderParams

            actionButtonsLayout.addView(buttonLike)

            var buttonAddFriendDrawable = R.drawable.add_friend

            firebaseDatabase
                .child("users")
                .child(currentUser.uid)
                .child("friends")
                .child(post.createdByUser!!.id)
                .get()
                .addOnCompleteListener { task ->
                    if(task.isSuccessful) {
                        val snapshot = task.result

                        if (snapshot.exists()) {
                            buttonAddFriendDrawable = R.drawable.added_friend
                        }

                        val buttonAddFriend = createButton(buttonAddFriendDrawable, Color.TRANSPARENT)

                        buttonAddFriend.setOnClickListener {
                            handleAddFriend(currentUser, post, buttonAddFriend)
                        }
                        buttonAddFriend.layoutParams = itemsHeaderParams
                        actionButtonsLayout.addView(buttonAddFriend)

                    }
                }

            headerLayout.addView(actionButtonsLayout)
        }

        else if (post != null && currentUser.uid == post.createdByUser?.id) {
            val button = createButton(R.drawable.deny_friend, Color.TRANSPARENT)

            button.setOnClickListener {
                handleDeletePost(post)
            }

            val buttonParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
            )

            buttonParams.gravity = Gravity.END
            button.layoutParams = buttonParams
            headerLayout.addView(button)
        }

        val imageView = ImageView(requireContext())
        imageView.layoutParams = layoutParams
        Picasso
            .get()
            .load(post?.imageUrl)
            .fit()
            .centerCrop(Gravity.END)
            .placeholder(R.drawable.post_image_default)
            .into(imageView)

        val contentView = TextView(context)
        contentView.textSize = 18f
        contentView.text = post?.content
        contentView.layoutParams = layoutParams
        contentView.setPadding(48,48,48,48)

        boxPostLayout.gravity = Gravity.CENTER

        boxPostLayout.layoutParams = layoutParams
        boxPostLayout.background = ContextCompat.getDrawable(requireContext(), R.drawable.header_drawable)
        boxPostLayout.addView(headerLayout)
        boxPostLayout.addView(imageView)
        boxPostLayout.addView(contentView)

        layout.addView(boxPostLayout)
    }

    private fun createButton(drawable: Int, color: Int): Button {
        val button = Button(context)
        button
            .setCompoundDrawablesRelativeWithIntrinsicBounds(
                null,
                null,
                ContextCompat.getDrawable(requireContext(), drawable),
                null
            )
        button.setBackgroundColor(color)
        return button
    }
    private fun handleLikePost(currentUser: FirebaseUser, post: PostModel?) {
        if (post == null) return

        firebaseDatabase
            .child("users")
            .child(currentUser.uid)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val snapshot = task.result
                    val user = generateUser(snapshot)

                    val postsRef = firebaseDatabase
                        .child("posts")
                        .child(post.id!!)
                        .child("likes")
                        .child(currentUser.uid)


                    postsRef.get()
                        .addOnCompleteListener { _task ->
                            if (_task.isSuccessful) {
                                val snapshot2 = _task.result

                                if (!snapshot2.exists())
                                    postsRef.setValue(user)
                                else
                                    postsRef.removeValue()
                            }
                        }
                }
            }


    }

    private fun handleAddFriend(currentUser: FirebaseUser, post: PostModel?, button: Button) {
        if (post == null) return

        val createdByUserId = post.createdByUser!!.id

        val friendsIdRef = firebaseDatabase
            .child("users")
            .child(currentUser.uid)
            .child("friends")
            .child(createdByUserId)

        firebaseDatabase
            .child("users")
            .child((createdByUserId))
            .child("friendRequests")
            .child(currentUser.uid)
            .setValue(currentUser.uid)

        friendsIdRef
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val snapshot = task.result

                    if (!snapshot.exists()) {
                        friendsIdRef
                            .setValue(post.createdByUser)

                        button.setCompoundDrawablesRelativeWithIntrinsicBounds(
                            null,
                            null,
                            ContextCompat.getDrawable(requireContext(), R.drawable.added_friend),
                            null
                        )

                        toastMessage("Amigo adicionado com sucesso!!")
                    } else {
                        toastMessage("Vocês já são amigos!!")
                    }
                }
            }

    }

    private fun handleDeletePost(post: PostModel) {

        val postsRef = firebaseDatabase
            .child("posts")

        postsRef
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) {
                        val id = child.key.toString()
                        if (post.id == id) {
                            postsRef
                                .child(id)
                                .setValue(null)

                            toastMessage("Post deletado com sucesso")
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    toastMessage("Erro ao deletar post!")
                }
            })
    }
    private fun toastMessage(message: String) {
        return Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
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

    companion object {
        @JvmStatic
        fun newInstance() =
            FeedFragment().apply {
                arguments = Bundle().apply {}
            }
    }
}