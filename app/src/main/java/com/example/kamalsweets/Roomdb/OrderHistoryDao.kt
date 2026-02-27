package com.example.kamalsweets.Roomdb

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface OrderHistoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderHistoryModel)

    @Query("SELECT * FROM order_history WHERE userId = :userId ORDER BY timestamp DESC")
    fun getAllOrders(userId: String): LiveData<List<OrderHistoryModel>>
}
