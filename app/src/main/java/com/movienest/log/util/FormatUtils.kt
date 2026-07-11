package com.movienest.log.util

import com.movienest.log.data.model.ContentType
import com.movienest.log.data.model.MovieEntry
import java.util.Locale

/** Safe numeric parsing helpers. Blank / spaces / non-numeric never throw. */
object NumberUtils {

    fun parseIntOrNull(raw: String?): Int? {
        val v = raw?.trim().orEmpty()
        if (v.isEmpty()) return null
        return v.toIntOrNull()
    }

    fun parseDoubleOrNull(raw: String?): Double? {
        val v = raw?.trim().orEmpty()
        if (v.isEmpty()) return null
        return v.toDoubleOrNull()
    }

    fun intToField(value: Int?): String = value?.toString() ?: ""

    fun doubleToField(value: Double?): String = value?.toString() ?: ""
}

/** Text helpers for notes and genres. */
object TextUtils {

    /** Trim outer whitespace but preserve internal line breaks. */
    fun trimOuter(value: String): String = value.trim()

    /** Normalize a genre for case-insensitive comparison. */
    fun normalizeGenre(value: String): String = value.trim().lowercase(Locale.ENGLISH)

    fun genreDisplay(value: String): String = value.trim()

    fun hasVisibleChar(value: String): Boolean = value.trim().isNotEmpty()
}

/** Rating display helpers. */
object RatingUtils {

    const val NOT_RATED_LABEL = "Not rated"

    /** Valid personal ratings: null (no rating) or 0.5..5.0 in half steps. */
    fun isValidRating(rating: Double?): Boolean {
        if (rating == null) return true
        if (rating < 0.5 || rating > 5.0) return false
        val doubled = rating * 2.0
        return kotlin.math.abs(doubled - kotlin.math.round(doubled)) < 1e-6
    }

    fun format(rating: Double?): String {
        if (rating == null) return NOT_RATED_LABEL
        return if (rating == rating.toLong().toDouble()) {
            "${rating.toLong()}.0"
        } else {
            String.format(Locale.ENGLISH, "%.1f", rating)
        }
    }

    /** Number of full stars for a rating (used purely for display). */
    fun fullStars(rating: Double?): Int = ((rating ?: 0.0)).toInt()

    fun hasHalfStar(rating: Double?): Boolean {
        val r = rating ?: return false
        return (r - r.toInt()) >= 0.5 - 1e-6
    }
}

/** Series progress helpers. */
object ProgressUtils {

    /**
     * Percentage 0..100, or null when a total episode count is not usable.
     * Never mutates stored values to fit a bar.
     */
    fun percentOrNull(watched: Int?, total: Int?): Int? {
        val t = total ?: return null
        if (t <= 0) return null
        val w = (watched ?: 0).coerceAtLeast(0)
        val pct = (w.toDouble() / t.toDouble()) * 100.0
        return pct.toInt().coerceIn(0, 100)
    }

    /** Descriptive label when a numeric percentage isn't available. */
    fun descriptiveLabel(entry: MovieEntry): String {
        if (entry.contentType != ContentType.Series) return ""
        val total = entry.totalEpisodes
        val watched = entry.watchedEpisodes
        val season = entry.currentSeason
        return when {
            total != null && total > 0 && watched != null ->
                "$watched / $total episodes"
            watched != null && watched > 0 -> "$watched episodes watched"
            season != null && season > 0 -> "Season $season in progress"
            else -> "Progress total not set"
        }
    }

    /** True when watched episodes exceed a known positive total. */
    fun watchedExceedsTotal(watched: Int?, total: Int?): Boolean {
        val t = total ?: return false
        if (t <= 0) return false
        val w = watched ?: return false
        return w > t
    }
}
