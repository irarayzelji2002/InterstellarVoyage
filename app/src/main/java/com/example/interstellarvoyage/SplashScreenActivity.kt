package com.example.interstellarvoyage

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val isLoggedIn = userPref.getBoolean("isLoggedIn", false)

        if (isLoggedIn) { //true
            startActivity(Intent(this, HomepageActivity::class.java))
        } else {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        finish()
    }
}