package com.example.interstellarvoyage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class DeleteAccountActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_delete_account)

        val btnBack = findViewById<Button>(R.id.btnBack)
        val btnDelete = findViewById<Button>(R.id.btnDelete)

        val editTextEmailAddress = findViewById<EditText>(R.id.editTextEmailAddress)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)

        val emailAddressErr = findViewById<TextView>(R.id.emailAddressErr)
        val passwordErr = findViewById<TextView>(R.id.passwordErr)
        val deleteAccountErr = findViewById<TextView>(R.id.deleteAccountErr)

        btnBack.setOnClickListener {
            startActivity(Intent(this, OptionsActivity::class.java))
        }

        btnDelete.setOnClickListener {
            val email = editTextEmailAddress.text.toString()
            val password = editTextPassword.text.toString()

            DatabaseFunctions.authenticateBeforeDelete(this, email, password, supportFragmentManager, "Delete Confirm Dialog") { errors ->
                if(errors != null) {
                    Log.d("Error", "Email Address: ${errors.emailAddressErr}")
                    Log.d("Error", "Password: ${errors.passwordErr}")
                    Log.d("Error", "Authenticate Error: ${errors.authenticateErr}")

                    if(errors.emailAddressErr != "" || errors.passwordErr !="") {
                        setErrorTextAndVisibility(emailAddressErr, errors.emailAddressErr)
                        setErrorTextAndVisibility(passwordErr, errors.passwordErr)
                    } else {
                        setErrorTextAndVisibility(deleteAccountErr, errors.authenticateErr)
                    }
                }
            }
        }
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