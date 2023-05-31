package com.realbook

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.realbook.models.PostModel
import com.realbook.models.UserModel
import java.io.ByteArrayOutputStream
import java.util.*


class CreatePostFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    lateinit var imageView: ImageView
    lateinit var button: Button
    private val pickImage = 100
    private var imageUri: Uri? = null
    private lateinit var firebaseStorage: FirebaseStorage
    private lateinit var firebaseDatabase: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var user: UserModel

    private lateinit var buttonCreatePost: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}

        firebaseStorage = FirebaseStorage.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance().reference
        firebaseAuth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_create_post, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        imageView = view.findViewById(R.id.image_view)
        button = view.findViewById(R.id.upload_image_btn)
        button.setOnClickListener {
            val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
            startActivityForResult(gallery, pickImage)
        }

        val user = firebaseAuth.currentUser ?: return
        var userId = user.uid

        firebaseDatabase
            .child("users")
            .child(userId)
            .get()
            .addOnCompleteListener { task ->
                if(task.isSuccessful) {
                    val snapshot = task.result

                    if (snapshot.exists()) {
                        val avatar = snapshot.child("avatar").getValue(String::class.java)!!
                        val email = snapshot.child("email").getValue(String::class.java)!!
                        val location = snapshot.child("location").getValue(UserModel.Coords::class.java)!!
                        val name = snapshot.child("name").getValue(String::class.java)!!
                        val shareLocation = snapshot.child("shareLocation").getValue(Boolean::class.java)!!

                        val user = UserModel(
                            avatar = avatar,
                            email = email,
                            location =  location,
                            name = name,
                            shareLocation = shareLocation,
                            friends = null,
                            id = userId
                        )

                        this.user = user
                    }
                }
            }

        buttonCreatePost = view.findViewById<Button>(R.id.create_post_btn)
        buttonCreatePost.setOnClickListener {
            handleCreatePost()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == pickImage) {
            imageUri = data?.data
            imageView.setImageURI(imageUri)
        }
    }

    private fun handleCreatePost() {
        buttonCreatePost.text = "Carregando..."

        val drawable = imageView.drawable as BitmapDrawable
        val bitmap = drawable.bitmap
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos)
        val data = baos.toByteArray()

        val timestamp = System.currentTimeMillis()
        val imageName = "$timestamp.jpg"
        val storageRef = FirebaseStorage.getInstance().getReference(imageName)
        val uploadTask = storageRef.putBytes(data)

        val postContentTextView = view?.findViewById<TextView>(R.id.post_content)
        val postContentText = postContentTextView?.text.toString()


        uploadTask.addOnSuccessListener { taskSnapshot ->
            taskSnapshot.storage.downloadUrl.addOnSuccessListener { uri ->
                val imageUrl = uri.toString()
                val postId = UUID.randomUUID().toString()
                val post = PostModel(
                    id = postId,
                    content = postContentText,
                    createdByUser = this.user,
                    imageUrl = imageUrl,
                    likes = null
                )

                firebaseDatabase.child("posts").child(postId).setValue(post)
                Toast.makeText(context, "Post criado com sucesso", Toast.LENGTH_SHORT).show()
                postContentTextView?.text = ""
                loadFragment(FeedFragment())

            }
        }.addOnFailureListener { exception ->

        }
    }

    private  fun loadFragment(fragment: Fragment){
        val transaction = parentFragmentManager.beginTransaction()
        transaction.replace(R.id.container,fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }

    companion object {
        fun newInstance() =
            CreatePostFragment().apply {
                arguments = Bundle().apply {}
            }
    }
}