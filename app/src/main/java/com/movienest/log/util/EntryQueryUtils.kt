package com.movienest.log.util

import com.movienest.log.data.model.ContentFilter
import com.movienest.log.data.model.ContentType
import com.movienest.log.data.model.MovieEntry
import com.movienest.log.data.model.SortOption
import com.movienest.log.data.model.WatchStatus
import java.util.Locale

/**
 * Pure, deterministic filtering / searching / sorting over entries.
 * None of these throw; missing values sort last where relevant.
 */
object EntryQueryUtils {

    fun search(entries: List<MovieEntry>, query: String): List<MovieEntry> {
        val q = query.trim().lowercase(Locale.ENGLISH)
        if (q.isEmpty()) return entries
        return entries.filter { entry ->
            entry.title.lowercase(Locale.ENGLISH).contains(q) ||
                entry.originalTitle.lowercase(Locale.ENGLISH).contains(q) ||
                entry.notes.lowercase(Locale.ENGLISH).contains(q)
        }
    }

    fun filterByStatus(entries: List<MovieEntry>, status: WatchStatus?): List<MovieEntry> =
        if (status == null) entries else entries.filter { it.status == status }

    fun filterByContentType(entries: List<MovieEntry>, type: ContentType?): List<MovieEntry> =
        if (type == null) entries else entries.filter { it.contentType == type }

    fun filterByGenre(entries: List<MovieEntry>, genre: String?): List<MovieEntry> {
        val g = genre?.trim().orEmpty()
        if (g.isEmpty()) return entries
        val target = TextUtils.normalizeGenre(g)
        return entries.filter { TextUtils.normalizeGenre(it.genre) == target }
    }

    fun filterFavorites(entries: List<MovieEntry>): List<MovieEntry> =
        entries.filter { it.isFavorite }

    /** Apply the library content filter (All / Movies / Series / Favorites). */
    fun applyContentFilter(entries: List<MovieEntry>, filter: ContentFilter): List<MovieEntry> =
        when (filter) {
            ContentFilter.All -> entries
            ContentFilter.Movies -> entries.filter { it.contentType == ContentType.Movie }
            ContentFilter.Series -> entries.filter { it.contentType == ContentType.Series }
            ContentFilter.Favorites -> entries.filter { it.isFavorite }
        }

    fun sort(entries: List<MovieEntry>, option: SortOption): List<MovieEntry> {
        val titleComparator = compareBy<MovieEntry> { it.title.lowercase(Locale.ENGLISH) }
        return when (option) {
            SortOption.RecentlyUpdated ->
                entries.sortedByDescending { it.updatedAt }
            SortOption.RecentlyAdded ->
                entries.sortedByDescending { it.createdAt }
            SortOption.TitleAscending ->
                entries.sortedWith(titleComparator)
            SortOption.TitleDescending ->
                entries.sortedWith(titleComparator.reversed())
            SortOption.NewestWatched ->
                entries.sortedWith(
                    compareByDescending<MovieEntry> { it.watchedDate != null }
                        .thenByDescending { it.watchedDate ?: "" }
                )
            SortOption.OldestWatched ->
                entries.sortedWith(
                    compareByDescending<MovieEntry> { it.watchedDate != null }
                        .thenBy { it.watchedDate ?: "" }
                )
            SortOption.HighestRated ->
                entries.sortedWith(
                    compareByDescending<MovieEntry> { it.rating != null }
                        .thenByDescending { it.rating ?: 0.0 }
                )
            SortOption.LowestRated ->
                entries.sortedWith(
                    compareByDescending<MovieEntry> { it.rating != null }
                        .thenBy { it.rating ?: Double.MAX_VALUE }
                )
        }
    }

    /** Most recently watched entries (those with a valid watched date). */
    fun latestWatched(entries: List<MovieEntry>, limit: Int): List<MovieEntry> =
        entries.filter { DateUtils.parseIsoOrNull(it.watchedDate) != null }
            .sortedByDescending { it.watchedDate }
            .take(limit.coerceAtLeast(0))

    fun countByStatus(entries: List<MovieEntry>, status: WatchStatus): Int =
        entries.count { it.status == status }

    fun countByContentType(entries: List<MovieEntry>, type: ContentType): Int =
        entries.count { it.contentType == type }
}
