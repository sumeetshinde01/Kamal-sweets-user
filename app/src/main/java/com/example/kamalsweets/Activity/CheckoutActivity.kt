package com.example.kamalsweets.Activity

import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.kamalsweets.MainActivity
import com.example.kamalsweets.R
import com.example.kamalsweets.Roomdb.AppDatabase
import com.example.kamalsweets.Roomdb.OrderHistoryModel
import com.example.kamalsweets.Roomdb.ProductModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.razorpay.Checkout
import com.razorpay.PaymentResultListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONObject

class CheckoutActivity : AppCompatActivity(), PaymentResultListener {
    private lateinit var dialog: Dialog
    private lateinit var pricePreference: SharedPreferences
    private lateinit var paymentPreference: SharedPreferences
    private var paymentStatus: Boolean? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_checkout)

        dialog = Dialog(this)
        dialog.setContentView(R.layout.progresslayout)
        supportActionBar?.hide()
        dialog.setCancelable(false)

        pricePreference = this.getSharedPreferences("totalAmount", MODE_PRIVATE)
        paymentPreference = this.getSharedPreferences("online_payment", MODE_PRIVATE)
        paymentStatus = paymentPreference.getBoolean("paid", false)
        val price = pricePreference.getFloat("total", 0.00f)

        if (paymentStatus == true) {
            val checkout = Checkout()
            checkout.setKeyID("rzp_test_SIIL7AQsoE3Eca")
            try {
                val options = JSONObject()
                options.put("name", "Kamal Sweets")
                options.put("description", "Best Payment Method To Pay")
                options.put("image", "https://shorturl.at/louJZ")
                options.put("theme.color", "#E6296A")
                options.put("currency", "INR")
                options.put("amount", (price * 100).toInt())

                val prefill = JSONObject()
                prefill.put("email", "customer@example.com")
                prefill.put("contact", "9876543210")
                options.put("prefill", prefill)

                checkout.open(this, options)
            } catch (e: Exception) {
                Toast.makeText(this, "Payment Error: ${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        } else {
            uploadData("Cash On Delivery", true)
        }
    }

    override fun onPaymentSuccess(p0: String?) {
        uploadData("Paid", false)
    }

    override fun onPaymentError(errorCode: Int, response: String?) {
        Toast.makeText(this, "Payment Failed", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun uploadData(paymentStatus: String, showDialog: Boolean) {
        val productIds = intent.getStringArrayListExtra("productIds") ?: return
        val quantities = intent.getStringArrayListExtra("productQuantity") ?: return
        val preference = getSharedPreferences("otpActivityUser", MODE_PRIVATE)
        val userId = preference.getString("number", "") ?: ""

        if (userId.isEmpty()) return

        if (showDialog) {
            dialog.setContentView(R.layout.betterprogresslayout)
            dialog.show()
        }

        // IMPORTANT: Use a single timestamp for all items in this checkout session
        // This acts as a 'Session ID' to group them together in the My Orders screen.
        val orderTimestamp = System.currentTimeMillis()

        // Fetch User Info Once
        Firebase.firestore.collection("users").document(userId).get()
            .addOnSuccessListener { userSnap ->
                val userName = userSnap.getString("userName") ?: "Guest"
                val address = "${userSnap.getString("village") ?: ""} ${userSnap.getString("city") ?: ""} ${userSnap.getString("state") ?: ""} ${userSnap.getString("pincode") ?: ""}"

                var completedUploads = 0
                for (i in productIds.indices) {
                    val pid = productIds[i]
                    val qty = quantities[i]

                    Firebase.firestore.collection("products").document(pid).get()
                        .addOnSuccessListener { pSnap ->
                            val pName = pSnap.getString("productName") ?: "Product"
                            val pSp = pSnap.getString("productSp")?.toDoubleOrNull() ?: 0.0
                            val total = (pSp * qty.toDouble()).toString()

                            val orderId = Firebase.firestore.collection("allOrders").document().id
                            val data = hashMapOf(
                                "name" to pName,
                                "price" to total,
                                "productId" to pid,
                                "status" to "Ordered",
                                "userId" to userId,
                                "userName" to userName,
                                "userAddress" to address,
                                "productQuantity" to qty,
                                "paymentStatus" to paymentStatus,
                                "timestamp" to orderTimestamp, // Use shared timestamp
                                "orderId" to orderId,
                                "cancelReason" to "",
                                "deliveryPersonName" to "",
                                "deliveryPersonNumber" to ""
                            )

                            Firebase.firestore.collection("allOrders").document(orderId).set(data)
                                .addOnCompleteListener {
                                    completedUploads++
                                    if (completedUploads == productIds.size) {
                                        showSuccessDialog()
                                    }
                                }

                            lifecycleScope.launch(Dispatchers.IO) {
                                val db = AppDatabase.getInstance(this@CheckoutActivity)
                                db.productDao().deleteProduct(ProductModel(pid))
                                
                                // Store order locally immediately to prevent disappearance later
                                db.orderHistoryDao().insertOrder(
                                    OrderHistoryModel(
                                        orderId = orderId,
                                        name = pName,
                                        userId = userId,
                                        status = "Ordered",
                                        productId = pid,
                                        price = total,
                                        userName = userName,
                                        userAddress = address,
                                        productQuantity = qty,
                                        timestamp = orderTimestamp,
                                        paymentStatus = paymentStatus,
                                        cancelReason = "",
                                        deliveryPersonName = "",
                                        deliveryPersonNumber = ""
                                    )
                                )
                            }
                        }
                }
            }
    }

    private fun showSuccessDialog() {
        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
            dialog.setContentView(R.layout.progresslayout2)
            dialog.show()
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }, 3000)
        }, 1000)
    }
}
