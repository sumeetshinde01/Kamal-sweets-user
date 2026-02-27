package com.example.kamalsweets.Model

data class AllOrderModel(
    val name: String? = "",
    val orderId: String? = "",
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