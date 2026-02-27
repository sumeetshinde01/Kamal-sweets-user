package com.example.kamalsweets.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.kamalsweets.Model.UserModel
import com.example.kamalsweets.R
import com.example.kamalsweets.databinding.ActivityRegisterBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding:ActivityRegisterBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        binding.button2.setOnClickListener {
            openLogin()
        }

        binding.button.setOnClickListener {
            validateUser()

        }

    }

    private fun validateUser() {
        if(binding.userName.text!!.trim().isEmpty() || binding.userNumber.text!!.trim().toString().isEmpty())
            Toast.makeText(this, "Please Fill All Fields", Toast.LENGTH_SHORT).show()
        else{

            Firebase.firestore.collection("users")
                .whereEqualTo("userPhoneNumber", binding.userNumber.text.toString())
                .get().addOnSuccessListener { querySnapshot ->
                    if (querySnapshot.size() > 0) {
                        Toast.makeText(this, "User Already Registered \n Go To Login Page", Toast.LENGTH_LONG).show()
//                        startActivity(Intent(this,LoginActivity::class.java))
//                        finish()
                    } else {
                        storeData()
                    }
                }.addOnFailureListener {
                    Toast.makeText(this, "Fail to Check Whether user is registered or not", Toast.LENGTH_SHORT).show()
                }

        }
    }


    private fun storeData() {
        val builder=AlertDialog.Builder(this)
            .setTitle("Loading...")
            .setMessage("Please Wait")
            .setCancelable(false)
            .create()
        builder.show()
        
        val preference = this.getSharedPreferences("registerActivityUser", MODE_PRIVATE)
        val editor=preference.edit()
        editor.putString("number",binding.userNumber.text.toString())
        editor.putString("name",binding.userName.text.toString())
        editor.apply()

        val data = UserModel(userName = binding.userName.text.toString(), userPhoneNumber = binding.userNumber.text.toString())


        Firebase.firestore.collection("users").document(binding.userNumber.text.toString())
            .set(data).addOnSuccessListener{
                builder.dismiss()
                Toast.makeText(this, "User Rigistered \n Go To Login Page", Toast.LENGTH_SHORT).show()
                //openLogin()
            }.addOnFailureListener {
                builder.dismiss()
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
    }

    private fun openLogin() {
        startActivity(Intent(this,LoginActivity::class.java))
        finish()
    }
}