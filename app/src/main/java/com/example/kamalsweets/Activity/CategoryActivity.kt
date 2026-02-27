package com.example.kamalsweets.Activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.kamalsweets.Adapter.CategoryProductAdapter
import com.example.kamalsweets.Model.AddProductModel
import com.example.kamalsweets.databinding.ActivityCategoryBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Locale

class CategoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoryBinding
    private var list = ArrayList<AddProductModel>()
    private lateinit var adapter: CategoryProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val categoryName = intent.getStringExtra("cat")
        binding.categoryTitle.text = categoryName ?: "Category Products"

        // Adapter and LayoutManager setup
        adapter = CategoryProductAdapter(this, list)
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        binding.backBtn.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Start shimmer
        binding.shimmerViewContainer.startShimmer()
        binding.shimmerViewContainer.visibility = View.VISIBLE
        binding.recyclerView.visibility = View.GONE

        getProducts(categoryName)

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
        val filteredList = ArrayList<AddProductModel>()
        if (query != null) {
            for (i in list) {
                if (i.productName?.lowercase(Locale.ROOT)?.contains(query.lowercase(Locale.ROOT)) == true) {
                    filteredList.add(i)
                }
            }

            adapter.setFilteredList(filteredList)

            if (filteredList.isEmpty()) {
                Toast.makeText(this, "No Data Found", Toast.LENGTH_SHORT).show()
            }
        } else {
            adapter.setFilteredList(list) // Restore original list if query is null
        }
    }

    private fun getProducts(category: String?) {
        Firebase.firestore.collection("products").whereEqualTo("productCategory", category)
            .get().addOnSuccessListener {
                list.clear()
                for (doc in it.documents) {
                    val data = doc.toObject(AddProductModel::class.java)
                    if (data != null) {
                        // If productID is empty in the document fields, use the Firestore Document ID
                        if (data.productID.isNullOrEmpty()) {
                            data.productID = doc.id
                        }
                        list.add(data)
                    }
                }
                
                // Stop and hide shimmer
                binding.shimmerViewContainer.stopShimmer()
                binding.shimmerViewContainer.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                
                adapter.notifyDataSetChanged()
            }.addOnFailureListener { error ->
                // Stop and hide shimmer even on failure
                binding.shimmerViewContainer.stopShimmer()
                binding.shimmerViewContainer.visibility = View.GONE
                binding.recyclerView.visibility = View.VISIBLE
                
                Toast.makeText(this, "$error", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        if (binding.shimmerViewContainer.visibility == View.VISIBLE) {
            binding.shimmerViewContainer.startShimmer()
        }
    }

    override fun onPause() {
        binding.shimmerViewContainer.stopShimmer()
        super.onPause()
    }
}
