package com.movienest.log.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movienest.log.data.model.WatchStatus
import com.movienest.log.data.repository.MovieNestRepository
import com.movienest.log.ui.navigation.Routes
import com.movienest.log.ui.state.EntryDetailUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class EntryDetailViewModel(
    private val repository: MovieNestRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val entryId: String = savedStateHandle.get<String>(Routes.ARG_ENTRY_ID).orEmpty()

    val uiState: StateFlow<EntryDetailUiState> =
        repository.entriesFlow.map { entries ->
            if (entryId.isBlank()) return@map EntryDetailUiState(loading = false, missing = true)
            val entry = entries.firstOrNull { it.id == entryId }
            if (entry == null) {
                EntryDetailUiState(loading = false, missing = true)
            } else {
                EntryDetailUiState(loading = false, entry = entry, missing = false)
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EntryDetailUiState())

    fun toggleFavorite() {
        if (entryId.isBlank()) return
        viewModelScope.launch { repository.toggleFavorite(entryId) }
    }

    fun changeStatus(status: WatchStatus) {
        if (entryId.isBlank()) return
        viewModelScope.launch {
            repository.changeStatus(entryId, status, suggestWatchedDate = status == WatchStatus.Watched)
        }
    }

    fun incrementEpisode() {
        if (entryId.isBlank()) return
        viewModelScope.launch { repository.incrementEpisode(entryId) }
    }

    fun markWatched() {
        if (entryId.isBlank()) return
        viewModelScope.launch { repository.changeStatus(entryId, WatchStatus.Watched, suggestWatchedDate = true) }
    }

    fun delete(onDone: () -> Unit) {
        if (entryId.isBlank()) { onDone(); return }
        viewModelScope.launch {
            repository.deleteEntry(entryId)
            onDone()
        }
    }
}
