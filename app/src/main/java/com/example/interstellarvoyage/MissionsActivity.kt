package com.example.interstellarvoyage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

class MissionsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_missions)

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
                val currentMission: String = userMissions.currentMission?.toString() ?: ""
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
}