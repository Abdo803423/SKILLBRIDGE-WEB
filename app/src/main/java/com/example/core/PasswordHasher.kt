package com.example.core

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Salted PBKDF2-HMAC-SHA256 credential hashing & constant-time verification.
 * Plaintext passwords are never stored in the database.
 */
object PasswordHasher {
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"
    private const val ITERATIONS = 120_000
    private const val KEY_LENGTH_BITS = 256
    private const val SALT_BYTES = 16

    fun generateSalt(): String {
        val salt = ByteArray(SALT_BYTES)
        SecureRandom().nextBytes(salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }

    fun hashPassword(password: CharArray, saltBase64: String): String {
        val salt = Base64.decode(saltBase64, Base64.NO_WRAP)
        val spec = PBEKeySpec(password, salt, ITERATIONS, KEY_LENGTH_BITS)
        return try {
            val factory = SecretKeyFactory.getInstance(ALGORITHM)
            val hash = factory.generateSecret(spec).encoded
            Base64.encodeToString(hash, Base64.NO_WRAP)
        } finally {
            spec.clearPassword()
        }
    }

    fun createSaltedHash(password: String): Pair<String, String> {
        val salt = generateSalt()
        val digest = hashPassword(password.toCharArray(), salt)
        return salt to digest
    }

    fun verifyPassword(password: String, saltBase64: String, expectedHashBase64: String): Boolean {
        if (password.isEmpty() || saltBase64.isBlank() || expectedHashBase64.isBlank()) return false
        return try {
            val actual = Base64.decode(hashPassword(password.toCharArray(), saltBase64), Base64.NO_WRAP)
            val expected = Base64.decode(expectedHashBase64, Base64.NO_WRAP)
            MessageDigest.isEqual(actual, expected)
        } catch (_: Exception) {
            false
        }
    }
}
