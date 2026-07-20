package com.example.api

import com.example.BuildConfig
import com.example.security.EncryptionManager
import com.example.data.entity.RawMaterial
import com.example.data.entity.StockTransaction
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

@JsonClass(generateAdapter = true)
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@JsonClass(generateAdapter = true)
data class Content(
    val parts: List<Part>
)

@JsonClass(generateAdapter = true)
data class Part(
    val text: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerationConfig(
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null,
    val responseMimeType: String? = null
)

@JsonClass(generateAdapter = true)
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

@JsonClass(generateAdapter = true)
data class Candidate(
    val content: Content? = null
)

interface GeminiApiService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

object GeminiClient {
    private const val BASE_URL = "https://generativelanguage.googleapis.com/"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi: Moshi = Moshi.Builder()
        .addLast(KotlinJsonAdapterFactory())
        .build()

    val service: GeminiApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
        retrofit.create(GeminiApiService::class.java)
    }

    suspend fun generateReport(
        materials: List<RawMaterial>,
        transactions: List<StockTransaction>
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext "Kunci API Gemini tidak dikonfigurasi. Harap tambahkan GEMINI_API_KEY di Panel Secrets AI Studio Anda."
        }

        val materialsData = materials.joinToString("\n") { m ->
            val price = EncryptionManager.decrypt(m.encryptedUnitPrice)
            "- SKU: ${m.sku} | Nama: ${m.name} | Kategori: ${m.category} | Stok: ${m.quantity} ${m.unit} | Batas Min: ${m.minThreshold} ${m.unit} | Harga Satuan: Rp$price"
        }

        val transactionsData = transactions.take(15).joinToString("\n") { t ->
            val cost = EncryptionManager.decrypt(t.encryptedCost)
            "- [${t.type}] ${t.quantity} ${t.unit} dari ${t.materialName} | Catatan: ${t.notes} | Total Nilai: Rp$cost"
        }

        val prompt = """
            Anda adalah Analis Pengadaan dan Logistik Gudang Bahan Baku profesional (WMS AI Analyst).
            Buat laporan analisis performa gudang dan pengadaan barang terperinci dalam format Markdown yang sangat rapi dan profesional (bergaya laporan dashboard web).
            
            Laporan harus mencakup:
            1. Ringkasan Status Persediaan (Total item, kategori, dan identifikasi bahan baku kritis yang berada di bawah batas minimum).
            2. Analisis Performa Pengadaan Barang (Melihat transaksi masuk/keluar baru-baru ini dan efisiensi pengadaan).
            3. Rekomendasi Pengadaan Cerdas & Forecasting (Memprediksi bahan baku apa saja yang harus dibeli, dalam jumlah berapa, dan estimasi anggaran berdasarkan data batas minimum stok).
            4. Ringkasan Keamanan & Enkripsi Data (Sebutkan bahwa semua harga barang dan detail supplier telah terlindungi dengan enkripsi militer AES-256 GCM lokal).
            
            Gunakan format Markdown dengan header, list, dan tabel yang sangat rapi. Bahasa yang digunakan harus Bahasa Indonesia yang profesional dan lugas.
            
            --- DATA GUDANG REAL-TIME ---
            BAHAN BAKU:
            $materialsData
            
            TRANSAKSI TERBARU (15 Terakhir):
            $transactionsData
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(Content(parts = listOf(Part(text = prompt)))),
            systemInstruction = Content(parts = listOf(Part(text = "Anda adalah analis gudang bahan baku cerdas yang berfokus pada visualisasi laporan berbasis web dan optimasi rantai pasok.")))
        )

        try {
            val response = service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "Gagal menghasilkan laporan otomatis. Respon kosong dari model."
        } catch (e: Exception) {
            e.printStackTrace()
            "Gagal memanggil API Gemini: ${e.localizedMessage}. Harap periksa koneksi internet atau validitas GEMINI_API_KEY Anda."
        }
    }
}
