package com.example.interstellarvoyage

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.firestore.FirebaseFirestore
import org.mindrot.jbcrypt.BCrypt

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        var btnReg : Button = findViewById(R.id.btnReg)
        var btnAccess : Button = findViewById(R.id.btnAccess)
        var btnLogin : Button = findViewById(R.id.btnLogin)

        FirebaseApp.initializeApp(this)
        val db = FirebaseFirestore.getInstance()

        btnReg.setOnClickListener{
            startActivity(Intent(this, TestActivity::class.java))
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

                var email = "irarayzel.ji.cics@ust.edu.ph"
                var password = "irarayzelji"
                var hashedPassword = BCrypt.hashpw(password, BCrypt.gensalt())
                val result = BCrypt.checkpw(password, hashedPassword)
                Log.i("login password", result.toString())

            } else {
                Toast.makeText(this, "User not found.", Toast.LENGTH_LONG).show()
            }
        }

        btnLogin.setOnClickListener {
            var email = "irarayzel.ji.cics@ust.edu.ph"
            var password = "irarayzelji"
            /*var email = "irarayzelji@gmail.com"*/
            /*var password = "irarayzelji2002"*/
            /*var password = "irarayzelji2002!"*/

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
                                    startActivity(Intent(this, TestActivity::class.java))
                                } else {
                                    Log.d("FirestoreData", "No such document")
                                }
                            }
                            .addOnFailureListener { exception ->
                                when (exception) {
                                    is FirebaseAuthInvalidUserException -> { // User does not exist or has been disabled
                                        Toast.makeText(this, "Invalid email or password.", Toast.LENGTH_LONG).show()
                                    }
                                    is FirebaseAuthInvalidCredentialsException -> { // Invalid password
                                        Toast.makeText(this, "Invalid email or password.", Toast.LENGTH_LONG).show()
                                    }
                                    else -> { // General error
                                        Toast.makeText(this, "Login failed. ${exception?.message}", Toast.LENGTH_LONG).show()
                                        Log.d("login failed", "${exception?.message}")
                                    }
                                }
                            }
                        }
                    } else {
                        Toast.makeText(this, "Invalid fields.", Toast.LENGTH_LONG).show()
                    }
                }
        }
        //Don't Delete below this
    }
}