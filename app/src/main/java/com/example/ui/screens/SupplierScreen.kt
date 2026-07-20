package com.example.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.Supplier
import com.example.viewmodel.WarehouseViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierScreen(viewModel: WarehouseViewModel) {
    val suppliers by viewModel.allSuppliers.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    var showFormDialog by remember { mutableStateOf(false) }
    var selectedSupplierForEdit by remember { mutableStateOf<Supplier?>(null) }
    var selectedSupplierForDelete by remember { mutableStateOf<Supplier?>(null) }

    val filteredSuppliers = remember(suppliers, searchQuery) {
        if (searchQuery.isBlank()) {
            suppliers
        } else {
            suppliers.filter {
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.contact.contains(searchQuery, ignoreCase = true) ||
                it.email.contains(searchQuery, ignoreCase = true) ||
                it.address.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kelola Mitra Pemasok", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    selectedSupplierForEdit = null
                    showFormDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                modifier = Modifier.testTag("add_supplier_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Pemasok")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .testTag("supplier_search_bar"),
                placeholder = { Text("Cari nama, kontak, atau lokasi...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Bersihkan")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )

            if (filteredSuppliers.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isEmpty()) "Belum ada pemasok terdaftar" else "Pemasok tidak ditemukan",
                            color = Color.Gray,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(filteredSuppliers, key = { it.id }) { supplier ->
                        SupplierCard(
                            supplier = supplier,
                            onEdit = {
                                selectedSupplierForEdit = supplier
                                showFormDialog = true
                            },
                            onDelete = {
                                selectedSupplierForDelete = supplier
                            }
                        )
                    }
                }
            }
        }

        // Add/Edit Dialog
        if (showFormDialog) {
            SupplierFormDialog(
                supplier = selectedSupplierForEdit,
                onDismiss = { showFormDialog = false },
                onSave = { name, contact, email, address ->
                    if (selectedSupplierForEdit == null) {
                        viewModel.addSupplier(name, contact, email, address)
                    } else {
                        viewModel.updateSupplier(selectedSupplierForEdit!!.id, name, contact, email, address)
                    }
                    showFormDialog = false
                }
            )
        }

        // Delete Confirmation Dialog
        if (selectedSupplierForDelete != null) {
            AlertDialog(
                onDismissRequest = { selectedSupplierForDelete = null },
                title = { Text("Hapus Pemasok") },
                text = { Text("Apakah Anda yakin ingin menghapus pemasok '${selectedSupplierForDelete!!.name}'? Tindakan ini tidak dapat dibatalkan.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteSupplier(selectedSupplierForDelete!!.id)
                            selectedSupplierForDelete = null
                        }
                    ) {
                        Text("Hapus", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { selectedSupplierForDelete = null }) {
                        Text("Batal")
                    }
                }
            )
        }
    }
}

@Composable
fun SupplierCard(
    supplier: Supplier,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("supplier_card_${supplier.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Initial Letter Avatar
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = supplier.name.take(1).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontSize = 18.sp
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = supplier.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Kontak: ${supplier.contact}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray.copy(alpha = 0.5f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Email, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = supplier.email,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Place, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = supplier.address,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SupplierFormDialog(
    supplier: Supplier?,
    onDismiss: () -> Unit,
    onSave: (name: String, contact: String, email: String, address: String) -> Unit
) {
    var name by remember { mutableStateOf(supplier?.name ?: "") }
    var contact by remember { mutableStateOf(supplier?.contact ?: "") }
    var email by remember { mutableStateOf(supplier?.email ?: "") }
    var address by remember { mutableStateOf(supplier?.address ?: "") }

    var nameError by remember { mutableStateOf(false) }
    var contactError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (supplier == null) "Tambah Mitra Pemasok Baru" else "Edit Detail Pemasok") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        nameError = it.isBlank()
                    },
                    label = { Text("Nama Pemasok *") },
                    isError = nameError,
                    supportingText = { if (nameError) Text("Nama wajib diisi") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("supplier_name_input")
                )

                OutlinedTextField(
                    value = contact,
                    onValueChange = {
                        contact = it
                        contactError = it.isBlank()
                    },
                    label = { Text("Nomor Telepon / Kontak *") },
                    isError = contactError,
                    supportingText = { if (contactError) Text("Kontak wajib diisi") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("supplier_contact_input")
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Perusahaan") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("supplier_email_input")
                )

                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Alamat Kantor / Pabrik") },
                    maxLines = 3,
                    modifier = Modifier.fillMaxWidth().testTag("supplier_address_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isBlank() || contact.isBlank()) {
                        nameError = name.isBlank()
                        contactError = contact.isBlank()
                    } else {
                        onSave(name, contact, email, address)
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
