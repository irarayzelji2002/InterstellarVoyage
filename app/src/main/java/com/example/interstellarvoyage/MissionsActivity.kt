package com.example.interstellarvoyage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button

class MissionsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_missions)

        val btnBack = findViewById<Button>(R.id.btnBack)

        btnBack.setOnClickListener {
            startActivity(Intent(this, HomepageActivity::class.java))
        }
    }
}