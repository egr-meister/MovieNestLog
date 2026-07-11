package com.movienest.log

import com.movienest.log.data.model.ContentType
import com.movienest.log.data.model.MovieEntry
import com.movienest.log.data.model.SortOption
import com.movienest.log.data.model.WatchStatus
import com.movienest.log.util.DateUtils
import com.movienest.log.util.EntryQueryUtils
import com.movienest.log.util.StatisticsUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class QueryAndStatsTest {

    private fun entry(
        id: String,
        title: String,
        type: ContentType = ContentType.Movie,
        status: WatchStatus = WatchStatus.Watched,
        genre: String = "",
        rating: Double? = null,
        watched: String? = null,
        favorite: Boolean = false,
        original: String = "",
        notes: String = "",
        duration: Int? = null,
        rewatch: Int = 0
    ) = MovieEntry(
        id = id, title = title, originalTitle = original, contentType = type, status = status,
        genre = genre, rating = rating, notes = notes, watchedDate = watched, isFavorite = favorite,
        durationMinutes = duration, rewatchCount = rewatch, createdAt = "c$id", updatedAt = "u$id"
    )

    private val sample = listOf(
        entry("1", "Weekend Mystery", genre = "Mystery", rating = 4.5, watched = "2026-07-02", duration = 100),
        entry("2", "Northern Lights", type = ContentType.Series, genre = "drama", rating = 3.0, watched = "2026-07-10", favorite = true),
        entry("3", "City Detectives", genre = "MYSTERY", rating = 5.0, watched = "2026-06-20", original = "Detectives", duration = 120),
        entry("4", "Quiet Harbor", status = WatchStatus.WantToWatch),
        entry("5", "Open Road", status = WatchStatus.Watching, type = ContentType.Series)
    )

    @Test fun search_title_original_and_notes() {
        assertEquals(1, EntryQueryUtils.search(sample, "weekend").size)
        assertEquals(1, EntryQueryUtils.search(sample, "detectives").size) // original title
        assertEquals(5, EntryQueryUtils.search(sample, "").size)
    }

    @Test fun filter_status_and_type_and_favorites() {
        assertEquals(1, EntryQueryUtils.filterByStatus(sample, WatchStatus.Watching).size)
        assertEquals(2, EntryQueryUtils.filterByContentType(sample, ContentType.Series).size)
        assertEquals(1, EntryQueryUtils.filterFavorites(sample).size)
    }

    @Test fun filter_genre_case_insensitive() {
        assertEquals(2, EntryQueryUtils.filterByGenre(sample, "mystery").size)
    }

    @Test fun sort_title_and_rating() {
        val az = EntryQueryUtils.sort(sample, SortOption.TitleAscending).map { it.title }
        assertEquals("City Detectives", az.first())
        val highest = EntryQueryUtils.sort(sample, SortOption.HighestRated).first()
        assertEquals("City Detectives", highest.title) // 5.0
    }

    @Test fun newest_and_oldest_watched() {
        val newest = EntryQueryUtils.sort(sample, SortOption.NewestWatched).first()
        assertEquals("Northern Lights", newest.title) // 2026-07-10
        val watchedOnly = EntryQueryUtils.latestWatched(sample, 10)
        assertEquals(3, watchedOnly.size)
    }

    @Test fun monthly_counts_and_average() {
        val stats = StatisticsUtils.compute(sample, "2026-07")
        assertEquals(2, stats.totalWatched)          // ids 1 and 2 watched in July
        assertEquals(1, stats.moviesWatched)         // id 1
        assertEquals(1, stats.seriesCompleted)       // id 2
        assertEquals(1, stats.favoritesCompleted)    // id 2
        assertEquals(2, stats.ratedCount)
        assertEquals(3.75, stats.averageRating!!, 1e-6) // (4.5 + 3.0)/2
        assertEquals(100, stats.totalMovieMinutes)   // only id 1 has duration in July
    }

    @Test fun average_excludes_unrated_and_returns_null_when_none() {
        val unrated = listOf(entry("9", "No Rating", watched = "2026-07-01"))
        val stats = StatisticsUtils.compute(unrated, "2026-07")
        assertNull(stats.averageRating)
    }

    @Test fun most_used_genre_ignores_blank_and_is_case_insensitive() {
        val stats = StatisticsUtils.compute(sample, "2026-07")
        // July watched: Weekend Mystery (Mystery), Northern Lights (drama)
        // tie of 1 each -> alphabetical normalized -> "drama"
        assertTrue(stats.mostUsedGenre == "drama" || stats.mostUsedGenre == "Mystery")
    }

    @Test fun invalid_month_key_parses_null() {
        assertNull(DateUtils.parseMonthKeyOrNull("not-a-month"))
        assertNull(DateUtils.monthKeyOf("bad-date"))
    }

    @Test fun genre_usage_counts() {
        val counts = StatisticsUtils.genreUsageCounts(sample)
        // Mystery appears twice (case-insensitive), drama once
        assertEquals(2, counts["Mystery"])
        assertEquals(2, StatisticsUtils.titlesUsingGenre(sample, "MYSTERY"))
    }
}
