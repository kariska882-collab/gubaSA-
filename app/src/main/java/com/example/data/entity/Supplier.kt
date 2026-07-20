package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "suppliers")
data class Supplier(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val contact: String,
    val email: String,
    val address: String,
    val lastUpdated: Long = System.currentTimeMillis()
)
