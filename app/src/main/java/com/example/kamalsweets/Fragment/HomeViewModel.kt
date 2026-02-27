package com.example.kamalsweets.Fragment

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.kamalsweets.Model.AddProductModel
import com.example.kamalsweets.Model.CategoryModel
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class HomeViewModel : ViewModel() {
    private val _products = MutableLiveData<ArrayList<AddProductModel>>()
    val products: LiveData<ArrayList<AddProductModel>> = _products

    private val _categories = MutableLiveData<ArrayList<CategoryModel>>()
    val categories: LiveData<ArrayList<CategoryModel>> = _categories

    fun fetchProductsIfNeeded() {
        if (_products.value != null) return
        
        Firebase.firestore.collection("products")
            .get().addOnSuccessListener {
                val list = ArrayList<AddProductModel>()
                for (doc in it.documents) {
                    val data = doc.toObject(AddProductModel::class.java)
                    if (data != null) list.add(data)
                }
                list.shuffle()
                _products.value = list
            }
    }

    fun fetchCategoriesIfNeeded() {
        if (_categories.value != null) return
        
        Firebase.firestore.collection("categories")
            .get().addOnSuccessListener {
                val list = ArrayList<CategoryModel>()
                for (doc in it.documents) {
                    val data = doc.toObject(CategoryModel::class.java)
                    if (data != null) list.add(data)
                }
                list.shuffle()
                _categories.value = list
            }
    }
}
