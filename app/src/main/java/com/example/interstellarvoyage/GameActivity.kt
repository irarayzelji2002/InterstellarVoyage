package com.example.interstellarvoyage

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.media.MediaPlayer
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.airbnb.lottie.LottieAnimationView
import jp.wasabeef.blurry.Blurry
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class GameActivity : AppCompatActivity(), MusicPlayerCallback {
    private var mediaPlayer: MediaPlayer? = null

    var Clicks: Int = 0
    var ClicksperSecond: Float = 0.0F


    var MissionReq: Int = 0
    var TotalMissionClicks: Int = 0;

    var Multiplier: Int = 2;

    var startTime = 0.0f

    private var lastClickTime: Long = 0
    private var clicksInCurrentSecond: Int = 0

    private var countdownTimer: CountDownTimer? = null
    private val countdownDuration = 500 // Change this to the desired number of clicks


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)





        // Click related
        // hide levelXGraphics if not in the level (e.g. level0Graphics.visibility = View.GONE/VISIBLE)
        // disable click count of btnLevelXClicker if storylineContainer is visible
        var btnLevel0Clicker : LottieAnimationView = findViewById(R.id.btnLevel0Clicker) //person
        var level0Graphics : RelativeLayout = findViewById(R.id.level0Graphics)
        var btnLevel1Clicker : LottieAnimationView = findViewById(R.id.btnLevel1Clicker) //spaceship
        var level1Graphics : RelativeLayout = findViewById(R.id.level1Graphics)
        var btnLevel2Clicker : LottieAnimationView = findViewById(R.id.btnLevel2Clicker) //scan button
        var level2Graphics : RelativeLayout = findViewById(R.id.level2Graphics)
        var btnLevel3Clicker : LottieAnimationView = findViewById(R.id.btnLevel3Clicker) //person with hands up
        var level3Graphics : RelativeLayout = findViewById(R.id.level3Graphics)

        // Story related
        var storylineContainer : RelativeLayout = findViewById(R.id.storylineContainer) //hide if clicks not reached
        var txtStoryline : TextView = findViewById(R.id.txtStoryline)
        var btnStoryline : Button = findViewById(R.id.btnStoryline)

        // Clicks Number and Time Duration
        var txtClicks : TextView = findViewById(R.id.txtClicks)
        var txtTime : TextView = findViewById(R.id.txtTime)
        var txtCPS : TextView = findViewById(R.id.txtCPS)

        // Options Button
        var btnOptions : ImageButton = findViewById(R.id.btnOptions)






        // Populate activity info from database
        DatabaseFunctions.accessUserDocument(this) { userDocument ->
            if (userDocument != null) {
                // Select music
                Log.d("FirestoreData", "Current Level: ${userDocument.currentLevel}")
                val dbCurrentLevel: Long? = userDocument.currentLevel
                val currentLevel: Int = dbCurrentLevel?.toInt() ?: 0
                changeMusic(currentLevel)

                // Set storyline and button text
                Log.d("FirestoreData", "Current Level: ${userDocument.currentLevel}")
                Log.d("FirestoreData", "Current Mission: ${userDocument.currentMission}")
                Log.d("FirestoreData", "Number of Clicks: ${userDocument.numberOfClicks}")
                Log.d("FirestoreData", "Total Time Completed: ${userDocument.totalTimeCompleted}")
                val currentMission = userDocument.currentMission ?: "0.0"
                val nextLine = GameFunctions.findNextLine(this, currentMission)
                if (nextLine != null) {
                    val id = nextLine.id
                    val btnText = nextLine.btnText
                    val line = nextLine.line
                    Log.d("Storyline", "currentMission: $currentMission, id: $id")
                    txtStoryline.text = line
                    var btnStorylineText = ""
                    if (btnText == 0) {
                        btnStorylineText = "NEXT"
                    } else if (btnText == 1) {
                        btnStorylineText = "START MISSION"
                    } else if (btnText == 1) {
                        btnStorylineText = "NEXT LEVEL"
                    } else if (btnText == 1) {
                        btnStorylineText = "CONTINUE"
                    }
                    btnStoryline.setText(btnStorylineText)
                } else {
                    Log.d("Storyline", "No matching Line found for id: $currentMission")
                    txtStoryline.text = Storyline.lines.find { it.id == "0.1.1" }?.line //first storyline
                    btnStoryline.setText("NEXT")
                }
                /*storylineContainer.visibility = View.VISIBLE*/

                // Set clicks count
                txtClicks.text = userDocument.numberOfClicks.toString()

                // Start time
            }
        }

        //Timer
       fun startCountdownTimer() {
            countdownTimer = object : CountDownTimer(600000L, 1000L) {
                override fun onTick(millisUntilFinished: Long) {
                    // Update the timer display on each tick
                    val secondsRemaining = millisUntilFinished / 1000
                    val minutes = secondsRemaining / 60
                    val seconds = secondsRemaining % 60
                    txtTime.text = String.format("%02d:%02d", minutes, seconds)
                }

                override fun onFinish() {
                    // Countdown is complete, update UI accordingly
                    txtTime.text = "Countdown Complete"
                }
            }
            countdownTimer?.start()
        }

        fun stopCountdownTimer() {
            countdownTimer?.cancel()
        }

        //Clicks Per Second.
        fun calculateCPS() {
            val currentTimeMillis = System.currentTimeMillis()

            // Check if one second has passed since the last click
            if (currentTimeMillis - lastClickTime >= 1000) {
                // Calculate CPS
                ClicksperSecond = clicksInCurrentSecond.toFloat()
                txtCPS.text = String.format("%.2f", ClicksperSecond)

                // Reset counters for the next second
                clicksInCurrentSecond = 0
                lastClickTime = currentTimeMillis
            } else {
                // Increment click count within the current second
                clicksInCurrentSecond++
            }
        }

        // LEVEL 0; 500 clicks (100/sub mission); Earth’s Great Dilemma
        btnLevel0Clicker.setOnClickListener(object : View.OnClickListener {

            override fun onClick(view: View) {
                //LEVEL MISSION
                TotalMissionClicks++
                if (TotalMissionClicks == 500)
                {
                    //next mission
                    stopCountdownTimer() // Stop the countdown timer when the activity is destroyed
                    // DatabaseFunctions.changeCurrentMission(this@GameActivity,  )
                }
                if (TotalMissionClicks == 1) {
                    startCountdownTimer()
                }

                //CLICKS PER SECOND

                // --SUB MISSION--
                MissionReq = 100;

                //PerSubMission
                if (Clicks == MissionReq){
                    //Sub Mission = Complete
                    Clicks = 0;
                }
                //Clicks
                Clicks++;
                txtClicks.text = Clicks.toString();
                calculateCPS()

                Toast.makeText(this@GameActivity,"Level 0 CLicker clicked", Toast.LENGTH_SHORT).show();
            }
        })

        // LEVEL 1; 1000 (200/sub mission); Search for New Habitat
        btnLevel1Clicker.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {


                Toast.makeText(this@GameActivity,"Level 1 CLicker clicked", Toast.LENGTH_SHORT)
            }
        })

        // LEVEL 2; 1000 (200/sub mission); Beacon in the Galaxy
        btnLevel2Clicker.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                Toast.makeText(this@GameActivity,"Level 2 CLicker clicked", Toast.LENGTH_SHORT)
            }
        })

        // LEVEL 3; 2000 (400/sub mission); The Cosmic Council
        btnLevel3Clicker.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                Toast.makeText(this@GameActivity,"Level 3 CLicker clicked", Toast.LENGTH_SHORT)
            }
        })

        btnStoryline.setOnClickListener {
            Toast.makeText(this@GameActivity,"story button clicked", Toast.LENGTH_SHORT)
        }

        btnOptions.setOnClickListener {
            var dialogFragment = GameOptionsActivity()
            dialogFragment.setCancelable(false)
            dialogFragment.setMusicPlayerCallback(this)
            dialogFragment.show(supportFragmentManager, "Logout Confirm Dialog")
        }
    }

    override fun onDestroy() {


        super.onDestroy()


        mediaPlayer?.release()
    }

    override fun playMusic() {
        mediaPlayer?.start()
    }

    override fun pauseMusic() {
        mediaPlayer?.pause()
    }

    override fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying ?: false
    }

    override fun transitionMusic(activity: String) {
        mediaPlayer?.release()
        if(activity=="homepage") {
            mediaPlayer = MediaPlayer.create(this, R.raw.level0_music)
        }
        mediaPlayer?.isLooping = true
        mediaPlayer?.setVolume(1.0f, 1.0f)
        val userPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val isMusicEnabled = userPref.getBoolean("isMusicEnabled", true)
        if(isMusicEnabled) {
            playMusic()
        } else {
            if (mediaPlayer?.isPlaying == true) {
                pauseMusic()
            }
        }
    }

    fun changeMusic(currentLevel: Int) {
        mediaPlayer?.release()
        if(currentLevel==0) {
            mediaPlayer = MediaPlayer.create(this, R.raw.level0_music)
        } else if(currentLevel==1) {
            mediaPlayer = MediaPlayer.create(this, R.raw.level1_music)
        } else if(currentLevel==2) {
            mediaPlayer = MediaPlayer.create(this, R.raw.level2_music)
        } else if(currentLevel==3) {
            mediaPlayer = MediaPlayer.create(this, R.raw.level3_music)
        }
        mediaPlayer?.isLooping = true
        mediaPlayer?.setVolume(1.0f, 1.0f)
        val userPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val isMusicEnabled = userPref.getBoolean("isMusicEnabled", true)
        if(isMusicEnabled) {
            playMusic()
        } else {
            if (mediaPlayer?.isPlaying == true) {
                pauseMusic()
            }
        }
    }



}