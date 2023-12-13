package com.example.interstellarvoyage

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.ViewTreeObserver
import android.widget.Button
import com.airbnb.lottie.LottieAnimationView

class HomepageActivity : AppCompatActivity(), MusicPlayerCallback {
    private lateinit var musicPlayer: MusicPlayer
    private lateinit var popSound: MusicPlayer
    private var bound = false
    private var serviceConnected = false
    private var isMusicServiceBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicPlayer.MusicBinder
            musicPlayer = binder.getService()
            bound = true
            serviceConnected = true

            // Change music only if the current music is different
            val currentMusicResourceId = musicPlayer.getCurrentMusicResourceId()
            Log.i("Music", currentMusicResourceId.toString())
            Log.i("Music", R.raw.homepage_music.toString())
            if (currentMusicResourceId != R.raw.homepage_music) {
                musicPlayer.changeMusic(R.raw.homepage_music)
            }
            musicPlayer.setMusicEnabled(true)

            val userPref = getSharedPreferences("UserPrefs", AppCompatActivity.MODE_PRIVATE)
            var isMusicEnabled = userPref.getBoolean("isMusicEnabled", true)
            Log.d("isMusicEnabled", isMusicEnabled.toString())
            if(!isMusicEnabled) {
                pauseMusic()
            }
            Log.i("Music", "Service Connected")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bound = false
            serviceConnected = false
            Log.e("Music", "Service Disconnected")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_homepage)

        val playIntent = Intent(this, MusicPlayer::class.java)
        playIntent.action = MusicPlayer.ACTION_PLAY_MUSIC
        playIntent.putExtra(MusicPlayer.EXTRA_MUSIC_RESOURCE_ID, R.raw.homepage_music)

        if (!isServiceRunning(MusicPlayer::class.java)) {
            startService(playIntent)
        }

        if (!isMusicServiceBound && bindService(playIntent, connection, Context.BIND_AUTO_CREATE)) {
            Log.i("Music", "Service binding successful")
            isMusicServiceBound = true
        } else {
            Log.e("Music", "Service binding failed")
        }

        popSound = MusicPlayer()

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
                popSound?.playPopSound(this@HomepageActivity)
                startActivity(Intent(this@HomepageActivity, GameActivity::class.java))
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bound) {
            unbindService(connection)
            bound = false
        }
    }

    override fun onStop() {
        super.onStop()
        if (bound) {
            unbindService(connection)
            bound = false
        }
    }
    override fun onResume() {
        super.onResume()
        bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun onPause() {
        super.onPause()
        if (bound) {
            unbindService(connection)
            bound = false
            serviceConnected = false
        }
    }

    fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    // MusicPlayerCallback functions
    override fun playMusic() {
        if (::musicPlayer.isInitialized) {
            musicPlayer?.playMusic()
            Log.i("Music", "Playing")
        } else {
            Log.e("Music", "MusicPlayer not initialized")
        }
    }

    override fun pauseMusic() {
        if (::musicPlayer.isInitialized) {
            musicPlayer?.pauseMusic()
            Log.i("Music", "Paused")
        } else {
            Log.e("Music", "MusicPlayer not initialized")
        }
    }

    override fun isPlaying(): Boolean {
        return if (::musicPlayer.isInitialized) {
            musicPlayer.isPlaying()
        } else {
            false
        }
    }

    override fun changeMusic(newMusicResourceId: Int) {
        if (::musicPlayer.isInitialized) {
            musicPlayer?.changeMusic(newMusicResourceId)
            Log.i("Music", "Changed")
        } else {
            Log.e("Music", "MusicPlayer not initialized")
        }
    }
}