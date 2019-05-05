package com.theblackthorn.kotlinsmessenger

import android.app.Activity
import android.content.Intent
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.support.constraint.ConstraintLayout
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import kotlinx.android.synthetic.main.activity_register.choosePictureRegisterBtn
import java.util.*

class RegisterActivity : AppCompatActivity() {

    //    private AnimationDrawable animationDrawable;
    lateinit var constraintLayout: ConstraintLayout
    lateinit var animationDrawable: AnimationDrawable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)


        registerBtnRegisterPage.setOnClickListener {
           performRegister()

        }

        alreadyHaveAccountTextViewRegisterPage.setOnClickListener {
            Log.d("RegisterActivity", "Try to show login activity")

            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

        choosePictureRegisterBtn.setOnClickListener {
            Log.d("RegisterActivity", "Try to show picture selector")

            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, 0)
        }


        // init constraintLayout
        constraintLayout = findViewById(R.id.constraintLayout) as ConstraintLayout

        // initializing animation drawable by getting background from constraint layout
        animationDrawable = constraintLayout.background as AnimationDrawable

        // setting enter fade animation duration to 5 seconds
        animationDrawable.setEnterFadeDuration(5000)

        // setting exit fade animation duration to 2 seconds
        animationDrawable.setExitFadeDuration(2000)


    }

    var selectedPhotoUri: Uri? = null

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 0 && resultCode == Activity.RESULT_OK && data != null) {
            Log.d("RegisterActivity", "Photo was selected")

            selectedPhotoUri = data.data

            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, selectedPhotoUri)

            val bitmapDrawable = BitmapDrawable(bitmap)
            choosePictureRegisterBtn.setBackgroundDrawable(bitmapDrawable)
        }
    }

    fun performRegister() {
        val userName = usernameEditTextRegisterPage.text.toString()
        val email = emailEditTextRegisterPage.text.toString()
        val password = passwordEditTextRegisterPage.text.toString()

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Could you kindly complete the email and password fields", Toast.LENGTH_LONG).show()
            return
        }

        Log.d("RegisterActivity", "Email is: $email")
        Log.d("RegisterActivity", "Password is: $password")
        Log.d("RegisterActivity", "Username is: $userName")

        // firebase authentication. Create user with email and password
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(!it.isSuccessful) return@addOnCompleteListener

                // else statement is if successful..wtf
                Log.d("RegisterActivity", "Successfully created user with uid: ${it.result!!.user.uid}")

                uploadImageToFirebaseStorage()
            }
            .addOnFailureListener {
                Log.d("RegisterActivity", "Failed to create user: ${it.message}")
                Toast.makeText(this, "Failed to create user as: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun uploadImageToFirebaseStorage() {
        if (selectedPhotoUri == null) return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                Log.d("RegisterActivity", "Successfully uploaded image: ${it.metadata?.path}")
            }
    }

    override fun onResume() {
        super.onResume()
        if (animationDrawable != null && !animationDrawable.isRunning) {
            // start the animation
            animationDrawable.start()
        }

    }


    override fun onPause() {
        super.onPause()
        if (animationDrawable != null && animationDrawable.isRunning) {
            // stop the animation
            animationDrawable.stop()
        }
    }
}
