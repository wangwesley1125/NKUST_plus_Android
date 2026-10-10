package com.example.nkust_app.data

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.core.content.edit
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class Credentials(val username: String, val password: String) {
    // 避免不小心 Log 出密碼
    override fun toString() = "Credentials(username=$username, password=***)"
}

/**
 * 帳密加密儲存（對應 iOS 的 CredentialStorage / Keychain）
 * 用 Android Keystore 內的 AES-256-GCM 金鑰加密後，才寫進 SharedPreferences。
 */
class CredentialStorage(context: Context) {
    private val prefs = context.getSharedPreferences("nkust_credentials", Context.MODE_PRIVATE)

    fun save(credentials: Credentials) {
        prefs.edit {
            putString(KEY_USERNAME, encrypt(credentials.username))
            putString(KEY_PASSWORD, encrypt(credentials.password))
        }
    }

    fun load(): Credentials? {
        val username = prefs.getString(KEY_USERNAME, null)?.let(::decrypt)
        val password = prefs.getString(KEY_PASSWORD, null)?.let(::decrypt)
        if (username.isNullOrEmpty() || password == null) return null
        return Credentials(username, password)
    }

    fun clear() = prefs.edit { clear() }

    // ── Keystore 加解密 ──

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE)
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()
        )
        return generator.generateKey()
    }

    private fun encrypt(plain: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val encrypted = cipher.doFinal(plain.toByteArray(Charsets.UTF_8))
        // 把 IV（12 bytes）放在密文前面一起存
        return Base64.encodeToString(cipher.iv + encrypted, Base64.NO_WRAP)
    }

    private fun decrypt(stored: String): String? = try {
        val bytes = Base64.decode(stored, Base64.NO_WRAP)
        val iv = bytes.copyOfRange(0, IV_SIZE)
        val encrypted = bytes.copyOfRange(IV_SIZE, bytes.size)
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, iv))
        String(cipher.doFinal(encrypted), Charsets.UTF_8)
    } catch (e: Exception) {
        null   // 金鑰不見（例如資料從備份還原到新手機）就當作沒有儲存
    }

    companion object {
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "nkust_autofill_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val IV_SIZE = 12
        private const val KEY_USERNAME = "username"
        private const val KEY_PASSWORD = "password"
    }
}