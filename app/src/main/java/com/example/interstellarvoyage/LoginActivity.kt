package com.example.interstellarvoyage

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        var btnLogin : Button = findViewById(R.id.btnLogin)
        var btnTestDatabaseFunctions : Button = findViewById(R.id.btnTestDatabaseFunctions)
        var btnTestGameFunctions : Button = findViewById(R.id.btnTestGameFunctions)

        FirebaseApp.initializeApp(this)
        val db = FirebaseFirestore.getInstance()

        btnLogin.setOnClickListener {
            DatabaseFunctions.login(this, "irarayzelji@gmail.com", "irarayzelji")
            // Trial
            /*DatabaseFunctions.login(this, "irarayzelji@gmail.com", "irarayzelji2002")
            DatabaseFunctions.login(this, "irarayzelji@gmail.com", "irarayzelji2002!")
            DatabaseFunctions.login(this, "irarayzel.ji.cics@ust.edu.ph", "irarayzelji")
            DatabaseFunctions.login(this, "irarayzel.ji.cics@ust.edu.ph", "irarayzelji2002")
            DatabaseFunctions.login(this, "irarayzel.ji.cics@ust.edu.ph", "irarayzelji2002!")*/
        }

        btnTestDatabaseFunctions.setOnClickListener{
            startActivity(Intent(this, TestDatabaseActivity::class.java))
        }

        btnTestGameFunctions.setOnClickListener{
            startActivity(Intent(this, TestGameActivity::class.java))
        }
        //Don't Delete below this
    }
}