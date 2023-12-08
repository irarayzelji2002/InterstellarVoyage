package com.example.interstellarvoyage

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.mindrot.jbcrypt.BCrypt

class RegistrationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registration)

        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val btnLogin = findViewById<TextView>(R.id.btnLogin)

        val editTextUsername = findViewById<EditText>(R.id.editTextUsername)
        val editTextEmailAddress = findViewById<EditText>(R.id.editTextEmailAddress)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val editTextConfirmPassword = findViewById<EditText>(R.id.editTextConfirmPassword)

        val usernameErr = findViewById<TextView>(R.id.usernameErr)
        val emailAddressErr = findViewById<TextView>(R.id.emailAddressErr)
        val passwordErr = findViewById<TextView>(R.id.passwordErr)
        val confirmPasswordErr = findViewById<TextView>(R.id.confirmPasswordErr)
        val registerErr = findViewById<TextView>(R.id.registerErr)

        btnRegister.setOnClickListener {
            val username = editTextUsername.text.toString()
            val email = editTextEmailAddress.text.toString()
            val password = editTextPassword.text.toString()
            val confirmPassword = editTextConfirmPassword.text.toString()
            DatabaseFunctions.regAccount(this, username, email, password, confirmPassword){ errors ->
                if(errors != null) {
                    Log.d("Error", "Username: ${errors.usernameErr}")
                    Log.d("Error", "Email Address: ${errors.emailAddressErr}")
                    Log.d("Error", "Password: ${errors.passwordErr}")
                    Log.d("Error", "Confirm Password: ${errors.confirmPasswordErr}")
                    Log.d("Error", "Register Error: ${errors.registerErr}")

                    setErrorTextAndVisibility(usernameErr, errors.usernameErr)
                    setErrorTextAndVisibility(emailAddressErr, errors.emailAddressErr)
                    setErrorTextAndVisibility(passwordErr, errors.passwordErr)
                    setErrorTextAndVisibility(confirmPasswordErr, errors.confirmPasswordErr)
                    setErrorTextAndVisibility(registerErr, errors.registerErr)
                }
            }
        }

        btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        }
        //Don't delete below this
    }

    fun setErrorTextAndVisibility(view: TextView, error: String) {
        if (error.isNotEmpty() && error != null) {
            view.visibility = View.VISIBLE
            view.text = error
        } else {
            view.visibility = View.GONE
        }
    }
}