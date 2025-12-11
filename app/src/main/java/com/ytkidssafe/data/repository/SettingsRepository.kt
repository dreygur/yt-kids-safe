package com.ytkidssafe.data.repository

import com.ytkidssafe.data.datastore.AppPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepository @Inject constructor(
    private val preferences: AppPreferences
) {
    val pinHash: Flow<String?> = preferences.pinHash
    val defaultDailyLimit: Flow<Int> = preferences.defaultDailyLimit
    val currentProfileId: Flow<String?> = preferences.currentProfileId
    val parentSessionExpiry: Flow<Long> = preferences.parentSessionExpiry

    suspend fun setPin(pin: String) {
        preferences.setPinHash(hashPin(pin))
    }

    suspend fun verifyPin(pin: String): Boolean {
        val storedHash = preferences.pinHash.first()
        return storedHash == hashPin(pin)
    }

    suspend fun isPinSet(): Boolean {
        return preferences.pinHash.first() != null
    }

    suspend fun setDefaultDailyLimit(minutes: Int) {
        preferences.setDefaultDailyLimit(minutes)
    }

    suspend fun setCurrentProfile(profileId: String?) {
        preferences.setCurrentProfileId(profileId)
    }

    suspend fun startParentSession(durationMinutes: Int = 5) {
        val expiry = System.currentTimeMillis() + (durationMinutes * 60 * 1000)
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
}
