package com.example.viewmodel

import android.app.Application
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.WarehouseDatabase
import com.example.data.entity.RawMaterial
import com.example.data.entity.StockTransaction
import com.example.data.entity.Supplier
import com.example.data.repository.WarehouseRepository
import com.example.api.GeminiClient
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WarehouseViewModel(application: Application) : AndroidViewModel(application) {

    val repository: WarehouseRepository
    private val prefs = application.getSharedPreferences("wms_prefs", Context.MODE_PRIVATE)

    init {
        val database = WarehouseDatabase.getDatabase(application)
        repository = WarehouseRepository(database.warehouseDao())
    }

    val allMaterials: StateFlow<List<RawMaterial>> = repository.allMaterials
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val lowStockMaterials: StateFlow<List<RawMaterial>> = repository.lowStockMaterials
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTransactions: StateFlow<List<StockTransaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allSuppliers: StateFlow<List<Supplier>> = repository.allSuppliers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Dynamic Settings & Logo / Profile Pics Customizations ---
    private val _appName = MutableStateFlow(prefs.getString("app_name", "Gudangku WMS") ?: "Gudangku WMS")
    val appName: StateFlow<String> = _appName.asStateFlow()

    private val _themeColor = MutableStateFlow(prefs.getString("theme_color", "blue") ?: "blue")
    val themeColor: StateFlow<String> = _themeColor.asStateFlow()

    private val _selectedLogo = MutableStateFlow(prefs.getString("selected_logo", "logo1") ?: "logo1")
    val selectedLogo: StateFlow<String> = _selectedLogo.asStateFlow()

    private val _selectedProfilePic = MutableStateFlow(prefs.getString("selected_profile_pic", "pic1") ?: "pic1")
    val selectedProfilePic: StateFlow<String> = _selectedProfilePic.asStateFlow()

    private val _isEncryptionMode = MutableStateFlow(false)
    val isEncryptionMode: StateFlow<Boolean> = _isEncryptionMode.asStateFlow()

    private val _aiReport = MutableStateFlow<String?>(null)
    val aiReport: StateFlow<String?> = _aiReport.asStateFlow()

    private val _isGeneratingReport = MutableStateFlow(false)
    val isGeneratingReport: StateFlow<Boolean> = _isGeneratingReport.asStateFlow()

    private val _notificationLogs = MutableStateFlow<List<String>>(emptyList())
    val notificationLogs: StateFlow<List<String>> = _notificationLogs.asStateFlow()

    fun toggleEncryptionMode() {
        _isEncryptionMode.value = !_isEncryptionMode.value
    }

    fun updateAppName(name: String) {
        _appName.value = name
        prefs.edit().putString("app_name", name).apply()
    }

    fun updateThemeColor(color: String) {
        _themeColor.value = color
        prefs.edit().putString("theme_color", color).apply()
    }

    fun updateSelectedLogo(logo: String) {
        _selectedLogo.value = logo
        prefs.edit().putString("selected_logo", logo).apply()
    }

    fun updateSelectedProfilePic(pic: String) {
        _selectedProfilePic.value = pic
        prefs.edit().putString("selected_profile_pic", pic).apply()
    }

    fun saveCustomLogo(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val resolver = context.contentResolver
                val inputStream = resolver.openInputStream(uri)
                if (inputStream != null) {
                    val file = File(context.filesDir, "custom_logo_${System.currentTimeMillis()}.png")
                    val outputStream = FileOutputStream(file)
                    inputStream.use { input ->
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }
                    updateSelectedLogo(file.absolutePath)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun saveCustomProfilePic(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val context = getApplication<Application>()
                val resolver = context.contentResolver
                val inputStream = resolver.openInputStream(uri)
                if (inputStream != null) {
                    val file = File(context.filesDir, "custom_profile_${System.currentTimeMillis()}.png")
                    val outputStream = FileOutputStream(file)
                    inputStream.use { input ->
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }
                    updateSelectedProfilePic(file.absolutePath)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playAlertSound() {
        try {
            val toneGen = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100)
            toneGen.startTone(ToneGenerator.TONE_PROP_BEEP, 300)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun seedDataIfNeeded() {
        viewModelScope.launch {
            // Seed suppliers if empty
            val anySupplier = repository.getSupplierById(1)
            if (anySupplier == null) {
                repository.insertSupplier("PT Krakatau Steel Tbk", "+6221-500-123", "sales@krakatausteel.com", "Cilegon, Banten")
                repository.insertSupplier("PT Chandra Asri Petrochemical", "info@chandra-asri.com", "contact@chandraasri.com", "Cilegon, Banten")
                repository.insertSupplier("Kimia Farma Industri", "+62812345678", "info@kimiafarma.co.id", "Jakarta Barat, DKI Jakarta")
                repository.insertSupplier("PT Sritex Sritrust", "procurement@sritex.co.id", "info@sritex.co.id", "Sukoharjo, Jawa Tengah")
                repository.insertSupplier("PT Inalum Persero", "contact@inalum.id", "sales@inalum.id", "Kuala Tanjung, Sumatera Utara")
            }

            // Seed materials if empty
            val existing = repository.getMaterialById(1)
            if (existing == null) {
                // Seed 5 initial diverse raw materials
                val id1 = repository.insertMaterial(
                    sku = "SL-STEEL-01",
                    name = "Baja Lembaran 2mm",
                    category = "Logam",
                    quantity = 150.0,
                    unit = "Lembar",
                    minThreshold = 50.0,
                    unitPrice = 120000.0,
                    supplierName = "PT Krakatau Steel Tbk",
                    supplierContact = "+6221-500-123"
                )
                val id2 = repository.insertMaterial(
                    sku = "PL-ABS-09",
                    name = "Pelet Plastik ABS",
                    category = "Plastik",
                    quantity = 25.0, // Low stock! Threshold is 100
                    unit = "kg",
                    minThreshold = 100.0,
                    unitPrice = 45000.0,
                    supplierName = "PT Chandra Asri Petrochemical",
                    supplierContact = "info@chandra-asri.com"
                )
                val id3 = repository.insertMaterial(
                    sku = "CH-SULF-05",
                    name = "Asam Sulfat 98%",
                    category = "Kimia",
                    quantity = 80.0,
                    unit = "Liter",
                    minThreshold = 30.0,
                    unitPrice = 85000.0,
                    supplierName = "Kimia Farma Industri",
                    supplierContact = "+62812345678"
                )
                val id4 = repository.insertMaterial(
                    sku = "TX-COT-12",
                    name = "Serat Kapas Organik",
                    category = "Tekstil",
                    quantity = 500.0,
                    unit = "kg",
                    minThreshold = 200.0,
                    unitPrice = 35000.0,
                    supplierName = "PT Sritex Sritrust",
                    supplierContact = "procurement@sritex.co.id"
                )
                val id5 = repository.insertMaterial(
                    sku = "AL-ING-03",
                    name = "Aluminium Ingot",
                    category = "Logam",
                    quantity = 12.0, // Low stock! Threshold is 25
                    unit = "Batang",
                    minThreshold = 25.0,
                    unitPrice = 320000.0,
                    supplierName = "PT Inalum Persero",
                    supplierContact = "contact@inalum.id"
                )

                // Add initial transaction history logs
                repository.addTransaction(id1.toInt(), "IN", 150.0, "Penerimaan batch awal pengadaan Q3")
                repository.addTransaction(id2.toInt(), "IN", 25.0, "Stok awal dari supplier")
                repository.addTransaction(id3.toInt(), "IN", 80.0, "Pengiriman drum kimia")
                repository.addTransaction(id4.toInt(), "IN", 500.0, "Impor serat kapas organik")
                repository.addTransaction(id5.toInt(), "IN", 12.0, "Stok awal aluminium logam")
            }
            updateNotifications()
        }
    }

    fun updateNotifications() {
        viewModelScope.launch {
            val list = allMaterials.value
            val lowStock = list.filter { it.quantity <= it.minThreshold }
            val alerts = lowStock.map { 
                "PERINGATAN: Stok '${it.name}' kritis! Sisa ${it.quantity} ${it.unit} (Minimum: ${it.minThreshold} ${it.unit})" 
            }
            
            // If new low stock alerts are added or verified, sound the alert chime!
            if (lowStock.isNotEmpty() && alerts.size != _notificationLogs.value.size) {
                playAlertSound()
            }
            
            _notificationLogs.value = alerts
        }
    }

    fun addMaterial(
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
        viewModelScope.launch {
            repository.insertMaterial(
                sku = sku,
                name = name,
                category = category,
                quantity = quantity,
                unit = unit,
                minThreshold = minThreshold,
                unitPrice = unitPrice,
                supplierName = supplierName,
                supplierContact = supplierContact
            )
            updateNotifications()
        }
    }

    fun updateMaterial(
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
        viewModelScope.launch {
            repository.updateMaterial(
                id = id,
                sku = sku,
                name = name,
                category = category,
                quantity = quantity,
                unit = unit,
                minThreshold = minThreshold,
                unitPrice = unitPrice,
                supplierName = supplierName,
                supplierContact = supplierContact
            )
            updateNotifications()
        }
    }

    fun deleteMaterial(id: Int) {
        viewModelScope.launch {
            repository.deleteMaterial(id)
            updateNotifications()
        }
    }

    fun addTransaction(
        materialId: Int,
        type: String,
        quantity: Double,
        notes: String,
        onComplete: (Boolean) -> Unit = {}
    ) {
        viewModelScope.launch {
            val success = repository.addTransaction(materialId, type, quantity, notes)
            if (success) {
                updateNotifications()
                
                // Sound the warning check dynamically on transaction completion if resulting stock is low
                val mat = repository.getMaterialById(materialId)
                if (mat != null && mat.quantity <= mat.minThreshold) {
                    playAlertSound()
                }
            }
            onComplete(success)
        }
    }

    fun generateAiReport() {
        viewModelScope.launch {
            _isGeneratingReport.value = true
            val report = GeminiClient.generateReport(allMaterials.value, allTransactions.value)
            _aiReport.value = report
            _isGeneratingReport.value = false
        }
    }

    // --- Supplier CRUD implementation ---
    fun addSupplier(name: String, contact: String, email: String, address: String) {
        viewModelScope.launch {
            repository.insertSupplier(name, contact, email, address)
        }
    }

    fun updateSupplier(id: Int, name: String, contact: String, email: String, address: String) {
        viewModelScope.launch {
            repository.updateSupplier(id, name, contact, email, address)
        }
    }

    fun deleteSupplier(id: Int) {
        viewModelScope.launch {
            repository.deleteSupplier(id)
        }
    }
}
