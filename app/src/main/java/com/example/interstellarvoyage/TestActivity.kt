package com.example.interstellarvoyage

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.mindrot.jbcrypt.BCrypt

class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        var btnRegAccount : Button = findViewById(R.id.btnRegAccount)
        var btnAccess : Button = findViewById(R.id.btnAccess)
        var btnEditAccount : Button = findViewById(R.id.btnEditAccount)
        var btnDeleteAccount : Button = findViewById(R.id.btnDeleteAccount)
        var btnLevelCompleted : Button = findViewById(R.id.btnLevelCompleted)
        var btnSubMissionCompleted : Button = findViewById(R.id.btnSubMissionCompleted)
        var btnEditUserInfo : Button = findViewById(R.id.btnEditUserInfo)
        var btnForgotPassword : Button = findViewById(R.id.btnForgotPassword)
        var btnAccessMissions : Button = findViewById(R.id.btnAccessMissions)
        var btnAccessLeaderboard : Button = findViewById(R.id.btnAccessLeaderboard)

        FirebaseApp.initializeApp(this)
        val db = FirebaseFirestore.getInstance()

        btnRegAccount.setOnClickListener {
            var username = "exampleUser"
            var email = "user@example.com"
            var password = "irarayzelji"
            var hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())
            val result = BCrypt.checkpw(password, hashedPassword)
            Log.i("reg password", result.toString())

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
                                            "userDetails" to hashMapOf(
                                                "username" to username,
                                                "email" to email,
                                                "password" to hashedPassword
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
                                                Toast.makeText(
                                                    this,
                                                    "Account created, please log in.",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                startActivity(
                                                    Intent(this, LoginActivity::class.java)
                                                )
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
            Log.i("test","outside if")
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

                            // Access nested fields
                            val userDetails = document.get("userDetails") as Map<String, Any>?
                            val username = userDetails?.get("username") as String?
                            val email = userDetails?.get("email") as String?
                            val password = userDetails?.get("password") as String?

                            // Access timestamps for levels
                            val timeCompletedForLevels = document.get("timeCompletedForLevels") as Map<String, Any>?
                            val level1Timestamp = timeCompletedForLevels?.get("level1") as Double?

                            Log.d("FirestoreData", "Current Level: $currentLevel")
                            Log.d("FirestoreData", "Current Mission: $currentMission")
                            Log.d("FirestoreData", "Number of Clicks: $numberOfClicks")

                            Log.d("FirestoreData", "User Details: $userDetails")
                            Log.d("FirestoreData", "Username: $username")
                            Log.d("FirestoreData", "Email: $email")
                            Log.d("FirestoreData", "Password: $password")

                            Log.d("FirestoreData", "Levels Timestamp: $timeCompletedForLevels")
                            Log.d("FirestoreData", "Level 1 Timestamp: $level1Timestamp")
                        } else {
                            Log.d("FirestoreData", "No such document")
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e(ContentValues.TAG, "Error getting user data.", e)
                    }

                var email = "user@example.com"
                var password = "irarayzelji"
                var hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())
                val result = BCrypt.checkpw(password, hashedPassword)
                Log.i("login password", result.toString())

            } else {
                Toast.makeText(this, "User not found.", Toast.LENGTH_LONG).show()
            }
        }

        btnEditAccount.setOnClickListener {
            var username = "exampleUserNewName"
            var email = "usernewemail@example.com"
            var password = "irarayzelji2002"
            var hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())
            val result = BCrypt.checkpw(password, hashedPassword)
            Log.i("edit account password", result.toString())

            val user = FirebaseAuth.getInstance().currentUser
            Log.i("test","outside if")
            if (user != null) {
                val userDocumentRef = db.collection("users").document(user.uid)
                userDocumentRef.get()
                    .addOnSuccessListener { document ->
                        if (document.exists()) {
                            //new details of user
                            val updatedData  = hashMapOf(
                                "currentLevel" to 1,
                                "currentMission" to "0.2",
                                "numberOfClicks" to 100L,
                                "userDetails" to hashMapOf(
                                    "username" to username,
                                    "email" to email,
                                    "password" to hashedPassword
                                ),
                                "timeCompletedForLevels" to hashMapOf(
                                    "level0" to 5000.70,
                                    "level1" to 3000.50,
                                    "level2" to 4000.33,
                                    "level3" to 10000.99
                                )
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
                Toast.makeText(this, "User not found.", Toast.LENGTH_LONG).show()
            }
        }

        btnLevelCompleted.setOnClickListener {

        }

        btnSubMissionCompleted.setOnClickListener {

        }

        btnEditUserInfo.setOnClickListener {

        }

        btnForgotPassword.setOnClickListener {

        }

        btnAccessMissions.setOnClickListener {

        }

        btnAccessLeaderboard.setOnClickListener {

        }

    //Don't Delete semi-colons below this
    }
}