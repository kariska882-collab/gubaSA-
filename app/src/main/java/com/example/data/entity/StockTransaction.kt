package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "stock_transactions")
data class StockTransaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val materialId: Int,
    val materialName: String,
    val type: String, // "IN" or "OUT"
    val quantity: Double,
    val unit: String,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String,
    val encryptedCost: String
)
