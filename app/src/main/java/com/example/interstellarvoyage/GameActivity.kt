package com.example.interstellarvoyage

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.ActivityManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.drawable.AnimationDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.IBinder
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.airbnb.lottie.LottieAnimationView
import com.example.interstellarvoyage.DatabaseFunctions.levelCompleted
import com.example.interstellarvoyage.DatabaseFunctions.subMissionCompleted
import com.example.interstellarvoyage.GameFunctions.getNextCurrentMissionAfterLevel

class GameActivity : AppCompatActivity(), MusicPlayerCallback {
    private lateinit var musicPlayer: MusicPlayer
    private lateinit var popSound: MusicPlayer
    private var bound = false
    private var serviceConnected = false
    var isBeatAnimationRunning = false

    var Clicks: Int = 0
    var ClicksperSecond: Float = 0.0F

    var MissionReq: Int = 0
    var TotalMissionClicks: Int = 0;
    var startTime = 0.0f

    var buffer: Int = 0


    private var lastClickTime: Long = 0
    private var clicksInCurrentSecond: Int = 0

    private var remainingSeconds: Long = 0
    private var remainingTime: Long = remainingSeconds.toLong()

    private var countdownTimer: CountDownTimer? = null
    private val countdownDuration = 500 // Change this to the desired number of clicks

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicPlayer.MusicBinder
            musicPlayer = binder.getService()
            bound = true
            serviceConnected = true

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
        setContentView(R.layout.activity_game)

        val playIntent = Intent(this, MusicPlayer::class.java)
        playIntent.action = MusicPlayer.ACTION_PLAY_MUSIC

        DatabaseFunctions.accessUserDocument(this) { userDocument ->
            if (userDocument != null) {
                // Select music
                Log.d("FirestoreData", "Current Level: ${userDocument.currentLevel}")
                val dbCurrentLevel: Long? = userDocument.currentLevel
                val currentLevel: Int = dbCurrentLevel?.toInt() ?: 0

                // Change music to user's level
                if (serviceConnected) {
                    changeLevelMusic(currentLevel)
                    if(currentLevel==0) {
                        playIntent.putExtra(MusicPlayer.EXTRA_MUSIC_RESOURCE_ID, R.raw.level0_music)
                    } else if(currentLevel==1) {
                        playIntent.putExtra(MusicPlayer.EXTRA_MUSIC_RESOURCE_ID, R.raw.level1_music)
                    } else if(currentLevel==2) {
                        playIntent.putExtra(MusicPlayer.EXTRA_MUSIC_RESOURCE_ID, R.raw.level2_music)
                    } else if(currentLevel==3) {
                        playIntent.putExtra(MusicPlayer.EXTRA_MUSIC_RESOURCE_ID, R.raw.level3_music)
                    } else if(currentLevel==4) {
                        playIntent.putExtra(MusicPlayer.EXTRA_MUSIC_RESOURCE_ID, R.raw.level3_music)
                    } else {
                        playIntent.putExtra(MusicPlayer.EXTRA_MUSIC_RESOURCE_ID, R.raw.level0_music)
                    }
                }
            }
        }
        startService(playIntent)
        if (bindService(playIntent, connection, Context.BIND_AUTO_CREATE)) {
            Log.i("Music", "Service binding successful")
        } else {
            Log.e("Music", "Service binding failed")
        }

        popSound = MusicPlayer()

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

/*

        var lvlCompleted: Button = findViewById(R.id.lvlComplete)
*/

