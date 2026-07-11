package com.movienest.log

import com.movienest.log.data.model.ContentType
import com.movienest.log.util.ProgressUtils
import com.movienest.log.util.RatingUtils
import com.movienest.log.validation.EntryValidation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ValidationTest {

    @Test fun title_required() {
        assertNotNull(EntryValidation.validateTitle("   "))
        assertNull(EntryValidation.validateTitle("Weekend Mystery"))
    }

    @Test fun title_too_long() {
        assertNotNull(EntryValidation.validateTitle("x".repeat(121)))
        assertNull(EntryValidation.validateTitle("x".repeat(120)))
    }

    @Test fun year_range() {
        assertNull(EntryValidation.validateYear(""))
        assertNotNull(EntryValidation.validateYear("1200"))
        assertNotNull(EntryValidation.validateYear("abc"))
        assertNull(EntryValidation.validateYear("1999"))
    }

    @Test fun rating_half_steps() {
        assertTrue(RatingUtils.isValidRating(null))
        assertTrue(RatingUtils.isValidRating(0.5))
        assertTrue(RatingUtils.isValidRating(4.5))
        assertTrue(RatingUtils.isValidRating(5.0))
        assertFalse(RatingUtils.isValidRating(0.3))
        assertFalse(RatingUtils.isValidRating(5.5))
        assertFalse(RatingUtils.isValidRating(0.0))
    }

    @Test fun rating_format() {
        assertEquals("Not rated", RatingUtils.format(null))
        assertEquals("4.0", RatingUtils.format(4.0))
        assertEquals("3.5", RatingUtils.format(3.5))
    }

    @Test fun duration_positive() {
        assertNull(EntryValidation.validateDuration(""))
        assertNotNull(EntryValidation.validateDuration("0"))
        assertNotNull(EntryValidation.validateDuration("-5"))
        assertNull(EntryValidation.validateDuration("120"))
        assertNotNull(EntryValidation.validateDuration("5000"))
    }

    @Test fun progress_percent_guards_zero_and_missing() {
        assertNull(ProgressUtils.percentOrNull(3, null))
        assertNull(ProgressUtils.percentOrNull(3, 0))
        assertEquals(50, ProgressUtils.percentOrNull(5, 10))
        assertEquals(100, ProgressUtils.percentOrNull(20, 10)) // clamped
        assertEquals(0, ProgressUtils.percentOrNull(0, 10))
    }

    @Test fun watched_exceeds_total() {
        assertTrue(ProgressUtils.watchedExceedsTotal(12, 10))
        assertFalse(ProgressUtils.watchedExceedsTotal(10, 10))
        assertFalse(ProgressUtils.watchedExceedsTotal(5, null))
    }

    @Test fun watched_episodes_validation_message() {
        assertNotNull(EntryValidation.validateWatchedEpisodes("12", "10"))
        assertNull(EntryValidation.validateWatchedEpisodes("9", "10"))
        assertNotNull(EntryValidation.validateNonNegative("-1", "watched episodes"))
    }

    @Test fun positive_totals() {
        assertNotNull(EntryValidation.validatePositiveTotal("0", "total episodes"))
        assertNull(EntryValidation.validatePositiveTotal("", "total episodes"))
        assertNull(EntryValidation.validatePositiveTotal("10", "total episodes"))
    }

    @Test fun date_order() {
        assertNotNull(EntryValidation.validateDateOrder("2026-05-10", "2026-05-01"))
        assertNull(EntryValidation.validateDateOrder("2026-05-01", "2026-05-10"))
        assertNull(EntryValidation.validateDateOrder(null, "2026-05-10"))
    }

    @Test fun form_savable_series() {
        val ok = EntryValidation.formIsSavable(
            title = "City Detectives", year = "2021", rating = 4.5, duration = "",
            notes = "", genre = "Mystery", contentType = ContentType.Series,
            totalSeasons = "3", currentSeason = "2", totalEpisodes = "30",
            watchedEpisodes = "18", rewatchCount = "", startDate = "2026-01-01",
            watchedDate = null, completionDate = null
        )
        assertTrue(ok)
        val bad = EntryValidation.formIsSavable(
            title = "", year = "", rating = null, duration = "",
            notes = "", genre = "", contentType = ContentType.Movie,
            totalSeasons = "", currentSeason = "", totalEpisodes = "",
            watchedEpisodes = "", rewatchCount = "", startDate = null,
            watchedDate = null, completionDate = null
        )
        assertFalse(bad)
    }
}
