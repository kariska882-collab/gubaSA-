package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.entity.RawMaterial
import com.example.data.entity.StockTransaction
import com.example.security.EncryptionManager
import com.example.viewmodel.WarehouseViewModel
import coil.compose.AsyncImage
import java.io.File
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DashboardScreen(
    viewModel: WarehouseViewModel,
    onNavigateToStock: () -> Unit
) {
    val materials by viewModel.allMaterials.collectAsState()
    val lowStockMaterials by viewModel.lowStockMaterials.collectAsState()
    val transactions by viewModel.allTransactions.collectAsState()
    val notifications by viewModel.notificationLogs.collectAsState()

    val appName by viewModel.appName.collectAsState()
    val selectedLogo by viewModel.selectedLogo.collectAsState()
    val selectedProfilePic by viewModel.selectedProfilePic.collectAsState()

    // Calculate dynamic dashboard stats
    val totalStockValue = remember(materials) {
        materials.sumOf { m ->
            val qty = m.quantity
            val rawPrice = EncryptionManager.decrypt(m.encryptedUnitPrice)
            val price = rawPrice.toDoubleOrNull() ?: 0.0
            qty * price
        }
    }

    val totalItems = materials.size
    val lowStockCount = lowStockMaterials.size
    val totalCategories = remember(materials) {
        materials.map { it.category }.distinct().size
    }

    // Currency Formatter for Rupiah
    val idrFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
            maximumFractionDigits = 0
        }
    }

    // Cloud sync animation state
    val infiniteTransition = rememberInfiniteTransition(label = "cloud")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cloud"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // --- 0. DYNAMIC USER PROFILE GREETING ROW ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Render selected profile avatar
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.secondaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedProfilePic.startsWith("/")) {
                        AsyncImage(
                            model = File(selectedProfilePic),
                            contentDescription = "Profil Kustom",
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
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    val profileName = when (selectedProfilePic) {
                        "pic1" -> "Admin Utama"
                        "pic2" -> "Manajer Logistik"
                        "pic3" -> "Staf Lapangan"
                        else -> if (selectedProfilePic.startsWith("/")) "Admin Kustom" else "Pengguna Gudang"
                    }
                    Text(
                        text = "Selamat Datang,",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                    Text(
                        text = profileName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // --- 1. CLOUD BANNER HEADER ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = appName,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF22C55E).copy(alpha = pulseAlpha))
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Berbasis Cloud • Sinkronisasi Aktif",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                            )
                        }
                    }
                    // Render selected dynamic logo preset
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedLogo.startsWith("/")) {
                            AsyncImage(
                                model = File(selectedLogo),
                                contentDescription = "Logo Kustom",
                                modifier = Modifier.fillMaxSize().clip(CircleShape),
                                contentScale = androidx.compose.ui.layout.ContentScale.Crop
                            )
                        } else {
                            when (selectedLogo) {
                                "logo1" -> Icon(Icons.Default.Widgets, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(24.dp))
                                "logo2" -> Icon(Icons.Default.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(24.dp))
                                "logo3" -> Icon(Icons.Default.PrecisionManufacturing, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(24.dp))
                                else -> Icon(Icons.Default.CloudQueue, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer, modifier = Modifier.size(24.dp))
                            }
                        }
                    }
                }
            }
        }

        // --- 2. NOTIFICATION WARNING BANNER ---
        if (notifications.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .testTag("low_stock_banner"),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.NotificationsActive,
                            contentDescription = "Peringatan",
                            tint = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "$lowStockCount Bahan Baku Kritis!",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = "Ada persediaan yang berada di bawah batas minimum.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f)
                            )
                        }
                        Button(
                            onClick = onNavigateToStock,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Pantau", fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }
        }

        // --- 3. CORE METRICS GRID ---
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    MetricCard(
                        title = "Nilai Persediaan",
                        value = idrFormatter.format(totalStockValue),
                        subtitle = "Terenkripsi AES",
                        icon = Icons.Outlined.Lock,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    MetricCard(
                        title = "Total Bahan Baku",
                        value = "$totalItems Item",
                        subtitle = "$totalCategories Kategori",
                        icon = Icons.Default.Category,
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    MetricCard(
                        title = "Stok Kritis",
                        value = "$lowStockCount Item",
                        subtitle = "Batas Minimum",
                        icon = Icons.Default.Warning,
                        color = if (lowStockCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    MetricCard(
                        title = "Keamanan Data",
                        value = "GCM 256",
                        subtitle = "Sertifikasi Militer",
                        icon = Icons.Default.VerifiedUser,
                        color = Color(0xFF10B981),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // --- 4. ANALYTICS CHART SECTION ---
        item {
            Text(
                text = "Dashboard Analitik Pengadaan",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 20.dp, bottom = 8.dp)
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Text(
                        text = "Stok per Kategori Bahan Baku",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Draw Category Distribution Chart
                    val categoryStocks = remember(materials) {
                        val map = mutableMapOf<String, Double>()
                        materials.forEach { m ->
                            map[m.category] = (map[m.category] ?: 0.0) + m.quantity
                        }
                        map.toList().sortedByDescending { it.second }
                    }

                    if (categoryStocks.isNotEmpty()) {
                        CategoryBarChart(categoryStocks = categoryStocks)
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(150.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Belum ada data visualisasi", color = Color.Gray)
                        }
                    }
                }
            }
        }

        // --- 5. RECENT TRANSACTIONS HEADER ---
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Aktivitas Gudang Terbaru",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Real-time",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // List of recent transactions (take 4)
        val recentTx = transactions.take(4)
        if (recentTx.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Belum ada mutasi barang masuk atau keluar.", style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
                    }
                }
            }
        } else {
            items(recentTx) { tx ->
                TransactionListItem(tx = tx)
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.height(115.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Column {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }
    }
}

@Composable
fun CategoryBarChart(categoryStocks: List<Pair<String, Double>>) {
    val maxStock = categoryStocks.maxOf { it.second }.toFloat()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        categoryStocks.take(4).forEach { (category, qty) ->
            val fraction = if (maxStock > 0f) (qty.toFloat() / maxStock) else 0f
            
            Column(modifier = Modifier.padding(vertical = 4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = category, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(text = "${qty.toInt()} unit", fontSize = 12.sp, color = Color.Gray)
                }
                Spacer(modifier = Modifier.height(4.dp))
                
                // Draw Animated Horizontal Bar
                val barWidth = remember { Animatable(0f) }
                LaunchedEffect(qty) {
                    barWidth.animateTo(
                        targetValue = fraction,
                        animationSpec = spring(dampingRatio = Spring.DampingRatioLowBouncy, stiffness = Spring.StiffnessLow)
                    )
                }

                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(CircleShape)
                ) {
                    // Draw Background bar
                    drawRoundRect(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        size = size
                    )
                    // Draw Foreground progress bar
                    drawRoundRect(
                        brush = Brush.horizontalGradient(
                            colors = listOf(Color(0xFF3B82F6), Color(0xFF60A5FA))
                        ),
                        size = Size(width = size.width * barWidth.value, height = size.height)
                    )
                }
            }
        }
    }
}

