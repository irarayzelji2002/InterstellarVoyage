package com.example.interstellarvoyage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class EditUserInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_user_info)

        val btnBack = findViewById<Button>(R.id.btnBack)

        btnBack.setOnClickListener {
            startActivity(Intent(this, OptionsActivity::class.java))
        }
    }
}