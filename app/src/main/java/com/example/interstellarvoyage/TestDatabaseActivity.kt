package com.example.interstellarvoyage

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.CompletableFuture

class TestDatabaseActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test_database)

        var btnRegAccount : Button = findViewById(R.id.btnRegAccount)
        var btnAccess : Button = findViewById(R.id.btnAccess)
        var btnChangeEmailAdd : Button = findViewById(R.id.btnChangeEmailAdd)
        var btnDeleteAccount : Button = findViewById(R.id.btnDeleteAccount)
        var btnLevelCompleted : Button = findViewById(R.id.btnLevelCompleted)
        var btnSubMissionCompleted : Button = findViewById(R.id.btnSubMissionCompleted)
        var btnForgotPassword : Button = findViewById(R.id.btnForgotPassword)
        var btnChangePassword : Button = findViewById(R.id.btnChangePassword)
        var btnChangeUsername : Button = findViewById(R.id.btnChangeUsername)
        var btnLogout : Button = findViewById(R.id.btnLogout)
        var btnAccessMissions : Button = findViewById(R.id.btnAccessMissions)
        var btnAccessLeaderboard : Button = findViewById(R.id.btnAccessLeaderboard)
        var btnCalculateTotalTimeCompleted : Button = findViewById(R.id.btnCalculateTotalTimeCompleted)

        FirebaseApp.initializeApp(this)
        val db = FirebaseFirestore.getInstance()

        btnRegAccount.setOnClickListener {
            DatabaseFunctions.regAccount(this, "exampleUser", "irarayzel.ji.cics@ust.edu.ph", "irarayzelji")
        }

        btnAccess.setOnClickListener{
            DatabaseFunctions.accessUserDocument(this) { userDocument ->
                if (userDocument != null) {
                    Log.d("FirestoreData", "Current Level: ${userDocument.currentLevel}")
                    Log.d("FirestoreData", "Current Mission: ${userDocument.currentMission}")
                    Log.d("FirestoreData", "Number of Clicks: ${userDocument.numberOfClicks}")
                    Log.d("FirestoreData", "Total Time Completed: ${userDocument.totalTimeCompleted}")

                    Log.d("FirestoreData", "User Details: ${userDocument.userDetails}")
                    Log.d("FirestoreData", "Username: ${userDocument.userDetails?.username}")
                    Log.d("FirestoreData", "Email: ${userDocument.userDetails?.email}")
                    Log.d("FirestoreData", "Username Change Flag: ${userDocument.userDetails?.emailChangeFlag}")

                    Log.d("FirestoreData", "Levels Time: ${userDocument.timeCompletedForLevels}")
                    Log.d("FirestoreData", "Level 0 Time: ${userDocument.timeCompletedForLevels?.level0}")
                    Log.d("FirestoreData", "Level 1 Time: ${userDocument.timeCompletedForLevels?.level1}")
                    Log.d("FirestoreData", "Level 2 Time: ${userDocument.timeCompletedForLevels?.level2}")
                    Log.d("FirestoreData", "Level 3 Time: ${userDocument.timeCompletedForLevels?.level3}")
                }
            }
        }

        btnChangeEmailAdd.setOnClickListener {
            DatabaseFunctions.changeEmailAdd(this, "irarayzelji@gmail.com", "irarayzelji")
        }

        btnChangePassword.setOnClickListener {
            DatabaseFunctions.changePassword(this, "irarayzelji", "irarayzelji2002", "irarayzelji2002")
        }

        btnForgotPassword.setOnClickListener {
            DatabaseFunctions.forgotPassword(this)
        }

        btnChangeUsername.setOnClickListener {
            DatabaseFunctions.changeUsername(this, "myNewUsername")
        }

        btnLogout.setOnClickListener {
            DatabaseFunctions.logout(this)
        }

        btnDeleteAccount.setOnClickListener {
            DatabaseFunctions.deleteAccount(this)
        }

        btnLevelCompleted.setOnClickListener {
            DatabaseFunctions.levelCompleted(this, 2, "2.0", 300L, 4000.65)
        }

        btnSubMissionCompleted.setOnClickListener {
            DatabaseFunctions.subMissionCompleted(this, "2.1", 600L)
        }

        btnAccessMissions.setOnClickListener {
            DatabaseFunctions.accessMissions(this) { userMissions ->
                Log.d("userMissions", (userMissions != null).toString())
                if (userMissions != null) {
                    Log.d("FirestoreData", "Current Level: ${userMissions.currentLevel}")
                    Log.d("completedLevels", "${userMissions.completedLevels}")
                    Log.d("FirestoreData", "Current Mission: ${userMissions.currentMission}")
                    Log.d("completedMissions", "${userMissions.completedMissions}")
                }
            }
        }

        btnAccessLeaderboard.setOnClickListener {
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
            }
        }

        btnCalculateTotalTimeCompleted.setOnClickListener {
            DatabaseFunctions.calculateTotalTimeCompleted(this)
        }

    //Don't Delete below this
    }
}