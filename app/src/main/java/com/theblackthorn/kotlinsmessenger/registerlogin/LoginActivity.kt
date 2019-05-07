package com.theblackthorn.kotlinsmessenger.registerlogin

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.theblackthorn.kotlinsmessenger.R
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        loginBtnLoginPage.setOnClickListener {
            val email = emailEditTextLoginPage.text.toString()
            val password = passwordEditTextLoginPage.text.toString()

            Log.d("Login", "Attempt login with email & password")

            FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener {
                    if (!it.isSuccessful) return@addOnCompleteListener
                    Log.d("Login", "User succesfully signed in with email: ${email} and password: ${password}")


                }
                .addOnFailureListener {
                    Log.d("Login", "Failed to sign user in: ${it.message}")
                    Toast.makeText(this, "Failed to log user in: ${it.message}",Toast.LENGTH_LONG).show()
                }

        }

        backToRegistrationTextViewLoginPage.setOnClickListener {
            finish()
        }


    }



}
