package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.entity.RawMaterial
import com.example.data.entity.StockTransaction
import com.example.data.entity.Supplier
import kotlinx.coroutines.flow.Flow

@Dao
interface WarehouseDao {
    @Query("SELECT * FROM raw_materials ORDER BY name ASC")
    fun getAllMaterials(): Flow<List<RawMaterial>>

    @Query("SELECT * FROM raw_materials WHERE id = :id")
    suspend fun getMaterialById(id: Int): RawMaterial?

    @Query("SELECT * FROM raw_materials WHERE id = :id")
    fun getMaterialFlowById(id: Int): Flow<RawMaterial?>

    @Query("SELECT * FROM raw_materials WHERE quantity <= minThreshold ORDER BY name ASC")
    fun getLowStockMaterials(): Flow<List<RawMaterial>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMaterial(material: RawMaterial): Long

    @Update
    suspend fun updateMaterial(material: RawMaterial)

    @Query("DELETE FROM raw_materials WHERE id = :id")
    suspend fun deleteMaterialById(id: Int)

    @Query("SELECT * FROM stock_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<StockTransaction>>

    @Query("SELECT * FROM stock_transactions WHERE materialId = :materialId ORDER BY timestamp DESC")
    fun getTransactionsForMaterial(materialId: Int): Flow<List<StockTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: StockTransaction): Long

    @Query("DELETE FROM stock_transactions WHERE id = :id")
    suspend fun deleteTransactionById(id: Int)

    // --- Supplier Queries ---
    @Query("SELECT * FROM suppliers ORDER BY name ASC")
    fun getAllSuppliers(): Flow<List<Supplier>>

    @Query("SELECT * FROM suppliers WHERE id = :id")
    suspend fun getSupplierById(id: Int): Supplier?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSupplier(supplier: Supplier): Long

    @Update
    suspend fun updateSupplier(supplier: Supplier)

    @Query("DELETE FROM suppliers WHERE id = :id")
    suspend fun deleteSupplierById(id: Int)
}
