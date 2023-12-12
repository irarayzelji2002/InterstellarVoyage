package com.example.interstellarvoyage

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat

class OptionsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_options)

        val goldUser = findViewById<LinearLayout>(R.id.goldUser)
        val silverUser = findViewById<LinearLayout>(R.id.silverUser)
        val bronzeUser = findViewById<LinearLayout>(R.id.bronzeUser)
        val ironUser = findViewById<LinearLayout>(R.id.ironUser)

        val txtUsernameGold = findViewById<TextView>(R.id.txtUsernameGold)
        val txtUsernameSilver = findViewById<TextView>(R.id.txtUsernameSilver)
        val txtUsernameBronze = findViewById<TextView>(R.id.txtUsernameBronze)
        val txtUsernameIron = findViewById<TextView>(R.id.txtUsernameIron)
        val txtEmailAddress = findViewById<TextView>(R.id.txtEmailAddress)

        val btnBack = findViewById<Button>(R.id.btnBack)
        val switchBGMusic = findViewById<SwitchCompat>(R.id.switchBGMusic)
        val btnEditUserInfo = findViewById<Button>(R.id.btnEditUserInfo)
        val btnLogout = findViewById<Button>(R.id.btnLogout)
        val btnDeleteAccount = findViewById<Button>(R.id.btnDeleteAccount)

        // Add Username & Email Address Text
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
                } else if (currentLevel >= 3) {
                    txtUsernameGold.text = username
                }
                txtEmailAddress.text = emailAdd
            }
        }

        btnBack.setOnClickListener {
            startActivity(Intent(this, HomepageActivity::class.java))
        }

        switchBGMusic.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                //playmusic
            } else {
                //pausemusic
            }
        }

        btnEditUserInfo.setOnClickListener {
            startActivity(Intent(this, EditUserInfoActivity::class.java))
        }

        btnLogout.setOnClickListener {
            var dialogFragment = LogoutConfirmActivity()
            dialogFragment.setCancelable(false)
            dialogFragment.show(supportFragmentManager, "Logout Confirm Dialog")
        }

        btnDeleteAccount.setOnClickListener {
            startActivity(Intent(this, DeleteAccountActivity::class.java))
        }
    }
}