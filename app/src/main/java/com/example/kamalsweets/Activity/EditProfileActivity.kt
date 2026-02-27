package com.example.kamalsweets.Activity

import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.example.kamalsweets.R
import com.example.kamalsweets.databinding.ActivityEditProfileBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import java.text.SimpleDateFormat
import java.util.*

class EditProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityEditProfileBinding
    private var imageUri: Uri? = null
    private lateinit var userId: String
    private val calendar = Calendar.getInstance()

    private val launchGallery = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri = result.data?.data
            binding.profileImage.setImageURI(imageUri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        val preference = getSharedPreferences("otpActivityUser", MODE_PRIVATE)
        userId = preference.getString("number", "")!!

        loadUserData()

        binding.backBtn.setOnClickListener { finish() }
        
        binding.btnPickImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            launchGallery.launch(intent)
        }

        binding.editDOB.setOnClickListener {
            showDatePicker()
        }

        binding.btnSave.setOnClickListener {
            validateAndSave()
        }

        binding.btnLogout.setOnClickListener {
            val editor = preference.edit()
            editor.putBoolean("flag", false)
            editor.apply()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun showDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val format = "dd/MM/yyyy"
            val sdf = SimpleDateFormat(format, Locale.US)
            binding.editDOB.setText(sdf.format(calendar.time))
        }

        DatePickerDialog(
            this,
            dateSetListener,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun loadUserData() {
        Firebase.firestore.collection("users").document(userId).get()
            .addOnSuccessListener { snapshot ->
                if (snapshot != null && snapshot.exists()) {
                    val name = snapshot.getString("userName") ?: "User Name"
                    val phone = snapshot.getString("userPhoneNumber") ?: userId
                    
                    binding.editName.setText(snapshot.getString("userName"))
                    binding.editEmail.setText(snapshot.getString("userEmail"))
                    binding.editDOB.setText(snapshot.getString("userDOB"))
                    binding.editAddress.setText(snapshot.getString("userAddress"))
                    
                    // Update header info
                    binding.displayUserName.text = name
                    binding.displayUserId.text = "ID: $phone"
                    
                    val imgUrl = snapshot.getString("userImage")
                    if (!imgUrl.isNullOrEmpty()) {
                        Glide.with(this).load(imgUrl).placeholder(R.drawable.user).into(binding.profileImage)
                    }
                }
            }
    }

    private fun validateAndSave() {
        val name = binding.editName.text.toString().trim()
        val email = binding.editEmail.text.toString().trim()
        val dob = binding.editDOB.text.toString().trim()
        val address = binding.editAddress.text.toString().trim()

        if (name.isEmpty()) {
            binding.editName.error = "Name is required"
            return
        }

        binding.btnSave.visibility = View.GONE
        binding.progressBar.visibility = View.VISIBLE

        if (imageUri != null) {
            uploadImage(name, email, dob, address)
        } else {
            updateFirestore(name, email, dob, address, null)
        }
    }

    private fun uploadImage(name: String, email: String, dob: String, address: String) {
        val fileName = "profile_$userId.jpg"
        val ref = Firebase.storage.reference.child("profile_images/$fileName")

        ref.putFile(imageUri!!)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener { url ->
                    updateFirestore(name, email, dob, address, url.toString())
                }
            }
            .addOnFailureListener {
                binding.btnSave.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Failed to upload image", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateFirestore(name: String, email: String, dob: String, address: String, imageUrl: String?) {
        val updates = mutableMapOf<String, Any>()
        updates["userName"] = name
        updates["userEmail"] = email
        updates["userDOB"] = dob
        updates["userAddress"] = address
        if (imageUrl != null) {
            updates["userImage"] = imageUrl
        }

        Firebase.firestore.collection("users").document(userId)
            .update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                // Update display name at the top immediately
                binding.displayUserName.text = name
                binding.btnSave.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
            }
            .addOnFailureListener {
                binding.btnSave.visibility = View.VISIBLE
                binding.progressBar.visibility = View.GONE
                Toast.makeText(this, "Update failed", Toast.LENGTH_SHORT).show()
            }
    }
}
