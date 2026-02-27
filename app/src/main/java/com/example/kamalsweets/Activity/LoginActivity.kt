package com.example.kamalsweets.Activity

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.kamalsweets.Model.AddProductModel
import com.example.kamalsweets.Model.UserModel
import com.example.kamalsweets.databinding.ActivityLoginBinding
import com.google.firebase.*
import com.google.firebase.R
import com.google.firebase.auth.*
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {
    private lateinit var binding:ActivityLoginBinding
    private lateinit var verificationCode:String
    private lateinit var numberPreferences: SharedPreferences
    private lateinit var dialog: Dialog
    private lateinit var resendingToken: ForceResendingToken
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.button2.setOnClickListener {
            startActivity(Intent(this,RegisterActivity::class.java))

        }
        binding.button.setOnClickListener {
            if(binding.userNumber.text!!.toString().trim().isEmpty()) {
                Toast.makeText(this, "Please Provide Number", Toast.LENGTH_SHORT).show()
            }else {
                Firebase.firestore.collection("users")
                    .whereEqualTo("userPhoneNumber", binding.userNumber.text!!.toString().trim())
                    .get().addOnSuccessListener { querySnapshot ->
                        if (querySnapshot.size() > 0) {

                            sendOtp(binding.userNumber.text!!.toString().trim())

                        } else {

                            Toast.makeText(this, "User Is Not Registered", Toast.LENGTH_SHORT)
                                .show()

                        }
                    }.addOnFailureListener {
                        Toast.makeText(this, "Fail to Check Whether user is registered or not", Toast.LENGTH_SHORT).show()
                    }
            }

        }
        
    }

    private fun sendOtp(number: String) {

        Firebase.firestore.collection("users").document(number)
            .get().addOnSuccessListener {
                val data=it.data
                val username= data?.get("userName").toString()
                numberPreferences = this.getSharedPreferences("login_number", MODE_PRIVATE)
                val editor = numberPreferences.edit()
                editor.putString("number", number)
                editor.putString("name", username)
                editor.apply()

            }.addOnFailureListener {
                Toast.makeText(this, "Fail to Check Whether user is registered or not", Toast.LENGTH_SHORT).show()
            }



        dialog= Dialog(this)
        dialog.setContentView(com.example.kamalsweets.R.layout.betterprogresslayout)
        dialog.setCancelable(false)
        dialog.show()
        binding.rootLayout.visibility=View.GONE

        val options = PhoneAuthOptions.newBuilder(FirebaseAuth.getInstance())
            .setPhoneNumber("+91${number.takeLast(10)}") // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(callbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)


    }

    val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        override fun onVerificationCompleted(credential: PhoneAuthCredential) {


        }

        override fun onVerificationFailed(e: FirebaseException) {
            Toast.makeText(this@LoginActivity, "${e.localizedMessage}", Toast.LENGTH_LONG).show()
            dialog.dismiss()
            binding.rootLayout.visibility=View.VISIBLE
            Log.d("user", "onVerificationFailed: ${e.localizedMessage}")
        }

        override fun onCodeSent(
            verificationId: String,
            token: PhoneAuthProvider.ForceResendingToken,
        ) {
            verificationCode=verificationId
            resendingToken=token
            Toast.makeText(this@LoginActivity, "OTP Send Successfully", Toast.LENGTH_SHORT).show()

            dialog.dismiss()
            binding.rootLayout.visibility=View.VISIBLE
            val intent =Intent(this@LoginActivity,OTPActivity::class.java)
            intent.putExtra("verificationId",verificationId)
            intent.putExtra("number",binding.userNumber.text.toString())
            startActivity(intent)
            finish()
        }
    }
}