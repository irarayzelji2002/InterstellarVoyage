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
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class MissionsActivity : AppCompatActivity(), MusicPlayerCallback {
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
        setContentView(R.layout.activity_missions)

        val playIntent = Intent(this, MusicPlayer::class.java)
        playIntent.action = MusicPlayer.ACTION_PLAY_MUSIC
        playIntent.putExtra(MusicPlayer.EXTRA_MUSIC_RESOURCE_ID, R.raw.homepage_music)

        fun isServiceRunning(serviceClass: Class<*>): Boolean {
            val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
                if (serviceClass.name == service.service.className) {
                    return true
                }
            }
            return false
        }

        if (!isServiceRunning(MusicPlayer::class.java)) {
            startService(playIntent)
        }

        if (!isMusicServiceBound && bindService(playIntent, connection, Context.BIND_AUTO_CREATE)) {
            Log.i("Music", "Service binding successful")
            isMusicServiceBound = true
        } else {
            Log.e("Music", "Service binding failed")
        }

        val btnBack = findViewById<Button>(R.id.btnBack)

        btnBack.setOnClickListener {
            startActivity(Intent(this, HomepageActivity::class.java))
        }

        DatabaseFunctions.accessMissions(this) { userMissions ->
            if (userMissions != null) {
                Log.d("FirestoreData", "Current Level: ${userMissions.currentLevel}")
                Log.d("completedLevels", "${userMissions.completedLevels}")
                Log.d("FirestoreData", "Current Mission: ${userMissions.currentMission}")
                Log.d("completedMissions", "${userMissions.completedMissions}")

                // Show level name or locked
                val dbCurrentLevel: Long? = userMissions.currentLevel
                val currentLevel: Int = dbCurrentLevel?.toInt() ?: 0
                val currentLevelData = GameData.levels.find { it.id == currentLevel }
                Log.d("Debug", "dbCurrentLevel: "+dbCurrentLevel)
                Log.d("Debug", "currentLevel: "+currentLevel)
                Log.d("Debug", "currentLevelData: "+currentLevelData)

                if (currentLevelData != null) {
                    val currentLevelIndex = GameData.levels.indexOf(currentLevelData)
                    Log.d("Debug", "currentLevelIndex: "+currentLevelIndex)
                    for (i in 0 until 4) { // 0 to 3
                        val levelNameTextView = findViewById<TextView>(resources.getIdentifier("txtLevel${i}Name", "id", packageName))
                        // Check if the current level index is less than or equal to the loop index
                        if (currentLevelIndex >= i) { // level unlocked
                            levelNameTextView.visibility = View.VISIBLE
                            levelNameTextView.text = GameData.levels[i].name
                        } else { //locked level
                            levelNameTextView.visibility = View.VISIBLE
                            levelNameTextView.text = "LOCKED"
                        }
                    }
                }

                // Show sub mission name or locked
                var currentMission: String = userMissions.currentMission?.toString() ?: ""
                if(currentMission == "1.0") {
                    currentMission = "0.5"
                } else if(currentMission == "2.0") {
                    currentMission = "1.5"
                } else if(currentMission == "3.0") {
                    currentMission = "2.5"
                } else if(currentMission == "4.0") {
                    currentMission = "3.5"
                }
                val currentMissionData = GameData.missions.find { it.id == currentMission }
                Log.d("Debug", "currentMission $currentMission")
                Log.d("Debug", "currentMissionData $currentMissionData")
                if (currentMissionData != null) {
                    val currentMissionIndex = GameData.missions.indexOf(currentMissionData)

                    for (i in 0 until 20) {
                        val subMissionNameTextView = findViewById<TextView>(resources.getIdentifier("subMission${i + 1}Name", "id", packageName))
                        val subMissionLockImageView = findViewById<ImageView>(resources.getIdentifier("subMission${i + 1}Lock", "id", packageName))
                        Log.d("Debug", "subMissionNameTextView for mission $i: $subMissionNameTextView")
                        // Check if the current mission index is less than or equal to the loop index
                        if (currentMissionIndex >= i) {
                            subMissionNameTextView.visibility = View.VISIBLE
                            subMissionLockImageView.visibility = View.GONE
                            subMissionNameTextView.text = GameData.missions[i].name
                        } else {
                            subMissionNameTextView.visibility = View.GONE
                            subMissionLockImageView.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
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