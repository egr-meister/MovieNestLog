package com.movienest.log.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movienest.log.data.model.ContentFilter
import com.movienest.log.data.model.SortOption
import com.movienest.log.data.model.WatchStatus
import com.movienest.log.data.repository.MovieNestRepository
import com.movienest.log.ui.state.LibraryUiState
import com.movienest.log.util.EntryQueryUtils
import com.movienest.log.util.TextUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Local, per-session UI filters not stored in settings. */
private data class LocalLibraryFilters(
    val query: String = "",
    val statusFilter: WatchStatus? = null
)

class LibraryViewModel(private val repository: MovieNestRepository) : ViewModel() {

    private val localFilters = MutableStateFlow(LocalLibraryFilters())

    val uiState: StateFlow<LibraryUiState> =
        combine(
            repository.entriesFlow,
            repository.settingsFlow,
            repository.genresFlow,
            localFilters
        ) { entries, settings, genres, local ->
            // Guard: if the persisted genre filter no longer exists, ignore it.
            val genreFilterValid = settings.selectedGenre?.let { sel ->
                entries.any { TextUtils.normalizeGenre(it.genre) == TextUtils.normalizeGenre(sel) } ||
                    genres.any { it.equals(sel, ignoreCase = true) }
            } ?: false
            val effectiveGenre = if (genreFilterValid) settings.selectedGenre else null

            var result = entries
            result = EntryQueryUtils.search(result, local.query)
            result = EntryQueryUtils.filterByStatus(result, local.statusFilter)
            result = EntryQueryUtils.applyContentFilter(result, settings.selectedContentFilter)
            result = EntryQueryUtils.filterByGenre(result, effectiveGenre)
            result = EntryQueryUtils.sort(result, settings.selectedSort)

            LibraryUiState(
                loading = false,
                query = local.query,
                statusFilter = local.statusFilter,
                contentFilter = settings.selectedContentFilter,
                genreFilter = effectiveGenre,
                sort = settings.selectedSort,
                availableGenres = genres,
                results = result,
                totalEntries = entries.size,
                showRatingOnTickets = settings.showRatingOnTickets,
                compactTicketMode = settings.compactTicketMode
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = LibraryUiState()
        )

    fun setQuery(query: String) { localFilters.value = localFilters.value.copy(query = query) }

    fun setStatusFilter(status: WatchStatus?) {
        localFilters.value = localFilters.value.copy(statusFilter = status)
    }

    fun setContentFilter(filter: ContentFilter) {
        viewModelScope.launch { repository.updateSettings { it.copy(selectedContentFilter = filter) } }
    }

    fun setGenreFilter(genre: String?) {
        viewModelScope.launch { repository.updateSettings { it.copy(selectedGenre = genre) } }
    }

    fun setSort(sort: SortOption) {
        viewModelScope.launch { repository.updateSettings { it.copy(selectedSort = sort) } }
    }

    fun clearFilters() {
        localFilters.value = LocalLibraryFilters()
        viewModelScope.launch { repository.resetFilters() }
    }

    fun toggleFavorite(entryId: String) {
        viewModelScope.launch { repository.toggleFavorite(entryId) }
    }

    fun deleteEntry(entryId: String) {
        viewModelScope.launch { repository.deleteEntry(entryId) }
    }
}
