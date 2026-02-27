package com.example.kamalsweets.Adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.example.kamalsweets.Activity.CategoryActivity
import com.example.kamalsweets.Model.CategoryModel
import com.example.kamalsweets.R
import com.example.kamalsweets.databinding.LayoutCategoryItemBinding

class CategoryAdapter(private val context: Context) :
    ListAdapter<CategoryModel, CategoryAdapter.CategoryViewHolder>(CategoryDiffCallback()) {

    init {
        setHasStableIds(true)
    }

    inner class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val binding = LayoutCategoryItemBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        return CategoryViewHolder(
            LayoutInflater.from(context).inflate(R.layout.layout_category_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val item = getItem(position)
        holder.binding.textView.text = item.cat
        Glide.with(context)
            .load(item.img)
            .placeholder(R.drawable.kamal_sweets)
            .error(R.drawable.kamal_sweets)
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .into(holder.binding.imageView2)

        holder.itemView.setOnClickListener {
            val intent = Intent(context, CategoryActivity::class.java)
            intent.putExtra("cat", item.cat)
            context.startActivity(intent)
        }
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).cat?.hashCode()?.toLong() ?: position.toLong()
    }

    class CategoryDiffCallback : DiffUtil.ItemCallback<CategoryModel>() {
        override fun areItemsTheSame(oldItem: CategoryModel, newItem: CategoryModel): Boolean {
            return oldItem.cat == newItem.cat
        }

        override fun areContentsTheSame(oldItem: CategoryModel, newItem: CategoryModel): Boolean {
            return oldItem == newItem
        }
    }
}
