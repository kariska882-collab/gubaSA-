package com.example.security

import android.util.Base64
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object EncryptionManager {
    private const val ALGORITHM = "AES/GCM/NoPadding"
    private const val TAG_LENGTH_BIT = 128
    private const val IV_LENGTH_BYTE = 12

    // 32-byte custom secure key (256-bit AES) derived for app session durability.
    private val keyBytes = byteArrayOf(
        0x56, 0x4d, 0x53, 0x47, 0x75, 0x64, 0x61, 0x6e,
        0x67, 0x6b, 0x75, 0x53, 0x65, 0x63, 0x75, 0x72,
        0x65, 0x4b, 0x65, 0x79, 0x32, 0x30, 0x32, 0x36,
        0x41, 0x45, 0x53, 0x45, 0x6e, 0x63, 0x72, 0x79
    )
    
    private val secretKey: SecretKey = SecretKeySpec(keyBytes, "AES")

    fun encrypt(plaintext: String): String {
        if (plaintext.isEmpty()) return ""
        return try {
            val cipher = Cipher.getInstance(ALGORITHM)
            val iv = ByteArray(IV_LENGTH_BYTE)
            SecureRandom().nextBytes(iv)
            val parameterSpec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec)
            
            val ciphertextBytes = cipher.doFinal(plaintext.toByteArray(Charsets.UTF_8))
            
            val combined = ByteArray(iv.size + ciphertextBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(ciphertextBytes, 0, combined, iv.size, ciphertextBytes.size)
            
            Base64.encodeToString(combined, Base64.DEFAULT or Base64.NO_WRAP)
        } catch (e: Exception) {
            e.printStackTrace()
            "ENC_ERR:${e.localizedMessage}"
        }
    }

    fun decrypt(ciphertext: String): String {
        if (ciphertext.isEmpty()) return ""
        if (ciphertext.startsWith("ENC_ERR:")) return "N/A"
        return try {
            val combined = Base64.decode(ciphertext, Base64.DEFAULT)
            if (combined.size < IV_LENGTH_BYTE) return "CIPHERTEXT_INVALID"
            
            val iv = ByteArray(IV_LENGTH_BYTE)
            val ciphertextBytes = ByteArray(combined.size - IV_LENGTH_BYTE)
            System.arraycopy(combined, 0, iv, 0, iv.size)
            System.arraycopy(combined, iv.size, ciphertextBytes, 0, ciphertextBytes.size)
            
            val cipher = Cipher.getInstance(ALGORITHM)
            val parameterSpec = GCMParameterSpec(TAG_LENGTH_BIT, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec)
            
            val decryptedBytes = cipher.doFinal(ciphertextBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            e.printStackTrace()
            "CIPHERTEXT_UNREADABLE"
        }
    }
}
