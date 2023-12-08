package com.example.interstellarvoyage

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.CompletableFuture

class LeaderboardActivity : AppCompatActivity() {
    @SuppressLint("WrongViewCast")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboard)

        val btnBack = findViewById<Button>(R.id.btnBack)

        btnBack.setOnClickListener {
            startActivity(Intent(this, HomepageActivity::class.java))
        }

        FirebaseApp.initializeApp(this)
        val db = FirebaseFirestore.getInstance()

        val getCurrentLevel = CompletableFuture<Long>()
        val getLeaderboardForLevel0 = CompletableFuture<UserLeaderboard>()
        val getLeaderboardForLevel1 = CompletableFuture<UserLeaderboard>()
        val getLeaderboardForLevel2 = CompletableFuture<UserLeaderboard>()
        val getLeaderboardForLevel3 = CompletableFuture<UserLeaderboard>()
        val getLeaderboardForAllLevels = CompletableFuture<UserLeaderboard>()

        val user = FirebaseAuth.getInstance().currentUser

        // Get the current level
        if (user != null) {
            val userDocumentRef = db.collection("users").document(user.uid)
            userDocumentRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val currentLevel = document.getLong("currentLevel") ?: 0L
                        getCurrentLevel.complete(currentLevel)

                        // Get leaderboard for level 0, 1, 2, 3, and all
                        DatabaseFunctions.accessLeaderboardOneLevel(this, 0) { userLeaderboard ->
                            getLeaderboardForLevel0.complete(userLeaderboard)
                        }
                        DatabaseFunctions.accessLeaderboardOneLevel(this, 1) { userLeaderboard ->
                            getLeaderboardForLevel1.complete(userLeaderboard)
                        }
                        DatabaseFunctions.accessLeaderboardOneLevel(this, 2) { userLeaderboard ->
                            getLeaderboardForLevel2.complete(userLeaderboard)
                        }
                        DatabaseFunctions.accessLeaderboardOneLevel(this, 3) { userLeaderboard ->
                            getLeaderboardForLevel3.complete(userLeaderboard)
                        }
                        DatabaseFunctions.accessLeaderboardAllLevels(this) { userLeaderboard ->
                            getLeaderboardForAllLevels.complete(userLeaderboard)
                        }
                    } else {
                        getCurrentLevel.completeExceptionally(Exception("No such document"))
                    }
                }
                .addOnFailureListener { e ->
                    getCurrentLevel.completeExceptionally(e)
                }
        } else {
            Toast.makeText(this, "User not found.", Toast.LENGTH_SHORT).show()
            getCurrentLevel.completeExceptionally(Exception("User not found"))
        }

        CompletableFuture.allOf(
            getCurrentLevel,
            getLeaderboardForLevel0,
            getLeaderboardForLevel1,
            getLeaderboardForLevel2,
            getLeaderboardForLevel3,
            getLeaderboardForAllLevels
        ).thenAccept {
            Log.d("leaderboard", "Reached the final callback.")
            val currentLevel = getCurrentLevel.join()
            val leaderboardForLevel0 = getLeaderboardForLevel0.join()
            val leaderboardForLevel1 = getLeaderboardForLevel1.join()
            val leaderboardForLevel2 = getLeaderboardForLevel2.join()
            val leaderboardForLevel3 = getLeaderboardForLevel3.join()
            val leaderboardForAllLevels = getLeaderboardForAllLevels.join()

            Log.d("FinalCallback", "Current Level: $currentLevel")
            Log.d("FinalCallback0", "Leaderboard for Level 0: ${leaderboardForLevel0.leaderboardForLevel}")
            Log.d("FinalCallback0", "Current User Rank for Level 0: ${leaderboardForLevel0.currentUserRankLevel}")
            Log.d("FinalCallback1", "Leaderboard for Level 1: ${leaderboardForLevel1.leaderboardForLevel}")
            Log.d("FinalCallback1", "Current User Rank for Level 1: ${leaderboardForLevel1.currentUserRankLevel}")
            Log.d("FinalCallback2", "Leaderboard for Level 2: ${leaderboardForLevel2.leaderboardForLevel}")
            Log.d("FinalCallback2", "Current User Rank for Level 2: ${leaderboardForLevel2.currentUserRankLevel}")
            Log.d("FinalCallback3", "Leaderboard for Level 3: ${leaderboardForLevel3.leaderboardForLevel}")
            Log.d("FinalCallback3", "Current User Rank for Level 3: ${leaderboardForLevel3.currentUserRankLevel}")
            Log.d("FinalCallback4", "Leaderboard for All Levels: ${leaderboardForAllLevels.leaderboardForLevel}")
            Log.d("FinalCallback4", "Current User Rank for All Levels: ${leaderboardForAllLevels.currentUserRankLevel}")

            // SET VISIBILITY OF LEADERBOARDS
            val levelContainers = arrayOf(
                findViewById<LinearLayout>(R.id.level0Container),
                findViewById<LinearLayout>(R.id.level1Container),
                findViewById<LinearLayout>(R.id.level2Container),
                findViewById<LinearLayout>(R.id.level3Container)
            )
            for (i in 0 until levelContainers.size) {
                val container = levelContainers[i]
                container.visibility = if (currentLevel >= i) View.VISIBLE else View.GONE
            }

            // POPULATE ALL LEVELS
            Log.d("Debug", "all levels not null: "+(leaderboardForAllLevels != null).toString())
            if(leaderboardForAllLevels != null) {
                Log.d("Debug", "inside all levels")
                // For User
                val userRankContainer = findViewById<LinearLayout>(R.id.allLevelsUserRankContainer)
                val txtRankUser = findViewById<TextView>(R.id.txtRankAllLevelsUserRank)
                val txtUsernameUser = findViewById<TextView>(R.id.txtUsernameAllLevelsUserRank)
                val txtTimeUser = findViewById<TextView>(R.id.txtTimeAllLevelsUserRank)
                val userRank: String = leaderboardForAllLevels.currentUserRankLevel?.rank.toString() ?: "?"
                val userUserId: String = leaderboardForAllLevels.currentUserRankLevel?.userId ?: "Unknown Id"
                val userUsername: String = leaderboardForAllLevels.currentUserRankLevel?.username ?: "Unknown User"
                val userTimeCompleted: String = leaderboardForAllLevels.currentUserRankLevel?.timeCompleted.toString() ?: "0.00"
                txtRankUser.setText(userRank)
                txtUsernameUser.setText(userUsername)
                txtTimeUser.setText(userTimeCompleted)
                // Remove User Rank if it's their level
                if(currentLevel?.toInt() != 4) {
                    userRankContainer.visibility = View.GONE
                }
                // For Top 5
                for (i in 0 until 5) {
                    val rankContainer = findViewById<LinearLayout>(resources.getIdentifier("allLevelsRank${i + 1}Container", "id", packageName))
                    val txtUsername = findViewById<TextView>(resources.getIdentifier("txtUsernameAllLevelsRank${i + 1}", "id", packageName))
                    val txtTime = findViewById<TextView>(resources.getIdentifier("txtTimeAllLevelsRank${i + 1}", "id", packageName))
                    if (i < leaderboardForAllLevels.leaderboardForLevel.size) {
                        val entry = leaderboardForAllLevels.leaderboardForLevel[i]
                        val rank = entry.rank
                        val userId = entry.userId
                        val username = entry.username
                        val timeCompleted = entry.timeCompleted

                        Log.d("All Levels Leaderboard", "Rank: $rank, UserId: $userId, Username: $username, Time Completed: $timeCompleted")
                        txtUsername.setText("$username")
                        txtTime.setText("$timeCompleted")

                        // Remove visibility if user is in top 5
                        if (userUserId == userId) {
                            userRankContainer.visibility = View.GONE
                            rankContainer.setBackgroundResource(R.drawable.blue_container)
                        }
                    } else {
                        // less than 5 entries in the leaderboard
                        rankContainer.visibility = View.GONE
                    }
                }
            }

            // POPULATE LEVEL 3
            Log.d("Debug", "level3 not null: "+(leaderboardForLevel3 != null).toString())
            if(leaderboardForLevel3 != null) {
                // For User
                val userRankContainer = findViewById<LinearLayout>(R.id.level3UserRankContainer)
                val txtRankUser = findViewById<TextView>(R.id.txtRankLevel3UserRank)
                val txtUsernameUser = findViewById<TextView>(R.id.txtUsernameLevel3UserRank)
                val txtTimeUser = findViewById<TextView>(R.id.txtTimeLevel3UserRank)
                val userRank: String = leaderboardForLevel3.currentUserRankLevel?.rank.toString() ?: "?"
                val userUserId: String = leaderboardForLevel3.currentUserRankLevel?.userId ?: "Unknown Id"
                val userUsername: String = leaderboardForLevel3.currentUserRankLevel?.username ?: "Unknown User"
                val userTimeCompleted: String = leaderboardForLevel3.currentUserRankLevel?.timeCompleted.toString() ?: "0.00"
                txtRankUser.setText(userRank)
                txtUsernameUser.setText(userUsername)
                txtTimeUser.setText(userTimeCompleted)
                // Remove User Rank if it's their level
                if(currentLevel?.toInt() == 3) {
                    userRankContainer.visibility = View.GONE
                }
                // For Top 5
                for (i in 0 until 5) {
                    val rankContainer = findViewById<LinearLayout>(resources.getIdentifier("level3Rank${i + 1}Container", "id", packageName))
                    val txtUsername = findViewById<TextView>(resources.getIdentifier("txtUsernameLevel3Rank${i + 1}", "id", packageName))
                    val txtTime = findViewById<TextView>(resources.getIdentifier("txtTimeLevel3Rank${i + 1}", "id", packageName))
                    if (i < leaderboardForLevel3.leaderboardForLevel.size) {
                        val entry = leaderboardForLevel3.leaderboardForLevel[i]
                        val rank = entry.rank
                        val userId = entry.userId
                        val username = entry.username
                        val timeCompleted = entry.timeCompleted

                        Log.d("Level3 Leaderboard", "Rank: $rank, UserId: $userId, Username: $username, Time Completed: $timeCompleted")
                        txtUsername.setText("$username")
                        txtTime.setText("$timeCompleted")

                        // Remove visibility if user is in top 5
                        if (userUserId == userId) {
                            userRankContainer.visibility = View.GONE
                            rankContainer.setBackgroundResource(R.drawable.blue_container)
                        }
                    } else {
                        // less than 5 entries in the leaderboard
                        rankContainer.visibility = View.GONE
                    }
                }
            }

            // POPULATE LEVEL 2
            Log.d("Debug", "level2 not null: "+(leaderboardForLevel2 != null).toString())
            if(leaderboardForLevel2 != null) {
                // For User
                val userRankContainer = findViewById<LinearLayout>(R.id.level2UserRankContainer)
                val txtRankUser = findViewById<TextView>(R.id.txtRankLevel2UserRank)
                val txtUsernameUser = findViewById<TextView>(R.id.txtUsernameLevel2UserRank)
                val txtTimeUser = findViewById<TextView>(R.id.txtTimeLevel2UserRank)
                val userRank: String = leaderboardForLevel2.currentUserRankLevel?.rank.toString() ?: "?"
                val userUserId: String = leaderboardForLevel2.currentUserRankLevel?.userId ?: "Unknown Id"
                val userUsername: String = leaderboardForLevel2.currentUserRankLevel?.username ?: "Unknown User"
                val userTimeCompleted: String = leaderboardForLevel2.currentUserRankLevel?.timeCompleted.toString() ?: "0.00"
                txtRankUser.setText(userRank)
                txtUsernameUser.setText(userUsername)
                txtTimeUser.setText(userTimeCompleted)
                // Remove User Rank if it's their level
                if(currentLevel?.toInt() == 2) {
                    userRankContainer.visibility = View.GONE
                }
                // For Top 5
                for (i in 0 until 5) {
                    val rankContainer = findViewById<LinearLayout>(resources.getIdentifier("level2Rank${i + 1}Container", "id", packageName))
                    val txtUsername = findViewById<TextView>(resources.getIdentifier("txtUsernameLevel2Rank${i + 1}", "id", packageName))
                    val txtTime = findViewById<TextView>(resources.getIdentifier("txtTimeLevel2Rank${i + 1}", "id", packageName))
                    if (i < leaderboardForLevel2.leaderboardForLevel.size) {
                        val entry = leaderboardForLevel2.leaderboardForLevel[i]
                        val rank = entry.rank
                        val userId = entry.userId
                        val username = entry.username
                        val timeCompleted = entry.timeCompleted

                        Log.d("Level2 Leaderboard", "Rank: $rank, UserId: $userId, Username: $username, Time Completed: $timeCompleted")
                        txtUsername.setText("$username")
                        txtTime.setText("$timeCompleted")

                        // Remove visibility if user is in top 5
                        if (userUserId == userId) {
                            userRankContainer.visibility = View.GONE
                            rankContainer.setBackgroundResource(R.drawable.blue_container)
                        }
                    } else {
                        // less than 5 entries in the leaderboard
                        rankContainer.visibility = View.GONE
                    }
                }
            }

            // POPULATE LEVEL 1
            Log.d("Debug", "level1 not null: "+(leaderboardForLevel1 != null).toString())
            if(leaderboardForLevel1 != null) {
                // For User
                val userRankContainer = findViewById<LinearLayout>(R.id.level1UserRankContainer)
                val txtRankUser = findViewById<TextView>(R.id.txtRankLevel1UserRank)
                val txtUsernameUser = findViewById<TextView>(R.id.txtUsernameLevel1UserRank)
                val txtTimeUser = findViewById<TextView>(R.id.txtTimeLevel1UserRank)
                val userRank: String = leaderboardForLevel1.currentUserRankLevel?.rank.toString() ?: "?"
                val userUserId: String = leaderboardForLevel1.currentUserRankLevel?.userId ?: "Unknown Id"
                val userUsername: String = leaderboardForLevel1.currentUserRankLevel?.username ?: "Unknown User"
                val userTimeCompleted: String = leaderboardForLevel1.currentUserRankLevel?.timeCompleted.toString() ?: "0.00"
                txtRankUser.setText(userRank)
                txtUsernameUser.setText(userUsername)
                txtTimeUser.setText(userTimeCompleted)
                // Remove User Rank if it's their level
                if(currentLevel?.toInt() == 1) {
                    userRankContainer.visibility = View.GONE
                }
                // For Top 5
                for (i in 0 until 5) {
                    val rankContainer = findViewById<LinearLayout>(resources.getIdentifier("level1Rank${i + 1}Container", "id", packageName))
                    val txtUsername = findViewById<TextView>(resources.getIdentifier("txtUsernameLevel1Rank${i + 1}", "id", packageName))
                    val txtTime = findViewById<TextView>(resources.getIdentifier("txtTimeLevel1Rank${i + 1}", "id", packageName))
                    if (i < leaderboardForLevel1.leaderboardForLevel.size) {
                        val entry = leaderboardForLevel1.leaderboardForLevel[i]
                        val rank = entry.rank
                        val userId = entry.userId
                        val username = entry.username
                        val timeCompleted = entry.timeCompleted

                        Log.d("Level1 Leaderboard", "Rank: $rank, UserId: $userId, Username: $username, Time Completed: $timeCompleted")
                        txtUsername.setText("$username")
                        txtTime.setText("$timeCompleted")

                        // Remove visibility if user is in top 5
                        if (userUserId == userId) {
                            userRankContainer.visibility = View.GONE
                            rankContainer.setBackgroundResource(R.drawable.blue_container)
                        }
                    } else {
                        // less than 5 entries in the leaderboard
                        rankContainer.visibility = View.GONE
                    }
                }
            }

            // POPULATE LEVEL 0
            Log.d("Debug", "level0 not null: "+(leaderboardForLevel0 != null).toString())
            if(leaderboardForLevel0 != null) {
                // For User
                val userRankContainer = findViewById<LinearLayout>(R.id.level0UserRankContainer)
                val txtRankUser = findViewById<TextView>(R.id.txtRankLevel0UserRank)
                val txtUsernameUser = findViewById<TextView>(R.id.txtUsernameLevel0UserRank)
                val txtTimeUser = findViewById<TextView>(R.id.txtTimeLevel0UserRank)
                val userRank: String = leaderboardForLevel0.currentUserRankLevel?.rank.toString() ?: "?"
                val userUserId: String = leaderboardForLevel0.currentUserRankLevel?.userId ?: "Unknown Id"
                val userUsername: String = leaderboardForLevel0.currentUserRankLevel?.username ?: "Unknown User"
                val userTimeCompleted: String = leaderboardForLevel0.currentUserRankLevel?.timeCompleted.toString() ?: "0.00"
                txtRankUser.setText(userRank)
                txtUsernameUser.setText(userUsername)
                txtTimeUser.setText(userTimeCompleted)
                // Remove User Rank if it's their level
                if(currentLevel?.toInt() == 0) {
                    userRankContainer.visibility = View.GONE
                }
                // For Top 5
                for (i in 0 until 5) {
                    val rankContainer = findViewById<LinearLayout>(resources.getIdentifier("level0Rank${i + 1}Container", "id", packageName))
                    val txtUsername = findViewById<TextView>(resources.getIdentifier("txtUsernameLevel0Rank${i + 1}", "id", packageName))
                    val txtTime = findViewById<TextView>(resources.getIdentifier("txtTimeLevel0Rank${i + 1}", "id", packageName))
                    if (i < leaderboardForLevel0.leaderboardForLevel.size) {
                        val entry = leaderboardForLevel0.leaderboardForLevel[i]
                        val rank = entry.rank
                        val userId = entry.userId
                        val username = entry.username
                        val timeCompleted = entry.timeCompleted

                        Log.d("Level0Leaderboard", "Rank: $rank, UserId: $userId, Username: $username, Time Completed: $timeCompleted")
                        txtUsername.setText("$username")
                        txtTime.setText("$timeCompleted")

                        // Remove visibility if user is in top 5
                        if (userUserId == userId) {
                            userRankContainer.visibility = View.GONE
                            rankContainer.setBackgroundResource(R.drawable.blue_container)
                        }
                    } else {
                        // less than 5 entries in the leaderboard
                        rankContainer.visibility = View.GONE
                    }
                }
            }
        }
    }
}
