package com.ytkidssafe.data.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppPreferences @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        private val PIN_HASH = stringPreferencesKey("pin_hash")
        private val DEFAULT_DAILY_LIMIT = intPreferencesKey("default_daily_limit")
        private val CURRENT_PROFILE_ID = stringPreferencesKey("current_profile_id")
        private val PARENT_SESSION_EXPIRY = longPreferencesKey("parent_session_expiry")
        private val CUSTOM_CATEGORIES = stringPreferencesKey("custom_categories")

        val DEFAULT_CATEGORIES = listOf("Cartoons", "Learning", "Music", "Stories", "Games")
    }

    val pinHash: Flow<String?> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[PIN_HASH] }

    val defaultDailyLimit: Flow<Int> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[DEFAULT_DAILY_LIMIT] ?: 60 }

    val currentProfileId: Flow<String?> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[CURRENT_PROFILE_ID] }

    val parentSessionExpiry: Flow<Long> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { it[PARENT_SESSION_EXPIRY] ?: 0L }

    val categories: Flow<List<String>> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { prefs ->
            prefs[CUSTOM_CATEGORIES]?.split(",")?.filter { it.isNotBlank() }
                ?: DEFAULT_CATEGORIES
        }

    suspend fun setPinHash(hash: String) {
        dataStore.edit { it[PIN_HASH] = hash }
    }

    suspend fun setDefaultDailyLimit(minutes: Int) {
        dataStore.edit { it[DEFAULT_DAILY_LIMIT] = minutes }
    }

    suspend fun setCurrentProfileId(profileId: String?) {
        dataStore.edit {
            if (profileId != null) {
                it[CURRENT_PROFILE_ID] = profileId
            } else {
                it.remove(CURRENT_PROFILE_ID)
            }
        }
    }

    suspend fun setParentSessionExpiry(expiry: Long) {
        dataStore.edit { it[PARENT_SESSION_EXPIRY] = expiry }
    }

    suspend fun clearParentSession() {
        dataStore.edit { it[PARENT_SESSION_EXPIRY] = 0L }
    }

    suspend fun isPinSet(): Boolean {
        var result = false
        dataStore.edit { result = it[PIN_HASH] != null }
        return result
    }

    suspend fun setCategories(categories: List<String>) {
        dataStore.edit { it[CUSTOM_CATEGORIES] = categories.joinToString(",") }
    }

    suspend fun addCategory(category: String) {
        dataStore.edit { prefs ->
            val current = prefs[CUSTOM_CATEGORIES]?.split(",")?.filter { it.isNotBlank() }
                ?: DEFAULT_CATEGORIES
            if (!current.contains(category)) {
                prefs[CUSTOM_CATEGORIES] = (current + category).joinToString(",")
            }
        }
    }

    suspend fun removeCategory(category: String) {
        dataStore.edit { prefs ->
            val current = prefs[CUSTOM_CATEGORIES]?.split(",")?.filter { it.isNotBlank() }
                ?: DEFAULT_CATEGORIES
            prefs[CUSTOM_CATEGORIES] = current.filter { it != category }.joinToString(",")
        }
    }
}
