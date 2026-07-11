package com.movienest.log.validation

import com.movienest.log.data.model.ContentType
import com.movienest.log.data.model.WatchStatus
import com.movienest.log.util.DateUtils
import com.movienest.log.util.ProgressUtils
import com.movienest.log.util.RatingUtils
import java.time.LocalDate

/**
 * Field-level validation. Every check returns a friendly message string or
 * null when the value is acceptable. Nothing here throws.
 */
object EntryValidation {

    const val MAX_TITLE = 120
    const val MAX_NOTES = 1000
    const val MAX_GENRE = 40
    const val MIN_YEAR = 1880
    const val MAX_DURATION = 2000

    fun maxYear(): Int = LocalDate.now().year + 10

    fun validateTitle(raw: String): String? {
        val trimmed = raw.trim()
        return when {
            trimmed.isEmpty() -> "Title is required."
            trimmed.length > MAX_TITLE -> "Title must be $MAX_TITLE characters or fewer."
            else -> null
        }
    }

    fun validateYear(raw: String): String? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null
        val year = trimmed.toIntOrNull() ?: return "Enter a valid year."
        return if (year < MIN_YEAR || year > maxYear()) {
            "Year must be between $MIN_YEAR and ${maxYear()}."
        } else null
    }

    fun validateRating(rating: Double?): String? =
        if (RatingUtils.isValidRating(rating)) null
        else "Rating must be between 0.5 and 5.0 in half-star steps."

    fun validateDuration(raw: String): String? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null
        val value = trimmed.toIntOrNull() ?: return "Enter a valid duration in minutes."
        return when {
            value <= 0 -> "Duration must be greater than zero."
            value > MAX_DURATION -> "Duration must be $MAX_DURATION minutes or fewer."
            else -> null
        }
    }

    fun validateNotes(raw: String): String? =
        if (raw.length > MAX_NOTES) "Notes must be $MAX_NOTES characters or fewer." else null

    fun validateGenre(raw: String): String? {
        val trimmed = raw.trim()
        return if (trimmed.length > MAX_GENRE) {
            "Genre must be $MAX_GENRE characters or fewer."
        } else null
    }

    /** Non-negative integer field (seasons watched, episodes, rewatch count). */
    fun validateNonNegative(raw: String, label: String): String? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null
        val value = trimmed.toIntOrNull() ?: return "Enter a valid $label."
        return if (value < 0) "$label cannot be negative." else null
    }

    /** Total fields must be > 0 when provided. */
    fun validatePositiveTotal(raw: String, label: String): String? {
        val trimmed = raw.trim()
        if (trimmed.isEmpty()) return null
        val value = trimmed.toIntOrNull() ?: return "Enter a valid $label."
        return if (value <= 0) "$label must be greater than zero." else null
    }

    fun validateWatchedEpisodes(watchedRaw: String, totalRaw: String): String? {
        val watched = watchedRaw.trim().toIntOrNull()
        val total = totalRaw.trim().toIntOrNull()
        val nonNeg = validateNonNegative(watchedRaw, "watched episodes")
        if (nonNeg != null) return nonNeg
        if (ProgressUtils.watchedExceedsTotal(watched, total)) {
            return "Watched episodes cannot exceed total episodes."
        }
        return null
    }

    fun validateDate(raw: String?): String? {
        val v = raw?.trim().orEmpty()
        if (v.isEmpty()) return null
        return if (DateUtils.isValidIsoDate(v)) null else "Enter a valid date."
    }

    fun validateWatchedRequiredForStatus(status: WatchStatus, watchedDate: String?): String? {
        if (status != WatchStatus.Watched) return null
        // Watched date is optional but if present must be valid; a missing date
        // is allowed (shown as a note in details) so we do not block saving.
        return validateDate(watchedDate)
    }

    fun validateDateOrder(startIso: String?, completionIso: String?): String? =
        if (DateUtils.isStartAfterCompletion(startIso, completionIso)) {
            "Start date cannot be after the completion date."
        } else null

    /**
     * Aggregate validity used to enable/disable the Save action.
     * Only the required fields and hard errors block saving.
     */
    fun formIsSavable(
        title: String,
        year: String,
        rating: Double?,
        duration: String,
        notes: String,
        genre: String,
        contentType: ContentType,
        totalSeasons: String,
        currentSeason: String,
        totalEpisodes: String,
        watchedEpisodes: String,
        rewatchCount: String,
        startDate: String?,
        watchedDate: String?,
        completionDate: String?
    ): Boolean {
        if (validateTitle(title) != null) return false
        if (validateYear(year) != null) return false
        if (validateRating(rating) != null) return false
        if (validateDuration(duration) != null) return false
        if (validateNotes(notes) != null) return false
        if (validateGenre(genre) != null) return false
        if (validateDate(startDate) != null) return false
        if (validateDate(watchedDate) != null) return false
        if (validateDate(completionDate) != null) return false
        if (validateDateOrder(startDate, completionDate) != null) return false
        if (validateNonNegative(rewatchCount, "rewatch count") != null) return false
        if (contentType == ContentType.Series) {
            if (validatePositiveTotal(totalSeasons, "total seasons") != null) return false
            if (validateNonNegative(currentSeason, "current season") != null) return false
            if (validatePositiveTotal(totalEpisodes, "total episodes") != null) return false
            if (validateWatchedEpisodes(watchedEpisodes, totalEpisodes) != null) return false
        }
        return true
    }
}
