package com.movienest.log.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movienest.log.data.model.ContentType
import com.movienest.log.data.model.WatchStatus
import com.movienest.log.data.repository.MovieNestRepository
import com.movienest.log.ui.state.HomeUiState
import com.movienest.log.util.DateUtils
import com.movienest.log.util.EntryQueryUtils
import com.movienest.log.util.StatisticsUtils
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: MovieNestRepository) : ViewModel() {

    val uiState: StateFlow<HomeUiState> =
        combine(repository.entriesFlow, repository.settingsFlow) { entries, settings ->
            val monthKey = DateUtils.currentMonthKey()
            val stats = StatisticsUtils.compute(entries, monthKey)
            val shelf = settings.defaultHomeShelf
            HomeUiState(
                loading = false,
                selectedShelf = shelf,
                totalCount = entries.size,
                shelfEntries = EntryQueryUtils.sort(
                    EntryQueryUtils.filterByStatus(entries, shelf),
                    com.movienest.log.data.model.SortOption.RecentlyUpdated
                ),
                nowWatching = EntryQueryUtils.filterByStatus(entries, WatchStatus.Watching)
                    .sortedByDescending { it.updatedAt },
                upNext = EntryQueryUtils.filterByStatus(entries, WatchStatus.WantToWatch)
                    .sortedByDescending { it.createdAt }
                    .take(5),
                recentlyWatched = EntryQueryUtils.latestWatched(entries, 6),
                monthLabel = DateUtils.monthDisplayLabel(monthKey),
                watchedThisMonth = stats.totalWatched,
                moviesThisMonth = stats.moviesWatched,
                seriesThisMonth = stats.seriesCompleted,
                averageRatingThisMonth = stats.averageRating,
                favoritesThisMonth = stats.favoritesCompleted,
                showRatingOnTickets = settings.showRatingOnTickets,
                compactTicketMode = settings.compactTicketMode
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = HomeUiState()
        )

    fun selectShelf(status: WatchStatus) {
        viewModelScope.launch { repository.updateSettings { it.copy(defaultHomeShelf = status) } }
    }

    fun toggleFavorite(entryId: String) {
        viewModelScope.launch { repository.toggleFavorite(entryId) }
    }

    fun moveToWatching(entryId: String) {
        viewModelScope.launch { repository.changeStatus(entryId, WatchStatus.Watching, suggestWatchedDate = false) }
    }

    fun countByType(entries: List<com.movienest.log.data.model.MovieEntry>, type: ContentType) =
        EntryQueryUtils.countByContentType(entries, type)
}
