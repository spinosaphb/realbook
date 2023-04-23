package com.realbook

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_home)

        loadFragment(FeedFragment())
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.setOnItemSelectedListener {
            when(it.itemId) {
                R.id.home -> {
                    loadFragment(FeedFragment())
                    true
                }
                R.id.search -> {
                    // loadFragment(SearchFragment())
                    true
                }
                R.id.profile -> {
                    // loadFragment(ProfileFragment())
                    true
                }
                R.id.new_post -> {
                    // loadFragment(CreatePostFragment())
                    true
                }
                R.id.friends -> {
                    // loadFragment(FriendsFragment())
                    true
                }
                else -> {
                    Log.i("Erro", "error")
                    true
                }
            }
        }

        val buttonChat = findViewById<Button>(R.id.chat_btn)
        buttonChat.setOnClickListener {
        }
    }

    private fun loadFragment(fragment: Fragment){
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.container,fragment)
        transaction.addToBackStack(null)
        transaction.commit()
    }
}