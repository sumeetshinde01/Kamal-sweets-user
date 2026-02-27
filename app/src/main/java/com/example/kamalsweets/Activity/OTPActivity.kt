package com.example.kamalsweets.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.kamalsweets.MainActivity
import com.example.kamalsweets.databinding.ActivityOtpactivityBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider

class OTPActivity : AppCompatActivity() {
    private lateinit var binding:ActivityOtpactivityBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityOtpactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.button.setOnClickListener { 
            if(binding.userOTP.text.toString().trim().isEmpty())
                Toast.makeText(this, "Please Provide OTP", Toast.LENGTH_SHORT).show()
            else
                verifyUser(binding.userOTP.text.toString().trim())
        }

    }

    private fun verifyUser(otp: String) {

        val credential = PhoneAuthProvider.getCredential(intent.getStringExtra("verificationId")!!, otp)
        signInWithPhoneAuthCredential(credential)
    }
    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        FirebaseAuth.getInstance()
            .signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val preferences=this.getSharedPreferences("login_number", MODE_PRIVATE)
                    val preference = this.getSharedPreferences("otpActivityUser", MODE_PRIVATE)
                    val editor=preference.edit()
                    editor.putString("number",intent.getStringExtra("number")!!)
                    editor.putString("name",preferences.getString("name","DEFAULT"))
                    editor.putBoolean("flag",true)
                    editor.apply()
                    startActivity(Intent(this,MainActivity::class.java))
                    finish()

                } else {
                    Toast.makeText(this, "Something Went Wrong", Toast.LENGTH_SHORT).show()

                }
            }
    }
}