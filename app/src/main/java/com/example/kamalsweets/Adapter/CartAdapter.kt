package com.example.kamalsweets.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.kamalsweets.Activity.ProductDetailActivity
import com.example.kamalsweets.R
import com.example.kamalsweets.Roomdb.AppDatabase
import com.example.kamalsweets.Roomdb.ProductModel
import com.example.kamalsweets.databinding.LayoutCartItemBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class CartAdapter (val context: Context, val list: List<ProductModel>): RecyclerView.Adapter<CartAdapter.CartViewHolder>(){

    inner class CartViewHolder(val binding:LayoutCartItemBinding):
            RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CartViewHolder {
        val binding= LayoutCartItemBinding.inflate(LayoutInflater.from(context),parent,false)
        return CartViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: CartViewHolder, position: Int) {
        val data = list[position]

        Glide.with(context)
            .load(data.productImage)
            .placeholder(R.drawable.kamal_sweets)
            .error(R.drawable.kamal_sweets)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .override(200, 200) // Optimization: resize image
            .into(holder.binding.imageView4)

        holder.binding.textView6.text="${data.productName}  x  ${data.productQuantity}"
        holder.binding.textView7.text="₹${data.productSp}"

        holder.itemView.setOnClickListener {
            val intent =Intent(context,ProductDetailActivity::class.java)
            intent.putExtra("id",data.productId)
            context.startActivity(intent)
        }

        val dao = AppDatabase.getInstance(context).productDao()

        holder.binding.imageView5.setOnClickListener {
            // Use a proper scope instead of GlobalScope
            CoroutineScope(Dispatchers.IO).launch {
                dao.deleteProduct(data)
            }
        }
    }
}
