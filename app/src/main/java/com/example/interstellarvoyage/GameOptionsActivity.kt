package com.example.interstellarvoyage

import android.app.Dialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment

class GameOptionsActivity : DialogFragment() {
    private var musicPlayerCallback: MusicPlayerCallback? = null

    fun setMusicPlayerCallback(callback: MusicPlayerCallback) {
        musicPlayerCallback = callback
    }

    interface GameOptionsListener {
        fun onContinueToGameClicked()
        fun onBackToHomeClicked()
    }

    private var gameOptionsListener: GameOptionsListener? = null

    fun setGameOptionsListener(listener: GameOptionsListener) {
        gameOptionsListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.activity_game_options, container, false)

        var constraintLayoutShadow : ConstraintLayout = rootView.findViewById(R.id.constraintLayoutShadow)
        var fade_in : Animation = AnimationUtils.loadAnimation(rootView.context,R.anim.fade_in)
        var fade_out : Animation = AnimationUtils.loadAnimation(rootView.context,R.anim.fade_out)
        constraintLayoutShadow.setAnimation(fade_in)

        var btnBackToHome : Button = rootView.findViewById(R.id.btnBackToHome)
        var btnContinueToGame : Button = rootView.findViewById(R.id.btnContinueToGame)
        var btnMusic : ImageButton = rootView.findViewById(R.id.btnMusic)

        val userPref = rootView.getContext().getSharedPreferences("UserPrefs", AppCompatActivity.MODE_PRIVATE)
        var isMusicEnabled = userPref.getBoolean("isMusicEnabled", true)
        Log.d("isMusicEnabled", isMusicEnabled.toString())
        if(isMusicEnabled) {
            btnMusic.setImageResource(R.drawable.ic_music)
            musicPlayerCallback?.playMusic()
        } else {
            btnMusic.setImageResource(R.drawable.ic_no_music)
            if (musicPlayerCallback?.isPlaying() == true) {
                musicPlayerCallback?.pauseMusic()
            }
        }

        btnBackToHome.setOnClickListener {
            constraintLayoutShadow.setAnimation(fade_out)
            dismiss()
            gameOptionsListener?.onBackToHomeClicked()
            startActivity(Intent(rootView.getContext(), HomepageActivity::class.java))
        }
        btnContinueToGame.setOnClickListener {
            constraintLayoutShadow.setAnimation(fade_out)
            dismiss()
            gameOptionsListener?.onContinueToGameClicked()
        }

        btnMusic.setOnClickListener {
            val userPref = rootView.getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE)
            val editor = userPref.edit()
            if (isMusicEnabled) {
                btnMusic.setImageResource(R.drawable.ic_no_music)
                Log.d("isMusicEnableinside1.1", isMusicEnabled.toString())
                musicPlayerCallback?.pauseMusic()
                editor.putBoolean("isMusicEnabled", false)
                editor.apply()
                Log.d("isMusicEnableinside1.2", isMusicEnabled.toString())
            } else {
                btnMusic.setImageResource(R.drawable.ic_music)
                Log.d("isMusicEnableinside2.1", isMusicEnabled.toString())
                musicPlayerCallback?.playMusic()
                editor.putBoolean("isMusicEnabled", true)
                Log.d("isMusicEnableinside2.2", isMusicEnabled.toString())
                editor.apply()
            }
            isMusicEnabled = !isMusicEnabled //change sate after click
        }

        return rootView
    }

    override fun getTheme(): Int {
        return R.style.NoBackgroundDialogTheme
    }
}