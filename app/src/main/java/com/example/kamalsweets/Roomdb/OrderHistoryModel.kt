package com.example.kamalsweets.Roomdb

import androidx.annotation.NonNull
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "order_history")
data class OrderHistoryModel(
    @PrimaryKey
    @NonNull
    val orderId: String,
    val name: String? = "",
    val userId: String? = "",
    val status: String? = "",
    val productId: String? = "",
    val price: String? = "",
    val userName: String = "",
    val userAddress: String = "",
    val productQuantity: String = "",
    val timestamp: Long = 0L,
    val paymentStatus: String = "",
    val cancelReason: String = "",
    val deliveryPersonName: String = "",
    val deliveryPersonNumber: String = ""
)
