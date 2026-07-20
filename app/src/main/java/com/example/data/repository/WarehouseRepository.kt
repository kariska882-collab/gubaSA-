package com.example.data.repository

import com.example.data.dao.WarehouseDao
import com.example.data.entity.RawMaterial
import com.example.data.entity.StockTransaction
import com.example.data.entity.Supplier
import com.example.security.EncryptionManager
import kotlinx.coroutines.flow.Flow

class WarehouseRepository(private val dao: WarehouseDao) {

    val allMaterials: Flow<List<RawMaterial>> = dao.getAllMaterials()
    val lowStockMaterials: Flow<List<RawMaterial>> = dao.getLowStockMaterials()
    val allTransactions: Flow<List<StockTransaction>> = dao.getAllTransactions()
    val allSuppliers: Flow<List<Supplier>> = dao.getAllSuppliers()

    suspend fun getMaterialById(id: Int): RawMaterial? {
        return dao.getMaterialById(id)
    }

    fun getMaterialFlowById(id: Int): Flow<RawMaterial?> {
        return dao.getMaterialFlowById(id)
    }

    suspend fun insertMaterial(
        sku: String,
        name: String,
        category: String,
        quantity: Double,
        unit: String,
        minThreshold: Double,
        unitPrice: Double,
        supplierName: String,
        supplierContact: String
    ): Long {
        val encPrice = EncryptionManager.encrypt(unitPrice.toString())
        val encSupName = EncryptionManager.encrypt(supplierName)
        val encSupContact = EncryptionManager.encrypt(supplierContact)

        val material = RawMaterial(
            sku = sku,
            name = name,
            category = category,
            quantity = quantity,
            unit = unit,
            minThreshold = minThreshold,
            encryptedUnitPrice = encPrice,
            encryptedSupplierName = encSupName,
            encryptedSupplierContact = encSupContact,
            lastUpdated = System.currentTimeMillis()
        )
        return dao.insertMaterial(material)
    }

    suspend fun updateMaterial(
        id: Int,
        sku: String,
        name: String,
        category: String,
        quantity: Double,
        unit: String,
        minThreshold: Double,
        unitPrice: Double,
        supplierName: String,
        supplierContact: String
    ) {
        val encPrice = EncryptionManager.encrypt(unitPrice.toString())
        val encSupName = EncryptionManager.encrypt(supplierName)
        val encSupContact = EncryptionManager.encrypt(supplierContact)

        val material = RawMaterial(
            id = id,
            sku = sku,
            name = name,
            category = category,
            quantity = quantity,
            unit = unit,
            minThreshold = minThreshold,
            encryptedUnitPrice = encPrice,
            encryptedSupplierName = encSupName,
            encryptedSupplierContact = encSupContact,
            lastUpdated = System.currentTimeMillis()
        )
        dao.updateMaterial(material)
    }

    suspend fun deleteMaterial(id: Int) {
        dao.deleteMaterialById(id)
    }

    suspend fun addTransaction(
        materialId: Int,
        type: String, // "IN" or "OUT"
        quantity: Double,
        notes: String
    ): Boolean {
        val material = dao.getMaterialById(materialId) ?: return false

        // Determine new quantity
        val newQuantity = if (type == "IN") {
            material.quantity + quantity
        } else {
            val resulting = material.quantity - quantity
            if (resulting < 0) return false // Prevent negative stock
            resulting
        }

        // Decrypt unit price to calculate cost
        val rawPriceStr = EncryptionManager.decrypt(material.encryptedUnitPrice)
        val unitPrice = rawPriceStr.toDoubleOrNull() ?: 0.0
        val costValue = unitPrice * quantity
        val encCost = EncryptionManager.encrypt(costValue.toString())

        // Save Transaction
        val transaction = StockTransaction(
            materialId = materialId,
            materialName = material.name,
            type = type,
            quantity = quantity,
            unit = material.unit,
            notes = notes,
            encryptedCost = encCost,
            timestamp = System.currentTimeMillis()
        )
        dao.insertTransaction(transaction)

        // Update Raw Material stock level
        val updatedMaterial = material.copy(
            quantity = newQuantity,
            lastUpdated = System.currentTimeMillis()
        )
        dao.updateMaterial(updatedMaterial)
        return true
    }

    suspend fun deleteTransaction(transactionId: Int) {
        dao.deleteTransactionById(transactionId)
    }

    fun getTransactionsForMaterial(materialId: Int): Flow<List<StockTransaction>> {
        return dao.getTransactionsForMaterial(materialId)
    }

    // --- Supplier CRUD ---
    suspend fun getSupplierById(id: Int): Supplier? {
        return dao.getSupplierById(id)
    }

    suspend fun insertSupplier(name: String, contact: String, email: String, address: String): Long {
        val supplier = Supplier(
            name = name,
            contact = contact,
            email = email,
            address = address,
            lastUpdated = System.currentTimeMillis()
        )
        return dao.insertSupplier(supplier)
    }

    suspend fun updateSupplier(id: Int, name: String, contact: String, email: String, address: String) {
        val supplier = Supplier(
            id = id,
            name = name,
            contact = contact,
            email = email,
            address = address,
            lastUpdated = System.currentTimeMillis()
        )
        dao.updateSupplier(supplier)
    }

    suspend fun deleteSupplier(id: Int) {
        dao.deleteSupplierById(id)
    }
}
