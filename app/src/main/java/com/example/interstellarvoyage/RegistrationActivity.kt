package com.example.interstellarvoyage

import android.content.ContentValues
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
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

        val editTextUsername = findViewById<EditText>(R.id.editTextUsername)
        val editTextEmailAddress = findViewById<EditText>(R.id.editTextEmailAddress)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)

        val usernameErr = findViewById<TextView>(R.id.usernameErr)
        val emailAddressErr = findViewById<TextView>(R.id.emailAddressErr)
        val passwordErr = findViewById<TextView>(R.id.passwordErr)

        btnRegister.setOnClickListener {
            val username = editTextUsername.text.toString()
            val email = editTextEmailAddress.text.toString()
            val password = editTextPassword.text.toString()
            DatabaseFunctions.regAccount(this, username, email, password)
        }
    }
}