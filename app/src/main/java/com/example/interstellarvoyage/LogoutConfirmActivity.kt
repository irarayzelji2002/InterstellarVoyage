package com.example.interstellarvoyage

import android.content.Context
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

class LogoutConfirmActivity : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.activity_logout_confirm, container, false)

        var constraintLayoutShadow : ConstraintLayout = rootView.findViewById(R.id.constraintLayoutShadow)
        var fade_in : Animation = AnimationUtils.loadAnimation(rootView.context,R.anim.fade_in)
        var fade_out : Animation = AnimationUtils.loadAnimation(rootView.context,R.anim.fade_out)
        constraintLayoutShadow.setAnimation(fade_in)

        var btnYes : Button = rootView.findViewById(R.id.btnYes)
        var btnNo : Button = rootView.findViewById(R.id.btnNo)

        btnYes.setOnClickListener {
            constraintLayoutShadow.setAnimation(fade_out)
            dismiss()
            DatabaseFunctions.logout(rootView.getContext())
        }
        btnNo.setOnClickListener {
            constraintLayoutShadow.setAnimation(fade_out)
            dismiss()
        }

        return rootView
    }

    override fun getTheme(): Int {
        return R.style.NoBackgroundDialogTheme
    }
}