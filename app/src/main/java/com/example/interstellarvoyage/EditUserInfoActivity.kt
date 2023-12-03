package com.example.interstellarvoyage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

class EditUserInfoActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_user_info)

        val goldUser = findViewById<LinearLayout>(R.id.goldUser)
        val silverUser = findViewById<LinearLayout>(R.id.silverUser)
        val bronzeUser = findViewById<LinearLayout>(R.id.bronzeUser)
        val ironUser = findViewById<LinearLayout>(R.id.ironUser)

        val txtUsernameGold = findViewById<TextView>(R.id.txtUsernameGold)
        val txtUsernameSilver = findViewById<TextView>(R.id.txtUsernameSilver)
        val txtUsernameBronze = findViewById<TextView>(R.id.txtUsernameBronze)
        val txtUsernameIron = findViewById<TextView>(R.id.txtUsernameIron)

        val btnBack = findViewById<Button>(R.id.btnBack)
        val btnChangeUsername = findViewById<Button>(R.id.btnChangeUsername)
        val btnChangeEmailAdd = findViewById<Button>(R.id.btnChangeEmailAdd)
        val btnChangePassword = findViewById<Button>(R.id.btnChangePassword)

        val editTextUsername = findViewById<EditText>(R.id.editTextUsername)
        val editTextEmailAddress = findViewById<EditText>(R.id.editTextEmailAddress)
        val editTextPassword = findViewById<EditText>(R.id.editTextPassword)
        val editTextOldPassword = findViewById<EditText>(R.id.editTextOldPassword)
        val editTextNewPassword = findViewById<EditText>(R.id.editTextNewPassword)
        val editTextConfirmNewPassword = findViewById<EditText>(R.id.editTextConfirmNewPassword)

        val usernameErr = findViewById<TextView>(R.id.usernameErr)
        val emailAddressErr = findViewById<TextView>(R.id.emailAddressErr)
        val passwordErr = findViewById<TextView>(R.id.passwordErr)

        // Add Username & Populate Username and Email Address Text Fields
        DatabaseFunctions.accessUserDocument(this) { userDocument ->
            if (userDocument != null) {
                Log.d("FirestoreData", "Current Level: ${userDocument.currentLevel}")
                Log.d("FirestoreData", "Username: ${userDocument.userDetails?.username}")
                Log.d("FirestoreData", "Email: ${userDocument.userDetails?.email}")
                val dbCurrentLevel: Long? = userDocument.currentLevel
                val currentLevel: Int = dbCurrentLevel?.toInt() ?: 0
                val dbUsername: String? = userDocument.userDetails?.username
                val username : String = dbUsername.toString()
                val dbEmailAdd: String? = userDocument.userDetails?.email
                val emailAdd : String = dbEmailAdd.toString()
                if (currentLevel == 0) {
                    goldUser.visibility = View.GONE
                    ironUser.visibility = View.VISIBLE
                    txtUsernameIron.text = username
                } else if (currentLevel == 1) {
                    goldUser.visibility = View.GONE
                    bronzeUser.visibility = View.VISIBLE
                    txtUsernameBronze.text = username
                } else if (currentLevel == 2) {
                    goldUser.visibility = View.GONE
                    silverUser.visibility = View.VISIBLE
                    txtUsernameSilver.text = username
                } else if (currentLevel == 3) {
                    txtUsernameGold.text = username
                }
                editTextUsername.setText(username)
                editTextEmailAddress.setText(emailAdd)
            }
        }

        btnBack.setOnClickListener {
            startActivity(Intent(this, OptionsActivity::class.java))
        }

        btnChangeUsername.setOnClickListener {
            val newUsername = editTextUsername.text.toString()
            DatabaseFunctions.changeUsername(this, newUsername)
        }

        btnChangeEmailAdd.setOnClickListener {
            val newEmail = editTextEmailAddress.text.toString()
            val password = editTextPassword.text.toString()
            DatabaseFunctions.changeEmailAdd(this, newEmail, password)
        }

        btnChangePassword.setOnClickListener {
            val oldPassword = editTextOldPassword.text.toString()
            val newPassword = editTextNewPassword.text.toString()
            val confirmNewPassword = editTextConfirmNewPassword.text.toString()
            DatabaseFunctions.changePassword(this, oldPassword, newPassword, confirmNewPassword)
        }
    }
}