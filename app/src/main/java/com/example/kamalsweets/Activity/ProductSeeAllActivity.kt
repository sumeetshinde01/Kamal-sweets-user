package com.example.kamalsweets.Activity

import android.os.Bundle
import android.view.View
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView.OnQueryTextListener
import com.example.kamalsweets.Adapter.SeeAllProductAdapter
import com.example.kamalsweets.Model.AddProductModel

import com.example.kamalsweets.databinding.ActivityProductSeeAllBinding
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import java.util.Locale

class ProductSeeAllActivity : AppCompatActivity() {


    private lateinit var binding: ActivityProductSeeAllBinding
    var list:ArrayList<AddProductModel> = ArrayList()
    private lateinit var adapter:SeeAllProductAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProductSeeAllBinding.inflate(layoutInflater)
        setContentView(binding.root)

        adapter = SeeAllProductAdapter(this, list)
        binding.productRecycleView.adapter = adapter

        // Start shimmer
        binding.shimmerViewContainer.startShimmer()
        binding.shimmerViewContainer.visibility = View.VISIBLE
        binding.productRecycleView.visibility = View.GONE

        getProducts()
        binding.searchView.setOnQueryTextListener(object : OnQueryTextListener {
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
        if(query != null){
            var filteredList = ArrayList<AddProductModel>()
            for (i in list){
                if(i.productName?.lowercase(Locale.ROOT)?.contains(query.lowercase(Locale.ROOT))==true){
                    filteredList.add((i))
                }
            }
            if(filteredList.isEmpty()){
                Toast.makeText(this, "No Data Found", Toast.LENGTH_SHORT).show()
            }else{
                adapter.setFilteredList(filteredList)
            }
        }
    }

    private fun getProducts() {
        Firebase.firestore.collection("products")
            .get().addOnSuccessListener {
                list.clear()
                for(doc in it.documents){
                    val data=doc.toObject(AddProductModel::class.java)
                    if (data != null) {
                        data.productID = doc.id
                        list.add(data)
                    }
                }
                list.shuffle()
                
                // Stop and hide shimmer
                binding.shimmerViewContainer.stopShimmer()
                binding.shimmerViewContainer.visibility = View.GONE
                binding.productRecycleView.visibility = View.VISIBLE
                
                adapter.notifyDataSetChanged()
            }.addOnFailureListener { error->
                binding.shimmerViewContainer.stopShimmer()
                binding.shimmerViewContainer.visibility = View.GONE
                binding.productRecycleView.visibility = View.VISIBLE
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