package com.example.interstellarvoyage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.DialogFragment

class ForgotPasswordActivity : DialogFragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.activity_forgot_password, container, false)

        var constraintLayoutShadow : ConstraintLayout = rootView.findViewById(R.id.constraintLayoutShadow)
        var fade_in : Animation = AnimationUtils.loadAnimation(rootView.context,R.anim.fade_in)
        var fade_out : Animation = AnimationUtils.loadAnimation(rootView.context,R.anim.fade_out)
        constraintLayoutShadow.setAnimation(fade_in)

        var btnOK : Button = rootView.findViewById(R.id.btnOK)
        var txtEmail : TextView = rootView.findViewById(R.id.txtEmail)

        // Get Email Add
        DatabaseFunctions.accessUserDocument(rootView.getContext()) { userDocument ->
            if (userDocument != null) {
                Log.d("FirestoreData", "Email: ${userDocument.userDetails?.email}")
                val dbEmailAdd: String? = userDocument.userDetails?.email
                val emailAdd : String = dbEmailAdd.toString()

                txtEmail.setText(emailAdd)
            }
        }

        btnOK.setOnClickListener {
            constraintLayoutShadow.setAnimation(fade_out)
            dismiss()
        }

        return rootView
    }

    override fun getTheme(): Int {
        return R.style.NoBackgroundDialogTheme
    }
}