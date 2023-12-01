package com.example.interstellarvoyage

import android.content.Context
import android.view.View
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore

data class ChangeNalang(
    val kahitano1: Long?,
    val kahitano2: Double?,
    val kahitano3: String?
)

object GameFunctions {
    val db = FirebaseFirestore.getInstance()

    fun findNextLine(context: Context, currentMission: String): Line? {
        val nextMissionId = getNextMissionId(currentMission)
        return Storyline.lines.find { it.id == nextMissionId }
    }

    fun getNextMissionId(currentMission: String): String? {
        val parts = currentMission.split(".") //split string
        if (parts.size == 2) {
            val incrementedLastPart = (parts[1].toInt() + 1).toString() //increment the last digit
            return "${parts[0]}.${incrementedLastPart}.1" //combine to string & get first storyline of the next sub mission
        }
        return currentMission
    }

    fun playMusic(context: Context) {

    }

    fun pauseMusic(context: Context) {

    }

    fun buttonClickSound(context: Context, view: View) {
        /*lateinit var lottiePerson: LottieAnimationView
        lateinit var mediaPlayer: MediaPlayer

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            lottiePerson = findViewById(R.id.lottiePerson)
            mediaPlayer = MediaPlayer.create(this, R.raw.bubble_sound)
        }*/

        /*// Play the click sound
        mediaPlayer.start()

        // Add your click effect animations
        val scaleUp = ScaleAnimation(1f, 1.2f, 1f, 1.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        scaleUp.duration = 300
        scaleUp.fillAfter = true

        val scaleDown = ScaleAnimation(1.2f, 1f, 1.2f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f)
        scaleDown.duration = 300
        scaleDown.fillAfter = true

        val animationSet = AnimationSet(true)
        animationSet.addAnimation(scaleUp)
        animationSet.addAnimation(scaleDown)

        lottiePerson.startAnimation(animationSet)*/
    }

    fun scaleAnimation(context: Context) {

    }
}