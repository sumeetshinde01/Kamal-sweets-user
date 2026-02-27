package com.example.kamalsweets.Model

import com.google.firebase.firestore.Exclude
import com.google.firebase.firestore.PropertyName

data class AddProductModel (
    @get:PropertyName("productCategory")
    @set:PropertyName("productCategory")
    var productCategory: String? = "",
    
    @get:PropertyName("productCoverImage")
    @set:PropertyName("productCoverImage")
    var productCoverImage: String? = "",
    
    @get:PropertyName("productDescription")
    @set:PropertyName("productDescription")
    var productDescription: String? = "",
    
    @get:PropertyName("productDiscription")
    @set:PropertyName("productDiscription")
    var productDiscription: String? = "",

    // Use Any? to handle both String and ArrayList from Firestore
    @get:PropertyName("productImage")
    @set:PropertyName("productImage")
    var productImage: Any? = null, 
    
    @get:PropertyName("productImages")
    @set:PropertyName("productImages")
    var productImages: ArrayList<String>? = ArrayList(),
    
    @get:PropertyName("productMRP")
    @set:PropertyName("productMRP")
    var productMRP: String? = "",
    
    @get:PropertyName("productName")
    @set:PropertyName("productName")
    var productName: String? = "",
    
    @get:PropertyName("productSp")
    @set:PropertyName("productSp")
    var productSp: String? = "",
    
    @get:PropertyName("productID")
    @set:PropertyName("productID")
    var productID: String? = "",
    
    @get:PropertyName("produductID")
    @set:PropertyName("produductID")
    var produductID: String? = "",
    
    @get:PropertyName("productUnit")
    @set:PropertyName("productUnit")
    var productUnit: String? = "",
    
    @get:PropertyName("stockStatus")
    @set:PropertyName("stockStatus")
    var stockStatus: String? = ""
) {
    /**
     * Helper method to safely get a single product image URL regardless of 
     * whether it's stored as a String or an ArrayList in Firestore.
     */
    @Exclude
    fun getSafeProductImage(): String? {
        return when (val image = productImage) {
            is String -> image
            is List<*> -> if (image.isNotEmpty()) image[0] as? String else null
            else -> productCoverImage
        }
    }

    /**
     * Helper method to safely get the product description from either field name.
     */
    @Exclude
    fun getSafeDescription(): String? {
        return if (!productDescription.isNullOrEmpty()) productDescription else productDiscription
    }

    /**
     * Helper method to safely get the product ID from either field name or Firestore Document ID.
     */
    @Exclude
    fun getSafeId(): String? {
        return when {
            !productID.isNullOrEmpty() -> productID
            !produductID.isNullOrEmpty() -> produductID
            else -> null
        }
    }
}
