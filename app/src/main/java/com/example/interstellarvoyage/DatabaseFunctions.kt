package com.example.interstellarvoyage

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.CompletableFuture

data class UserDocument(
    val currentLevel: Long?,
    val currentMission: String?,
    val numberOfClicks: Long?,
    val totalTimeCompleted: Double?,
    val userDetails: UserDetails?,
    val timeCompletedForLevels: TimeCompletedForLevels?
)

data class UserDetails(
    val username: String?,
    val email: String?,
    val emailChangeFlag: Boolean?
)

data class TimeCompletedForLevels(
    val level0: Double?,
    val level1: Double?,
    val level2: Double?,
    val level3: Double?
)

data class UserMissions(
    val currentLevel: Long?,
    var completedLevels: MutableList<String>?,
    val currentMission: String?,
    val completedMissions: MutableList<String>?
)

data class UserLeaderboard(
    val currentLevel: Long?,
    val leaderboardForLevel: List<LeaderboardEntry>,
    val currentUserRankLevel: LeaderboardEntry?,
)

object DatabaseFunctions {
    val db = FirebaseFirestore.getInstance()

    fun login(context: Context, email: String, password: String) {
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { signInTask ->
                if (signInTask.isSuccessful) {
                    val user = FirebaseAuth.getInstance().currentUser
                    if (user != null) {
                        val userDocumentRef = db.collection("users").document(user.uid)
                        userDocumentRef.get().addOnSuccessListener { documentSnapshot ->
                            if (documentSnapshot.exists()) {
                                val emailChangeFlag = documentSnapshot.getBoolean("userDetails.emailChangeFlag") ?: false
                                Log.d("emailChangeFlag", emailChangeFlag.toString())
                                if (emailChangeFlag) {
                                    val userData = hashMapOf(
                                        "userDetails.email" to email,
                                        "userDetails.emailChangeFlag" to false
                                    )

                                    val updatedDataMap: Map<String, Any> = userData

                                    userDocumentRef.update(updatedDataMap)
                                        .addOnSuccessListener {
                                            Log.d("FirestoreData", "email change flag cleared and email updated")
                                        }
                                        .addOnFailureListener { e ->
                                            Log.e("FirestoreData", "Error clearing email change flag and updating email: $e")
                                        }
                                }
                                val userPref = context.getSharedPreferences("UserPrefs", MODE_PRIVATE)
                                val editor = userPref.edit()
                                editor.putBoolean("isLoggedIn", true)
                                editor.apply()
                                context.startActivity(Intent(context, TestDatabaseActivity::class.java))
                            } else {
                                Log.d("FirestoreData", "No such document")
                            }
                        }
                            .addOnFailureListener { exception ->
                                when (exception) {
                                    is FirebaseAuthInvalidUserException -> { // User does not exist or has been disabled
                                        Toast.makeText(context, "Invalid email or password.", Toast.LENGTH_LONG).show()
                                    }
                                    is FirebaseAuthInvalidCredentialsException -> { // Invalid password
                                        Toast.makeText(context, "Invalid email or password.", Toast.LENGTH_LONG).show()
                                    }
                                    else -> { // General error
                                        Toast.makeText(context, "Login failed. ${exception?.message}", Toast.LENGTH_LONG).show()
                                        Log.d("login failed", "${exception?.message}")
                                    }
                                }
                            }
                    }
                } else {
                    Toast.makeText(context, "Invalid fields.", Toast.LENGTH_LONG).show()
                }
            }
    }

