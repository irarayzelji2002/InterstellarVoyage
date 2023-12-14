package com.example.interstellarvoyage

import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity(), MusicPlayerCallback  {
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
        setContentView(R.layout.activity_login)

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

        var btnTestDatabaseFunctions : Button = findViewById(R.id.btnTestDatabaseFunctions)
        var btnTestGameFunctions : Button = findViewById(R.id.btnTestGameFunctions)

        var btnLogin : Button = findViewById(R.id.btnLogin)
        var btnRegAccount : TextView = findViewById(R.id.btnRegAccount)
        var btnForgotPassword : TextView = findViewById(R.id.btnForgotPassword)

        val editTextEmailAddress = findViewById<EditText>(R.id.editTextEmailAddress)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)

        val emailAddressErr = findViewById<TextView>(R.id.emailAddressErr)
        val passwordErr = findViewById<TextView>(R.id.passwordErr)
        val loginErr = findViewById<TextView>(R.id.loginErr)

        FirebaseApp.initializeApp(this)
        val db = FirebaseFirestore.getInstance()

        btnLogin.setOnClickListener {
            val email = editTextEmailAddress.text.toString()
            val password = editTextPassword.text.toString()
            DatabaseFunctions.login(this, email, password) { errors ->
                if(errors != null) {
                    Log.d("Error", "Email Address: ${errors.emailAddressErr}")
                    Log.d("Error", "Password: ${errors.passwordErr}")
                    Log.d("Error", "Login Error: ${errors.authenticateErr}")

                    if(errors.emailAddressErr != "" || errors.passwordErr !="") {
                        setErrorTextAndVisibility(emailAddressErr, errors.emailAddressErr)
                        setErrorTextAndVisibility(passwordErr, errors.passwordErr)
                    } else {
                        setErrorTextAndVisibility(loginErr, errors.authenticateErr)
                    }
                }
            }
        }

        btnRegAccount.setOnClickListener{
            startActivity(Intent(this, RegistrationActivity::class.java))
        }

        btnForgotPassword.setOnClickListener {
            var dialogFragment = ForgotPasswordActivity()
            dialogFragment.setCancelable(false)
            dialogFragment.show(supportFragmentManager, "Forgot Password Dialog")
        }

        btnTestDatabaseFunctions.setOnClickListener{
            startActivity(Intent(this, TestDatabaseActivity::class.java))
        }

        btnTestGameFunctions.setOnClickListener{
            startActivity(Intent(this, TestGameActivity::class.java))
        }
        //Don't Delete below this
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

    // Login Activity Functions
    fun setErrorTextAndVisibility(view: TextView, error: String) {
        if (error.isNotEmpty() && error != null) {
            view.visibility = View.VISIBLE
            view.text = error
        } else {
            view.visibility = View.GONE
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