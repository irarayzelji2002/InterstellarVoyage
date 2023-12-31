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
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.example.interstellarvoyage.DatabaseFunctions.levelCompleted
import com.example.interstellarvoyage.DatabaseFunctions.subMissionCompleted
import com.example.interstellarvoyage.GameFunctions.elapsedTime
import com.example.interstellarvoyage.GameFunctions.getNextCurrentMissionAfterLevel

class GameActivity : AppCompatActivity(), MusicPlayerCallback, GameOptionsActivity.GameOptionsListener {
    private lateinit var musicPlayer: MusicPlayer
    private lateinit var popSound: MusicPlayer
    private var bound = false
    private var serviceConnected = false
    var isBeatAnimationRunning = false

    var MissionReq: Int = 0
    var TotalMissionClicks: Int = 0;

    var buffer: Int = 0

    var lastClickTime: Long = 0
    var clicksInCurrentSecond: Int = 0

    var currentStoryline = ""
    var disableStoryline = false
    var toChangeMusic = false
    var isCountdownRunning = false
    var currentDuration = 0L
    var boost = 0

    private lateinit var txtTime: TextView
    private lateinit var txtBoost: TextView
    private lateinit var storylineContainer: RelativeLayout
    private lateinit var CountdownBG: LottieAnimationView

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

        getBackgroundMusic(playIntent)

        popSound = MusicPlayer()
        popSound.setVolume(0.1f, 0.1f)

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
        CountdownBG = findViewById(R.id.CountdownBG)
        var CompletedLevelsBG : LottieAnimationView = findViewById(R.id.CompletedLevelsBG)
        var levelGraphicsList = listOf(level0Graphics, level1Graphics, level2Graphics, level3Graphics)

        // Story related
        storylineContainer = findViewById(R.id.storylineContainer) //hide if clicks not reached
        var txtStoryline : TextView = findViewById(R.id.txtStoryline)
        var btnStoryline : Button = findViewById(R.id.btnStoryline)
        btnStoryline.isClickable = false

        // Clicks Number and Time Duration
        var txtClicks : TextView = findViewById(R.id.txtClicks)
        txtTime = findViewById(R.id.txtTime)
        var txtCPS : TextView = findViewById(R.id.txtCPS)
        txtBoost = findViewById(R.id.txtBoost)

        // Options Button
        var btnOptions : ImageButton = findViewById(R.id.btnOptions)

