package com.example.kamalsweets.Activity

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.example.kamalsweets.databinding.ActivityAddressBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class AddressActivity : AppCompatActivity() {
    private lateinit var binding:ActivityAddressBinding
    private lateinit var preference:SharedPreferences
    private lateinit var totalCost:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityAddressBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        preference= this.getSharedPreferences("otpActivityUser", MODE_PRIVATE)
        totalCost=intent.getStringExtra("totalCost")!!
        loadUserInfo1()

        binding.proceed.setOnClickListener {
            
            validateData(
                binding.userNumber.text.toString(),
                binding.userName.text.toString(),
                binding.userPincode.text.toString().trim(),
                binding.userCity.text.toString(),
                binding.userState.text.toString(),
                binding.userVillage.text.toString(),
            )
        }
    }

    private fun validateData(number: String, name: String, pinCode: String, city: String, state: String, village: String) {

        if(number.isEmpty() || state.isEmpty() || name.isEmpty() || pinCode.isEmpty() || city.isEmpty() || state.isEmpty() || village.isEmpty() || pinCode.length<6 || pinCode.length>6 ){
            if (name.isEmpty()){
                binding.userName.requestFocus()
                binding.userName.error=("Empty!")
            }

            else if (number.isEmpty()){
                binding.userNumber.requestFocus()
                binding.userNumber.error=("Empty!")
            }

            else if (village.isEmpty()){
                binding.userVillage.requestFocus()
                binding.userVillage.error=("Empty!")
            }

            else if (city.isEmpty()){
                binding.userCity.requestFocus()
                binding.userCity.error=("Empty!")
            }

            else if (state.isEmpty()){
                binding.userState.requestFocus()
                binding.userState.error=("Empty!")
            }
            else if(pinCode.isEmpty()){
                binding.userPincode.requestFocus()
                binding.userPincode.error=("Empty!")
            }else{
                binding.userPincode.error=("Incorrect Pincode")
            }


        } else{
            storeData(pinCode,city,state,village)

        }

    }

    private fun storeData(pinCode: String, city: String, state: String, village: String) {

        val map = mutableMapOf<String, Any>()
        map["village"]= village
        map["state"]= state
        map["city"]= city
        map["pincode"]=pinCode

        Firebase.firestore.collection("users")
            .document(preference.getString("number","")!!)
            .update(map).addOnSuccessListener{

                val b= Bundle()
                b.putStringArrayList("productIds", intent.getStringArrayListExtra("productIds"))
                b.putStringArrayList("productQuantity", intent.getStringArrayListExtra("productQuantity"))
                b.putString("totalCost",totalCost)

                val intent = Intent(this, CheckoutActivity::class.java)
                intent.putExtras(b)
                startActivity(intent)

            }.addOnFailureListener {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
            }

    }

    private fun loadUserInfo1(){

            val phoneNumber = preference.getString("number", null)


            if (phoneNumber != null) {
                Firebase.firestore.collection("users")
                    .document(phoneNumber)
                    .get()
                    .addOnSuccessListener { documentSnapshot ->
                        val user = documentSnapshot.data
                        if (user != null) {
                            binding.userName.setText(user["userName"].toString())
                            binding.userNumber.setText(user["userPhoneNumber"].toString())
                            binding.userVillage.setText(user["village"].toString())
                            binding.userState.setText(user["state"].toString())
                            binding.userCity.setText(user["city"].toString())
                            binding.userPincode.setText(user["pincode"].toString())
                        } else {
                            // Handle the case where the document doesn't exist or user data is missing
                            Toast.makeText(this, "User data not found", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Something went wrong: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                // Handle the case where the "number" key is not found in SharedPreferences
                Toast.makeText(this, "Phone number not found in SharedPreferences", Toast.LENGTH_SHORT).show()
            }


    }
}