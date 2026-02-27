package com.example.kamalsweets.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.GridLayoutManager
import com.example.kamalsweets.Adapter.CategoryAdapter
import com.example.kamalsweets.Model.CategoryModel
import com.example.kamalsweets.databinding.ActivityCategorySeeAllBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Locale

class CategorySeeAllActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCategorySeeAllBinding
    private var list = ArrayList<CategoryModel>()
    private lateinit var adapter: CategoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategorySeeAllBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = CategoryAdapter(this)
        binding.categoryRecyclerView.adapter = adapter
        binding.categoryRecyclerView.layoutManager = GridLayoutManager(this, 2)

        binding.backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        getCategories()

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })
    }

    private fun filterList(query: String?) {
        if (query != null) {
            val filteredList = ArrayList<CategoryModel>()
            for (i in list) {
                if (i.cat?.lowercase(Locale.ROOT)?.contains(query.lowercase()) == true) {
                    filteredList.add(i)
                }
            }
            adapter.submitList(filteredList)
        }
    }

    private fun getCategories() {
        Firebase.firestore.collection("categories")
            .get().addOnSuccessListener {
                list.clear()
                for (doc in it.documents) {
                    val data = doc.toObject(CategoryModel::class.java)
                    if (data != null) list.add(data)
                }
                adapter.submitList(list)
            }.addOnFailureListener { error ->
                Toast.makeText(this, "$error", Toast.LENGTH_SHORT).show()
            }
    }
}
