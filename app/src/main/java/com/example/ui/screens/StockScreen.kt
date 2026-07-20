package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.RawMaterial
import com.example.security.EncryptionManager
import com.example.viewmodel.WarehouseViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StockScreen(viewModel: WarehouseViewModel) {
    val materials by viewModel.allMaterials.collectAsState()
    val isEncryptionMode by viewModel.isEncryptionMode.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("Semua") }

    // Dialog trigger states
    var showAddDialog by remember { mutableStateOf(false) }
    var editingMaterial by remember { mutableStateOf<RawMaterial?>(null) }

    // Available Categories
    val categories = listOf("Semua", "Logam", "Plastik", "Kimia", "Tekstil", "Lainnya")

    // Filtered materials
    val filteredMaterials = remember(materials, searchQuery, selectedCategory) {
        materials.filter { m ->
            val matchesSearch = m.name.contains(searchQuery, ignoreCase = true) || m.sku.contains(searchQuery, ignoreCase = true)
            val matchesCategory = selectedCategory == "Semua" || m.category == selectedCategory
            matchesSearch && matchesCategory
        }
    }

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Header + Encryption Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Stok Bahan Baku",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Encryption toggle button
                    FilledTonalButton(
                        onClick = { viewModel.toggleEncryptionMode() },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (isEncryptionMode) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.testTag("encryption_toggle_button"),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(
                            imageVector = if (isEncryptionMode) Icons.Outlined.Lock else Icons.Outlined.LockOpen,
                            contentDescription = "Toggle Encryption View",
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isEncryptionMode) "Mode Ciphertext" else "Mode Terdekripsi",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(8.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Cari SKU atau nama bahan baku...", fontSize = 14.sp) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Cari") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Reset")
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("search_bar"),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Category Selection Chips
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(categories) { category ->
                        FilterChip(
                            selected = selectedCategory == category,
                            onClick = { selectedCategory = category },
                            label = { Text(category, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = Color.White
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("add_material_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Bahan Baku")
            }
        }
    ) { innerPadding ->
        if (filteredMaterials.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Inbox,
                        contentDescription = "Empty",
                        tint = Color.LightGray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Tidak ada bahan baku ditemukan",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // --- LOW STOCK ALERT RATIO DIAGRAM ---
                val lowStockList = materials.filter { it.quantity <= it.minThreshold }
                if (lowStockList.isNotEmpty()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.9f)),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Rasio Ambang Batas Kritis (${lowStockList.size} Item)",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onErrorContainer
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                
                                lowStockList.forEach { item ->
                                    val ratio = if (item.minThreshold > 0) (item.quantity / item.minThreshold).toFloat().coerceIn(0f, 1f) else 0f
                                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = item.name,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                            Text(
                                                text = "${item.quantity} / ${item.minThreshold} ${item.unit} (${(ratio * 100).toInt()}%)",
                                                fontSize = 11.sp,
                                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = ratio,
                                            color = MaterialTheme.colorScheme.error,
                                            trackColor = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.15f),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(8.dp)
                                                .clip(RoundedCornerShape(4.dp))
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                items(filteredMaterials) { material ->
                    MaterialCard(
                        material = material,
                        isEncryptionActive = isEncryptionMode,
                        onEdit = { editingMaterial = material },
                        onDelete = { viewModel.deleteMaterial(material.id) }
                    )
                }
            }
        }

        // --- ADD MATERIAL DIALOG ---
        if (showAddDialog) {
            MaterialFormDialog(
                title = "Tambah Bahan Baku Baru",
                onDismiss = { showAddDialog = false },
                onSave = { sku, name, cat, qty, unit, min, price, sName, sContact ->
                    viewModel.addMaterial(sku, name, cat, qty, unit, min, price, sName, sContact)
                    showAddDialog = false
                }
            )
        }

        // --- EDIT MATERIAL DIALOG ---
        editingMaterial?.let { material ->
            MaterialFormDialog(
                title = "Edit Bahan Baku",
                material = material,
                onDismiss = { editingMaterial = null },
                onSave = { sku, name, cat, qty, unit, min, price, sName, sContact ->
                    viewModel.updateMaterial(material.id, sku, name, cat, qty, unit, min, price, sName, sContact)
                    editingMaterial = null
                }
            )
        }
    }
}