    fun regAccount(context: Context, username: String, email: String, password: String) {
        var username = username
        var email = email
        var password = password

        checkUsernameUnique(username) { isUnique ->
            if (isUnique) {
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
                                                    Toast.makeText(context, "Account created, please log in.", Toast.LENGTH_LONG).show()
                                                    context.startActivity(Intent(context, LoginActivity::class.java))
                                                }
                                                .addOnFailureListener { e ->
                                                    Log.e(ContentValues.TAG, "Error adding user account.", e)
                                                    Toast.makeText(context, "Registration failed, please try again.", Toast.LENGTH_LONG).show()
                                                }
                                        }
                                    }
                                }
                            } else {
                                val exception = task.exception
                                Toast.makeText(context, "Registration failed.", Toast.LENGTH_LONG).show()
                                Log.e("account reg", exception.toString())
                            }
                        }
                    }
            } else {
                Toast.makeText(context, "Username is not unique.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun checkUsernameUnique(username: String, callback: (Boolean) -> Unit) {
        db.collection("users")
            .whereEqualTo("userDetails.username", username)
            .get()
            .addOnSuccessListener { querySnapshot ->
                callback(querySnapshot.isEmpty) // true if empty, false if not
            }
            .addOnFailureListener { e ->
                Log.e(ContentValues.TAG, "Error checking username uniqueness.", e)
                callback(false) // false if failed to get data
            }
    }

    fun accessUserDocument(context: Context, callback: (UserDocument?) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            val userDocumentRef = db.collection("users").document(user.uid)

            userDocumentRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        // DocumentSnapshot data
                        val currentLevel = document.getLong("currentLevel")
                        val currentMission = document.getString("currentMission")
                        val numberOfClicks = document.getLong("numberOfClicks")
                        val totalTimeCompleted = document.getDouble("totalTimeCompleted")

                        // Access nested fields
                        val userDetails = document.get("userDetails") as Map<String, Any>?
                        val username = userDetails?.get("username") as String?
                        val email = userDetails?.get("email") as String?
                        val emailChangeFlag = userDetails?.get("emailChangeFlag") as Boolean?

                        // Access timestamps for levels
                        val timeCompletedForLevels = document.get("timeCompletedForLevels") as Map<String, Any>?
                        val level0Time = timeCompletedForLevels?.get("level0")?.let {
                            when (it) {
                                is Double -> it
                                is Long -> it.toDouble()
                                else -> null
                            }
                        }
                        val level1Time = timeCompletedForLevels?.get("level1")?.let {
                            when (it) {
                                is Double -> it
                                is Long -> it.toDouble()
                                else -> null
                            }
                        }
                        val level2Time = timeCompletedForLevels?.get("level2")?.let {
                            when (it) {
                                is Double -> it
                                is Long -> it.toDouble()
                                else -> null
                            }
                        }
                        val level3Time = timeCompletedForLevels?.get("level3")?.let {
                            when (it) {
                                is Double -> it
                                is Long -> it.toDouble()
                                else -> null
                            }
                        }

                        val userDocument =  UserDocument(
                            currentLevel, currentMission, numberOfClicks, totalTimeCompleted,
                            UserDetails(username, email, emailChangeFlag),
                            TimeCompletedForLevels(level0Time, level1Time, level2Time, level3Time)
                        )
                        callback(userDocument)
                    } else {
                        Log.d("FirestoreData", "No such document")
                        callback(null)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(ContentValues.TAG, "Error getting user data.", e)
                    callback(null)
                }
        } else {
            Toast.makeText(context, "User not found.", Toast.LENGTH_SHORT).show()
            callback(null)
        }
    }

    fun changeEmailAdd(context: Context, oldemail: String, email: String, password: String,) {
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
                                    Toast.makeText(context, "Verification email sent to $oldemail", Toast.LENGTH_LONG).show()

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
                                            val userPref = context.getSharedPreferences("UserPrefs", MODE_PRIVATE)
                                            val editor = userPref.edit()
                                            editor.putBoolean("isLoggedIn", false)
                                            editor.apply()
                                            context.startActivity(Intent(context, LoginActivity::class.java))
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
                                    Toast.makeText(context, "Error sending verification email: ${emailVerificationTask.exception?.message}", Toast.LENGTH_LONG).show()
                                    Log.e("error sending verification email", "${emailVerificationTask.exception?.message}")
                                }
                            }
                    } else {
                        Toast.makeText(context, "Reauthentication failed: ${reauthTask.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    fun changePassword(context: Context, oldpassword: String, newpassword: String, confirmnewpassword: String) {
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            //Check if new password is same as confirm new password
            if(newpassword != confirmnewpassword) {
                Toast.makeText(context, "New password and confirm new password must match", Toast.LENGTH_SHORT).show()
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
                                        Toast.makeText(context, "Password updated", Toast.LENGTH_SHORT).show()
                                        // Sign out and go to login page
                                        FirebaseAuth.getInstance().signOut()
                                        val userPref = context.getSharedPreferences("UserPrefs", MODE_PRIVATE)
                                        val editor = userPref.edit()
                                        editor.putBoolean("isLoggedIn", false)
                                        editor.apply()
                                        context.startActivity(Intent(context, LoginActivity::class.java))
                                    } else {
                                        Toast.makeText(context, "Error updating password1", Toast.LENGTH_LONG).show()
                                        Log.d("error updating user password", "${task.exception?.message}")
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    Toast.makeText(context, "Error updating password2", Toast.LENGTH_LONG).show()
                                    Log.d("Error updating password: ","${exception.message}")
                                }
                        } else {
                            Log.d("Incorrect Pasword: ", "${reauthTask.exception?.message}")
                        }
                    }
                    .addOnFailureListener { exception ->
                        Toast.makeText(context, "Incorrect password", Toast.LENGTH_SHORT).show()
                        Log.d("reauthentication failed", "${exception.message}")
                    }
            }
        }
    }

    fun forgotPassword(context: Context){
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            FirebaseAuth.getInstance().sendPasswordResetEmail(user.email!!)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Email verification sent.", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(context, "Failed to send verification email.", Toast.LENGTH_LONG).show()
                    }
                }
        } else {
            Toast.makeText(context, "User not found.", Toast.LENGTH_SHORT).show()
        }
    }

    fun changeUsername(context: Context, newUsername: String) {
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            val userDocumentRef = db.collection("users").document(user.uid)

            checkUsernameUnique(newUsername) { isUnique ->
                if (isUnique) {
                    userDocumentRef.update("userDetails.username", newUsername)
                        .addOnSuccessListener {
                            Toast.makeText(context, "Username updated successfully.", Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener { e ->
                            Log.e("EditUsername", "Error updating username", e)
                            Toast.makeText(context, "Failed to update username.", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    Toast.makeText(context, "Username is not unique.", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "User not found.", Toast.LENGTH_SHORT).show()
        }
    }

    fun logout(context: Context) {
        FirebaseAuth.getInstance().signOut()
        val userPref = context.getSharedPreferences("UserPrefs", MODE_PRIVATE)
        val editor = userPref.edit()
        editor.putBoolean("isLoggedIn", false)
        editor.apply()
        context.startActivity(Intent(context, LoginActivity::class.java))
        if (context is Activity) {
            (context as Activity).finish()
        }
    }

    fun deleteAccount(context: Context) {
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
                                    Toast.makeText(context, "User deleted.", Toast.LENGTH_LONG).show()
                                    context.startActivity(Intent(context, LoginActivity::class.java))
                                }
                                .addOnFailureListener { e ->
                                    Toast.makeText(context, "Error deleting user: ${e.message}", Toast.LENGTH_LONG).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(context, "Error deleting user document: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                } else {
                    Toast.makeText(context, "User document does not exist.", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Toast.makeText(context, "User not found.", Toast.LENGTH_SHORT).show()
        }
    }

    fun levelCompleted (context: Context, currentLevel: Long, currentMission: String, numberOfClicks: Long, timeCompletedForLevel: Double) {
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
                                Toast.makeText(context, "User updated successfully.", Toast.LENGTH_LONG).show()
                            }
                            .addOnFailureListener { e ->
                                Log.e(ContentValues.TAG, "Error updating user document: $e")
                                Toast.makeText(context, "Updating user details failed, please try again.", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        Log.d("FirestoreData", "No such document")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(ContentValues.TAG, "Error getting user data: $e")
                }
        } else {
            Toast.makeText(context, "User not found.", Toast.LENGTH_SHORT).show()
        }
    }

    fun subMissionCompleted(context: Context, currentMission: String, numberOfClicks: Long) {
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
                                Toast.makeText(context, "User updated successfully.", Toast.LENGTH_LONG).show()
                            }
                            .addOnFailureListener { e ->
                                Log.e(ContentValues.TAG, "Error updating user document: $e")
                                Toast.makeText(context, "Updating user details failed, please try again.", Toast.LENGTH_LONG).show()
                            }
                    } else {
                        Log.d("FirestoreData", "No such document")
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(ContentValues.TAG, "Error getting user data: $e")
                }
        } else {
            Toast.makeText(context, "User not found.", Toast.LENGTH_SHORT).show()
        }
    }

    fun accessMissions(context: Context, callback: (UserMissions?) -> Unit) {
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

                        val userMissions =  UserMissions(
                            currentLevel, completedLevels, currentMission, completedMissions
                        )
                        callback(userMissions)
                    } else {
                        Log.d("FirestoreData", "No such document")
                        callback(null)
                    }
                }
                .addOnFailureListener { e ->
                    Log.e(ContentValues.TAG, "Error getting user data.", e)
                    callback(null)
                }
        } else {
            Toast.makeText(context, "User not found.", Toast.LENGTH_SHORT).show()
            callback(null)
        }
    }

    fun accessLeaderboardOneLevel(context: Context, levelToQuery: Long, callback: (UserLeaderboard?) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            val userDocumentRef = db.collection("users").document(user.uid)

            val getCurrentLevel = CompletableFuture<Long>()
            val getLeaderboardForLevel = CompletableFuture<List<LeaderboardEntry>>()
            val getRankForLevel = CompletableFuture<LeaderboardEntry?>()

            // Query 1: Get current level
            userDocumentRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val currentLevel = document.getLong("currentLevel") ?: 0L
                        getCurrentLevel.complete(currentLevel)

                        // Query 2: Get leaderboard for that level
                        // (only gets users who completed that level, meaning their current level is higher than levelToQuery)
                        db.collection("users")
                            /*.whereGreaterThan("currentLevel", levelToQuery)
                            .orderBy("timeCompletedForLevels.level$levelToQuery")
                            .limit(5)*/
                            .get()
                            .addOnSuccessListener { documents ->
                                val filteredUsers = documents.filter { document ->
                                    val currentLevel = document.getLong("currentLevel") ?: 0L
                                    val timeCompleted = document.getDouble("timeCompletedForLevels.level$levelToQuery") ?: 0.0
                                    currentLevel > levelToQuery && timeCompleted > 0.0
                                }.take(5)

                                val leaderboardForCurrentLevel = mutableListOf<LeaderboardEntry>()

                                var userRank = 1
                                for (document in filteredUsers) {
                                    Log.d("FirestoreData1", "User Lvl: ${document.getLong("currentLevel")}, UserId: ${document.id}")
                                    // Extract details from the document
                                    val userId = document.id
                                    val username = document.getString("userDetails.username") ?: "Unknown User"
                                    val timeCompleted = document.getDouble("timeCompletedForLevels.level$levelToQuery") ?: 0.0
                                    // Create a LeaderboardEntry and add to mutable list
                                    val leaderboardEntry = LeaderboardEntry(userRank, userId, username, timeCompleted)
                                    leaderboardForCurrentLevel.add(leaderboardEntry)
                                    // Increment the rank
                                    userRank++
                                }

                                getLeaderboardForLevel.complete(leaderboardForCurrentLevel)

                            }
                            .addOnFailureListener { e ->
                                getLeaderboardForLevel.completeExceptionally(e)
                            }

                        // Query 3: Get rank and details of user for that level (if user's level is higher than levelToQuery)
                        db.collection("users")
                            .get()
                            .addOnSuccessListener { documents ->
                                val filteredUsers = documents.filter { document ->
                                    val currentLevel = document.getLong("currentLevel") ?: 0L
                                    val timeCompleted = document.getDouble("timeCompletedForLevels.level$levelToQuery") ?: 0.0
                                    currentLevel > levelToQuery && timeCompleted > 0.0
                                }

                                var currentUserRankCurrentLevel: LeaderboardEntry? = null

                                var userRank1 = 1
                                for (document in filteredUsers) {
                                    Log.d("FirestoreData2", "User Lvl: ${document.getLong("currentLevel")}, UserId: ${document.id}")
                                    if(document.id == user.uid) {
                                        // Extract details from the document
                                        val userId = document.id
                                        val username = document.getString("userDetails.username") ?: "Unknown User"
                                        val timeCompleted = document.getDouble("timeCompletedForLevels.level$levelToQuery") ?: 0.0
                                        // Store current user's rank
                                        currentUserRankCurrentLevel = LeaderboardEntry(userRank1, userId, username, timeCompleted)
                                    }
                                    // Increment the rank
                                    userRank1++
                                }

                                getRankForLevel.complete(currentUserRankCurrentLevel)
                            }
                            .addOnFailureListener { e ->
                                getRankForLevel.completeExceptionally(e)
                            }
                    } else {

                        getCurrentLevel.completeExceptionally(Exception("No such document"))
                    }
                }
                .addOnFailureListener { e ->

                    getCurrentLevel.completeExceptionally(e)
                }

            CompletableFuture.allOf(
                getCurrentLevel,
                getLeaderboardForLevel,
                getRankForLevel,
            ).thenAccept {
                val userLeaderboard = UserLeaderboard(
                    getCurrentLevel.join(),
                    getLeaderboardForLevel.join(),
                    getRankForLevel.join()
                )
                callback(userLeaderboard)
            }
        } else {
            Toast.makeText(context, "User not found.", Toast.LENGTH_SHORT).show()
            callback(null)
        }
    }

    fun accessLeaderboardAllLevels(context: Context, callback: (UserLeaderboard?) -> Unit) {
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            val userDocumentRef = db.collection("users").document(user.uid)

            val getCurrentLevel = CompletableFuture<Long>()
            val getLeaderboardForAllLevels = CompletableFuture<List<LeaderboardEntry>>()
            val getRankForAllLevels = CompletableFuture<LeaderboardEntry?>()

            // Query 1: Get current level
            userDocumentRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val currentLevel = document.getLong("currentLevel") ?: 0L
                        getCurrentLevel.complete(currentLevel)

                        // Query 2: Get leaderboard for all levels
                        // (where user completed all levels, meaning current level is 3 and totalTimeCompleted > 0.0 or calculated)
                        db.collection("users")
                            .whereEqualTo("currentLevel", 3)
                            .whereGreaterThan("totalTimeCompleted", 0.0)
                            .orderBy("totalTimeCompleted")
                            .limit(5)
                            .get()
                            .addOnSuccessListener { documents ->
                                val leaderboardForAllLevels = mutableListOf<LeaderboardEntry>()

                                var userRank2 = 1
                                for (document in documents) {
                                    Log.d("FirestoreData3", "User Lvl: ${document.getLong("currentLevel")}, UserId: ${document.id}")
                                    // Extract details from the document
                                    val userId = document.id
                                    val username = document.getString("userDetails.username") ?: "Unknown User"
                                    val totalTimeCompleted = document.getDouble("totalTimeCompleted") ?: 0.0
                                    // Create a LeaderboardEntry and add to mutable list
                                    val leaderboardEntry = LeaderboardEntry(userRank2, userId, username, totalTimeCompleted)
                                    leaderboardForAllLevels.add(leaderboardEntry)
                                    // Increment the rank
                                    userRank2++
                                }

                                getLeaderboardForAllLevels.complete(leaderboardForAllLevels)
                            }
                            .addOnFailureListener { e ->
                                getLeaderboardForAllLevels.completeExceptionally(e)
                            }

                        // Query 3: Get rank and details of user for all levels (if user completed all levels)
                        db.collection("users")
                            .whereEqualTo("currentLevel", 3)
                            .whereGreaterThan("totalTimeCompleted", 0.0)
                            .orderBy("totalTimeCompleted")
                            .get()
                            .addOnSuccessListener { documents ->
                                var currentUserRankAllLevels: LeaderboardEntry? = null

                                var userRank3 = 1
                                for (document in documents) {
                                    Log.d("FirestoreData4", "User Lvl: ${document.getLong("currentLevel")}, UserId: ${document.id}")
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

                                getRankForAllLevels.complete(currentUserRankAllLevels)
                            }
                            .addOnFailureListener { e ->
                                getRankForAllLevels.completeExceptionally(e)
                            }
                    } else {
                        getCurrentLevel.completeExceptionally(Exception("No such document"))
                    }
                }
                .addOnFailureListener { e ->
                    getCurrentLevel.completeExceptionally(e)
                }

            CompletableFuture.allOf(
                getCurrentLevel,
                getLeaderboardForAllLevels,
                getRankForAllLevels
            ).thenAccept {
                val userLeaderboard = UserLeaderboard(
                    getCurrentLevel.join(),
                    getLeaderboardForAllLevels.join(),
                    getRankForAllLevels.join()
                )
                callback(userLeaderboard)
            }
        } else {
            Toast.makeText(context, "User not found.", Toast.LENGTH_SHORT).show()
            callback(null)
        }
    }

    fun calculateTotalTimeCompleted(context: Context){
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
            Toast.makeText(context, "User not found.", Toast.LENGTH_SHORT).show()
        }
    }
    // Don't delete below this
}