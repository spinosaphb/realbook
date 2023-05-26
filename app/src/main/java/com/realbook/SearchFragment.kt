package com.realbook

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.realbook.ChatMessagesActivity
import com.realbook.models.UserModel

class SearchFragment : Fragment(), OnMapReadyCallback {
    private lateinit var googleMap: GoogleMap
    private lateinit var database: DatabaseReference
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {}
        database = FirebaseDatabase.getInstance().reference
        auth = FirebaseAuth.getInstance()
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap
        val quixada = LatLng(-4.9783253, -39.0256111)
        val zoomlevel = 15f
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(quixada, zoomlevel))
        val currentUser = auth.currentUser ?: return
        val currentUserId = currentUser.uid

        val usersRef = database.child("users")
        usersRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    if (!child.exists()) return;

                    var userKey = child.key
                    val userName = child.child("name").getValue(String::class.java)!!
                    val userId = child.child("id").getValue(String::class.java)!!
                    val email = child.child("email").getValue(String::class.java)!!
                    val userAvatar = child.child("avatar").getValue(String::class.java)!!
                    val shareLocation = child.child("shareLocation").getValue(Boolean::class.java)!!
                    val userLocation = child.child("location").getValue(UserModel.Coords::class.java)!!

                    val user = UserModel(
                        id = userId,
                        name = userName,
                        avatar = userAvatar,
                        location = userLocation,
                        shareLocation = shareLocation,
                        email = email,
                        friends = null
                    )

                    if(userKey == currentUserId || !user.shareLocation) continue

                    val location = user.location
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(LatLng(location?.latitude!!, location?.longitude!!))
                            .title(user.id)
                            .icon(bitmapDescriptorFromVector(context!!, R.drawable.person_pin))
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        googleMap.setOnMarkerClickListener {marker ->
            val userId = marker.title.toString()
            val intent = Intent(context, ChatMessagesActivity::class.java)
            intent.putExtra("user_id", userId)
            startActivity(intent)
            true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = (childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment)
        mapFragment.getMapAsync(this)
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SearchFragment().apply {
                arguments = Bundle().apply {}
            }
    }

    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }
}