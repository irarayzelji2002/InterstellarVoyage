package com.example.interstellarvoyage

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore

object GameFunctions {
    val db = FirebaseFirestore.getInstance()
    var handler: Handler? = null
    var isTimerRunning = false
    var elapsedTime = 0L

    fun findNextLine(context: Context, currentMission: String, currentStoryline: String): Line? {
        Log.d("findNextLine", "currMission: ${currentMission}; currStoryline: ${currentStoryline}")
            var nextMissionId = ""
            nextMissionId = getNextMissionId(currentMission)
            if (currentStoryline != "") {
                val parts = currentStoryline.split(".")
                if (parts[2].toInt() >= 1) {
                    nextMissionId = getNextMissionIdStoryline(currentStoryline)
                } else {
                    nextMissionId = getNextMissionId(currentMission)
                }
            }
            return Storyline.lines.find { it.id == nextMissionId }
    }

    fun getNextMissionId(currentMission: String): String {
        Log.d("Storyline Debug", "inside getNextMissionId")
        val parts = currentMission.split(".") //split string
        if (parts.size == 2) {
            val incrementedLastPart = (parts[1].toInt() + 1).toString() //increment the last digit
            return "${parts[0]}.${incrementedLastPart}.1" //combine to string & get first storyline of the next sub mission
        }
        return currentMission

        /*0.0 -> 0.1.1
        * 0.1 -> 0.2.1
        * 0.5 -> 0.6.1
        * 1.0 -> 1.1.1
        */
    }

    fun getNextMissionIdStoryline(currentStoryline: String) : String {
        Log.d("Storyline Debug", "inside getNextMissionIdStoryline")
        Log.d("Storyline Debug", currentStoryline)
        val parts = currentStoryline.split(".") //split string
        if (parts.size == 3) {
            val incrementedLastPart = (parts[2].toInt() + 1).toString() //increment the last digit
            return "${parts[0]}.${parts[1]}.${incrementedLastPart}" //combine to string & get first storyline of the next sub mission
        }
        return currentStoryline
    }

    fun getNextCurrentMission(currentMission: String): String? { //0.1 -> 0.2
        val parts = currentMission.split(".") //split string
        if (parts.size == 2) {
            val incrementedLastPart = (parts[1].toInt() + 1).toString() //increment the last digit
            return "${parts[0]}.${incrementedLastPart}" //combine to string
        }
        return currentMission
    }

    fun getNextCurrentMissionAfterLevel(currentLevel: String): String? { //0.1 -> 0.2
        //val parts = currentMission.split(".") //split string
        //if (parts.size == 2) {
        //    val incrementedFirstPart = (parts[0].toInt() + 1).toString() //increment the first digit
            return "${currentLevel}.0" //combine to string
        //}
        //return currentMission
    }

    fun getNextLevel(currentLevel: Int): Int? {
        return currentLevel + 1
    }

    fun initializeTimer(context: Context, txtTime: TextView) {
        DatabaseFunctions.accessUserDocument(context) { userDocument ->
            if (userDocument != null) {
                val dbCurrentDuration = userDocument.currentDuration
                elapsedTime = dbCurrentDuration?.toLong() ?: 0 //seconds
                elapsedTime *= 1000
                val seconds = elapsedTime / 1000
                val minutes = seconds / 60
                val remainingSeconds = seconds % 60
                txtTime.text = String.format("%02d:%02d", minutes, remainingSeconds)
                Log.d("Timer", "Initialized elapsedTime: $elapsedTime")
            }
        }
    }

    // Start Timer
    fun startCountupTimer(txtTime: TextView) {
        handler = Handler(Looper.getMainLooper())
        isTimerRunning = true
        Log.d("Timer", "elapsedTime: $elapsedTime")

        handler?.post(object : Runnable {
            override fun run() {
                if (isTimerRunning) {
                    elapsedTime += 1000L
                    Log.d("Timer", "elapsedTime inside: $elapsedTime")
                    val seconds = elapsedTime / 1000
                    val minutes = seconds / 60
                    val remainingSeconds = seconds % 60
                    txtTime.text = String.format("%02d:%02d", minutes, remainingSeconds)
                    var timeShown = String.format("%02d:%02d", minutes, remainingSeconds)
                    Log.d("Timer", timeShown)

                    // Post delayed with 1000ms (1 second) interval
                    handler?.postDelayed(this, 1000)
                }
            }
        })
    }

    // Pause Timer
    fun pauseCountupTimer() {
        isTimerRunning = false
    }

    // Resume Timer
    fun resumeCountupTimer(txtTime: TextView, storylineContainer: RelativeLayout, isCountdownRunning: Boolean) {
        Log.d("Timer", "isTimerRunning: "+isTimerRunning.toString())
        if (!isTimerRunning && storylineContainer.visibility == View.GONE && !isCountdownRunning) {
            isTimerRunning = true
            startCountupTimer(txtTime)
        }
    }

    // Stop Timer
    fun stopCountupTimer() {
        isTimerRunning = false
        elapsedTime = 0
        handler?.removeCallbacksAndMessages(null)
        handler = null
    }
}