package com.example.kamalsweets.Activity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.denzcoskun.imageslider.constants.ScaleTypes
import com.denzcoskun.imageslider.models.SlideModel
import com.example.kamalsweets.MainActivity
import com.example.kamalsweets.Room.productDao
import com.example.kamalsweets.Roomdb.AppDatabase
import com.example.kamalsweets.Roomdb.ProductModel
import com.example.kamalsweets.databinding.ActivityProductDetailBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ProductDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProductDetailBinding
    private var stockStatus: String = "Out Of Stock"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProductDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupQuantityButtons()
        val productId = intent.getStringExtra("id")
        if (productId.isNullOrEmpty()) {
            Toast.makeText(this, "Product ID is missing", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            getProductDetail(productId)
        }
    }

    private fun setupQuantityButtons() {
        binding.btnPlus.setOnClickListener {
            val currentQty = binding.quantity.text.toString().toIntOrNull() ?: 1
            binding.quantity.setText((currentQty + 1).toString())
        }

        binding.btnMinus.setOnClickListener {
            val currentQty = binding.quantity.text.toString().toIntOrNull() ?: 1
            if (currentQty > 1) {
                binding.quantity.setText((currentQty - 1).toString())
            }
        }
    }

    private fun getProductDetail(prodId: String) {
        Firebase.firestore.collection("products")
            .document(prodId).get().addOnSuccessListener { document ->
                if (!document.exists()) {
                    Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show()
                    finish()
                    return@addOnSuccessListener
                }

                try {
                    // Get product details with backward compatibility
                    val productName = getProductName(document.data)
                    val productMrp = getProductMRP(document.data)
                    val productSp = getProductSP(document.data)
                    val productUnit = getProductUnit(document.data)
                    val description = getProductDescription(document.data)
                    stockStatus = getStockStatus(document.data)

                    // Build image list with backward compatibility
                    val imageUrlList = getProductImages(document.data)

                    // If we got here with valid data, display it
                    if (productName.isNotEmpty()) {
                        displayProductDetails(
                            productName,
                            productSp,
                            productMrp,
                            productUnit,
                            description,
                            imageUrlList,
                            prodId
                        )
                    } else {
                        Toast.makeText(this, "Product details not available", Toast.LENGTH_SHORT).show()
                        finish()
                    }

                } catch (e: Exception) {
                    Log.e("ProductDetail", "Error parsing product: ${e.message}", e)
                    Toast.makeText(this, "Error loading product details", Toast.LENGTH_SHORT).show()
                    finish()
                }

            }.addOnFailureListener {
                Toast.makeText(this, "Something went Wrong", Toast.LENGTH_SHORT).show()
                finish()
            }
    }

    private fun getProductName(data: Map<String, Any>?): String {
        return data?.get("productName") as? String
            ?: data?.get("ProductName") as? String
            ?: ""
    }

    private fun getProductMRP(data: Map<String, Any>?): String {
        return data?.get("productMRP") as? String
            ?: data?.get("ProductMRP") as? String
            ?: ""
    }

    private fun getProductSP(data: Map<String, Any>?): String {
        return data?.get("productSp") as? String
            ?: data?.get("ProductSp") as? String
            ?: ""
    }

    private fun getProductUnit(data: Map<String, Any>?): String {
        return data?.get("productUnit") as? String
            ?: data?.get("ProductUnit") as? String
            ?: ""
    }

    private fun getProductDescription(data: Map<String, Any>?): String {
        return data?.get("productDescription") as? String
            ?: data?.get("productDiscription") as? String
            ?: data?.get("ProductDiscription") as? String
            ?: "No description available"
    }

    private fun getStockStatus(data: Map<String, Any>?): String {
        return data?.get("stockStatus") as? String
            ?: data?.get("StockStatus") as? String
            ?: "In Stock" // Default to In Stock for old products
    }

    private fun getProductImages(data: Map<String, Any>?): ArrayList<String> {
        val imageUrlList = ArrayList<String>()

        // Try new field: productImages (ArrayList)
        val productImagesField = data?.get("productImages")
        if (productImagesField is List<*>) {
            productImagesField.forEach { item ->
                if (item is String && item.isNotEmpty()) {
                    imageUrlList.add(item)
                }
            }
        }

        // If no images yet, try: ProductImage (ArrayList) - old format
        if (imageUrlList.isEmpty()) {
            val productImageField = data?.get("ProductImage")
            when (productImageField) {
                is String -> {
                    if (productImageField.isNotEmpty()) imageUrlList.add(productImageField)
                }
                is List<*> -> {
                    productImageField.forEach { item ->
                        if (item is String && item.isNotEmpty()) {
                            imageUrlList.add(item)
                        }
                    }
                }
            }
        }

        // If still no images, try: productImage (String or ArrayList)
        if (imageUrlList.isEmpty()) {
            val productImageField = data?.get("productImage")
            when (productImageField) {
                is String -> {
                    if (productImageField.isNotEmpty()) imageUrlList.add(productImageField)
                }
                is List<*> -> {
                    productImageField.forEach { item ->
                        if (item is String && item.isNotEmpty()) {
                            imageUrlList.add(item)
                        }
                    }
                }
            }
        }

        // Final fallback: productCoverImage
        if (imageUrlList.isEmpty()) {
            val coverImg = data?.get("productCoverImage") as? String
                ?: data?.get("ProductCoverImage") as? String
            if (!coverImg.isNullOrEmpty()) {
                imageUrlList.add(coverImg)
            }
        }

        return imageUrlList
    }

    private fun displayProductDetails(
        productName: String,
        productSp: String,
        productMrp: String,
        productUnit: String,
        description: String,
        imageUrlList: ArrayList<String>,
        prodId: String
    ) {
        val unitWithSpace = if (productUnit.isNotEmpty()) " $productUnit" else ""

        // Format MRP with Strikethrough
        val mrpFullText = if (productMrp.isNotEmpty()) "MRP=₹$productMrp$unitWithSpace" else ""
        if (mrpFullText.isNotEmpty()) {
            val spannableString = SpannableString(mrpFullText)
            val strikethroughSpan = StrikethroughSpan()
            spannableString.setSpan(strikethroughSpan, 0, mrpFullText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            binding.mrp.text = spannableString
        } else {
            binding.mrp.visibility = View.GONE
        }

        binding.title.text = productName
        binding.prize.text = "₹$productSp$unitWithSpace"
        binding.Discription.text = description

        // Setup image slider
        val slideList = ArrayList<SlideModel>()
        imageUrlList.forEach { imageUrl ->
            slideList.add(SlideModel(imageUrl, productName, ScaleTypes.CENTER_CROP))
        }

        if (slideList.isNotEmpty()) {
            binding.imageSlider.visibility = View.VISIBLE
            binding.imageSlider.setImageList(slideList)
        } else {
            binding.imageSlider.visibility = View.VISIBLE
            // Could add a placeholder if needed
        }

        // Setup cart button
        if (stockStatus != "In Stock") {
            binding.cartview.text = "Out Of Stock"
            binding.cartview.alpha = 0.5f
        } else {
            binding.cartview.alpha = 1.0f
            // Get cover image for cart
            val coverImg = imageUrlList.firstOrNull()
            cartAction(prodId, productName, productSp, coverImg)
        }
    }

    private fun cartAction(prodId: String, productName: String?, productSp: String?, coverImg: String?) {
        val productDao = AppDatabase.getInstance(this).productDao()

        lifecycleScope.launch(Dispatchers.IO) {
            val existingProduct = productDao.isExist(prodId)
            launch(Dispatchers.Main) {
                if (existingProduct != null) {
                    binding.cartview.text = "Go To Cart"
                } else {
                    binding.cartview.text = "Add To Cart"
                }
            }
        }

        binding.cartview.setOnClickListener {
            if (stockStatus == "In Stock") {
                val qtyText = binding.quantity.text.toString().trim()
                val qtyInt = qtyText.toIntOrNull() ?: 0
                if (qtyInt <= 0) {
                    Toast.makeText(this, "Please Specify valid Quantity", Toast.LENGTH_SHORT).show()
                    binding.quantity.requestFocus()
                } else {
                    lifecycleScope.launch(Dispatchers.IO) {
                        val isExist = productDao.isExist(prodId)
                        launch(Dispatchers.Main) {
                            if (isExist != null) {
                                openCart()
                            } else {
                                val productSpiDouble = productSp?.toDoubleOrNull() ?: 0.0
                                val priceAfterMultiple = (productSpiDouble * qtyInt).toString()
                                addToCart(productDao, prodId, productName, priceAfterMultiple, coverImg, qtyText)
                            }
                        }
                    }
                }
            } else {
                Toast.makeText(this, "Product Out Of Stock", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun addToCart(productDao: productDao, prodId: String, productName: String?, totalPrice: String?, coverImg: String?, productQuantity: String) {
        val data = ProductModel(prodId, productName, coverImg, totalPrice, productQuantity)
        lifecycleScope.launch(Dispatchers.IO) {
            productDao.insertProduct(data)
            launch(Dispatchers.Main) {
                binding.cartview.text = "Go To Cart"
                Toast.makeText(this@ProductDetailActivity, "Added to cart", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun openCart() {
        val preference = this.getSharedPreferences("info", MODE_PRIVATE)
        val editor = preference.edit()
        editor.putBoolean("isCart", true)
        editor.apply()

        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}