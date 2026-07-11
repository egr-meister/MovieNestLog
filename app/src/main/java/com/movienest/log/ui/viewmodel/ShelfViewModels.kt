package com.movienest.log.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movienest.log.data.model.ContentFilter
import com.movienest.log.data.model.ContentType
import com.movienest.log.data.model.WatchStatus
import com.movienest.log.data.repository.MovieNestRepository
import com.movienest.log.ui.state.FavoritesUiState
import com.movienest.log.ui.state.WatchingUiState
import com.movienest.log.util.EntryQueryUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class WatchingViewModel(private val repository: MovieNestRepository) : ViewModel() {

    val uiState: StateFlow<WatchingUiState> =
        repository.entriesFlow.map { entries ->
            WatchingUiState(
                loading = false,
                entries = EntryQueryUtils.filterByStatus(entries, WatchStatus.Watching)
                    .sortedByDescending { it.updatedAt }
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), WatchingUiState())

    fun incrementEpisode(entryId: String) {
        viewModelScope.launch { repository.incrementEpisode(entryId) }
    }

    fun markWatched(entryId: String) {
        viewModelScope.launch { repository.changeStatus(entryId, WatchStatus.Watched, suggestWatchedDate = true) }
    }

    fun updateProgress(
        entryId: String,
        currentSeason: Int?,
        totalSeasons: Int?,
        watchedEpisodes: Int?,
        totalEpisodes: Int?
    ) {
        viewModelScope.launch {
            repository.updateSeriesProgress(entryId, currentSeason, totalSeasons, watchedEpisodes, totalEpisodes)
        }
    }
}

class FavoritesViewModel(private val repository: MovieNestRepository) : ViewModel() {

    private val contentFilter = MutableStateFlow(ContentFilter.All)
    private val sortByRatingDesc = MutableStateFlow(true)

    val uiState: StateFlow<FavoritesUiState> =
        combine(
            repository.entriesFlow,
            repository.settingsFlow,
            contentFilter,
            sortByRatingDesc
        ) { entries, settings, filter, ratingDesc ->
            var favs = EntryQueryUtils.filterFavorites(entries)
            favs = when (filter) {
                ContentFilter.Movies -> favs.filter { it.contentType == ContentType.Movie }
                ContentFilter.Series -> favs.filter { it.contentType == ContentType.Series }
                else -> favs
            }
            favs = if (ratingDesc) {
                favs.sortedWith(
                    compareByDescending<com.movienest.log.data.model.MovieEntry> { it.rating != null }
                        .thenByDescending { it.rating ?: 0.0 }
                )
            } else {
                favs.sortedWith(
                    compareByDescending<com.movienest.log.data.model.MovieEntry> { it.rating != null }
                        .thenBy { it.rating ?: Double.MAX_VALUE }
                )
            }
            FavoritesUiState(
                loading = false,
                entries = favs,
                contentFilter = filter,
                sortByRatingDesc = ratingDesc,
                showRatingOnTickets = settings.showRatingOnTickets
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), FavoritesUiState())

    fun setContentFilter(filter: ContentFilter) { contentFilter.value = filter }
    fun setSortByRatingDesc(desc: Boolean) { sortByRatingDesc.value = desc }

    fun toggleFavorite(entryId: String) {
        viewModelScope.launch { repository.toggleFavorite(entryId) }
    }

    fun changeStatus(entryId: String, status: WatchStatus) {
        viewModelScope.launch {
            repository.changeStatus(entryId, status, suggestWatchedDate = status == WatchStatus.Watched)
        }
    }
}
