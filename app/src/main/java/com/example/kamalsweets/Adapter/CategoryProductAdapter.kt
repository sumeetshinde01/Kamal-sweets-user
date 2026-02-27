package com.example.kamalsweets.Adapter

import android.content.Context
import android.content.Intent
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kamalsweets.Activity.ProductDetailActivity
import com.example.kamalsweets.Model.AddProductModel
import com.example.kamalsweets.databinding.ItemCategoryProductLayoutBinding

class CategoryProductAdapter(val context: Context, var list: ArrayList<AddProductModel>) : RecyclerView.Adapter<CategoryProductAdapter.CategoryProductViewHolder>() {

    inner class CategoryProductViewHolder(val binding: ItemCategoryProductLayoutBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryProductViewHolder {
        val binding = ItemCategoryProductLayoutBinding.inflate(LayoutInflater.from(context), parent, false)
        return CategoryProductViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setFilteredList(list: ArrayList<AddProductModel>) {
        this.list = list
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: CategoryProductViewHolder, position: Int) {
        val product = list[position]
        
        // Add space before unit to prevent price and unit from merging (e.g., ₹90 1kg)
        val unit = if (product.productUnit.isNullOrEmpty()) "" else " ${product.productUnit}"
        val mrp = product.productMRP ?: ""
        val sp = product.productSp ?: ""

        val mrpText = "MRP: ₹$mrp$unit"
        val spannableString = SpannableString(mrpText)
        val strikethroughSpan = StrikethroughSpan()
        
        if (mrpText.isNotEmpty()) {
            spannableString.setSpan(strikethroughSpan, 0, mrpText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }

        Glide.with(context)
            .load(product.productCoverImage)
            .placeholder(android.R.drawable.progress_horizontal)
            .into(holder.binding.imageView3)
            
        holder.binding.textView4.text = product.productName
        holder.binding.textView5.text = "₹$sp$unit"
        holder.binding.textView6.text = spannableString

        holder.itemView.setOnClickListener {
            openDetailActivity(product.getSafeId())
        }
        
        holder.binding.addBtn.setOnClickListener {
            openDetailActivity(product.getSafeId())
        }
    }

    private fun openDetailActivity(productId: String?) {
        if (productId.isNullOrEmpty()) {
            Toast.makeText(context, "Product ID not found", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(context, ProductDetailActivity::class.java)
        intent.putExtra("id", productId)
        context.startActivity(intent)
    }
}
