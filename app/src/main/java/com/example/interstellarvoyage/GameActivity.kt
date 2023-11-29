package com.example.interstellarvoyage

import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import jp.wasabeef.blurry.Blurry

class GameActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // Blur Storyline BG
        val backgroundLayout = findViewById<ViewGroup>(R.id.backgroundLayout)
        Blurry.with(this)
            .radius(10)
            .sampling(8)
            .color(Color.argb(66, 255, 255, 0))
            .async()
            .onto(backgroundLayout)
    }
}