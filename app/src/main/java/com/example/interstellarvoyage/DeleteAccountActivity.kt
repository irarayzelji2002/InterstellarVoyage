package com.example.interstellarvoyage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
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

        val deleteAccountErr = findViewById<TextView>(R.id.deleteAccountErr)

        btnBack.setOnClickListener {
            startActivity(Intent(this, OptionsActivity::class.java))
        }

        btnDelete.setOnClickListener {
            val email = editTextEmailAddress.text.toString()
            val password = editTextPassword.text.toString()
            //validate user, if correct:
            var dialogFragment = DeleteConfirmActivity()
            dialogFragment.setCancelable(false)
            dialogFragment.show(supportFragmentManager, "Delete Confirm Dialog")
        }
    }
}