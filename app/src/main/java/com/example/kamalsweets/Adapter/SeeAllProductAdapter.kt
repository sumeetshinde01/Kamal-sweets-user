package com.example.kamalsweets.Adapter

import android.content.Context
import android.content.Intent
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kamalsweets.Activity.ProductDetailActivity
import com.example.kamalsweets.MainActivity
import com.example.kamalsweets.Model.AddProductModel
import com.example.kamalsweets.R
import com.example.kamalsweets.Roomdb.AppDatabase
import com.example.kamalsweets.Roomdb.ProductModel
import com.example.kamalsweets.databinding.ItemSeeAllProductLayoutBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SeeAllProductAdapter(val context:Context, var list:ArrayList<AddProductModel>) : RecyclerView.Adapter<SeeAllProductAdapter.ProductViewHolder>(){

    inner class ProductViewHolder(val binding: ItemSeeAllProductLayoutBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding=ItemSeeAllProductLayoutBinding.inflate(LayoutInflater.from(context),parent,false)
        return ProductViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setFilteredList(list: ArrayList<AddProductModel>){
        this.list=list
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val data = list[position]
        val productId = data.getSafeId()
        
        // Add space before unit to prevent price and unit from merging (e.g., ₹90 1kg)
        val unit = if (data.productUnit.isNullOrEmpty()) "" else " ${data.productUnit}"
        
        val mrpText = "MRP=₹${data.productMRP ?: ""}$unit"
        val spannableString = SpannableString(mrpText)
        val strikethroughSpan = StrikethroughSpan()
        if (mrpText.isNotEmpty()) {
            spannableString.setSpan(strikethroughSpan, 0, mrpText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        
        Glide.with(context)
            .load(data.productCoverImage)
            .placeholder(R.drawable.kamal_sweets)
            .into(holder.binding.imageView3)
            
        holder.binding.textView4.text = data.productName
        holder.binding.textView5.text = data.productCategory
        holder.binding.MRP.text = spannableString
        holder.binding.sellingPrice.text = "₹${data.productSp ?: ""}$unit"

        val productDao = AppDatabase.getInstance(context).productDao()

        if (data.stockStatus != "In Stock") {
            holder.binding.addBtn.text = "OUT OF STOCK"
            holder.binding.addBtn.textSize = 10f
            holder.binding.addBtn.isEnabled = false
            holder.binding.addBtn.alpha = 0.5f
        } else {
            holder.binding.addBtn.isEnabled = true
            holder.binding.addBtn.alpha = 1.0f

            if (productId != null) {
                if (productDao.isExist(productId) != null) {
                    holder.binding.addBtn.text = "GO TO CART"
                    holder.binding.addBtn.textSize = 10f
                } else {
                    holder.binding.addBtn.text = "ADD"
                    holder.binding.addBtn.textSize = 12f
                }
            }
        }

        holder.binding.addBtn.setOnClickListener {
            if (productId.isNullOrEmpty()) {
                Toast.makeText(context, "Product ID not available", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (productDao.isExist(productId) != null) {
                val preference = context.getSharedPreferences("info", AppCompatActivity.MODE_PRIVATE)
                preference.edit().putBoolean("isCart", true).apply()
                context.startActivity(Intent(context, MainActivity::class.java))
                if (context is AppCompatActivity) context.finish()
            } else {
                val cartProduct = ProductModel(
                    productId = productId,
                    productName = data.productName,
                    productImage = data.productCoverImage,
                    productSp = data.productSp,
                    productQuantity = "1"
                )
                
                if (context is AppCompatActivity) {
                    context.lifecycleScope.launch(Dispatchers.IO) {
                        productDao.insertProduct(cartProduct)
                        withContext(Dispatchers.Main) {
                            holder.binding.addBtn.text = "GO TO CART"
                            holder.binding.addBtn.textSize = 10f
                            Toast.makeText(context, "Added to cart", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }

        holder.itemView.setOnClickListener {
            if (productId.isNullOrEmpty()) {
                Toast.makeText(context, "Product details not available", Toast.LENGTH_SHORT).show()
            } else {
                val intent = Intent(context, ProductDetailActivity::class.java)
                intent.putExtra("id", productId)
                context.startActivity(intent)
            }
        }
    }
}
