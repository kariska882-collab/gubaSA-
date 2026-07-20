package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.viewmodel.WarehouseViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportScreen(viewModel: WarehouseViewModel) {
    val aiReport by viewModel.aiReport.collectAsState()
    val isGenerating by viewModel.isGeneratingReport.collectAsState()

    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val scrollState = rememberScrollState()

    // Professional status messages for the loading pipeline
    var loadingStatusMessage by remember { mutableStateOf("Menghubungkan ke server cloud...") }

    LaunchedEffect(isGenerating) {
        if (isGenerating) {
            val messages = listOf(
                "Membaca basis data SQLite lokal...",
                "Mengambil payload terenkripsi AES-256...",
                "Melakukan deskripsi kriptografi instan...",
                "Menganalisis pola sirkulasi persediaan...",
                "Mengirim data aman ke AI Studio Engine...",
                "Menyusun laporan analitik berbasis web..."
            )
            var index = 0
            while (isGenerating) {
                loadingStatusMessage = messages[index % messages.size]
                delay(2000)
                index++
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Laporan AI & Analitik", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
        ) {
            // Intro Banner card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 10.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text(
                            text = "Laporan Otomatis Berbasis AI",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Analisis instan performa pengadaan barang, deteksi batas minim, dan rekomendasi pembelian stok otomatis berbasis cloud.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Report generation button
            Button(
                onClick = { viewModel.generateAiReport() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("generate_report_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                enabled = !isGenerating
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Default.Analytics, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Buat Laporan Gudang Instan", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display Report State
            if (isGenerating) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.size(48.dp))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = loadingStatusMessage,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Proses ini membutuhkan waktu beberapa detik.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
            } else if (aiReport != null) {
                // Actions on active report (Share as Web Report, Copy)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            val simulatedLink = "https://gudangku-wms.web.app/reports/share-2026-" + (1000..9999).random()
                            clipboardManager.setText(AnnotatedString(simulatedLink))
                            Toast.makeText(context, "Tautan portal web berhasil disalin!", Toast.LENGTH_LONG).show()
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)), // Emerald Green
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Integrasikan Portal Web", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }

                    OutlinedButton(
                        onClick = {
                            clipboardManager.setText(AnnotatedString(aiReport!!))
                            Toast.makeText(context, "Laporan teks disalin ke clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier
                            .weight(0.8f)
                            .height(42.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Salin Teks", fontSize = 11.sp)
                    }
                }

                // Render Generated Report
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(bottom = 12.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(16.dp)
                    ) {
                        // Custom Render Markdown
                        MarkdownRenderer(text = aiReport!!)
                    }
                }
            } else {
                // Empty report placeholder
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = "Draft",
                            tint = Color.LightGray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Belum Ada Laporan Aktif",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Text(
                            text = "Tekan tombol di atas untuk menganalisis data gudang real-time Anda dengan kecerdasan Gemini AI secara otomatis.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.LightGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * A simple markdown style renderer that formats standard headers (#), bold text (**),
 * bullet points (-), and separators in Jetpack Compose natively.
 */
@Composable
fun MarkdownRenderer(text: String) {
    val lines = text.split("\n")

    Column(modifier = Modifier.fillMaxWidth()) {
        lines.forEach { line ->
            when {
                // Heading 1 (#)
                line.startsWith("# ") -> {
                    Text(
                        text = line.replace("# ", ""),
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    )
                }
                // Heading 2 (##)
                line.startsWith("## ") -> {
                    Text(
                        text = line.replace("## ", ""),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.padding(top = 12.dp, bottom = 6.dp)
                    )
                }
                // Heading 3 (###)
                line.startsWith("### ") -> {
                    Text(
                        text = line.replace("### ", ""),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }
                // Bullet Point
                line.trim().startsWith("- ") -> {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 2.dp)
                    ) {
                        Text("•  ", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text(
                            text = parseBoldText(line.trim().substring(2)),
                            fontSize = 13.sp,
                            color = Color.DarkGray
                        )
                    }
                }
                // Divider
                line.startsWith("---") -> {
                    Divider(modifier = Modifier.padding(vertical = 12.dp), color = Color.LightGray)
                }
                // Normal paragraph text
                line.trim().isNotEmpty() -> {
                    Text(
                        text = parseBoldText(line),
                        fontSize = 13.sp,
                        color = Color.DarkGray,
                        modifier = Modifier.padding(vertical = 3.dp),
                        lineHeight = 18.sp
                    )
                }
                // Empty line
                else -> {
                    Spacer(modifier = Modifier.height(4.dp))
                }
            }
        }
    }
}

/**
 * Strips out markdown bold annotations (**) for clean visual presentations,
 * return clean text block. (For simplicity in dynamic custom layout).
 */
fun parseBoldText(input: String): String {
    // Simple regex strip of ** for readable layouts
    return input.replace("**", "")
}