@Composable
fun MaterialCard(
    material: RawMaterial,
    isEncryptionActive: Boolean,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    val idrFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
            maximumFractionDigits = 0
        }
    }

    val isLowStock = material.quantity <= material.minThreshold

    // Dynamically encrypt or decrypt depending on the active mode toggle
    val unitPriceStr = remember(material.encryptedUnitPrice, isEncryptionActive) {
        if (isEncryptionActive) {
            material.encryptedUnitPrice
        } else {
            val decrypted = EncryptionManager.decrypt(material.encryptedUnitPrice)
            decrypted.toDoubleOrNull()?.let { idrFormatter.format(it) } ?: "Rp 0"
        }
    }

    val supplierNameStr = remember(material.encryptedSupplierName, isEncryptionActive) {
        if (isEncryptionActive) material.encryptedSupplierName else EncryptionManager.decrypt(material.encryptedSupplierName)
    }

    val supplierContactStr = remember(material.encryptedSupplierContact, isEncryptionActive) {
        if (isEncryptionActive) material.encryptedSupplierContact else EncryptionManager.decrypt(material.encryptedSupplierContact)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: SKU and Category Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = material.sku,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primaryContainer, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                
                Text(
                    text = material.category,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.DarkGray,
                    modifier = Modifier
                        .background(Color.LightGray.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Material Name
            Text(
                text = material.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Divider
            Divider(color = Color.LightGray.copy(alpha = 0.3f))

            Spacer(modifier = Modifier.height(10.dp))

            // Row containing Stock quantities, threshold and pricing
            Row(modifier = Modifier.fillMaxWidth()) {
                // Stock Info
                Column(modifier = Modifier.weight(1f)) {
                    Text("Jumlah Stok", fontSize = 11.sp, color = Color.Gray)
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "${material.quantity} ${material.unit}",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLowStock) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                        )
                        if (isLowStock) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.Default.Warning,
                                contentDescription = "Kritis",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Text(
                        text = "Batas minimum: ${material.minThreshold} ${material.unit}",
                        fontSize = 10.sp,
                        color = if (isLowStock) MaterialTheme.colorScheme.error else Color.Gray
                    )
                }

                // Pricing Info
                Column(modifier = Modifier.weight(1.2f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Harga Satuan", fontSize = 11.sp, color = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = if (isEncryptionActive) Icons.Default.Lock else Icons.Default.LockOpen,
                            contentDescription = null,
                            tint = if (isEncryptionActive) MaterialTheme.colorScheme.error else Color.Gray,
                            modifier = Modifier.size(11.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = unitPriceStr,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isEncryptionActive) MaterialTheme.colorScheme.error else Color.DarkGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Supplier details box
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Supplier & Hubungan", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                        Spacer(modifier = Modifier.width(4.dp))
                        if (isEncryptionActive) {
                            Icon(Icons.Default.Lock, contentDescription = "Encrypted", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(10.dp))
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Nama: $supplierNameStr",
                        fontSize = 11.sp,
                        color = if (isEncryptionActive) MaterialTheme.colorScheme.error else Color.Black,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Kontak: $supplierContactStr",
                        fontSize = 11.sp,
                        color = if (isEncryptionActive) MaterialTheme.colorScheme.error else Color.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Edit & Delete Actions row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Hapus",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MaterialFormDialog(
    title: String,
    material: RawMaterial? = null,
    onDismiss: () -> Unit,
    onSave: (sku: String, name: String, category: String, quantity: Double, unit: String, minThreshold: Double, unitPrice: Double, supplierName: String, supplierContact: String) -> Unit
) {
    var sku by remember { mutableStateOf(material?.sku ?: "") }
    var name by remember { mutableStateOf(material?.name ?: "") }
    var category by remember { mutableStateOf(material?.category ?: "Logam") }
    var quantityStr by remember { mutableStateOf(material?.quantity?.toString() ?: "0") }
    var unit by remember { mutableStateOf(material?.unit ?: "kg") }
    var minThresholdStr by remember { mutableStateOf(material?.minThreshold?.toString() ?: "10") }
    
    // Decrypt fields if editing
    val initialPrice = remember(material) {
        material?.let {
            EncryptionManager.decrypt(it.encryptedUnitPrice).toDoubleOrNull()?.toString()
        } ?: ""
    }
    val initialSupplier = remember(material) {
        material?.let { EncryptionManager.decrypt(it.encryptedSupplierName) } ?: ""
    }
    val initialContact = remember(material) {
        material?.let { EncryptionManager.decrypt(it.encryptedSupplierContact) } ?: ""
    }

    var unitPriceStr by remember { mutableStateOf(initialPrice) }
    var supplierName by remember { mutableStateOf(initialSupplier) }
    var supplierContact by remember { mutableStateOf(initialContact) }

    var errorText by remember { mutableStateOf("") }

    val categories = listOf("Logam", "Plastik", "Kimia", "Tekstil", "Lainnya")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    if (errorText.isNotEmpty()) {
                        Text(errorText, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
                
                item {
                    OutlinedTextField(
                        value = sku,
                        onValueChange = { sku = it },
                        label = { Text("SKU / Kode Bahan") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Nama Bahan Baku") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    // Category Selection Chips
                    Text("Kategori", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = category == cat,
                                onClick = { category = cat },
                                label = { Text(cat, fontSize = 11.sp) }
                            )
                        }
                    }
                }

                item {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = quantityStr,
                            onValueChange = { quantityStr = it },
                            label = { Text("Stok Awal") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedTextField(
                            value = unit,
                            onValueChange = { unit = it },
                            label = { Text("Satuan (eg: kg, lembar)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }

                item {
                    OutlinedTextField(
                        value = minThresholdStr,
                        onValueChange = { minThresholdStr = it },
                        label = { Text("Batas Minim (Alert Threshold)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = unitPriceStr,
                        onValueChange = { unitPriceStr = it },
                        label = { Text("Harga Satuan (IDR)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                    Text("Detail Pengadaan & Supplier (AES-256 Encrypted)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }

                item {
                    OutlinedTextField(
                        value = supplierName,
                        onValueChange = { supplierName = it },
                        label = { Text("Nama Supplier") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }

                item {
                    OutlinedTextField(
                        value = supplierContact,
                        onValueChange = { supplierContact = it },
                        label = { Text("Kontak Supplier (Phone/Email)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantityStr.toDoubleOrNull()
                    val min = minThresholdStr.toDoubleOrNull()
                    val price = unitPriceStr.toDoubleOrNull()

                    if (sku.isEmpty() || name.isEmpty() || qty == null || min == null || price == null || supplierName.isEmpty()) {
                        errorText = "Lengkapi semua isian dengan benar."
                    } else {
                        onSave(sku, name, category, qty, unit, min, price, supplierName, supplierContact)
                    }
                }
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