@Composable
fun TransactionListItem(tx: StockTransaction) {
    val idrFormatter = remember {
        NumberFormat.getCurrencyInstance(Locale("in", "ID")).apply {
            maximumFractionDigits = 0
        }
    }

    val cost = remember(tx.encryptedCost) {
        val decrypted = EncryptionManager.decrypt(tx.encryptedCost)
        decrypted.toDoubleOrNull() ?: 0.0
    }

    val dateFormatter = remember {
        SimpleDateFormat("dd MMM yyyy • HH:mm", Locale("id", "ID"))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Type Icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(
                        if (tx.type == "IN") Color(0xFFD1FAE5) else Color(0xFFFEE2E2)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (tx.type == "IN") Icons.Default.ArrowDownward else Icons.Default.ArrowUpward,
                    contentDescription = tx.type,
                    tint = if (tx.type == "IN") Color(0xFF059669) else Color(0xFFDC2626),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            // Material & Date Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = tx.materialName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = dateFormatter.format(Date(tx.timestamp)),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Text(
                    text = tx.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            // Quantity & Cost
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${if (tx.type == "IN") "+" else "-"}${tx.quantity} ${tx.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (tx.type == "IN") Color(0xFF059669) else Color(0xFFDC2626)
                )
                Text(
                    text = idrFormatter.format(cost),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray,
                    fontSize = 11.sp
                )
            }
        }
    }
}