        // Populate activity info from database
        DatabaseFunctions.accessUserDocument(this) { userDocument ->
            if (userDocument != null) {
                // Store Click & Duration to local variable
                GameFunctions.initializeTimer(this, txtTime)
                val dbNumofClicks = userDocument.numberOfClicks
                TotalMissionClicks = dbNumofClicks?.toInt()?: 0

                // show level graphics
                showLevelGraphics(levelGraphicsList, CompletedLevelsBG)

                // Set storyline and button text
                Log.d("FirestoreData", "Current Level: ${userDocument.currentLevel}")
                Log.d("FirestoreData", "Current Mission: ${userDocument.currentMission}")
                Log.d("FirestoreData", "Current Duration: ${userDocument.currentDuration}")
                Log.d("FirestoreData", "Number of Clicks: ${userDocument.numberOfClicks}")
                Log.d("FirestoreData", "Total Time Completed: ${userDocument.totalTimeCompleted}")
                var btnClicker: LottieAnimationView = btnLevel0Clicker
                DatabaseFunctions.accessUserDocument(this) { userDocument ->
                    if(userDocument != null) {
                        val dbCurrentLevel: Long? = userDocument.currentLevel
                        val currentLevel: Int = dbCurrentLevel?.toInt() ?: 0
                        if(currentLevel == 0) {
                            btnClicker = btnLevel0Clicker
                        } else if(currentLevel == 1) {
                            btnClicker = btnLevel1Clicker
                        } else if(currentLevel == 2) {
                            btnClicker = btnLevel2Clicker
                        } else if(currentLevel == 3) {
                            btnClicker = btnLevel3Clicker
                        } else if(currentLevel == 4) {
                            btnClicker = btnLevel3Clicker
                        }
                    }
                }
                //displayStoryline(storylineContainer, txtStoryline, btnStoryline, btnClicker)

                // Set clicks count
                txtClicks.text = userDocument.numberOfClicks.toString()

                CountdownBG.addAnimatorListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {
                        isCountdownRunning = true
                        btnLevel0Clicker.isClickable = !isCountdownRunning
                        btnLevel1Clicker.isClickable = !isCountdownRunning
                        btnLevel2Clicker.isClickable = !isCountdownRunning
                        btnLevel3Clicker.isClickable = !isCountdownRunning
                    }

                    override fun onAnimationEnd(animation: Animator) {
                        isCountdownRunning = false
                        CountdownBG.visibility = View.GONE
                        btnLevel0Clicker.isClickable = !isCountdownRunning
                        btnLevel1Clicker.isClickable = !isCountdownRunning
                        btnLevel2Clicker.isClickable = !isCountdownRunning
                        btnLevel3Clicker.isClickable = !isCountdownRunning
                        GameFunctions.startCountupTimer(txtTime)
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        isCountdownRunning = false
                    }

                    override fun onAnimationRepeat(animation: Animator) {}
                })
                CountdownBG.playAnimation()
            }
        }

        // LEVEL 0; 500 clicks (100/sub mission); Earth’s Great Dilemma
        btnLevel0Clicker.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                btnClicker(btnLevel0Clicker, txtClicks, txtCPS, txtTime, levelGraphicsList, CompletedLevelsBG, 100, 0, playIntent, storylineContainer, txtStoryline, btnStoryline)
            }
        })

        // LEVEL 1; 1000 (200/sub mission); Search for New Habitat
        btnLevel1Clicker.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                btnClicker(btnLevel1Clicker, txtClicks, txtCPS, txtTime, levelGraphicsList, CompletedLevelsBG, 200, 1, playIntent, storylineContainer, txtStoryline, btnStoryline)
                //Toast.makeText(this@GameActivity,"Level 1 CLicker clicked", Toast.LENGTH_SHORT)
            }
        })

        // LEVEL 2; 1000 (200/sub mission); Beacon in the Galaxy
        btnLevel2Clicker.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                btnClicker(btnLevel2Clicker, txtClicks, txtCPS, txtTime, levelGraphicsList, CompletedLevelsBG, 300, 2, playIntent, storylineContainer, txtStoryline, btnStoryline)
                //Toast.makeText(this@GameActivity,"Level 2 CLicker clicked", Toast.LENGTH_SHORT)
            }
        })

        // LEVEL 3; 2000 (400/sub mission); The Cosmic Council
        btnLevel3Clicker.setOnClickListener(object : View.OnClickListener {
            override fun onClick(view: View) {
                btnClicker(btnLevel3Clicker, txtClicks, txtCPS, txtTime, levelGraphicsList, CompletedLevelsBG, 400, 3, playIntent, storylineContainer, txtStoryline, btnStoryline)
                //Toast.makeText(this@GameActivity,"Level 3 CLicker clicked", Toast.LENGTH_SHORT)
            }
        })

        btnStoryline.setOnClickListener {
            //Toast.makeText(this@GameActivity,"story button clicked", Toast.LENGTH_SHORT)
            var btnClicker: LottieAnimationView = btnLevel0Clicker
            DatabaseFunctions.accessUserDocument(this) { userDocument ->
                if(userDocument != null) {
                    val dbCurrentLevel: Long? = userDocument.currentLevel
                    val currentLevel: Int = dbCurrentLevel?.toInt() ?: 0
                    Log.d("Storyline next", "currLvl: "+currentLevel.toString())
                    if(currentLevel == 0) {
                        btnClicker = btnLevel0Clicker
                    } else if(currentLevel == 1) {
                        btnClicker = btnLevel1Clicker
                    } else if(currentLevel == 2) {
                        btnClicker = btnLevel2Clicker
                    } else if(currentLevel == 3) {
                        btnClicker = btnLevel3Clicker
                    } else if(currentLevel == 4) {
                        btnClicker = btnLevel3Clicker
                    }

                    Log.d("Storyline next", btnStoryline.text.toString())
                    Log.d("Storyline next", disableStoryline.toString())
                    if (disableStoryline == true || currentStoryline == "" || btnStoryline.text == "") {
                        storylineContainer.visibility = View.GONE
                        btnClicker.isClickable = true
                        btnStoryline.isClickable = false
                        btnStoryline.text = ""
                        currentStoryline = ""
                        disableStoryline = false
                        GameFunctions.resumeCountupTimer(txtTime, storylineContainer, isCountdownRunning)
                        Log.d("Storyline next", "inside start mission/next level/continue")
                    } else if (disableStoryline == false && toChangeMusic == false && btnStoryline.text == "NEXT" && currentStoryline != "") {
                        btnStoryline.isClickable = true
                        displayStoryline(storylineContainer, txtStoryline, btnStoryline, btnClicker)
                        storylineContainer.visibility = View.VISIBLE
                        Log.d("Storyline next", "inside next")
                    } else {
                        storylineContainer.visibility = View.GONE
                        btnClicker.isClickable = true
                        btnStoryline.isClickable = false
                        btnStoryline.text = ""
                        currentStoryline = ""
                        disableStoryline = false
                        GameFunctions.resumeCountupTimer(txtTime, storylineContainer, isCountdownRunning)
                    }

                    Log.d("Storyline next", toChangeMusic.toString())
                    if(toChangeMusic == true) {
                        GameFunctions.stopCountupTimer()
                        GameFunctions.startCountupTimer(txtTime)
                        TotalMissionClicks = 0
                        txtClicks.text = TotalMissionClicks.toString()
                        showLevelGraphics(levelGraphicsList, CompletedLevelsBG)
                        getBackgroundMusic(playIntent)
                        Log.d("Storyline next", "inside next level/continue")
                        toChangeMusic = false
                    }
                }
            }
        }

        btnOptions.setOnClickListener {
            var dialogFragment = GameOptionsActivity()
            dialogFragment.setCancelable(false)
            dialogFragment.setGameOptionsListener(this)
            dialogFragment.setMusicPlayerCallback(this)
            dialogFragment.show(supportFragmentManager, "Options Dialog")
            GameFunctions.pauseCountupTimer()
            if (isCountdownRunning) {
                CountdownBG.pauseAnimation()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (bound) {
            unbindService(connection)
            bound = false
        }
        DatabaseFunctions.accessUserDocument(this) { userDocument ->
            if (userDocument != null) {
                val dbCurrentMission = userDocument.currentMission.toString()
                subMissionCompleted(this@GameActivity, dbCurrentMission, currentDuration, TotalMissionClicks.toLong())
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (bound) {
            unbindService(connection)
            bound = false
        }
        DatabaseFunctions.accessUserDocument(this) { userDocument ->
            if (userDocument != null) {
                val dbCurrentMission = userDocument.currentMission.toString()
                subMissionCompleted(this@GameActivity, dbCurrentMission, currentDuration, TotalMissionClicks.toLong())
            }
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
        DatabaseFunctions.accessUserDocument(this) { userDocument ->
            if (userDocument != null) {
                val dbCurrentMission = userDocument.currentMission.toString()
                subMissionCompleted(this@GameActivity, dbCurrentMission, currentDuration, TotalMissionClicks.toLong())
            }
        }
    }

    //Clicks Per Seconds
    fun calculateCPS(): Float {
        val MIN_CLICK_INTERVAL = 10 //constant for the minimum click interval (in milliseconds)
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

        clicksInCurrentSecond++ // increment click count within the current second
        lastClickTime = currentTimeMillis  // Update last click time

        return String.format("%.2f", cps).toFloat()
    }

    fun showLevelGraphics(levelGraphicsList: List<View>, CompletedLevelsBG: LottieAnimationView) {
        DatabaseFunctions.accessUserDocument(this) { userDocument ->
            if (userDocument != null) {
                Log.d("FirestoreData", "Current Level: ${userDocument.currentLevel}")
                val dbCurrentLevel: Long? = userDocument.currentLevel
                val currentLevel: Int = dbCurrentLevel?.toInt() ?: 0

                when (currentLevel) {
                    0 -> {
                        levelGraphicsList[0].visibility = View.VISIBLE
                        levelGraphicsList[1].visibility = View.GONE
                        levelGraphicsList[2].visibility = View.GONE
                        levelGraphicsList[3].visibility = View.GONE
                        CompletedLevelsBG.visibility = View.GONE
                    }
                    1 -> {
                        levelGraphicsList[0].visibility = View.GONE
                        levelGraphicsList[1].visibility = View.VISIBLE
                        levelGraphicsList[2].visibility = View.GONE
                        levelGraphicsList[3].visibility = View.GONE
                        CompletedLevelsBG.visibility = View.GONE
                    }
                    2 -> {
                        levelGraphicsList[0].visibility = View.GONE
                        levelGraphicsList[1].visibility = View.GONE
                        levelGraphicsList[2].visibility = View.VISIBLE
                        levelGraphicsList[3].visibility = View.GONE
                        CompletedLevelsBG.visibility = View.GONE
                    }
                    3 -> {
                        levelGraphicsList[0].visibility = View.GONE
                        levelGraphicsList[1].visibility = View.GONE
                        levelGraphicsList[2].visibility = View.GONE
                        levelGraphicsList[3].visibility = View.VISIBLE
                        CompletedLevelsBG.visibility = View.GONE
                    }
                    4 -> {
                        levelGraphicsList[0].visibility = View.GONE
                        levelGraphicsList[1].visibility = View.GONE
                        levelGraphicsList[2].visibility = View.GONE
                        levelGraphicsList[3].visibility = View.GONE
                        CompletedLevelsBG.visibility = View.VISIBLE
                    }
                    else -> {
                        levelGraphicsList[0].visibility = View.VISIBLE
                        levelGraphicsList[1].visibility = View.GONE
                        levelGraphicsList[2].visibility = View.GONE
                        levelGraphicsList[3].visibility = View.GONE
                        CompletedLevelsBG.visibility = View.GONE
                    }
                }
            }
        }
    }

    fun getBackgroundMusic(playIntent: Intent) {
        DatabaseFunctions.accessUserDocument(this) { userDocument ->
            if (userDocument != null) {
                // Select music
                Log.d("FirestoreData", "Current Level: ${userDocument.currentLevel}")
                val dbCurrentLevel: Long? = userDocument.currentLevel
                var currentLevel: Int = dbCurrentLevel?.toInt() ?: 0
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
                        playIntent.putExtra(MusicPlayer.EXTRA_MUSIC_RESOURCE_ID, R.raw.homepage_music)
                    } else {
                        playIntent.putExtra(MusicPlayer.EXTRA_MUSIC_RESOURCE_ID, R.raw.level0_music)
                    }
                }
            }
        }
        if (!isServiceRunning(MusicPlayer::class.java)) {
            startService(playIntent)
        }
        if (bindService(playIntent, connection, Context.BIND_AUTO_CREATE)) {
            Log.i("Music", "Service binding successful")
        } else {
            Log.e("Music", "Service binding failed")
        }
    }

    fun displayStoryline(storylineContainer: RelativeLayout, txtStoryline: TextView, btnStoryline: Button, btnClicker: LottieAnimationView) {
        Log.d("Storyline Debug", "inside function")
        DatabaseFunctions.accessUserDocument(this@GameActivity) { userDocument ->
            if(userDocument != null) {
                Log.d("Storyline Debug", "Doc not null")
                val currentMission = userDocument.currentMission ?: "0.0"
                val currentLevel = userDocument.currentLevel ?: 0
                var nextLine = GameFunctions.findNextLine(this, currentMission, currentStoryline)
                if(currentLevel.toInt() == 4) {
                    nextLine = GameFunctions.findNextLine(this, "3.4", currentStoryline)
                } else if (currentLevel.toInt() == 3 && currentMission == "0.0") {
                    nextLine = GameFunctions.findNextLine(this, "3.4", currentStoryline)
                }
                if (nextLine != null) {
                    Log.d("Storyline Debug", "found next line: ${nextLine.id}")
                    Log.d("Storyline Debug", "found next line: ${nextLine.line}")
                    val id = nextLine.id
                    currentStoryline = nextLine.id
                    val btnText = nextLine.btnText
                    val line = nextLine.line
                    Log.d("Storyline", "currentMission: $currentMission, id: $id")
                    txtStoryline.text = line
                    var btnStorylineText = ""
                    if (btnText == 0) {
                        btnStorylineText = "NEXT"
                    } else if (btnText == 1) {
                        btnStorylineText = "START MISSION"
                        disableStoryline = true
                        currentStoryline = ""
                    } else if (btnText == 2) {
                        btnStorylineText = "NEXT LEVEL"
                        disableStoryline = true
                        toChangeMusic = true
                        currentStoryline = ""
                    } else if (btnText == 3) {
                        btnStorylineText = "CONTINUE"
                        disableStoryline = true
                        toChangeMusic = true
                        currentStoryline = ""
                    }
                    btnStoryline.setText(btnStorylineText)
                    btnClicker.isClickable = false
                    disableStoryline = false
                    //btnStoryline.isClickable = true
                    val handler = Handler(Looper.getMainLooper())
                    handler.postDelayed({ btnStoryline.isClickable = true }, 1000)
                    GameFunctions.pauseCountupTimer()
                    storylineContainer.visibility = View.VISIBLE
                } else {
                    disableStoryline = false
                    btnStoryline.isClickable = false
                    btnClicker.isClickable = true
                    Log.d("Storyline", "No matching Line found for id: $currentMission")
                    storylineContainer.visibility = View.GONE
                }
            }
        }
    }

    // btnLevelXClicker function
    fun btnClicker(btnClicker: LottieAnimationView, txtClicks: TextView, txtCPS:TextView, txtTime: TextView, levelGraphicsList: List<View>, CompletedLevelsBG:LottieAnimationView, MissionReg: Int, currentLevel: Int, playIntent:Intent, storylineContainer: RelativeLayout, txtStoryline: TextView, btnStoryline: Button) {
        popSound?.playPopSound(this@GameActivity)
        startBeatAnimation(btnClicker)

        currentDuration = elapsedTime / 1000

        var isSubMissionComplete = false
        if (MissionReq != 0) {
            /*if ((TotalMissionClicks % MissionReq == 0 || TotalMissionClicks % MissionReq == 5 || TotalMissionClicks % MissionReq == 10 || TotalMissionClicks % MissionReq == 15 || TotalMissionClicks % MissionReq == 20) && boost !=0) {
                isSubMissionComplete = true
                boost = 0
            } else*/ if (TotalMissionClicks % MissionReq == 0) {
                isSubMissionComplete = true
            }
        }

        //CLICKS PER SECOND
        val cpsFloat = calculateCPS()
        txtCPS.text = cpsFloat.toString()
        Log.i("CPS", cpsFloat.toString())

        boost = 5
        val cps = String.format("%.2f", cpsFloat).toFloat().toInt()
        if(cps <= 1) {
            txtBoost.text = ""
            txtBoost.visibility = View.GONE
        } else if(cps <= 5) {
            txtBoost.text = "1ST BOOST"
            boost += (5 * 2)
            txtBoost.visibility = View.VISIBLE
        } else if (cps <= 10) {
            txtBoost.text = "2ND BOOST"
            boost += (5 * 3)
            txtBoost.visibility = View.VISIBLE
        } else if (cps <= 15) {
            txtBoost.text = "3RD BOOST"
            boost += (5 * 3)
            txtBoost.visibility = View.VISIBLE
        } else if (cps <= 20) {
            txtBoost.text = "MAX BOOST"
            boost += (5 * 4)
            txtBoost.visibility = View.VISIBLE
        } else {
            txtBoost.text = "MAX BOOST"
            boost += (5 * 4)
            txtBoost.visibility = View.VISIBLE
        }

        txtClicks.text = TotalMissionClicks.toString();
        MissionReq = MissionReg
        var maxClicksCount = MissionReg * 5 // no. of sub missions
        var toIncrement = false
        TotalMissionClicks += 5

        Log.d("Game Debug", TotalMissionClicks.toString() +" % "+MissionReg.toString()+" before boost: "+boost.toString())
        Log.d("Game Debug", TotalMissionClicks.toString() +" == "+maxClicksCount.toString()+" before boost: "+boost.toString())

        //ADJUST CLICKS WITH BOOST
        val effectiveClicks = TotalMissionClicks
        val effectiveClicksWithBoost = effectiveClicks + boost

        if (effectiveClicks % MissionReq == 0 && effectiveClicks < maxClicksCount && effectiveClicks != 0) { // FOR SUB MISSION
            if (effectiveClicksWithBoost % MissionReg != 0) {
                // Calculate the excess clicks beyond the next sub-mission completion
                val excessClicks = effectiveClicksWithBoost % MissionReq
                // Subtract the excess clicks to ensure the condition is satisfied
                TotalMissionClicks = TotalMissionClicks + boost - excessClicks
            } else {
                TotalMissionClicks += boost
            }
        } else if (effectiveClicks == maxClicksCount) { // FOR LEVEL
            if (effectiveClicksWithBoost > maxClicksCount) {
                TotalMissionClicks = maxClicksCount
            } else {
                TotalMissionClicks += boost
            }
        }

        Log.d("Game Debug", TotalMissionClicks.toString() +" % "+MissionReg.toString()+" after boost: "+boost.toString())
        Log.d("Game Debug", (TotalMissionClicks % MissionReq).toString())
        Log.d("Game Debug", (TotalMissionClicks % MissionReq == 0).toString())
        Log.d("Game Debug", TotalMissionClicks.toString() +" == "+maxClicksCount.toString()+" after boost: "+boost.toString())
        Log.d("Game Debug", (TotalMissionClicks == maxClicksCount).toString())
        //LEVEL MISSION
        if (TotalMissionClicks == maxClicksCount) {
            //next mission
            Log.i("info", "Current Duration: " + currentDuration.toString())
            DatabaseFunctions.accessUserDocument(this@GameActivity) { userDocument ->
                if (userDocument != null) {
                    Log.i("info", "Mission Complete")
                    val dbCurrentLevel: Long? = userDocument.currentLevel
                    val currentLevel: Int = dbCurrentLevel?.toInt() ?: 0
                    val currentMission = userDocument.currentMission ?: "0.0"
                    var newLevel: Int? = GameFunctions.getNextLevel(currentLevel)
                    var newCurMissionAfterLevel: String? = getNextCurrentMissionAfterLevel(newLevel.toString())
                    Log.d("Game Debug", "newCurrMissionAfterLevel: "+newCurMissionAfterLevel.toString())
                    if (newCurMissionAfterLevel != null && newLevel != null && currentLevel!=4) {
                        levelCompleted(
                            this@GameActivity,
                            newLevel.toLong(),
                            newCurMissionAfterLevel,
                            0L,
                            0L,
                            currentDuration
                        )
                        /*Toast.makeText(this@GameActivity,
                                "Current Level: " + newLevel.toString() +
                                " Current Mission: " + newCurMissionAfterLevel +
                                " TotalMissionClicks: " + TotalMissionClicks +
                                " Duration: " + GameFunctions.getElapsedTime().toString(),
                            Toast.LENGTH_LONG).show();*/
                        if(newLevel==4) {
                            DatabaseFunctions.calculateTotalTimeCompleted(this, currentDuration)
                        }
                        GameFunctions.stopCountupTimer()
                        //TotalMissionClicks = 0
                    }
                }
            }
            displayStoryline(storylineContainer, txtStoryline, btnStoryline, btnClicker)
        } else if (TotalMissionClicks % MissionReq == 0 && TotalMissionClicks < maxClicksCount && TotalMissionClicks != 0) {
            //Sub Mission = Complete
            Log.i("info", "Sub Mission Complete")
            DatabaseFunctions.accessUserDocument(this@GameActivity){ userDocument ->
                if (userDocument != null){
                    val currentMission = userDocument.currentMission?: "0.0"
                    var newCurMission: String? = GameFunctions.getNextCurrentMission(currentMission)
                    if (newCurMission != null && newCurMission!="4.1") {
                        Log.i("info", "Current Mission: " + newCurMission + " TotalMissionClicks: " + TotalMissionClicks + "Duration: " + currentDuration.toString())
                        subMissionCompleted(
                            this@GameActivity, newCurMission,
                            currentDuration,
                            TotalMissionClicks.toLong()
                        )
                        //Toast.makeText(this@GameActivity, "Current Mission: " + newCurMission + " TotalMissionClicks: " + TotalMissionClicks + "Duration: " + GameFunctions.getElapsedTime().toString(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
            displayStoryline(storylineContainer, txtStoryline, btnStoryline, btnClicker)
        }

        if (TotalMissionClicks >= 0) {
            if (buffer == 0) {
                buffer++
            }
        }
    }

    // Game Options Listener functions
    override fun onContinueToGameClicked() {
        GameFunctions.resumeCountupTimer(txtTime, storylineContainer, isCountdownRunning)
        if (isCountdownRunning) {
            CountdownBG.resumeAnimation()
        }
    }

    override fun onBackToHomeClicked() {
        if(!isCountdownRunning) {
            GameFunctions.stopCountupTimer()
            DatabaseFunctions.accessUserDocument(this) { userDocument ->
                if (userDocument != null) {
                    val dbCurrentMission = userDocument.currentMission.toString()
                    subMissionCompleted(this@GameActivity, dbCurrentMission, currentDuration, TotalMissionClicks.toLong())
                }
            }
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

    // Music and Animation Functions
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
            changeMusic(R.raw.homepage_music)
        }
        musicPlayer?.setVolume(0.1f, 0.1f)
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
            val scaleUpX = ObjectAnimator.ofFloat(lottieView, View.SCALE_X, 1.0f, 1.2f)
            val scaleUpY = ObjectAnimator.ofFloat(lottieView, View.SCALE_Y, 1.0f, 1.2f)
            // Scale down animation
            val scaleDownX = ObjectAnimator.ofFloat(lottieView, View.SCALE_X, 1.2f, 1.0f)
            val scaleDownY = ObjectAnimator.ofFloat(lottieView, View.SCALE_Y, 1.2f, 1.0f)
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