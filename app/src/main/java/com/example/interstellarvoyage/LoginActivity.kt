package com.example.interstellarvoyage

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        var btnTestDatabaseFunctions : Button = findViewById(R.id.btnTestDatabaseFunctions)
        var btnTestGameFunctions : Button = findViewById(R.id.btnTestGameFunctions)

        var btnLogin : Button = findViewById(R.id.btnLogin)
        var btnRegAccount : TextView = findViewById(R.id.btnRegAccount)
        var btnForgotPassword : TextView = findViewById(R.id.btnForgotPassword)

        val editTextEmailAddress = findViewById<EditText>(R.id.editTextEmailAddress)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)

        val emailAddressErr = findViewById<TextView>(R.id.emailAddressErr)
        val passwordErr = findViewById<TextView>(R.id.passwordErr)
        val loginErr = findViewById<TextView>(R.id.loginErr)

        FirebaseApp.initializeApp(this)
        val db = FirebaseFirestore.getInstance()

        btnLogin.setOnClickListener {
            val email = editTextEmailAddress.text.toString()
            val password = editTextPassword.text.toString()
            DatabaseFunctions.login(this, email, password) { errors ->
                if(errors != null) {
                    Log.d("Error", "Email Address: ${errors.emailAddressErr}")
                    Log.d("Error", "Password: ${errors.passwordErr}")
                    Log.d("Error", "Login Error: ${errors.authenticateErr}")

                    if(errors.emailAddressErr != "" || errors.passwordErr !="") {
                        setErrorTextAndVisibility(emailAddressErr, errors.emailAddressErr)
                        setErrorTextAndVisibility(passwordErr, errors.passwordErr)
                    } else {
                        setErrorTextAndVisibility(loginErr, errors.authenticateErr)
                    }
                }
            }
        }

        btnRegAccount.setOnClickListener{
            startActivity(Intent(this, RegistrationActivity::class.java))
        }

        btnForgotPassword.setOnClickListener {
            DatabaseFunctions.forgotPassword(this)
            var dialogFragment = ForgotPasswordActivity()
            dialogFragment.setCancelable(false)
            dialogFragment.show(supportFragmentManager, "Forgot Password Dialog")
        }

        btnTestDatabaseFunctions.setOnClickListener{
            startActivity(Intent(this, TestDatabaseActivity::class.java))
        }

        btnTestGameFunctions.setOnClickListener{
            startActivity(Intent(this, TestGameActivity::class.java))
        }
        //Don't Delete below this
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