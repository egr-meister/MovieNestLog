package com.movienest.log.ui.state

import com.movienest.log.data.model.ContentType
import com.movienest.log.data.model.MovieEntry
import com.movienest.log.data.model.WatchStatus
import com.movienest.log.util.NumberUtils
import com.movienest.log.util.TextUtils
import com.movienest.log.validation.EntryValidation

/**
 * All editor fields as raw strings (numbers held as text so partial typing
 * never crashes). Produces validation messages and a persisted MovieEntry.
 */
data class EntryFormState(
    val id: String? = null,
    val title: String = "",
    val originalTitle: String = "",
    val contentType: ContentType = ContentType.Movie,
    val status: WatchStatus = WatchStatus.WantToWatch,
    val genre: String = "",
    val releaseYear: String = "",
    val rating: Double? = null,
    val notes: String = "",
    val startDate: String? = null,
    val watchedDate: String? = null,
    val completionDate: String? = null,
    val durationMinutes: String = "",
    val totalSeasons: String = "",
    val currentSeason: String = "",
    val totalEpisodes: String = "",
    val watchedEpisodes: String = "",
    val isFavorite: Boolean = false,
    val rewatchCount: String = "",
    val createdAt: String = "",
    // Non-persisted flags
    val watchedExceedsWarning: Boolean = false
) {
    val isSeries: Boolean get() = contentType == ContentType.Series

    val titleError: String? get() = EntryValidation.validateTitle(title)
    val yearError: String? get() = EntryValidation.validateYear(releaseYear)
    val ratingError: String? get() = EntryValidation.validateRating(rating)
    val durationError: String? get() = EntryValidation.validateDuration(durationMinutes)
    val notesError: String? get() = EntryValidation.validateNotes(notes)
    val genreError: String? get() = EntryValidation.validateGenre(genre)
    val startDateError: String? get() = EntryValidation.validateDate(startDate)
    val watchedDateError: String? get() = EntryValidation.validateDate(watchedDate)
    val completionDateError: String? get() = EntryValidation.validateDate(completionDate)
    val dateOrderError: String? get() = EntryValidation.validateDateOrder(startDate, completionDate)
    val rewatchError: String? get() = EntryValidation.validateNonNegative(rewatchCount, "rewatch count")
    val totalSeasonsError: String?
        get() = if (isSeries) EntryValidation.validatePositiveTotal(totalSeasons, "total seasons") else null
    val currentSeasonError: String?
        get() = if (isSeries) EntryValidation.validateNonNegative(currentSeason, "current season") else null
    val totalEpisodesError: String?
        get() = if (isSeries) EntryValidation.validatePositiveTotal(totalEpisodes, "total episodes") else null
    val watchedEpisodesError: String?
        get() = if (isSeries) EntryValidation.validateWatchedEpisodes(watchedEpisodes, totalEpisodes) else null

    val notesRemaining: Int get() = EntryValidation.MAX_NOTES - notes.length

    val isSavable: Boolean
        get() = EntryValidation.formIsSavable(
            title = title,
            year = releaseYear,
            rating = rating,
            duration = durationMinutes,
            notes = notes,
            genre = genre,
            contentType = contentType,
            totalSeasons = totalSeasons,
            currentSeason = currentSeason,
            totalEpisodes = totalEpisodes,
            watchedEpisodes = watchedEpisodes,
            rewatchCount = rewatchCount,
            startDate = startDate,
            watchedDate = watchedDate,
            completionDate = completionDate
        )

    fun toEntry(newId: String): MovieEntry {
        val rewatch = NumberUtils.parseIntOrNull(rewatchCount)?.coerceAtLeast(0) ?: 0
        return MovieEntry(
            id = id ?: newId,
            title = title.trim(),
            originalTitle = originalTitle.trim(),
            contentType = contentType,
            status = status,
            genre = TextUtils.genreDisplay(genre),
            releaseYear = NumberUtils.parseIntOrNull(releaseYear),
            rating = rating,
            notes = TextUtils.trimOuter(notes),
            startDate = startDate?.trim()?.ifBlank { null },
            watchedDate = watchedDate?.trim()?.ifBlank { null },
            completionDate = completionDate?.trim()?.ifBlank { null },
            durationMinutes = if (isSeries) NumberUtils.parseIntOrNull(durationMinutes)
            else NumberUtils.parseIntOrNull(durationMinutes),
            totalSeasons = if (isSeries) NumberUtils.parseIntOrNull(totalSeasons) else null,
            currentSeason = if (isSeries) NumberUtils.parseIntOrNull(currentSeason) else null,
            totalEpisodes = if (isSeries) NumberUtils.parseIntOrNull(totalEpisodes) else null,
            watchedEpisodes = if (isSeries) NumberUtils.parseIntOrNull(watchedEpisodes) else null,
            isFavorite = isFavorite,
            rewatchCount = rewatch,
            createdAt = createdAt,
            updatedAt = ""
        )
    }

    companion object {
        fun fromEntry(entry: MovieEntry): EntryFormState = EntryFormState(
            id = entry.id,
            title = entry.title,
            originalTitle = entry.originalTitle,
            contentType = entry.contentType,
            status = entry.status,
            genre = entry.genre,
            releaseYear = NumberUtils.intToField(entry.releaseYear),
            rating = entry.rating,
            notes = entry.notes,
            startDate = entry.startDate,
            watchedDate = entry.watchedDate,
            completionDate = entry.completionDate,
            durationMinutes = NumberUtils.intToField(entry.durationMinutes),
            totalSeasons = NumberUtils.intToField(entry.totalSeasons),
            currentSeason = NumberUtils.intToField(entry.currentSeason),
            totalEpisodes = NumberUtils.intToField(entry.totalEpisodes),
            watchedEpisodes = NumberUtils.intToField(entry.watchedEpisodes),
            isFavorite = entry.isFavorite,
            rewatchCount = if (entry.rewatchCount == 0) "" else entry.rewatchCount.toString(),
            createdAt = entry.createdAt
        )
    }
}
