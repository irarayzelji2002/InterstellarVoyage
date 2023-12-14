package com.example.interstellarvoyage

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
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

        var typeEmailContainer : LinearLayout = rootView.findViewById(R.id.typeEmailContainer)
        var btnSendEmail : Button = rootView.findViewById(R.id.btnSendEmail)
        var editTextEmailAddress : EditText = rootView.findViewById(R.id.editTextEmailAddress)
        var emailErr : TextView = rootView.findViewById(R.id.emailAddressErr)
        var errCount = 0

        var emailSentContainer : LinearLayout = rootView.findViewById(R.id.emailSentContainer)
        var btnOK : Button = rootView.findViewById(R.id.btnOK)
        var txtEmail : TextView = rootView.findViewById(R.id.txtEmail)

        var btnClose : ImageButton = rootView.findViewById(R.id.btnClose)

        btnSendEmail.setOnClickListener {
            var email = editTextEmailAddress.text.toString()
            val emailRegex = "^[A-Za-z](.*)([@]{1})(.{1,})(\\.)(.{1,})"
            if(email == "" || email == null) {
                setErrorTextAndVisibility(emailErr, "This field is required")
                errCount++
            } else if (!email.matches(emailRegex.toRegex())) {
                setErrorTextAndVisibility(emailErr, "Email address is not valid.")
                errCount++
            }
            if(errCount  == 0) {
                DatabaseFunctions.forgotPassword(rootView.getContext(), email, emailSentContainer, typeEmailContainer, txtEmail, emailErr)
            }
        }

        btnOK.setOnClickListener {
            constraintLayoutShadow.setAnimation(fade_out)
            dismiss()
        }

        btnClose.setOnClickListener {
            constraintLayoutShadow.setAnimation(fade_out)
            dismiss()
        }

        return rootView
    }

    fun setErrorTextAndVisibility(view: TextView, error: String) {
        if (error.isNotEmpty() && error != null) {
            view.visibility = View.VISIBLE
            view.text = error
        } else {
            view.visibility = View.GONE
        }
    }

    override fun getTheme(): Int {
        return R.style.NoBackgroundDialogTheme
    }
}