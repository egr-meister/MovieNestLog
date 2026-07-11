package com.movienest.log.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * The type of a tracked title. Unknown/legacy values decode to [Movie]
 * to keep old stored data usable after schema changes.
 */
@Serializable
enum class ContentType {
    @SerialName("Movie")
    Movie,

    @SerialName("Series")
    Series;

    companion object {
        fun fromNameSafe(raw: String?): ContentType =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: Movie
    }
}

/** Where a title sits in the personal viewing pipeline. */
@Serializable
enum class WatchStatus {
    @SerialName("WantToWatch")
    WantToWatch,

    @SerialName("Watching")
    Watching,

    @SerialName("Watched")
    Watched;

    val displayLabel: String
        get() = when (this) {
            WantToWatch -> "Want to Watch"
            Watching -> "Watching"
            Watched -> "Watched"
        }

    companion object {
        fun fromNameSafe(raw: String?): WatchStatus =
            entries.firstOrNull { it.name.equals(raw, ignoreCase = true) } ?: WantToWatch
    }
}

@Serializable
enum class SortOption {
    RecentlyUpdated,
    TitleAscending,
    TitleDescending,
    NewestWatched,
    OldestWatched,
    HighestRated,
    LowestRated,
    RecentlyAdded;

    val displayLabel: String
        get() = when (this) {
            RecentlyUpdated -> "Recently Updated"
            TitleAscending -> "Title A–Z"
            TitleDescending -> "Title Z–A"
            NewestWatched -> "Newest Watched"
            OldestWatched -> "Oldest Watched"
            HighestRated -> "Highest Rated"
            LowestRated -> "Lowest Rated"
            RecentlyAdded -> "Recently Added"
        }
}

@Serializable
enum class ContentFilter {
    All,
    Movies,
    Series,
    Favorites;

    val displayLabel: String
        get() = when (this) {
            All -> "All"
            Movies -> "Movies"
            Series -> "Series"
            Favorites -> "Favorites"
        }
}

/**
 * A single manually-entered movie or series record.
 * Every field has a safe default so older stored JSON that is missing
 * newer fields still decodes without throwing.
 */
@Serializable
data class MovieEntry(
    val id: String,
    val title: String = "",
    val originalTitle: String = "",
    val contentType: ContentType = ContentType.Movie,
    val status: WatchStatus = WatchStatus.WantToWatch,
    val genre: String = "",
    val releaseYear: Int? = null,
    val rating: Double? = null,
    val notes: String = "",
    val startDate: String? = null,
    val watchedDate: String? = null,
    val completionDate: String? = null,
    val durationMinutes: Int? = null,
    val totalSeasons: Int? = null,
    val currentSeason: Int? = null,
    val totalEpisodes: Int? = null,
    val watchedEpisodes: Int? = null,
    val isFavorite: Boolean = false,
    val rewatchCount: Int = 0,
    val createdAt: String = "",
    val updatedAt: String = ""
)

@Serializable
data class AppSettings(
    val onboardingCompleted: Boolean = false,
    val defaultHomeShelf: WatchStatus = WatchStatus.WantToWatch,
    val selectedSort: SortOption = SortOption.RecentlyUpdated,
    val selectedContentFilter: ContentFilter = ContentFilter.All,
    val selectedGenre: String? = null,
    val statisticsMonth: String? = null,
    val showRatingOnTickets: Boolean = true,
    val compactTicketMode: Boolean = false
)

@Serializable
data class AppData(
    val entries: List<MovieEntry> = emptyList(),
    val genres: List<String> = emptyList(),
    val settings: AppSettings = AppSettings()
)

/** Default genre labels offered on first launch. Not copyrighted content. */
val DEFAULT_GENRES: List<String> = listOf(
    "Action", "Animation", "Comedy", "Documentary", "Drama", "Fantasy",
    "Horror", "Mystery", "Romance", "Sci-Fi", "Thriller", "Other"
)
