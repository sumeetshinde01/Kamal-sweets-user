package com.example.kamalsweets.Roomdb

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.kamalsweets.Room.productDao

@Database(entities = [ProductModel::class, OrderHistoryModel::class], version = 2)
abstract class AppDatabase : RoomDatabase() {

    companion object {
        private var database: AppDatabase? = null
        private val DATABASE_NAME = "kamal sweets"

        @Synchronized
        fun getInstance(context: Context): AppDatabase {
            if (database == null) {
                database = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                ).allowMainThreadQueries()
                    .fallbackToDestructiveMigration()
                    .build()
            }
            return database!!
        }
    }
    abstract fun productDao(): productDao
    abstract fun orderHistoryDao(): OrderHistoryDao
}
