package com.movienest.log.util

import com.movienest.log.data.model.ContentType
import com.movienest.log.data.model.MovieEntry
import com.movienest.log.data.model.WatchStatus
import java.util.Locale

/** Result of aggregating a single month of watched titles. */
data class MonthlyStatistics(
    val monthKey: String,
    val totalWatched: Int,
    val moviesWatched: Int,
    val seriesCompleted: Int,
    val averageRating: Double?,
    val ratedCount: Int,
    val favoritesCompleted: Int,
    val totalMovieMinutes: Int,
    val totalRewatches: Int,
    val mostUsedGenre: String?,
    val topRated: List<MovieEntry>,
    val watchedEntries: List<MovieEntry>
) {
    val hasData: Boolean get() = totalWatched > 0
}

object StatisticsUtils {

    /** Entries that count as "watched in this month" — must be Watched with a matching watchedDate. */
    fun watchedInMonth(entries: List<MovieEntry>, monthKey: String): List<MovieEntry> =
        entries.filter {
            it.status == WatchStatus.Watched &&
                DateUtils.monthKeyOf(it.watchedDate) == monthKey
        }

    /**
     * Average rating over rated entries only. Returns null when there are no
     * rated entries so callers can show "No rated titles" instead of zero.
     */
    fun averageRating(entries: List<MovieEntry>): Double? {
        val rated = entries.mapNotNull { it.rating }.filter { it in 0.5..5.0 }
        if (rated.isEmpty()) return null
        return rated.sum() / rated.size
    }

    /**
     * Most used non-blank genre, case-insensitive grouping, preserving a
     * display capitalization. Null when there is no genre data. Ties resolved
     * neutrally (alphabetical) for deterministic output.
     */
    fun mostUsedGenre(entries: List<MovieEntry>): String? {
        val withGenre = entries.filter { it.genre.trim().isNotEmpty() }
        if (withGenre.isEmpty()) return null
        val groups = withGenre.groupBy { TextUtils.normalizeGenre(it.genre) }
        val best = groups.entries
            .sortedWith(
                compareByDescending<Map.Entry<String, List<MovieEntry>>> { it.value.size }
                    .thenBy { it.key }
            )
            .firstOrNull() ?: return null
        // Preferred display capitalization = first occurrence's display form.
        return best.value.first().genre.trim()
    }

    fun compute(entries: List<MovieEntry>, monthKey: String): MonthlyStatistics {
        val watched = watchedInMonth(entries, monthKey)
        val movies = watched.filter { it.contentType == ContentType.Movie }
        val series = watched.filter { it.contentType == ContentType.Series }
        val ratedValues = watched.mapNotNull { it.rating }.filter { it in 0.5..5.0 }
        val avg = if (ratedValues.isEmpty()) null else ratedValues.sum() / ratedValues.size
        // Only include real durations; never treat a missing duration as zero.
        val movieMinutes = movies.mapNotNull { it.durationMinutes }.filter { it > 0 }.sum()
        val rewatches = watched.sumOf { it.rewatchCount.coerceAtLeast(0) }
        val topRated = watched.filter { it.rating != null }
            .sortedByDescending { it.rating ?: 0.0 }
            .take(5)

        return MonthlyStatistics(
            monthKey = monthKey,
            totalWatched = watched.size,
            moviesWatched = movies.size,
            seriesCompleted = series.size,
            averageRating = avg,
            ratedCount = ratedValues.size,
            favoritesCompleted = watched.count { it.isFavorite },
            totalMovieMinutes = movieMinutes,
            totalRewatches = rewatches,
            mostUsedGenre = mostUsedGenre(watched),
            topRated = topRated,
            watchedEntries = watched.sortedByDescending { it.watchedDate }
        )
    }

    /** Count of how many entries use each genre (case-insensitive), keyed by display form. */
    fun genreUsageCounts(entries: List<MovieEntry>): Map<String, Int> {
        val result = LinkedHashMap<String, Int>()
        val normalizedToDisplay = LinkedHashMap<String, String>()
        entries.filter { it.genre.trim().isNotEmpty() }.forEach { entry ->
            val norm = TextUtils.normalizeGenre(entry.genre)
            val display = normalizedToDisplay.getOrPut(norm) { entry.genre.trim() }
            result[display] = (result[display] ?: 0) + 1
        }
        return result
    }

    fun titlesUsingGenre(entries: List<MovieEntry>, genre: String): Int {
        val target = TextUtils.normalizeGenre(genre)
        if (target.isEmpty()) return 0
        return entries.count { TextUtils.normalizeGenre(it.genre) == target }
    }
}
