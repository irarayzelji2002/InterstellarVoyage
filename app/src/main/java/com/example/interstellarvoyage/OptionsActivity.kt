package com.example.interstellarvoyage

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat

class OptionsActivity : AppCompatActivity(), MusicPlayerCallback {
    private lateinit var musicPlayer: MusicPlayer
    private var bound = false
    private var serviceConnected = false
    private var isMusicServiceBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicPlayer.MusicBinder
            musicPlayer = binder.getService()
            bound = true
            serviceConnected = true
            musicPlayer.setVolume(0.1f,0.1f)
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
        setContentView(R.layout.activity_options)

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

        val goldUser = findViewById<LinearLayout>(R.id.goldUser)
        val silverUser = findViewById<LinearLayout>(R.id.silverUser)
        val bronzeUser = findViewById<LinearLayout>(R.id.bronzeUser)
        val ironUser = findViewById<LinearLayout>(R.id.ironUser)

        val txtUsernameGold = findViewById<TextView>(R.id.txtUsernameGold)
        val txtUsernameSilver = findViewById<TextView>(R.id.txtUsernameSilver)
        val txtUsernameBronze = findViewById<TextView>(R.id.txtUsernameBronze)
        val txtUsernameIron = findViewById<TextView>(R.id.txtUsernameIron)
        val txtEmailAddress = findViewById<TextView>(R.id.txtEmailAddress)

        val btnBack = findViewById<Button>(R.id.btnBack)
        val switchBGMusic = findViewById<SwitchCompat>(R.id.switchBGMusic)
        val btnEditUserInfo = findViewById<Button>(R.id.btnEditUserInfo)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val btnDeleteAccount = findViewById<Button>(R.id.btnDeleteAccount)

        // Add Username & Email Address Text
        DatabaseFunctions.accessUserDocument(this) { userDocument ->
            if (userDocument != null) {
                Log.d("FirestoreData", "Current Level: ${userDocument.currentLevel}")
                Log.d("FirestoreData", "Username: ${userDocument.userDetails?.username}")
                Log.d("FirestoreData", "Email: ${userDocument.userDetails?.email}")
                val dbCurrentLevel: Long? = userDocument.currentLevel
                val currentLevel: Int = dbCurrentLevel?.toInt() ?: 0
                val dbUsername: String? = userDocument.userDetails?.username
                val username : String = dbUsername.toString()
                val dbEmailAdd: String? = userDocument.userDetails?.email
                val emailAdd : String = dbEmailAdd.toString()
                if (currentLevel == 0) {
                    goldUser.visibility = View.GONE
                    ironUser.visibility = View.VISIBLE
                    txtUsernameIron.text = username
                } else if (currentLevel == 1) {
                    goldUser.visibility = View.GONE
                    bronzeUser.visibility = View.VISIBLE
                    txtUsernameBronze.text = username
                } else if (currentLevel == 2) {
                    goldUser.visibility = View.GONE
                    silverUser.visibility = View.VISIBLE
                    txtUsernameSilver.text = username
                } else if (currentLevel >= 3) {
                    txtUsernameGold.text = username
                }
                txtEmailAddress.text = emailAdd
            }
        }

        btnBack.setOnClickListener {
            startActivity(Intent(this, HomepageActivity::class.java))
        }

        val userPref = getSharedPreferences("UserPrefs", AppCompatActivity.MODE_PRIVATE)
        var isMusicEnabled = userPref.getBoolean("isMusicEnabled", true)
        Log.d("isMusicEnabled", isMusicEnabled.toString())
        if(isMusicEnabled) {
            switchBGMusic.isChecked = true
            playMusic()
        } else {
            switchBGMusic.isChecked = false
            pauseMusic()
        }

        switchBGMusic.setOnCheckedChangeListener { buttonView, isChecked ->
            val editor = userPref.edit()
            if (isChecked) {
                playMusic()
                editor.putBoolean("isMusicEnabled", true)
            } else {
                pauseMusic()
                editor.putBoolean("isMusicEnabled", false)
            }
            editor.apply()
            isMusicEnabled = userPref.getBoolean("isMusicEnabled", true)
            Log.d("isMusicEnableinside", isMusicEnabled.toString())
        }

        btnEditUserInfo.setOnClickListener {
            startActivity(Intent(this, EditUserInfoActivity::class.java))
        }

        btnLogout.setOnClickListener {
            DatabaseFunctions.logout(this)
        }

        btnDeleteAccount.setOnClickListener {
            startActivity(Intent(this, DeleteAccountActivity::class.java))
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