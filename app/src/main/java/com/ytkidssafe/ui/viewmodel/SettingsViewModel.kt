package com.ytkidssafe.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ytkidssafe.data.backup.BackupService
import com.ytkidssafe.data.datastore.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.security.MessageDigest
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val appPreferences: AppPreferences,
    private val backupService: BackupService
) : ViewModel() {

    val defaultDailyLimit: StateFlow<Int> = appPreferences.defaultDailyLimit
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 60)

    private val _exportResult = MutableStateFlow<Result<Unit>?>(null)
    val exportResult: StateFlow<Result<Unit>?> = _exportResult.asStateFlow()

    private val _importResult = MutableStateFlow<Result<Int>?>(null)
    val importResult: StateFlow<Result<Int>?> = _importResult.asStateFlow()

    fun setDefaultDailyLimit(minutes: Int) {
        viewModelScope.launch {
            appPreferences.setDefaultDailyLimit(minutes)
        }
    }

    fun setPin(pin: String) {
        viewModelScope.launch {
            val hash = hashPin(pin)
            appPreferences.setPinHash(hash)
        }
    }

    private fun hashPin(pin: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(pin.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun exportData(uri: Uri) {
        viewModelScope.launch {
            _exportResult.value = backupService.exportToUri(uri)
        }
    }

    fun importData(uri: Uri) {
        viewModelScope.launch {
            _importResult.value = backupService.importFromUri(uri)
        }
    }

    fun clearExportResult() {
        _exportResult.value = null
    }

    fun clearImportResult() {
        _importResult.value = null
    }
}
