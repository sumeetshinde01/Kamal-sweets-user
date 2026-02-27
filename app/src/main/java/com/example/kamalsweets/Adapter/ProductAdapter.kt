package com.example.kamalsweets.Adapter

import android.content.Context
import android.content.Intent
import android.text.SpannableString
import android.text.Spanned
import android.text.style.StrikethroughSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kamalsweets.Activity.ProductDetailActivity
import com.example.kamalsweets.Model.AddProductModel
import com.example.kamalsweets.R
import com.example.kamalsweets.databinding.LayoutProductItemBinding
import java.util.*
import kotlin.collections.ArrayList

class ProductAdapter(val context:Context, var list:ArrayList<AddProductModel>) :
    RecyclerView.Adapter<ProductAdapter.ProductViewHolder>(), Filterable {

    private var listFull: ArrayList<AddProductModel> = ArrayList(list)

    inner class ProductViewHolder(val binding: LayoutProductItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val binding=LayoutProductItemBinding.inflate(LayoutInflater.from(context),parent,false)
        return ProductViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val data = list[position]
        
        // Format Unit string to avoid sticking to price (e.g., ₹90 1kg instead of ₹901kg)
        val unit = if (data.productUnit.isNullOrEmpty()) "" else " ${data.productUnit}"
        
        // MRP with Strikethrough
        val mrpText = "₹${data.productMRP ?: ""}$unit"
        val spannableString = SpannableString(mrpText)
        val strikethroughSpan = StrikethroughSpan()
        if (mrpText.isNotEmpty()) {
            spannableString.setSpan(strikethroughSpan, 0, mrpText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
        
        Glide.with(context)
            .load(data.productCoverImage)
            .placeholder(R.drawable.kamal_sweets)
            .into(holder.binding.productImage)
            
        holder.binding.textView3.text = data.productName
        holder.binding.textView2.text = data.productCategory
        
        holder.binding.textView1.text = spannableString // MRP
        holder.binding.button4.text = "₹${data.productSp ?: ""}$unit" // Selling Price

        if(data.stockStatus != "In Stock"){
            holder.binding.addToCartButton.text = "Out Of Stock"
            holder.binding.addToCartButton.textSize = 9f
        } else {
            holder.binding.addToCartButton.text = "ADD"
            holder.binding.addToCartButton.textSize = 12f
        }

        holder.binding.addToCartButton.setOnClickListener {
            if (data.stockStatus == "In Stock"){
                openDetailActivity(data.getSafeId())
            } else {
                Toast.makeText(context, "Product Out Of Stock", Toast.LENGTH_SHORT).show()
            }
        }

        holder.itemView.setOnClickListener {
            openDetailActivity(data.getSafeId())
        }
    }

    private fun openDetailActivity(productId: String?) {
        if (productId.isNullOrEmpty()) {
            Toast.makeText(context, "Product details not available", Toast.LENGTH_SHORT).show()
            return
        }
        val intent = Intent(context, ProductDetailActivity::class.java)
        intent.putExtra("id", productId)
        context.startActivity(intent)
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList = ArrayList<AddProductModel>()
                if (constraint.isNullOrEmpty()) {
                    filteredList.addAll(listFull)
                } else {
                    val filterPattern = constraint.toString().lowercase(Locale.getDefault()).trim()
                    for (product in listFull) {
                        if (product.productName?.lowercase(Locale.getDefault())?.contains(filterPattern) == true) {
                            filteredList.add(product)
                        }
                    }
                }
                val results = FilterResults()
                results.values = filteredList
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                list.clear()
                if (results?.values != null) {
                    @Suppress("UNCHECKED_CAST")
                    list.addAll(results.values as ArrayList<AddProductModel>)
                }
                notifyDataSetChanged()
            }
        }
    }

    fun updateList(newList: ArrayList<AddProductModel>) {
        list = ArrayList(newList)
        listFull = ArrayList(newList)
        notifyDataSetChanged()
    }
}
