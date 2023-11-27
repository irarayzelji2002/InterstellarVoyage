package com.example.interstellarvoyage

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import org.mindrot.jbcrypt.BCrypt

class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        var btnRegAccount : Button = findViewById(R.id.btnRegAccount)
        var btnAccess : Button = findViewById(R.id.btnAccess)
        var btnChangeEmailAdd : Button = findViewById(R.id.btnChangeEmailAdd)
        var btnDeleteAccount : Button = findViewById(R.id.btnDeleteAccount)
        var btnLevelCompleted : Button = findViewById(R.id.btnLevelCompleted)
        var btnSubMissionCompleted : Button = findViewById(R.id.btnSubMissionCompleted)
        var btnForgotPassword : Button = findViewById(R.id.btnForgotPassword)
        var btnChangePassword : Button = findViewById(R.id.btnChangePassword)
        var btnAccessMissions : Button = findViewById(R.id.btnAccessMissions)
        var btnAccessLeaderboard : Button = findViewById(R.id.btnAccessLeaderboard)
        var btnCalculateTotalTimeCompleted : Button = findViewById(R.id.btnCalculateTotalTimeCompleted)

        FirebaseApp.initializeApp(this)
        val db = FirebaseFirestore.getInstance()

        btnRegAccount.setOnClickListener {
            var username = "exampleUser"
            var email = "irarayzel.ji.cics@ust.edu.ph"
            var password = "irarayzelji"
            /*var hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())
            val result = BCrypt.checkpw(password, hashedPassword)
            Log.i("reg password", result.toString())*/

            FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = FirebaseAuth.getInstance().currentUser
                        if (user != null) {
                            val userDocumentRef = db.collection("users").document(user.uid)

                            userDocumentRef.get().addOnCompleteListener { documentTask ->
                                if (documentTask.isSuccessful) {
                                    val documentSnapshot = documentTask.result

                                    if (documentSnapshot != null && documentSnapshot.exists()) {
                                        // User document already exists
                                        Log.d(ContentValues.TAG, "User document already exists")
                                    } else {
                                        // User document does not exist, create it
                                        val userData = hashMapOf(
                                            "currentLevel" to 0,
                                            "currentMission" to "0.1",
                                            "numberOfClicks" to 0L,
                                            "totalTimeCompleted" to 0.0,
                                            "userDetails" to hashMapOf(
                                                "username" to username,
                                                "email" to email,
                                                "emailChangeFlag" to false
                                            ),
                                            "timeCompletedForLevels" to hashMapOf(
                                                "level0" to 0.0,
                                                "level1" to 0.0,
                                                "level2" to 0.0,
                                                "level3" to 0.0
                                            )
                                        )

                                        userDocumentRef.set(userData)
                                            .addOnSuccessListener {
                                                Toast.makeText(this, "Account created, please log in.", Toast.LENGTH_LONG).show()
                                                startActivity(Intent(this, LoginActivity::class.java))
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e(ContentValues.TAG, "Error adding user account.", e)
                                                Toast.makeText(this, "Registration failed, please try again.", Toast.LENGTH_LONG).show()
                                            }
                                    }
                                }
                            }
                        } else {
                            val exception = task.exception
                            Toast.makeText(this, "Registration failed.", Toast.LENGTH_LONG).show()
                            Log.e("account reg", exception.toString())
                        }
                    }
                }
        }

        btnAccess.setOnClickListener{
            val user = FirebaseAuth.getInstance().currentUser

            if (user != null) {
                Log.i("test","User is not null")
                val userDocumentRef = db.collection("users").document(user.uid)

                userDocumentRef.get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            // DocumentSnapshot data
                            val currentLevel = document.getLong("currentLevel")
                            val currentMission = document.getString("currentMission")
                            val numberOfClicks = document.getLong("numberOfClicks")
                            val totalTimeCompleted = document.getLong("totalTimeCompleted")

                            // Access nested fields
                            val userDetails = document.get("userDetails") as Map<String, Any>?
                            val username = userDetails?.get("username") as String?
                            val email = userDetails?.get("email") as String?
                            val emailChangeFlag = userDetails?.get("emailChangeFlag") as Boolean?

                            // Access timestamps for levels
                            val timeCompletedForLevels = document.get("timeCompletedForLevels") as Map<String, Any>?
                            val level1Timestamp = timeCompletedForLevels?.get("level1")?.let {
                                when (it) {
                                    is Double -> it
                                    is Long -> it.toDouble()
                                    else -> null // Handle other types if needed
                                }
                            }

                            Log.d("FirestoreData", "Current Level: $currentLevel")
                            Log.d("FirestoreData", "Current Mission: $currentMission")
                            Log.d("FirestoreData", "Number of Clicks: $numberOfClicks")
                            Log.d("FirestoreData", "Total Time Completed: $totalTimeCompleted")

                            Log.d("FirestoreData", "User Details: $userDetails")
                            Log.d("FirestoreData", "Username: $username")
                            Log.d("FirestoreData", "Email: $email")
                            Log.d("FirestoreData", "Username Change Flag: $emailChangeFlag")

                            Log.d("FirestoreData", "Levels Timestamp: $timeCompletedForLevels")
                            Log.d("FirestoreData", "Level 1 Timestamp: $level1Timestamp")
                        } else {
                            Log.d("FirestoreData", "No such document")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(ContentValues.TAG, "Error getting user data.", e)
                    }
            } else {
                Toast.makeText(this, "User not found.", Toast.LENGTH_SHORT).show()
            }
        }

        btnChangeEmailAdd.setOnClickListener {
            /*var oldemail = "irarayzel.ji.cics@ust.edu.ph"
            var email = "irarayzelji@gmail.com"
            var password = "irarayzelji"*/
            var oldemail = "irarayzelji@gmail.com"
            var email = "irarayzel.ji.cics@ust.edu.ph"
            var password = "irarayzelji2002"

            val user = FirebaseAuth.getInstance().currentUser
            Log.i("test","outside if")
            if (user != null) {
                // Reauthenticate the user
                val credential = EmailAuthProvider.getCredential(user.email!!, password)
                user.reauthenticate(credential)
                    .addOnCompleteListener { reauthTask ->
                        if (reauthTask.isSuccessful) {
                            // Update the email address in Firebase Authentication
                            user.sendEmailVerification()
                                .addOnCompleteListener { emailVerificationTask ->
                                    if (emailVerificationTask.isSuccessful) {
                                        Toast.makeText(this, "Verification email sent to $oldemail", Toast.LENGTH_LONG).show()

                                        val userDocumentRef = db.collection("users").document(user.uid)
                                        userDocumentRef.get().addOnSuccessListener { documentSnapshot ->
                                            if (documentSnapshot.exists()) {
                                                // Update email change flag in document
                                                userDocumentRef.update("userDetails.emailChangeFlag", true)
                                                    .addOnSuccessListener {
                                                        Log.d("FirestoreData", "Email change flag set to true")
                                                    }
                                                    .addOnFailureListener { e ->
                                                        Log.e("FirestoreData", "Email change flag update failed: $e")
                                                    }
                                                // Sign out and go to login page
                                                FirebaseAuth.getInstance().signOut()
                                                startActivity(Intent(this, LoginActivity::class.java))
                                            } else {
                                                Log.d("FirestoreData", "No such document")
                                            }
                                        }

                                        user.verifyBeforeUpdateEmail(email)
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    if(user.isEmailVerified) {
                                                    }
                                                }
                                            }
                                    } else {
                                        Toast.makeText(this, "Error sending verification email: ${emailVerificationTask.exception?.message}", Toast.LENGTH_LONG).show()
                                        Log.e("error sending verification email", "${emailVerificationTask.exception?.message}")
                                    }
                                }
                        } else {
                            Toast.makeText(this, "Reauthentication failed: ${reauthTask.exception?.message}", Toast.LENGTH_LONG).show()
                        }
                    }
            }
        }

        btnChangePassword.setOnClickListener {
            /*var oldpassword = "irarayzelji"
            var newpassword = "irarayzelji2002"
            var confirmnewpassword = "irarayzelji2002"*/
            var oldpassword = "irarayzelji2002"
            var newpassword = "irarayzelji"
            var confirmnewpassword = "irarayzelji"

            val user = FirebaseAuth.getInstance().currentUser

            if (user != null) {
                //Check if new password is same as confirm new password
                if(newpassword != confirmnewpassword) {
                    Toast.makeText(this, "New password and confirm new password must match", Toast.LENGTH_SHORT).show()
                } else {
                    // Reauthenticate the user
                    val credential = EmailAuthProvider.getCredential(user.email!!, oldpassword)
                    user.reauthenticate(credential)
                        .addOnCompleteListener { reauthTask ->
                            if (reauthTask.isSuccessful) {
                                // Update the password in Firebase Authentication
                                user.updatePassword(newpassword)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Toast.makeText(this, "Password updated", Toast.LENGTH_SHORT).show()
                                            // Sign out and go to login page
                                            FirebaseAuth.getInstance().signOut()
                                            startActivity(Intent(this, LoginActivity::class.java))
                                        } else {
                                            Toast.makeText(this, "Error updating password1", Toast.LENGTH_LONG).show()
                                            Log.d("error updating user password", "${task.exception?.message}")
                                        }
                                    }
                                    .addOnFailureListener { exception ->
                                        Toast.makeText(this, "Error updating password2", Toast.LENGTH_LONG).show()
                                        Log.d("Error updating password: ","${exception.message}")
                                    }
                            } else {
                                Log.d("Incorrect Pasword: ", "${reauthTask.exception?.message}")
                            }
                        }
                        .addOnFailureListener { exception ->
                            Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show()
                            Log.d("reauthentication failed", "${exception.message}")
                        }
                }
            }
        }

        btnForgotPassword.setOnClickListener {
            var email = "irarayzelji@gmail.com"
            val user = FirebaseAuth.getInstance().currentUser

            if (user != null) {
                FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(this, "Email verification sent.", Toast.LENGTH_LONG).show()
                        } else {
                            Toast.makeText(this, "Failed to send verification email.", Toast.LENGTH_LONG).show()
                        }
                    }
            } else {
                Toast.makeText(this, "User not found.", Toast.LENGTH_SHORT).show()
            }
        }

        btnDeleteAccount.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser
            Log.i("UserID", user?.uid ?: "User not logged in")
            Log.i("outside if","inside btnDeleteAccount")
            if (user != null) {
                val userDocumentRef = db.collection("users").document(user.uid)
                Log.i("DocumentToDelete", userDocumentRef.id)

                userDocumentRef.get().addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        userDocumentRef.delete()
                            .addOnSuccessListener {
                                Log.i("Firestore", "User document deleted successfully")
                                user.delete()
                                    .addOnSuccessListener {
                                        Toast.makeText(this, "User deleted.", Toast.LENGTH_LONG).show()
                                        startActivity(Intent(this, LoginActivity::class.java))
                                    }
                                    .addOnFailureListener { e ->
                                        Toast.makeText(this, "Error deleting user: ${e.message}", Toast.LENGTH_LONG).show()
                                    }
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, "Error deleting user document: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        Toast.makeText(this, "User document does not exist.", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                Toast.makeText(this, "User not found.", Toast.LENGTH_SHORT).show()
            }
        }

        btnLevelCompleted.setOnClickListener {
            var currentLevel = 2
            var currentMission = "2.0"
            var numberOfClicks = 300L
            var timeCompletedForLevel = 4000.65

            val user = FirebaseAuth.getInstance().currentUser

            if (user != null) {
                val userDocumentRef = db.collection("users").document(user.uid)
                userDocumentRef.get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            //new details of user
                            val updatedData  = hashMapOf(
                                "currentLevel" to currentLevel,
                                "currentMission" to currentMission,
                                "numberOfClicks" to numberOfClicks,
                                "timeCompletedForLevels.level$currentLevel" to timeCompletedForLevel
                            )

                            val updatedDataMap: Map<String, Any> = updatedData

                            //update in database
                            userDocumentRef.update(updatedDataMap)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "User updated successfully.", Toast.LENGTH_LONG).show()
                                }
                                .addOnFailureListener { e ->
                                    Log.e(ContentValues.TAG, "Error updating user document: $e")
                                    Toast.makeText(this, "Updating user details failed, please try again.", Toast.LENGTH_LONG).show()
                                }
                        } else {
                            Log.d("FirestoreData", "No such document")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(ContentValues.TAG, "Error getting user data: $e")
                    }
            } else {
                Toast.makeText(this, "User not found.", Toast.LENGTH_SHORT).show()
            }
        }

        btnSubMissionCompleted.setOnClickListener {
            var currentMission = "2.1"
            var numberOfClicks = 600L

            val user = FirebaseAuth.getInstance().currentUser

            if (user != null) {
                val userDocumentRef = db.collection("users").document(user.uid)
                userDocumentRef.get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            //new details of user
                            val updatedData  = hashMapOf(
                                "currentMission" to currentMission,
                                "numberOfClicks" to numberOfClicks
                            )

                            val updatedDataMap: Map<String, Any> = updatedData

                            //update in database
                            userDocumentRef.update(updatedDataMap)
                                .addOnSuccessListener {
                                    Toast.makeText(this, "User updated successfully.", Toast.LENGTH_LONG).show()
                                }
                                .addOnFailureListener { e ->
                                    Log.e(ContentValues.TAG, "Error updating user document: $e")
                                    Toast.makeText(this, "Updating user details failed, please try again.", Toast.LENGTH_LONG).show()
                                }
                        } else {
                            Log.d("FirestoreData", "No such document")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(ContentValues.TAG, "Error getting user data: $e")
                    }
            } else {
                Toast.makeText(this, "User not found.", Toast.LENGTH_SHORT).show()
            }
        }

        btnAccessMissions.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser

            if (user != null) {
                val userDocumentRef = db.collection("users").document(user.uid)

                userDocumentRef.get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            // DocumentSnapshot data
                            val currentLevel = document.getLong("currentLevel")
                            val currentMission = document.getString("currentMission")

                            // Get level names until user's current level
                            var completedLevels = mutableListOf<String>()
                            for ((index, level) in GameData.levels.withIndex()) {
                                val levelName = if (index <= currentLevel!!) {
                                    level.name
                                } else {
                                    ""
                                }
                                completedLevels.add(levelName)
                            }
                            Log.d("FirestoreData", "Current Level: $currentLevel")
                            Log.d("completedLevels", completedLevels.toString())

                            // Get mission names until user's current mission
                            var completedMissions = mutableListOf<String>()
                            for (mission in GameData.missions) {
                                val missionName = if (mission.id <= currentMission!!) {
                                    mission.name
                                } else {
                                    ""
                                }
                                completedMissions.add(missionName)
                            }
                            Log.d("FirestoreData", "Current Mission: $currentMission")
                            Log.d("completedMissions", completedMissions.toString())

                        } else {
                            Log.d("FirestoreData", "No such document")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(ContentValues.TAG, "Error getting user data.", e)
                    }

            } else {
                Toast.makeText(this, "User not found.", Toast.LENGTH_SHORT).show()
            }
        }

        btnAccessLeaderboard.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser

            if (user != null) {
                val userDocumentRef = db.collection("users").document(user.uid)

                userDocumentRef.get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            // DocumentSnapshot data
                            val currentLevel = document.getLong("currentLevel")
                            Log.d("FirestoreData", "Current Level: $currentLevel")

                            // Get top 5 shortest time completed of user's current level
                            val leaderboardForCurrentLevel = mutableListOf<LeaderboardEntry>()
                            db.collection("users")
                                .orderBy("timeCompletedForLevels.level$currentLevel")
                                .limit(5)
                                .get()
                                .addOnSuccessListener { documents ->
                                    var userRank = 0

                                    for (document in documents) {
                                        // Extract details from the document
                                        val userId = document.id
                                        val username = document.getString("userDetails.username") ?: "Unknown User"
                                        val timeCompleted = document.getDouble("timeCompletedForLevels.level$currentLevel") ?: 0.0
                                        // Increment the rank
                                        userRank++
                                        // Create a LeaderboardEntry and add to mutable list
                                        val leaderboardEntry = LeaderboardEntry(userRank, userId, username, timeCompleted)
                                        leaderboardForCurrentLevel.add(leaderboardEntry)
                                    }

                                    Log.d("leaderboardForCurrentLevel", leaderboardForCurrentLevel.toString())
                                }
                                .addOnFailureListener { e ->
                                    Log.d("Leaderboard", "Error getting leaderboard for current level", e)
                                }

                            // Get rank and details of user for current level
                            var currentUserRankCurrentLevel: LeaderboardEntry? = null
                            db.collection("users")
                                .orderBy("timeCompletedForLevels.level$currentLevel")
                                .get()
                                .addOnSuccessListener { documents ->
                                    var userRank1 = 0

                                    for (document in documents) {
                                        if(document.id == user.uid) {
                                        // Extract details from the document
                                        val userId = document.id
                                        val username = document.getString("userDetails.username") ?: "Unknown User"
                                        val timeCompleted = document.getDouble("timeCompletedForLevels.level$currentLevel") ?: 0.0

                                        // Store current user's rank
                                        currentUserRankCurrentLevel = LeaderboardEntry(userRank1, userId, username, timeCompleted)
                                        }
                                        // Increment the rank
                                        userRank1++
                                    }
                                    if (currentUserRankCurrentLevel == null) {
                                        Log.d("currentUserRankCurrentLevel", "User not in top 5 for current level")
                                    } else {
                                        Log.d("currentUserRankCurrentLevel", currentUserRankCurrentLevel.toString())
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.d("Leaderboard", "Error getting leaderboard for current level", e)
                                }

                            // Get top 5 shortest time completed for all levels
                            val leaderboardForAllLevels = mutableListOf<LeaderboardEntry>()
                            db.collection("users")
                                .orderBy("totalTimeCompleted")
                                .limit(5)
                                .get()
                                .addOnSuccessListener { documents ->
                                    var userRank2 = 0

                                    for (document in documents) {
                                        // Extract details from the document
                                        val userId = document.id
                                        val username = document.getString("userDetails.username") ?: "Unknown User"
                                        val totalTimeCompleted = document.getDouble("totalTimeCompleted") ?: 0.0
                                        // Increment the rank
                                        userRank2++
                                        // Create a LeaderboardEntry and add to mutable list
                                        val leaderboardEntry = LeaderboardEntry(userRank2, userId, username, totalTimeCompleted)
                                        leaderboardForAllLevels.add(leaderboardEntry)
                                    }

                                    Log.d("leaderboardForAllLevels", leaderboardForAllLevels.toString())
                                }
                                .addOnFailureListener { e ->
                                    Log.d("Leaderboard", "Error getting leaderboard for current level", e)
                                }

                            // Get rank and details of user for all levels
                            var currentUserRankAllLevels: LeaderboardEntry? = null
                            db.collection("users")
                                .orderBy("totalTimeCompleted")
                                .get()
                                .addOnSuccessListener { documents ->
                                    var userRank3 = 0

                                    for (document in documents) {
                                        if(document.id == user.uid) {
                                            // Extract details from the document
                                            val userId = document.id
                                            val username = document.getString("userDetails.username") ?: "Unknown User"
                                            val timeCompleted = document.getDouble("totalTimeCompleted") ?: 0.0

                                            // Store current user's rank
                                            currentUserRankAllLevels = LeaderboardEntry(userRank3, userId, username, timeCompleted)
                                        }
                                        // Increment the rank
                                        userRank3++
                                    }
                                    if (currentUserRankAllLevels == null) {
                                        Log.d("currentUserRankAllLevels", "User not in top 5 for all levels")
                                    } else {
                                        Log.d("currentUserRankAllLevels", currentUserRankAllLevels.toString())
                                    }
                                }
                                .addOnFailureListener { e ->
                                    Log.d("Leaderboard", "Error getting leaderboard for current level", e)
                                }
                        } else {
                            Log.d("FirestoreData", "No such document")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.d("Leaderboard", "Error getting leaderboard for current level", e)
                    }

            } else {
                Toast.makeText(this, "User not found.", Toast.LENGTH_SHORT).show()
            }
        }

        btnCalculateTotalTimeCompleted.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser

            if (user != null) {
                val userDocumentRef = db.collection("users").document(user.uid)
                userDocumentRef.get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            // Access time completed for levels
                            val timeCompletedForLevels = document.get("timeCompletedForLevels") as Map<String, Any>?
                            val level0Time = timeCompletedForLevels?.get("level0")?.let {
                                when (it) {
                                    is Double -> it
                                    is Long -> it.toDouble()
                                    else -> null // Handle other types if needed
                                }
                            }
                            val level1Time = timeCompletedForLevels?.get("level1")?.let {
                                when (it) {
                                    is Double -> it
                                    is Long -> it.toDouble()
                                    else -> null // Handle other types if needed
                                }
                            }
                            val level2Time = timeCompletedForLevels?.get("level2")?.let {
                                when (it) {
                                    is Double -> it
                                    is Long -> it.toDouble()
                                    else -> null // Handle other types if needed
                                }
                            }
                            val level3Time = timeCompletedForLevels?.get("level3")?.let {
                                when (it) {
                                    is Double -> it
                                    is Long -> it.toDouble()
                                    else -> null // Handle other types if needed
                                }
                            }
                            // Calculate sum
                            Log.d("FirestoreData", "Levels Timestamp: $timeCompletedForLevels")
                            var sumOfTimeCompletedForLevels = level0Time!! + level1Time!! + level2Time!! + level3Time!!
                            Log.d("Calculated Total", sumOfTimeCompletedForLevels.toString())
                            // Store to Firestore
                            userDocumentRef.update("totalTimeCompleted", sumOfTimeCompletedForLevels)
                                .addOnSuccessListener {
                                    Log.d("FirestoreData", "Updated total time completed")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("FirestoreData", "Error updating total time completed: $e")
                                }
                        } else {
                            Log.d("FirestoreData", "No such document")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(ContentValues.TAG, "Error getting user data: $e")
                    }
            } else {
                Toast.makeText(this, "User not found.", Toast.LENGTH_SHORT).show()
            }
        }

    //Don't Delete below this
    }
}