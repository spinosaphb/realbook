package com.realbook

import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.database.*
import com.realbook.models.UserModel

class MainActivity : AppCompatActivity() {
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var auth: FirebaseAuth
    private lateinit var database: DatabaseReference
    private lateinit var locationHelper: LocationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().reference

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.firebase_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        val loginGoogleBtn = findViewById<Button>(R.id.login_google_button)
        loginGoogleBtn.setOnClickListener {
            signIn()
        }

        locationHelper = LocationHelper(this)
        if (!checkPermissions()) requestPermissions()
        else locationHelper.startLocationUpdates()

    }

    private fun signIn() {
        val intent = googleSignInClient.signInIntent
        openActivity.launch(intent)
    }

    private fun registerForResult(result: ActivityResult) {
        Log.w("MainActivity", "result code = ${result.resultCode}")
        if (result.resultCode == RESULT_OK) {
            val intent = result.data
            val task = GoogleSignIn.getSignedInAccountFromIntent(intent)
            try {
                val account = task.getResult(ApiException::class.java)
                loginWithGoogle(account.idToken!!)
            } catch (exception: ApiException) {
                Toast.makeText(baseContext, exception.message, Toast.LENGTH_SHORT).show()
            }

        }
    }

    private var openActivity = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult -> registerForResult(result)
    }

    private fun loginWithGoogle(token: String) {
        val credential = GoogleAuthProvider.getCredential(token, null)
        auth.signInWithCredential(credential).addOnCompleteListener(this) {
                task: Task<AuthResult> ->
            if(task.isSuccessful) {
                Toast.makeText(baseContext, "Autenticação efetuada com google", Toast.LENGTH_SHORT).show()
                verifyUserExistsAndCreate()
                openApp()
            } else {
                Toast.makeText(baseContext, "Erro de autenticação com google", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        locationHelper.startLocationUpdates()
        verifyUserExistsAndCreate()
    }

    override fun onStop() {
        super.onStop()
        locationHelper.stopLocationUpdates()
    }


    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(this,
            android. Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this,
            arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            PERMISSIONS_REQUEST_LOCATION
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSIONS_REQUEST_LOCATION) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                locationHelper.startLocationUpdates()
            }
        }
    }

    private fun verifyUserExistsAndCreate() {
        val currentUser = auth.currentUser ?: return;
        val location = locationHelper.currentLocation

        val userId = currentUser.uid
        val email = currentUser.email!!
        val name = currentUser.displayName!!

        val user = UserModel(
            id = userId,
            name = name,
            email = email,
            avatar = currentUser.photoUrl.toString(),
            shareLocation = false,
            location = UserModel.Coords(location?.latitude, location?.longitude),
            friends = null
        )

        database
            .child("users")
            .child(userId)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val snapshot = task.result
                    if (!snapshot.exists()) {
                        database.child("users").child(userId).setValue(user)
                    }
                }
            }
    }

    private fun openApp() {
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
    }


    companion object {
        private const val PERMISSIONS_REQUEST_LOCATION = 1
    }
}