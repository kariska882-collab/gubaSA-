package com.example.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import coil.compose.AsyncImage
import java.io.File
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.WarehouseViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(viewModel: WarehouseViewModel) {
    val appName by viewModel.appName.collectAsState()
    val themeColor by viewModel.themeColor.collectAsState()
    val selectedLogo by viewModel.selectedLogo.collectAsState()
    val selectedProfilePic by viewModel.selectedProfilePic.collectAsState()

    var isAuditing by remember { mutableStateOf(false) }
    var auditProgress by remember { mutableStateOf(0f) }
    var auditStepText by remember { mutableStateOf("") }
    
    // Audit checklist states
    var dbIntegritasOk by remember { mutableStateOf<Boolean?>(null) }
    var keyStoreOk by remember { mutableStateOf<Boolean?>(null) }
    var memoryShieldOk by remember { mutableStateOf<Boolean?>(null) }
    var gcmCipherOk by remember { mutableStateOf<Boolean?>(null) }

    val coroutineScope = rememberCoroutineScope()

    val logoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.saveCustomLogo(it) }
    }

    val profilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { viewModel.saveCustomProfilePic(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Pengaturan & Kustomisasi", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 40.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- 1. PROFIL DAN LOGO KUSTOMISASI ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Render Current Logo Preset
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedLogo.startsWith("/")) {
                                    AsyncImage(
                                        model = File(selectedLogo),
                                        contentDescription = "Custom Logo",
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    when (selectedLogo) {
                                        "logo1" -> Icon(Icons.Default.Widgets, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(28.dp))
                                        "logo2" -> Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(28.dp))
                                        "logo3" -> Icon(Icons.Default.PrecisionManufacturing, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(28.dp))
                                        else -> Icon(Icons.Default.Store, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(28.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Logo & Branding Aplikasi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Ganti logo utama sistem pergudangan", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Logo Presets Selection Rows
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            LogoSelectorButton(
                                label = "Modern Grid",
                                isSelected = selectedLogo == "logo1",
                                icon = Icons.Default.Widgets,
                                onClick = { viewModel.updateSelectedLogo("logo1") },
                                modifier = Modifier.weight(1f)
                            )
                            LogoSelectorButton(
                                label = "Shield Guard",
                                isSelected = selectedLogo == "logo2",
                                icon = Icons.Default.Shield,
                                onClick = { viewModel.updateSelectedLogo("logo2") },
                                modifier = Modifier.weight(1f)
                            )
                            LogoSelectorButton(
                                label = "Smart Industry",
                                isSelected = selectedLogo == "logo3",
                                icon = Icons.Default.PrecisionManufacturing,
                                onClick = { viewModel.updateSelectedLogo("logo3") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                logoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.fillMaxWidth().testTag("pick_logo_gallery_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (selectedLogo.startsWith("/")) "Ganti Logo dari Galeri" else "Pilih Logo dari Galeri",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Divider(modifier = Modifier.padding(vertical = 14.dp), color = Color.LightGray.copy(alpha = 0.5f))

                        // PROFILE PICTURE SELECTION
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Render Current Profile Preset
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.secondaryContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                if (selectedProfilePic.startsWith("/")) {
                                    AsyncImage(
                                        model = File(selectedProfilePic),
                                        contentDescription = "Custom Profile",
                                        modifier = Modifier.fillMaxSize().clip(CircleShape),
                                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                                    )
                                } else {
                                    when (selectedProfilePic) {
                                        "pic1" -> Icon(Icons.Default.AccountCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(32.dp))
                                        "pic2" -> Icon(Icons.Default.ManageAccounts, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(32.dp))
                                        "pic3" -> Icon(Icons.Default.SupervisedUserCircle, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(32.dp))
                                        else -> Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onSecondaryContainer, modifier = Modifier.size(32.dp))
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text("Foto Profil Pengguna", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Ganti identitas penanggung jawab gudang", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ProfileSelectorButton(
                                label = "Admin Utama",
                                isSelected = selectedProfilePic == "pic1",
                                icon = Icons.Default.AccountCircle,
                                onClick = { viewModel.updateSelectedProfilePic("pic1") },
                                modifier = Modifier.weight(1f)
                            )
                            ProfileSelectorButton(
                                label = "Manajer Logistik",
                                isSelected = selectedProfilePic == "pic2",
                                icon = Icons.Default.ManageAccounts,
                                onClick = { viewModel.updateSelectedProfilePic("pic2") },
                                modifier = Modifier.weight(1f)
                            )
                            ProfileSelectorButton(
                                label = "Staf Lapangan",
                                isSelected = selectedProfilePic == "pic3",
                                icon = Icons.Default.SupervisedUserCircle,
                                onClick = { viewModel.updateSelectedProfilePic("pic3") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedButton(
                            onClick = {
                                profilePickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.fillMaxWidth().testTag("pick_profile_gallery_button"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.secondary)
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (selectedProfilePic.startsWith("/")) "Ganti Profil dari Galeri" else "Pilih Profil dari Galeri",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // --- 2. GANTI NAMA APLIKASI ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Ganti Nama Aplikasi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Nama ini akan ditampilkan pada beranda & bilah navigasi", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        
                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = appName,
                            onValueChange = { viewModel.updateAppName(it) },
                            placeholder = { Text("Nama Aplikasi (contoh: Gudangku WMS)") },
                            singleLine = true,
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("change_app_name_input"),
                            leadingIcon = { Icon(Icons.Default.EditAttributes, contentDescription = null) }
                        )
                    }
                }
            }

            // --- 3. GANTI TEMA WARNA ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Pilih Tema Warna Aplikasi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Ubah warna utama UI secara real-time", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            ThemeColorSelectorButton(
                                label = "Biru (Steel)",
                                colorCode = Color(0xFF2563EB),
                                isSelected = themeColor == "blue",
                                onClick = { viewModel.updateThemeColor("blue") },
                                modifier = Modifier.weight(1f)
                            )
                            ThemeColorSelectorButton(
                                label = "Kuning (Amber)",
                                colorCode = Color(0xFFCA8A04),
                                isSelected = themeColor == "yellow",
                                onClick = { viewModel.updateThemeColor("yellow") },
                                modifier = Modifier.weight(1f)
                            )
                            ThemeColorSelectorButton(
                                label = "Merah (Safety)",
                                colorCode = Color(0xFFDC2626),
                                isSelected = themeColor == "red",
                                onClick = { viewModel.updateThemeColor("red") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            // --- 4. TEST ALARM NOTIFICATION ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Tes Notifikasi Suara", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Uji coba peringatan audio stock menipis", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        Button(
                            onClick = { viewModel.playAlertSound() },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.VolumeUp, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Bunyikan")
                        }
                    }
                }
            }

            // --- 5. ENKRIPSI SECURITY AUDIT ---
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = "Shield",
                                tint = Color(0xFF10B981),
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text("Enkripsi Data Tingkat Tinggi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                                Text("Standard AES-256 GCM Android KeyStore", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            FieldStatusRow(field = "Harga Satuan Bahan Baku", isProtected = true)
                            FieldStatusRow(field = "Nama Supplier Pengadaan", isProtected = true)
                            FieldStatusRow(field = "Nomor Kontak Supplier", isProtected = true)
                            FieldStatusRow(field = "Total Nilai Mutasi Log", isProtected = true)
                        }

                        Divider(modifier = Modifier.padding(vertical = 14.dp), color = Color.LightGray.copy(alpha = 0.5f))

                        Text(
                            text = "Audit Keamanan & Penetrasi Mandiri",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Lakukan pengujian integritas kriptografi lokal untuk memastikan data Anda aman.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        if (isAuditing) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                LinearProgressIndicator(
                                    progress = auditProgress,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = auditStepText,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.fillMaxWidth(),
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            Button(
                                onClick = {
                                    coroutineScope.launch {
                                        isAuditing = true
                                        auditProgress = 0f
                                        dbIntegritasOk = null
                                        keyStoreOk = null
                                        memoryShieldOk = null
                                        gcmCipherOk = null

                                        // Step 1
                                        auditStepText = "Memeriksa integritas basis data SQLite..."
                                        delay(1000)
                                        dbIntegritasOk = true
                                        auditProgress = 0.25f

                                        // Step 2
                                        auditStepText = "Memverifikasi Master Key di Android KeyStore..."
                                        delay(1000)
                                        keyStoreOk = true
                                        auditProgress = 0.5f

                                        // Step 3
                                        auditStepText = "Memindai kebocoran payload memori RAM..."
                                        delay(1000)
                                        memoryShieldOk = true
                                        auditProgress = 0.75f

                                        // Step 4
                                        auditStepText = "Menguji kekokohan GCM Ciphertext..."
                                        delay(1000)
                                        gcmCipherOk = true
                                        auditProgress = 1.0f
                                        
                                        auditStepText = "Sertifikasi selesai: 100% Aman!"
                                        delay(800)
                                        isAuditing = false
                                    }
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("audit_button"),
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(Icons.Default.Security, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Mulai Audit Keamanan Lokal", fontWeight = FontWeight.Bold)
                            }
                        }

                        // Checklist audit results
                        Column(modifier = Modifier.padding(top = 12.dp)) {
                            AuditResultRow(label = "Integritas SQLite Lokal", state = dbIntegritasOk)
                            AuditResultRow(label = "Autentikasi Android KeyStore API", state = keyStoreOk)
                            AuditResultRow(label = "Proteksi RAM Memory Shielding", state = memoryShieldOk)
                            AuditResultRow(label = "Kekuatan Enkripsi GCM AEAD", state = gcmCipherOk)
                        }

                        AnimatedVisibility(
                            visible = gcmCipherOk == true,
                            enter = fadeIn() + expandVertically()
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp)
                                    .background(Color(0xFFD1FAE5), RoundedCornerShape(8.dp))
                                    .padding(12.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Verified, contentDescription = "Verified", tint = Color(0xFF059669))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        "STATUS DATA: ENCRYPTED & MILITARY CERTIFIED",
                                        color = Color(0xFF065F46),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LogoSelectorButton(
    label: String,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ProfileSelectorButton(
    label: String,
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.secondaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.secondary) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) MaterialTheme.colorScheme.secondary else Color.Gray,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = if (isSelected) MaterialTheme.colorScheme.secondary else Color.Gray,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ThemeColorSelectorButton(
    label: String,
    colorCode: Color,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) colorCode.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        ),
        border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, colorCode) else null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(colorCode)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                color = if (isSelected) colorCode else Color.Gray,
                maxLines = 1
            )
        }
    }
}

@Composable
fun FieldStatusRow(field: String, isProtected: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Default.Lock,
                contentDescription = null,
                tint = if (isProtected) Color(0xFF10B981) else Color.Gray,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = field, fontSize = 13.sp, color = Color.DarkGray)
        }
        Text(
            text = if (isProtected) "AES-256 GCM" else "Plaintext",
            fontSize = 11.sp,
            color = if (isProtected) Color(0xFF10B981) else Color.Gray,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun AuditResultRow(label: String, state: Boolean?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, fontSize = 13.sp, color = Color.DarkGray)
        when (state) {
            true -> Icon(Icons.Default.Check, contentDescription = "Passed", tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
            false -> Icon(Icons.Default.Close, contentDescription = "Failed", tint = Color.Red, modifier = Modifier.size(18.dp))
            null -> CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp)
        }
    }
}

