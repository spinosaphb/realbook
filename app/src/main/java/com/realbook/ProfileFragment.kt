package com.realbook

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import com.google.firebase.auth.FirebaseAuth

import com.bumptech.glide.Glide
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import org.w3c.dom.Text

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ProfileFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ProfileFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("90329938024-4uuqphub1mj2kmuv8usqamr6rsd0lhio.apps.googleusercontent.com")
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val shareLocationSwitch = view.findViewById<SwitchCompat>(R.id.share_location_switch)
        shareLocationSwitch?.setOnCheckedChangeListener { _, isChecked ->
            toggleShareLocation(isChecked)
        }
        updateUI(shareLocationSwitch)

        view.findViewById<Button>(R.id.logout_btn)
            .setOnClickListener {
                logout()
            }
    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ProfileFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ProfileFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    private fun toggleShareLocation(checked: Boolean) {
        val currentUser = auth.currentUser ?: return
        val userId = currentUser.uid

        val shareLocationRef = database
            .child("users")
            .child(userId)
            .child("shareLocation")

        shareLocationRef.setValue(checked)

    }

    private fun logout() {
        auth.signOut()
        googleSignInClient.signOut()
        val intent = Intent(context, MainActivity::class.java)
        startActivity(intent)
    }

    private fun updateUI(shareLocationSwitch: SwitchCompat) {
        val currentUser = auth.currentUser
        if(currentUser != null) {
            val profileImageView = view?.findViewById<ImageView>(R.id.profile_image_view)
            val photUrl = currentUser.photoUrl
            val userId = currentUser.uid

            Glide.with(this)
                .load(photUrl)
                .into(profileImageView!!)

            val nameTextView = view?.findViewById<TextView>(R.id.profile_user_name)
            val emailTextView = view?.findViewById<TextView>(R.id.profile_user_email)
            nameTextView?.text = currentUser.displayName.toString()
            emailTextView?.text = currentUser.email.toString()


            database
                .child("users")
                .child(userId)
                .child("shareLocation")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val snapshot = task.result
                        shareLocationSwitch.isChecked = snapshot.getValue(Boolean::class.java) == true
                    }
                }

        }
    }
}