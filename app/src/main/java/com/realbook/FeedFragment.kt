package com.realbook

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.squareup.picasso.Picasso
import com.realbook.models.PostModel
import com.realbook.models.UserModel

class FeedFragment : Fragment() {

    private lateinit var layout: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let { }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_post_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        layout = view.findViewById(R.id.feed)

        var friends = emptyArray<UserModel>();
        var user = UserModel(
            name = "Vinicius",
            email = "",
            avatar = "",
            shareLocation = false,
            location = UserModel.Coords(10.0, 100.0),
            friends,
            id = null
        )

        var post1 = PostModel(
            id = "1",
            content = "Conteúdo 1",
            imageUrl = "https://api.deepai.org/job-view-file/907b9cce-0ab6-4884-b38f-554c8bfcb950/outputs/output.jpg",
            createdByUser = user,
            likes = mutableListOf<UserModel>()
        )

        var post2 = PostModel (
            id = "2",
            content = "Conteúdo 2",
            imageUrl = "https://api.deepai.org/job-view-file/81389afb-d306-448f-9bb9-527b614d38a8/outputs/output.jpg",
            createdByUser = user,
            likes = mutableListOf<UserModel>()
        )

        updateUI(user, post1, true)
        updateUI(user, post2, false)
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

    private fun updateUI(currentUser: UserModel?, post: PostModel?, isPostLiked: Boolean) {
        if(currentUser == null || post == null) return

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

        val postUser = post.createdByUser




        // Se o usuário logado não for quem criou o post
        if(currentUser?.id != postUser?.id) {
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

            // buttonLike.setOnClickListener {
            //
            // }

            actionButtonsLayout.layoutParams = itemsHeaderParams

            actionButtonsLayout.addView(buttonLike)

            var buttonAddFriendDrawable = R.drawable.add_friend

            var isFriend = currentUser?.friends?.find { it.id == postUser?.id } != null

            if(isFriend) {
                buttonAddFriendDrawable = R.drawable.added_friend
            }

            val buttonAddFriend = createButton(buttonAddFriendDrawable, Color.TRANSPARENT)

            // buttonAddFriend.setOnClickListener {
            //
            // }
            buttonAddFriend.layoutParams = itemsHeaderParams
            actionButtonsLayout.addView(buttonAddFriend)

            headerLayout.addView(actionButtonsLayout)
        }
        else {
            val button = createButton(R.drawable.deny_friend, Color.TRANSPARENT)

            // button.setOnClickListener {
            //
            // }

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

    companion object {
        @JvmStatic
        fun newInstance() =
            FeedFragment().apply {
                arguments = Bundle().apply {}
            }
    }
}