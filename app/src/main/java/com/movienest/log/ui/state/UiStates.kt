package com.movienest.log.ui.state

import com.movienest.log.data.model.AppSettings
import com.movienest.log.data.model.ContentFilter
import com.movienest.log.data.model.MovieEntry
import com.movienest.log.data.model.SortOption
import com.movienest.log.data.model.WatchStatus
import com.movienest.log.util.MonthlyStatistics

data class HomeUiState(
    val loading: Boolean = true,
    val selectedShelf: WatchStatus = WatchStatus.WantToWatch,
    val totalCount: Int = 0,
    val shelfEntries: List<MovieEntry> = emptyList(),
    val nowWatching: List<MovieEntry> = emptyList(),
    val upNext: List<MovieEntry> = emptyList(),
    val recentlyWatched: List<MovieEntry> = emptyList(),
    val monthLabel: String = "",
    val watchedThisMonth: Int = 0,
    val moviesThisMonth: Int = 0,
    val seriesThisMonth: Int = 0,
    val averageRatingThisMonth: Double? = null,
    val favoritesThisMonth: Int = 0,
    val showRatingOnTickets: Boolean = true,
    val compactTicketMode: Boolean = false
)

data class LibraryUiState(
    val loading: Boolean = true,
    val query: String = "",
    val statusFilter: WatchStatus? = null,
    val contentFilter: ContentFilter = ContentFilter.All,
    val genreFilter: String? = null,
    val sort: SortOption = SortOption.RecentlyUpdated,
    val availableGenres: List<String> = emptyList(),
    val results: List<MovieEntry> = emptyList(),
    val totalEntries: Int = 0,
    val showRatingOnTickets: Boolean = true,
    val compactTicketMode: Boolean = false
) {
    val hasActiveFilters: Boolean
        get() = query.isNotBlank() || statusFilter != null ||
            contentFilter != ContentFilter.All || genreFilter != null
}

data class WatchingUiState(
    val loading: Boolean = true,
    val entries: List<MovieEntry> = emptyList()
)

data class FavoritesUiState(
    val loading: Boolean = true,
    val entries: List<MovieEntry> = emptyList(),
    val contentFilter: ContentFilter = ContentFilter.All,
    val sortByRatingDesc: Boolean = true,
    val showRatingOnTickets: Boolean = true
)

data class StatisticsUiState(
    val loading: Boolean = true,
    val monthKey: String = "",
    val monthLabel: String = "",
    val stats: MonthlyStatistics? = null
)

data class SettingsUiState(
    val loading: Boolean = true,
    val settings: AppSettings = AppSettings(),
    val entryCount: Int = 0,
    val genreCount: Int = 0
)

data class GenreItem(
    val name: String,
    val usageCount: Int
)

data class GenreUiState(
    val loading: Boolean = true,
    val genres: List<GenreItem> = emptyList()
)

/** Detail screen: either a loaded entry or a "no longer available" state. */
data class EntryDetailUiState(
    val loading: Boolean = true,
    val entry: MovieEntry? = null,
    val missing: Boolean = false
)
