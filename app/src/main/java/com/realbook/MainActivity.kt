package com.realbook

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.realbook.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        val loginGoogleBtn = findViewById<Button>(R.id.login_google_button)
        loginGoogleBtn.setOnClickListener {
            openHome()
        }
    }

    private fun openHome() {
        val intent = Intent(this, FeedActivity::class.java)
        startActivity(intent)
    }
}