package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.WarehouseDao
import com.example.data.entity.RawMaterial
import com.example.data.entity.StockTransaction
import com.example.data.entity.Supplier

@Database(entities = [RawMaterial::class, StockTransaction::class, Supplier::class], version = 2, exportSchema = false)
abstract class WarehouseDatabase : RoomDatabase() {
    abstract fun warehouseDao(): WarehouseDao

    companion object {
        @Volatile
        private var INSTANCE: WarehouseDatabase? = null

        fun getDatabase(context: Context): WarehouseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    WarehouseDatabase::class.java,
                    "warehouse_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
