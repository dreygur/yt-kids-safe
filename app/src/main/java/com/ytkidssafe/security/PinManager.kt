package com.ytkidssafe.security

import com.ytkidssafe.data.datastore.AppPreferences
import kotlinx.coroutines.flow.first
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PinManager @Inject constructor(
    private val preferences: AppPreferences
) {
    suspend fun setPin(pin: String) {
        val hash = hashPin(pin)
        preferences.setPinHash(hash)
    }

    suspend fun verifyPin(pin: String): Boolean {
        val storedHash = preferences.pinHash.first()
        return storedHash == hashPin(pin)
    }

    suspend fun isPinSet(): Boolean {
        return preferences.pinHash.first() != null
    }

    suspend fun startParentSession() {
        val expiry = System.currentTimeMillis() + SESSION_DURATION_MS
        preferences.setParentSessionExpiry(expiry)
    }

    suspend fun isParentSessionValid(): Boolean {
        val expiry = preferences.parentSessionExpiry.first()
        return System.currentTimeMillis() < expiry
    }

    suspend fun endParentSession() {
        preferences.clearParentSession()
    }

    private fun hashPin(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    companion object {
        private const val SESSION_DURATION_MS = 5 * 60 * 1000L // 5 minutes
    }
}
