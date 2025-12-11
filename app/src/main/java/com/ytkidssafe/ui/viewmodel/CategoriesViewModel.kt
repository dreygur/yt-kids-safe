package com.ytkidssafe.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ytkidssafe.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val categories: StateFlow<List<String>> = settingsRepository.categories
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun addCategory(category: String) {
        viewModelScope.launch {
            settingsRepository.addCategory(category)
        }
    }

    fun removeCategory(category: String) {
        viewModelScope.launch {
            settingsRepository.removeCategory(category)
        }
    }

    fun renameCategory(oldName: String, newName: String) {
        viewModelScope.launch {
            settingsRepository.renameCategory(oldName, newName)
        }
    }
}
