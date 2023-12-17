package com.example.interstellarvoyage

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentManager
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.CompletableFuture

data class UserDocument(
    val currentLevel: Long?,
    val currentMission: String?,
    val currentDuration: Double?,
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

data class RegErr(
    val usernameErr: String,
    val emailAddressErr: String,
    val passwordErr: String,
    val confirmPasswordErr: String,
    val registerErr: String
)

data class AuthenticateErr( //for login and delete account
    val emailAddressErr: String,
    val passwordErr: String,
    val authenticateErr: String
)

data class ChangeUsernameErr( //for change username
    val usernameErr: String,
    val changeUsernameErr: String
)

data class ChangePasswordErr( //for change password
    val oldPasswordErr: String,
    val newPasswordErr: String,
    val confirmNewPasswordErr: String,
    val changePasswordErr: String
)

data class ChangeEmailErr( //for change email
    val newEmailErr: String,
    val passwordErr: String,
    val changeEmailErr: String
)

object DatabaseFunctions {
    val db = FirebaseFirestore.getInstance()

    fun login(context: Context, email: String, password: String, callback: (AuthenticateErr?) -> Unit) {
        var email = email
        var password = password
        var loginErr: AuthenticateErr? = null
        var emailAddressErr = ""
        var passwordErr = ""
        var authenticateErr = ""
        var errCount = 0

        if(email == "" || email == null) {
            emailAddressErr = "This field is required"
            errCount++
        }
        if(password == "" || password == null) {
            passwordErr = "This field is required"
            errCount++
        }

        if(errCount == 0) {
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
                                    context.startActivity(Intent(context, HomepageActivity::class.java))
                                } else {
                                    Log.d("FirestoreData", "No such document")
                                    authenticateErr = "Failed to get details. Please try again."
                                    loginErr = AuthenticateErr(
                                        emailAddressErr = emailAddressErr,
                                        passwordErr = passwordErr,
                                        authenticateErr = authenticateErr
                                    )
                                    if (loginErr != null) {
                                        callback(loginErr)
                                    } else {
                                        callback(null)
                                    }
                                }
                            }
                                .addOnFailureListener { exception ->
                                    when (exception) {
                                        is FirebaseAuthInvalidUserException -> { // User does not exist or has been disabled
                                            Log.d("Invalid email or password.", "${exception?.message}")
                                            //emailAddressErr = "User does not exists"
                                            authenticateErr = "Invalid email or password."
                                            loginErr = AuthenticateErr(
                                                emailAddressErr = emailAddressErr,
                                                passwordErr = passwordErr,
                                                authenticateErr = authenticateErr
                                            )
                                            if (loginErr != null) {
                                                callback(loginErr)
                                            } else {
                                                callback(null)
                                            }
                                        }
                                        is FirebaseAuthInvalidCredentialsException -> { // Invalid password
                                            Log.d("Invalid email or password.", "${exception?.message}")
                                            //passwordErr = "Invalid password"
                                            authenticateErr = "Invalid email or password."
                                            loginErr = AuthenticateErr(
                                                emailAddressErr = emailAddressErr,
                                                passwordErr = passwordErr,
                                                authenticateErr = authenticateErr
                                            )
                                            if (loginErr != null) {
                                                callback(loginErr)
                                            } else {
                                                callback(null)
                                            }
                                        }
                                        else -> { // General error
                                            Log.d("login failed", "${exception?.message}")
                                            authenticateErr = "Login failed. Please try again."
                                            loginErr = AuthenticateErr(
                                                emailAddressErr = emailAddressErr,
                                                passwordErr = passwordErr,
                                                authenticateErr = authenticateErr
                                            )
                                            if (loginErr != null) {
                                                callback(loginErr)
                                            } else {
                                                callback(null)
                                            }
                                        }
                                    }
                                }
                        } else {
                            authenticateErr = "User not found. Please register."
                            loginErr = AuthenticateErr(
                                emailAddressErr = emailAddressErr,
                                passwordErr = passwordErr,
                                authenticateErr = authenticateErr
                            )
                            if (loginErr != null) {
                                callback(loginErr)
                            } else {
                                callback(null)
                            }
                        }
                    } else {
                        authenticateErr = "Invalid email or password."
                        loginErr = AuthenticateErr(
                            emailAddressErr = emailAddressErr,
                            passwordErr = passwordErr,
                            authenticateErr = authenticateErr
                        )
                        if (loginErr != null) {
                            callback(loginErr)
                        } else {
                            callback(null)
                        }
                    }
                }
        }
        loginErr = AuthenticateErr(
            emailAddressErr = emailAddressErr,
            passwordErr = passwordErr,
            authenticateErr = authenticateErr
        )
        if (loginErr != null) {
            callback(loginErr)
        } else {
            callback(null)
        }
    }

    fun regAccount(context: Context, username: String, email: String, password: String, confirmPassword: String, callback: (RegErr?) -> Unit) {
        var username = username
        var email = email
        var password = password
        var confirmPassword = confirmPassword
        var regErr: RegErr? = null
        var usernameErr = ""
        var emailAddressErr = ""
        var passwordErr = ""
        var confirmPasswordErr = ""
        var registerErr = ""
        var errCount = 0

        if(username == "" || username == null) {
            usernameErr = "This field is required"
            errCount++
        }
        val emailRegex = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})"
        if(email == "" || email == null) {
            emailAddressErr = "This field is required"
            errCount++
        } else if (!email.matches(emailRegex.toRegex())) {
            emailAddressErr = "Email address is not valid."
            errCount++
        }
        if(password == "" || password == null) {
            passwordErr = "This field is required"
            errCount++
        } else if(password.length < 8) {
            passwordErr = "Password must have a minimum length of 8 characters."
            errCount++
        }
        if(confirmPassword == "" || confirmPassword == null) {
            confirmPasswordErr = "This field is required"
            errCount++
        } else if(confirmPassword != password) {
            Log.d("Debug", "inside")
            confirmPasswordErr = "Password does not match"
            errCount++
        }

        if(errCount == 0) {
            try{
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
                                                    "currentMission" to "0.0",
                                                    "currentDuration" to 0L,
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
                                                        //Toast.makeText(context, "Account created, please log in.", Toast.LENGTH_LONG).show()
                                                        context.startActivity(Intent(context, LoginActivity::class.java))
                                                    }
                                                    .addOnFailureListener { e ->
                                                        Log.e(ContentValues.TAG, "Error adding user account.", e)
                                                        registerErr = e.message ?: "Registration failed. Please try again."
                                                        regErr = RegErr(
                                                            usernameErr = usernameErr,
                                                            emailAddressErr = emailAddressErr,
                                                            passwordErr = passwordErr,
                                                            confirmPasswordErr = confirmPasswordErr,
                                                            registerErr = registerErr
                                                        )
                                                        if (regErr != null) {
                                                            callback(regErr)
                                                        } else {
                                                            callback(null)
                                                        }
                                                    }
                                            }
                                        }
                                    }
                                } else {
                                    val exception = task.exception
                                    registerErr = exception?.message ?: "Registration failed. Please try again."
                                    Log.e("account reg", exception.toString())
                                    regErr = RegErr(
                                        usernameErr = usernameErr,
                                        emailAddressErr = emailAddressErr,
                                        passwordErr = passwordErr,
                                        confirmPasswordErr = confirmPasswordErr,
                                        registerErr = registerErr
                                    )
                                    if (regErr != null) {
                                        callback(regErr)
                                    } else {
                                        callback(null)
                                    }
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            try {
                                throw e
                            } catch (e: FirebaseAuthUserCollisionException) {
                                emailAddressErr = "Email Address already exists"
                            } catch (e: Exception) {
                                Log.e("account reg", "Exception occurred: ${e.message}")
                                registerErr = "Registration failed. Please try again."
                            }
                            regErr = RegErr(
                                usernameErr = usernameErr,
                                emailAddressErr = emailAddressErr,
                                passwordErr = passwordErr,
                                confirmPasswordErr = confirmPasswordErr,
                                registerErr = registerErr
                            )
                            if (regErr != null) {
                                callback(regErr)
                            } else {
                                callback(null)
                            }
                        }
                } else {
                    usernameErr = "Username is not unique"
                    regErr = RegErr(
                        usernameErr = usernameErr,
                        emailAddressErr = emailAddressErr,
                        passwordErr = passwordErr,
                        confirmPasswordErr = confirmPasswordErr,
                        registerErr = registerErr
                    )
                    if (regErr != null) {
                        callback(regErr)
                    } else {
                        callback(null)
                    }
                }
            }
            } catch (e: Exception) {
                Log.e("account reg", "Exception occurred: ${e.message}")
                registerErr = "Registration failed. Please try again."

                when (e) {
                    is FirebaseAuthInvalidCredentialsException -> {
                        registerErr = "Invalid fields. Please recheck."
                    }
                    is FirebaseAuthUserCollisionException -> {
                        emailAddressErr = "Email Address already exists"
                    }
                    is FirebaseNetworkException -> {
                        registerErr = "Please check your network connection."
                    }
                    is FirebaseTooManyRequestsException -> {
                        registerErr = "Too many requests. please try again later."
                    }
                }
            }
        }
        regErr = RegErr(
            usernameErr = usernameErr,
            emailAddressErr = emailAddressErr,
            passwordErr = passwordErr,
            confirmPasswordErr = confirmPasswordErr,
            registerErr = registerErr
        )
        if (regErr != null) {
            callback(regErr)
        } else {
            callback(null)
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
                        val currentDuration = document.getDouble("currentDuration")
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
                            currentLevel, currentMission, currentDuration, numberOfClicks, totalTimeCompleted,
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

    fun changeEmailAdd(context: Context, email: String, password: String, callback: (ChangeEmailErr?) -> Unit) {
        var email = email
        var password = password
        var changeEmailErr: ChangeEmailErr? = null
        var emailErr = ""
        var passwordErr = ""
        var changeEmailGeneralErr = ""
        var errCount = 0

        val emailRegex = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})"
        if(email == "" || email == null) {
            emailErr = "This field is required"
            errCount++
        } else if (!email.matches(emailRegex.toRegex())) {
            emailErr = "Email address is not valid."
            errCount++
        }
        if(password == "" || password == null) {
            passwordErr = "This field is required"
            errCount++
        }

        if(errCount == 0) {
            val user = FirebaseAuth.getInstance().currentUser
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
                                        Toast.makeText(context, "Verification email sent to ${user.email} and $email", Toast.LENGTH_LONG).show()

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
                                                changeEmailGeneralErr = "Error updating email. Please try again."
                                                changeEmailErr = ChangeEmailErr(
                                                    newEmailErr = emailErr,
                                                    passwordErr = passwordErr,
                                                    changeEmailErr = changeEmailGeneralErr
                                                )
                                                if (changeEmailErr != null) {
                                                    callback(changeEmailErr)
                                                } else {
                                                    callback(null)
                                                }
                                            }
                                        }

                                        user.verifyBeforeUpdateEmail(email)
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    if(user.isEmailVerified) {
                                                        Toast.makeText(context, "Email updated successfully.", Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                            }
                                    } else {
                                        Toast.makeText(context, "Error sending verification email: ${emailVerificationTask.exception?.message}", Toast.LENGTH_LONG).show()
                                        Log.e("error sending verification email", "${emailVerificationTask.exception?.message}")
                                        changeEmailGeneralErr = "Error sending verification email. Please try again."
                                        changeEmailErr = ChangeEmailErr(
                                            newEmailErr = emailErr,
                                            passwordErr = passwordErr,
                                            changeEmailErr = changeEmailGeneralErr
                                        )
                                        if (changeEmailErr != null) {
                                            callback(changeEmailErr)
                                        } else {
                                            callback(null)
                                        }
                                    }
                                }
                        } else {
                            Log.d("Debug", "Incorrect password: ${reauthTask.exception?.message}")
                            passwordErr = "Incorrect password"
                            changeEmailErr = ChangeEmailErr(
                                newEmailErr = emailErr,
                                passwordErr = passwordErr,
                                changeEmailErr = changeEmailGeneralErr
                            )
                            if (changeEmailErr != null) {
                                callback(changeEmailErr)
                            } else {
                                callback(null)
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d("Reauthentication failed", "${exception.message}")
                        changeEmailGeneralErr = "Reauthentication failed."
                        changeEmailErr = ChangeEmailErr(
                            newEmailErr = emailErr,
                            passwordErr = passwordErr,
                            changeEmailErr = changeEmailGeneralErr
                        )
                        if (changeEmailErr != null) {
                            callback(changeEmailErr)
                        } else {
                            callback(null)
                        }
                    }
            } else {
                Log.d("Debug", "User not found")
                changeEmailGeneralErr = "User not found. Please log in again."
                changeEmailErr = ChangeEmailErr(
                    newEmailErr = emailErr,
                    passwordErr = passwordErr,
                    changeEmailErr = changeEmailGeneralErr
                )
                if (changeEmailErr != null) {
                    callback(changeEmailErr)
                } else {
                    callback(null)
                }
            }
        }

        changeEmailErr = ChangeEmailErr(
            newEmailErr = emailErr,
            passwordErr = passwordErr,
            changeEmailErr = changeEmailGeneralErr
        )
        if (changeEmailErr != null) {
            callback(changeEmailErr)
        } else {
            callback(null)
        }
    }

    fun changePassword(context: Context, oldpassword: String, newpassword: String, confirmnewpassword: String, callback: (ChangePasswordErr?) -> Unit) {
        var oldpassword = oldpassword
        var newpassword = newpassword
        var confirmnewpassword = confirmnewpassword
        var changePasswordErr: ChangePasswordErr? = null
        var oldpasswordErr = ""
        var newpasswordErr = ""
        var confirmnewpasswordErr = ""
        var changePasswordGeneralErr = ""
        var errCount = 0
        if(oldpassword == "" || oldpassword == null) {
            oldpasswordErr = "This field is required"
            errCount++
        }
        if(newpassword == "" || newpassword == null) {
            newpasswordErr = "This field is required"
            errCount++
        } else if(newpassword.length < 8) {
            newpasswordErr = "Password must have a minimum length of 8 characters."
            errCount++
        }
        if(confirmnewpassword == "" || confirmnewpassword == null) {
            confirmnewpasswordErr = "This field is required"
            errCount++
        } else if(confirmnewpassword != newpassword) {
            Log.d("Debug", "inside")
            confirmnewpasswordErr = "New password and confirm new password does not match"
            errCount++
        }

        if(errCount == 0) {
            val user = FirebaseAuth.getInstance().currentUser

            if (user != null) {
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
                                        Log.d("error updating password: ", "${task.exception?.message}")
                                        changePasswordGeneralErr = "Error updating password. Please try again."
                                        changePasswordErr = ChangePasswordErr(
                                            oldPasswordErr = oldpasswordErr,
                                            newPasswordErr = newpasswordErr,
                                            confirmNewPasswordErr = confirmnewpasswordErr,
                                            changePasswordErr = changePasswordGeneralErr
                                        )
                                        if (changePasswordErr != null) {
                                            callback(changePasswordErr)
                                        } else {
                                            callback(null)
                                        }
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    Log.d("Error updating password: ","${exception.message}")
                                    changePasswordGeneralErr = "Error updating password. Please try again."
                                    changePasswordErr = ChangePasswordErr(
                                        oldPasswordErr = oldpasswordErr,
                                        newPasswordErr = newpasswordErr,
                                        confirmNewPasswordErr = confirmnewpasswordErr,
                                        changePasswordErr = changePasswordGeneralErr
                                    )
                                    if (changePasswordErr != null) {
                                        callback(changePasswordErr)
                                    } else {
                                        callback(null)
                                    }
                                }
                        } else {
                            Log.d("Incorrect Pasword: ", "${reauthTask.exception?.message}")
                            oldpasswordErr = "Incorrect password"
                            changePasswordErr = ChangePasswordErr(
                                oldPasswordErr = oldpasswordErr,
                                newPasswordErr = newpasswordErr,
                                confirmNewPasswordErr = confirmnewpasswordErr,
                                changePasswordErr = changePasswordGeneralErr
                            )
                            if (changePasswordErr != null) {
                                callback(changePasswordErr)
                            } else {
                                callback(null)
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d("Reauthentication failed", "${exception.message}")
                        changePasswordGeneralErr = "Reauthentication failed."
                        changePasswordErr = ChangePasswordErr(
                            oldPasswordErr = oldpasswordErr,
                            newPasswordErr = newpasswordErr,
                            confirmNewPasswordErr = confirmnewpasswordErr,
                            changePasswordErr = changePasswordGeneralErr
                        )
                        if (changePasswordErr != null) {
                            callback(changePasswordErr)
                        } else {
                            callback(null)
                        }
                    }
            } else {
                changePasswordGeneralErr = "User not found. Please log in again."
                changePasswordErr = ChangePasswordErr(
                    oldPasswordErr = oldpasswordErr,
                    newPasswordErr = newpasswordErr,
                    confirmNewPasswordErr = confirmnewpasswordErr,
                    changePasswordErr = changePasswordGeneralErr
                )
                if (changePasswordErr != null) {
                    callback(changePasswordErr)
                } else {
                    callback(null)
                }
            }
        }

        changePasswordErr = ChangePasswordErr(
            oldPasswordErr = oldpasswordErr,
            newPasswordErr = newpasswordErr,
            confirmNewPasswordErr = confirmnewpasswordErr,
            changePasswordErr = changePasswordGeneralErr
        )
        if (changePasswordErr != null) {
            callback(changePasswordErr)
        } else {
            callback(null)
        }
    }

    fun forgotPassword(context: Context, email: String, emailSentContainer: LinearLayout, typeEmailContainer: LinearLayout, txtEmail: TextView, emailErr: TextView){
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, "dummyPassword")
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Email is not in use, delete the dummy user
                    val user = FirebaseAuth.getInstance().currentUser
                    user?.delete()
                    emailErr.text = "User with this email does not exist"
                    emailErr.visibility = View.VISIBLE
                } else {
                    // Email is in use or there was an error
                    val exception = task.exception
                    if (exception is FirebaseAuthUserCollisionException) {
                        // Email is in use
                        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    //Toast.makeText(context, "Email verification sent.", Toast.LENGTH_LONG).show()
                                    txtEmail.text = email
                                    typeEmailContainer.visibility = View.GONE
                                    emailSentContainer.visibility = View.VISIBLE
                                } else {
                                    Toast.makeText(context, "Failed to send verification email.", Toast.LENGTH_LONG).show()
                                }
                            }
                    }
                }
            }
    }

    fun changeUsername(context: Context, newUsername: String, callback: (ChangeUsernameErr?) -> Unit) {
        var newUsername = newUsername
        var changeUsernameErr: ChangeUsernameErr? = null
        var usernameErr = ""
        var changeUsernameGeneralErr = ""
        var errCount = 0
        if(newUsername == "" || newUsername == null) {
            usernameErr = "This field is required"
            errCount++
        }

        if(errCount == 0) {
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
                                changeUsernameGeneralErr = "Error updating username. Please try again."
                                changeUsernameErr = ChangeUsernameErr(
                                    usernameErr = usernameErr,
                                    changeUsernameErr = changeUsernameGeneralErr
                                )
                                if (changeUsernameErr != null) {
                                    callback(changeUsernameErr)
                                } else {
                                    callback(null)
                                }
                            }
                    } else {
                        usernameErr = "Username is not unique"
                        changeUsernameErr = ChangeUsernameErr(
                            usernameErr = usernameErr,
                            changeUsernameErr = changeUsernameGeneralErr
                        )
                        if (changeUsernameErr != null) {
                            callback(changeUsernameErr)
                        } else {
                            callback(null)
                        }
                    }
                }
            } else {
                changeUsernameGeneralErr = "User not found. Please log in again."
                changeUsernameErr = ChangeUsernameErr(
                    usernameErr = usernameErr,
                    changeUsernameErr = changeUsernameGeneralErr
                )
                if (changeUsernameErr != null) {
                    callback(changeUsernameErr)
                } else {
                    callback(null)
                }
            }
        }
        changeUsernameErr = ChangeUsernameErr(
            usernameErr = usernameErr,
            changeUsernameErr = changeUsernameGeneralErr
        )
        if (changeUsernameErr != null) {
            callback(changeUsernameErr)
        } else {
            callback(null)
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

    fun authenticateBeforeDelete(context: Context, email: String, password: String, supportFragmentManager: FragmentManager, fragmentTag: String, callback: (AuthenticateErr?) -> Unit) {
        var email = email
        var password = password
        var authErr: AuthenticateErr? = null
        var emailAddressErr = ""
        var passwordErr = ""
        var authenticateErr = ""
        var errCount = 0


        if(email == "" || email == null) {
            emailAddressErr = "This field is required"
            errCount++
        }

        val user = FirebaseAuth.getInstance().currentUser
        if ((email != null  && email.isNotEmpty()) && user != null) {
            val userEmail = user.email
            if (userEmail != null) {
                if (!email.equals(userEmail, ignoreCase = true)) {
                    authenticateErr = "Invalid email or password"
                    errCount++
                }
            }
        }

        if(password == "" || password == null) {
            passwordErr = "This field is required"
            errCount++
        }

        if(errCount == 0) {
            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { signInTask ->
                    if (signInTask.isSuccessful) {
                        if (user != null) {
                            var dialogFragment = DeleteConfirmActivity()
                            dialogFragment.setCancelable(false)
                            dialogFragment.show(supportFragmentManager, fragmentTag)
                        } else {
                            authenticateErr = "User not found. Please register."
                            authErr = AuthenticateErr(
                                emailAddressErr = emailAddressErr,
                                passwordErr = passwordErr,
                                authenticateErr = authenticateErr
                            )
                            if (authErr != null) {
                                callback(authErr)
                            } else {
                                callback(null)
                            }
                        }
                    } else {
                        authenticateErr = "Invalid email or password."
                        authErr = AuthenticateErr(
                            emailAddressErr = emailAddressErr,
                            passwordErr = passwordErr,
                            authenticateErr = authenticateErr
                        )
                        if (authErr != null) {
                            callback(authErr)
                        } else {
                            callback(null)
                        }
                    }
                }
        }
        authErr = AuthenticateErr(
            emailAddressErr = emailAddressErr,
            passwordErr = passwordErr,
            authenticateErr = authenticateErr
        )
        if (authErr != null) {
            callback(authErr)
        } else {
            callback(null)
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
                                    Toast.makeText(context, "User deleted.", Toast.LENGTH_SHORT).show()
                                    context.startActivity(Intent(context, LoginActivity::class.java))
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Error", "Error deleting user: ${e.message}")
                                    Toast.makeText(context, "Error deleting user.", Toast.LENGTH_LONG).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            Log.e("Error", "Error deleting user document: ${e.message}")
                            Toast.makeText(context, "Error deleting user.", Toast.LENGTH_LONG).show()
                        }
                } else {
                    Log.e("Error", "User document does not exist.")
                    Toast.makeText(context, "Error deleting user.", Toast.LENGTH_LONG).show()
                }
            }
        } else {
            Log.e("Error", "User not found.")
            Toast.makeText(context, "User not found. Please log in again.", Toast.LENGTH_LONG).show()
        }
    }

    fun levelCompleted (context: Context, currentLevel: Long, currentMission: String, currentDuration: Long, numberOfClicks: Long, timeCompletedForLevel: Long) {
        val user = FirebaseAuth.getInstance().currentUser
        Log.d("lvlCompleted1", "curreLvl: "+currentLevel.toString()+"; currMiss: "+currentMission+"; currDur: "+currentDuration+"; numOfClicks: "+numberOfClicks+"; timeCompleted: "+timeCompletedForLevel)
        var oldLevel: Int = currentLevel.toInt() - 1
        if (user != null) {
            val userDocumentRef = db.collection("users").document(user.uid)
            userDocumentRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        //new details of user
                        val updatedData  = hashMapOf(
                            "currentLevel" to currentLevel,
                            "currentMission" to currentMission,
                            "currentDuration" to currentDuration,
                            "numberOfClicks" to numberOfClicks,
                            "timeCompletedForLevels.level$oldLevel" to timeCompletedForLevel
                        )

                        val updatedDataMap: Map<String, Any> = updatedData
                        Log.d("lvlCompleted1", updatedData.toString())

                        //update in database
                        userDocumentRef.update(updatedDataMap)
                            .addOnSuccessListener {
                                //Toast.makeText(context, "User updated successfully.", Toast.LENGTH_LONG).show()
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

    fun subMissionCompleted(context: Context, currentMission: String, currentDuration: Long, numberOfClicks: Long) {
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            val userDocumentRef = db.collection("users").document(user.uid)
            userDocumentRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        //new details of user
                        val updatedData  = hashMapOf(
                            "currentMission" to currentMission,
                            "currentDuration" to currentDuration,
                            "numberOfClicks" to numberOfClicks
                        )

                        val updatedDataMap: Map<String, Any> = updatedData

                        //update in database
                        userDocumentRef.update(updatedDataMap)
                            .addOnSuccessListener {
                                //Toast.makeText(context, "User updated successfully.", Toast.LENGTH_LONG).show()
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
                                }

                                val sortedUsers = filteredUsers.sortedBy { it.getDouble("timeCompletedForLevels.level$levelToQuery") }
                                val leaderboardForCurrentLevel = mutableListOf<LeaderboardEntry>()

                                var userRank = 1
                                for (document in sortedUsers.take(5)) {
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

                                val sortedUsers2 = filteredUsers.sortedBy { it.getDouble("timeCompletedForLevels.level$levelToQuery") }
                                var currentUserRankCurrentLevel: LeaderboardEntry? = null

                                var userRank1 = 1
                                for (document in sortedUsers2) {
                                    Log.d("FirestoreData2", "User Lvl: ${document.getLong("currentLevel")}, UserId: ${document.id}")
                                    if(document.id == user.uid) {
                                        // Extract details from the document
                                        val userId = document.id
                                        val username = document.getString("userDetails.username") ?: "Unknown User"
                                        val timeCompleted = document.getDouble("timeCompletedForLevels.level$levelToQuery") ?: 0.0
                                        // Store current user's rank
                                        currentUserRankCurrentLevel = LeaderboardEntry(userRank1, userId, username, timeCompleted)
                                        break
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
                            .whereEqualTo("currentLevel", 4)
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
                            .whereEqualTo("currentLevel", 4)
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
