package com.movienest.log.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movienest.log.data.model.WatchStatus
import com.movienest.log.data.repository.MovieNestRepository
import com.movienest.log.ui.state.GenreItem
import com.movienest.log.ui.state.GenreUiState
import com.movienest.log.ui.state.SettingsUiState
import com.movienest.log.util.StatisticsUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(private val repository: MovieNestRepository) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> =
        combine(repository.entriesFlow, repository.settingsFlow, repository.genresFlow) { entries, settings, genres ->
            SettingsUiState(
                loading = false,
                settings = settings,
                entryCount = entries.size,
                genreCount = genres.size
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState())

    fun setDefaultShelf(status: WatchStatus) {
        viewModelScope.launch { repository.updateSettings { it.copy(defaultHomeShelf = status) } }
    }

    fun setShowRatingOnTickets(value: Boolean) {
        viewModelScope.launch { repository.updateSettings { it.copy(showRatingOnTickets = value) } }
    }

    fun setCompactTicketMode(value: Boolean) {
        viewModelScope.launch { repository.updateSettings { it.copy(compactTicketMode = value) } }
    }

    fun replayOnboarding() {
        viewModelScope.launch { repository.setOnboardingCompleted(false) }
    }

    fun clearFilters() {
        viewModelScope.launch { repository.resetFilters() }
    }

    fun deleteAllEntries() {
        viewModelScope.launch { repository.deleteAllEntries() }
    }

    fun resetAllData() {
        viewModelScope.launch { repository.resetAllData() }
    }
}

class GenreViewModel(private val repository: MovieNestRepository) : ViewModel() {

    val uiState: StateFlow<GenreUiState> =
        combine(repository.entriesFlow, repository.genresFlow) { entries, genres ->
            val items = genres.map { g ->
                GenreItem(name = g, usageCount = StatisticsUtils.titlesUsingGenre(entries, g))
            }
            GenreUiState(loading = false, genres = items)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GenreUiState())

    fun addGenre(name: String) { viewModelScope.launch { repository.addGenre(name) } }
    fun renameGenre(oldName: String, newName: String) {
        viewModelScope.launch { repository.renameGenre(oldName, newName) }
    }
    fun removeGenreLabelOnly(name: String) {
        viewModelScope.launch { repository.removeGenreLabel(name) }
    }
    fun removeGenreAndClear(name: String) {
        viewModelScope.launch { repository.removeGenreAndClearFromEntries(name) }
    }
}

class OnboardingViewModel(private val repository: MovieNestRepository) : ViewModel() {
    fun complete() { viewModelScope.launch { repository.setOnboardingCompleted(true) } }
}
