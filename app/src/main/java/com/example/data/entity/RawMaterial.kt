package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "raw_materials")
data class RawMaterial(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val sku: String,
    val name: String,
    val category: String,
    val quantity: Double,
    val unit: String,
    val minThreshold: Double,
    val encryptedUnitPrice: String,
    val encryptedSupplierName: String,
    val encryptedSupplierContact: String,
    val lastUpdated: Long = System.currentTimeMillis()
)
