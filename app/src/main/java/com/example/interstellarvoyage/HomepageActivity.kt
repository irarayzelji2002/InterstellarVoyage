package com.example.interstellarvoyage

import android.content.Intent
import android.graphics.Color
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Button
import com.airbnb.lottie.LottieAnimationView

class HomepageActivity : AppCompatActivity() {
    private var musicPlayerCallback: MusicPlayerCallback? = null

    fun setMusicPlayerCallback(callback: MusicPlayerCallback) {
        musicPlayerCallback = callback
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)

        // Adjust position of title by percent
        val rootView = findViewById<View>(R.id.rootView)
        val myView = findViewById<View>(R.id.lottieTitle)
        rootView.viewTreeObserver.addOnPreDrawListener(object : ViewTreeObserver.OnPreDrawListener {
            override fun onPreDraw(): Boolean {
                rootView.viewTreeObserver.removeOnPreDrawListener(this)
                val screenHeight = rootView.height
                val desiredY = (screenHeight * -0.12).toInt() //-11% of Y
                myView.y = desiredY.toFloat()
                return true
            }
        })

        var btnMissions : Button = findViewById(R.id.btnMissions)
        var btnOptions : Button = findViewById(R.id.btnOptions)
        var btnLeaderboard : Button = findViewById(R.id.btnLeaderboard)
        var btnPlaySpaceship : LottieAnimationView = findViewById(R.id.btnPlaySpaceship)

        musicPlayerCallback?.transitionMusic("homepage")

        btnMissions.setOnClickListener {
            startActivity(Intent(this, MissionsActivity::class.java))
        }

        btnOptions.setOnClickListener {
            startActivity(Intent(this, OptionsActivity::class.java))
        }

        btnLeaderboard.setOnClickListener {
            startActivity(Intent(this, LeaderboardActivity::class.java))
        }

        btnPlaySpaceship.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                startActivity(Intent(this@HomepageActivity, GameActivity::class.java))
            }
        })
    }
}