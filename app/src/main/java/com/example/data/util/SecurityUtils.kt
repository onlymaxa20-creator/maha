package com.example.data.util

import java.security.MessageDigest
import java.security.SecureRandom

object SecurityUtils {
    fun generateSalt(length: Int = 16): String {
        val random = SecureRandom()
        val bytes = ByteArray(length)
        random.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun hashPassword(password: String, salt: String): String {
        val combined = "$password:$salt"
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(combined.toByteArray(Charsets.UTF_8))
        return hashBytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(password: String, salt: String, expectedHash: String): Boolean {
        val calculatedHash = hashPassword(password, salt)
        return calculatedHash == expectedHash
    }
}