        // Populate activity info from database
        DatabaseFunctions.accessUserDocument(this) { userDocument ->
            if (userDocument != null) {
                // Store Click & Duration to local variable
                val dBsecondsRemaining  = userDocument.currentDuration
                remainingTime = dBsecondsRemaining?.toLong()?: 0
                val dbNumofClicks = userDocument.numberOfClicks
                Clicks = dbNumofClicks?.toInt()?: 0

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
                    val secondsRemaining = (600000 - millisUntilFinished) / 1000
                    val minutes = secondsRemaining / 60
                    val seconds = secondsRemaining % 60
                    txtTime.text = String.format("%02d:%02d", minutes, seconds)


                    remainingTime= (600000 - millisUntilFinished)/1000
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

        //Clicks Per Seconds

        // Define a constant for the minimum click interval (in milliseconds)
         val MIN_CLICK_INTERVAL = 10 // Adjust this value as needed

        fun calculateCPS(): Float {
            val currentTimeMillis = System.currentTimeMillis()
            val elapsedTime = currentTimeMillis - lastClickTime

            // Check if the click occurred within the minimum interval
            if (elapsedTime < MIN_CLICK_INTERVAL) {
                return 0f // Ignore the click, too soon after the last one
            }

            Log.d("CPS Debug", "Elapsed Time: $elapsedTime ms")

            // Calculate CPS based on total clicks and elapsed time
            val cps = if (elapsedTime > 0) {
                (TotalMissionClicks.toFloat() * 1000f / elapsedTime) / 1000f
            } else {
                0f
            }

            Log.d("CPS Debug", "CPS: $cps")

            // Increment click count within the current second
            clicksInCurrentSecond++

            // Update last click time
            lastClickTime = currentTimeMillis

            // Limit CPS to two decimal points
            return String.format("%.2f", cps).toFloat()
        }


      /*  lvlCompleted.setOnClickListener{
            Clicks = 0;
            TotalMissionClicks = 0;
            stopCountdownTimer()
            DatabaseFunctions.levelCompleted(this,0,"0.0",0.0,0,0.0)
            Log.i("info", "Current Mission: " + remainingTime.toDouble()+ " TotalMissionClicks: " + TotalMissionClicks + "Duration: " + remainingTime.toString())
        }*/

        // LEVEL 0; 500 clicks (100/sub mission); Earth’s Great Dilemma
        btnLevel0Clicker.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                popSound?.playPopSound(this@GameActivity)
                startBeatAnimation(btnLevel0Clicker)

                Clicks +=5;
                txtClicks.text = Clicks.toString();

                // --SUB MISSION--
                MissionReq = 100

                //CLICKS PER SECOND
                // Calculate CPS
                val cps = calculateCPS()
                txtCPS.text = String.format("%.2f", cps)

                Log.i("CPS", cps.toString())

                //LEVEL MISSION
                TotalMissionClicks+=5
                if (TotalMissionClicks >= 500)
                {
                    DatabaseFunctions.levelCompleted(this@GameActivity,0,"0.0",0.0,0,remainingTime.toDouble())

                    //next mission
                    Log.i("info", "Remaining Time Before: " + remainingTime.toString())
                    Log.i("info", "Time Remaining = " + remainingTime.toDouble().toString())
                    DatabaseFunctions.accessUserDocument(this@GameActivity) { userDocument ->
                        if (userDocument != null) {
                            Log.i("info", "Mission Complete")
                            //Current level
                            val dbCurrentLevel: Long? = userDocument.currentLevel
                            val currentLevel: Int = dbCurrentLevel?.toInt() ?: 0
                            val currentMission = userDocument.currentMission ?: "0.0"
                            var newCurMissionAfterLevel: String? = getNextCurrentMissionAfterLevel(currentMission)
                            var newLevel: Int? = GameFunctions.getNextLevel(currentLevel)
                            if (newCurMissionAfterLevel != null && newLevel != null) {
                                Log.i("DatabaseFunctions", "Remaining Time Before: " + remainingTime.toString())
                                Log.i("DatabaseFunctions", "Time Remaining = " + remainingTime.toDouble().toString())
                                levelCompleted(
                                    this@GameActivity,
                                    newLevel.toLong(),
                                    newCurMissionAfterLevel,
                                    0.00,
                                    TotalMissionClicks.toLong(),
                                    remainingTime.toDouble()
                                )
                                Toast.makeText(
                                    this@GameActivity,
                                    "Current Level: " + newLevel.toString() +
                                            "Current Mission: " + newCurMissionAfterLevel +
                                            "TotalMissionClicks: " + TotalMissionClicks +
                                            "Duration: " + remainingTime.toString(),
                                            Toast.LENGTH_LONG).show();
                            }}}


                   stopCountdownTimer() // Stop the countdown timer when the a ctivity is destroyed
                    TotalMissionClicks = 0;
                    Clicks = 0;

                    startCountdownTimer()
                }

                else if (Clicks % MissionReq == 0 && Clicks < 500 && Clicks != 0) {
                    //Sub Mission = Complete
                    DatabaseFunctions.accessUserDocument(this@GameActivity){ userDocument ->
                        if (userDocument != null){
                            val currentMission = userDocument.currentMission?: "0.0"
                            var newCurMission: String? = GameFunctions.getNextCurrentMission(currentMission)
                            if (newCurMission != null) {

                                Log.i("info", "Current Mission: " + newCurMission + " TotalMissionClicks: " + TotalMissionClicks + "Duration: " + remainingTime.toString())
                                subMissionCompleted(this@GameActivity, newCurMission, remainingTime.toDouble(), TotalMissionClicks.toLong())
                                Toast.makeText(this@GameActivity, "Current Mission: " + newCurMission + " TotalMissionClicks: " + TotalMissionClicks + "Duration: " + remainingTime.toString(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }

                if (TotalMissionClicks >= 0) {
                    if (buffer == 0) {
                        startCountdownTimer()
                        buffer++
                    }

                }

            }
        })

        // LEVEL 1; 1000 (200/sub mission); Search for New Habitat
        btnLevel1Clicker.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                popSound?.playPopSound(this@GameActivity)
                startBeatAnimation(btnLevel1Clicker)
                Toast.makeText(this@GameActivity,"Level 1 CLicker clicked", Toast.LENGTH_SHORT)
            }
        })

        // LEVEL 2; 1000 (200/sub mission); Beacon in the Galaxy
        btnLevel2Clicker.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                popSound?.playPopSound(this@GameActivity)
                startBeatAnimation(btnLevel2Clicker)
                Toast.makeText(this@GameActivity,"Level 2 CLicker clicked", Toast.LENGTH_SHORT)
            }
        })

        // LEVEL 3; 2000 (400/sub mission); The Cosmic Council
        btnLevel3Clicker.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                popSound?.playPopSound(this@GameActivity)
                startBeatAnimation(btnLevel3Clicker)
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

    // Game Activity Functions
    fun changeLevelMusic(currentLevel: Int) {
        musicPlayer?.release()
        if(currentLevel==0) {
            changeMusic(R.raw.level0_music)
        } else if(currentLevel==1) {
            changeMusic(R.raw.level1_music)
        } else if(currentLevel==2) {
            changeMusic(R.raw.level2_music)
        } else if(currentLevel==3) {
            changeMusic(R.raw.level3_music)
        } else if(currentLevel==4) {
            changeMusic(R.raw.level3_music)
        }
        musicPlayer?.setVolume(1.0f, 1.0f)
        val userPref = getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val isMusicEnabled = userPref.getBoolean("isMusicEnabled", true)
        if(isMusicEnabled) {
            playMusic()
        } else {
            if (isPlaying()) {
                pauseMusic()
            }
        }
    }

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun startBeatAnimation(lottieView: LottieAnimationView) {
        if (!isBeatAnimationRunning) {
            isBeatAnimationRunning = true
            // Scale up animation
            val scaleUpX = ObjectAnimator.ofFloat(lottieView, View.SCALE_X, 1.0f, 1.1f)
            val scaleUpY = ObjectAnimator.ofFloat(lottieView, View.SCALE_Y, 1.0f, 1.1f)
            // Scale down animation
            val scaleDownX = ObjectAnimator.ofFloat(lottieView, View.SCALE_X, 1.1f, 1.0f)
            val scaleDownY = ObjectAnimator.ofFloat(lottieView, View.SCALE_Y, 1.1f, 1.0f)
            // Create the animation sets
            val scaleUp = AnimatorSet().apply {
                play(scaleUpX).with(scaleUpY)
                duration = 100
                interpolator = AccelerateDecelerateInterpolator()
            }
            val scaleDown = AnimatorSet().apply {
                play(scaleDownX).with(scaleDownY)
                duration = 100
                interpolator = AccelerateDecelerateInterpolator()
            }
            // Create the final animation set
            val scaleAnimation = AnimatorSet().apply {
                playSequentially(scaleUp, scaleDown)
            }
            // Start the animations
            scaleAnimation.start()
            // Add a listener to reset the flag when the animation is finished
            scaleAnimation.addListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    isBeatAnimationRunning = false
                }
            })
        }
    }

    // Don't delete below this
}