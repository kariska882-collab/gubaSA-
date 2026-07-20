package com.example.ui.screens

import android.content.Context
import android.os.Environment
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.RawMaterial
import com.example.data.entity.StockTransaction
import com.example.security.EncryptionManager
import com.example.viewmodel.WarehouseViewModel
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionScreen(viewModel: WarehouseViewModel) {
    val transactions by viewModel.allTransactions.collectAsState()
    val materials by viewModel.allMaterials.collectAsState()
    val context = LocalContext.current

    var showAddDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var exportedFileName by remember { mutableStateOf("") }
    var showExportSuccess by remember { mutableStateOf(false) }

    fun writeExportFile(fileName: String, content: String) {
        try {
            val directory = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            val file = File(directory, fileName)
            file.writeText(content)
            exportedFileName = file.absolutePath
            showExportSuccess = true
            viewModel.playAlertSound()
        } catch (e: Exception) {
            Toast.makeText(context, "Gagal menulis file: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun exportToExcel() {
        val header = "ID,Nama Bahan Baku,Jenis Mutasi,Jumlah,Satuan,Keterangan,Waktu\n"
        val rows = transactions.joinToString("\n") { tx ->
            val dateStr = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date(tx.timestamp))
            "${tx.id},${tx.materialName},${tx.type},${tx.quantity},${tx.unit},\"${tx.notes}\",${dateStr}"
        }
        writeExportFile("Laporan_Mutasi_Gudang_${System.currentTimeMillis()}.csv", header + rows)
    }

    fun exportToPdfEmulated() {
        // Generates an structured human-readable text-invoice file that represents printable PDF report
        val report = StringBuilder()
        report.append("==================================================\n")
        report.append("          LAPORAN MUTASI KELUAR MASUK BARANG     \n")
        report.append("==================================================\n")
        report.append("Dicetak pada: ${SimpleDateFormat("dd MMMM yyyy HH:mm", Locale("id", "ID")).format(Date())}\n")
        report.append("Total Transaksi: ${transactions.size}\n\n")
        transactions.forEach { tx ->
            val dateStr = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault()).format(Date(tx.timestamp))
            report.append("ID: ${tx.id} | ${tx.materialName} | Tipe: ${tx.type}\n")
            report.append("Jumlah: ${tx.quantity} ${tx.unit} | Keterangan: ${tx.notes}\n")
            report.append("Waktu: ${dateStr}\n")
            report.append("--------------------------------------------------\n")
        }
        writeExportFile("Laporan_Mutasi_Gudang_${System.currentTimeMillis()}.txt", report.toString())
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mutasi & Log Gudang", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
                actions = {
                    if (transactions.isNotEmpty()) {
                        IconButton(onClick = { showExportDialog = true }) {
                            Icon(Icons.Default.FileDownload, contentDescription = "Unduh Laporan")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("add_transaction_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Catat Mutasi")
            }
        }
    ) { innerPadding ->
        if (transactions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "No Logs",
                        tint = Color.LightGray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Belum Ada Riwayat Mutasi",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Silakan tambahkan transaksi masuk atau keluar barang menggunakan tombol + di bawah.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.LightGray,
                        textAlign = TextAlign.Center
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
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(transactions) { tx ->
                    TransactionListItem(tx = tx)
                }
            }
        }

        // --- EXPORT OPTIONS DIALOG ---
        if (showExportDialog) {
            AlertDialog(
                onDismissRequest = { showExportDialog = false },
                title = { Text("Unduh Laporan Mutasi", fontWeight = FontWeight.Bold) },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Pilih format dokumen laporan keluar masuk barang untuk diekspor ke penyimpanan lokal:", fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = {
                                exportToExcel()
                                showExportDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Text("Ekspor ke Excel (Format CSV)")
                        }
                        Button(
                            onClick = {
                                exportToPdfEmulated()
                                showExportDialog = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Text("Ekspor ke PDF (Format Cetak Text)")
                        }
                    }
                },
                confirmButton = {},
                dismissButton = {
                    TextButton(onClick = { showExportDialog = false }) {
                        Text("Batal")
                    }
                }
            )
        }

        // --- EXPORT SUCCESS FEEDBACK DIALOG ---
        if (showExportSuccess) {
            AlertDialog(
                onDismissRequest = { showExportSuccess = false },
                icon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(48.dp)) },
                title = { Text("Laporan Berhasil Diunduh", fontWeight = FontWeight.Bold, textAlign = TextAlign.Center) },
                text = {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Dokumen laporan mutasi gudang telah diekspor dan disimpan dengan aman di perangkat lokal Anda.", fontSize = 12.sp, textAlign = TextAlign.Center)
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Lokasi Berkas:\n$exportedFileName",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(10.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(onClick = { showExportSuccess = false }) {
                        Text("Selesai")
                    }
                }
            )
        }

        // --- ADD MUTATION LOG DIALOG ---
        if (showAddDialog) {
            TransactionFormDialog(
                materials = materials,
                onDismiss = { showAddDialog = false },
                onSave = { materialId, type, qty, notes ->
                    viewModel.addTransaction(materialId, type, qty, notes) { success ->
                        if (success) {
                            showAddDialog = false
                        }
                    }
                }
            )
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFormDialog(
    materials: List<RawMaterial>,
    onDismiss: () -> Unit,
    onSave: (materialId: Int, type: String, quantity: Double, notes: String) -> Unit
) {
    var selectedMaterial by remember { mutableStateOf<RawMaterial?>(null) }
    var type by remember { mutableStateOf("IN") } // "IN" or "OUT"
    var quantityStr by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }

    var isDropdownExpanded by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf("") }

    // Real-time stock safety check for OUT transactions
    val quantity = quantityStr.toDoubleOrNull() ?: 0.0
    val isOverdrawn = remember(selectedMaterial, type, quantity) {
        type == "OUT" && selectedMaterial != null && quantity > selectedMaterial!!.quantity
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Catat Mutasi Stok", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (errorText.isNotEmpty()) {
                    Text(errorText, color = MaterialTheme.colorScheme.error, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                // Material selection dropdown
                Column {
                    Text("Pilih Bahan Baku", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
                            .clickable { isDropdownExpanded = true }
                            .padding(horizontal = 12.dp, vertical = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = selectedMaterial?.let { "${it.sku} - ${it.name} (${it.quantity} ${it.unit})" } ?: "Ketuk untuk memilih...",
                                fontSize = 13.sp,
                                color = if (selectedMaterial == null) Color.Gray else Color.Black
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Dropdown")
                        }

                        DropdownMenu(
                            expanded = isDropdownExpanded,
                            onDismissRequest = { isDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.8f)
                        ) {
                            materials.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text("${m.sku} - ${m.name} (Stok: ${m.quantity} ${m.unit})", fontSize = 12.sp) },
                                    onClick = {
                                        selectedMaterial = m
                                        isDropdownExpanded = false
                                        errorText = ""
                                    }
                                )
                            }
                        }
                    }
                }

                // Transaction Type Tabs (IN vs OUT)
                Column {
                    Text("Jenis Mutasi", fontSize = 11.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilledTonalButton(
                            onClick = { type = "IN"; errorText = "" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (type == "IN") Color(0xFFD1FAE5) else Color.LightGray.copy(alpha = 0.2f),
                                contentColor = if (type == "IN") Color(0xFF065F46) else Color.Gray
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Barang Masuk (IN)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                        FilledTonalButton(
                            onClick = { type = "OUT" },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (type == "OUT") Color(0xFFFEE2E2) else Color.LightGray.copy(alpha = 0.2f),
                                contentColor = if (type == "OUT") Color(0xFF991B1B) else Color.Gray
                            ),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Barang Keluar (OUT)", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Quantity Text Field
                OutlinedTextField(
                    value = quantityStr,
                    onValueChange = { 
                        quantityStr = it
                        errorText = ""
                    },
                    label = { Text("Jumlah Mutasi ${selectedMaterial?.unit?.let { "($it)" } ?: ""}") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    enabled = selectedMaterial != null
                )

                // Safe Threshold Alerts for OUT Mutations
                if (isOverdrawn) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Eror", tint = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Pengeluaran melebihi stok yang tersedia!",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.error,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else if (type == "OUT" && selectedMaterial != null && quantity > 0.0) {
                    val remaining = selectedMaterial!!.quantity - quantity
                    if (remaining <= selectedMaterial!!.minThreshold) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "Perhatian", tint = MaterialTheme.colorScheme.onTertiaryContainer)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    "Perhatian: Stok akan turun menjadi $remaining ${selectedMaterial!!.unit} (Kritis/Batas Minim)!",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Notes Field
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Catatan / Keterangan (eg: PO #102, Produksi A)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qtyVal = quantityStr.toDoubleOrNull()
                    if (selectedMaterial == null) {
                        errorText = "Pilih bahan baku terlebih dahulu."
                    } else if (qtyVal == null || qtyVal <= 0) {
                        errorText = "Masukkan jumlah mutasi yang valid."
                    } else if (isOverdrawn) {
                        errorText = "Gagal: Pengeluaran melebihi stok tersedia."
                    } else {
                        onSave(selectedMaterial!!.id, type, qtyVal, notes)
                    }
                },
                enabled = !isOverdrawn && selectedMaterial != null
            ) {
                Text("Simpan Mutasi")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
