package com.movienest.log.data.repository

import android.content.Context
import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.movienest.log.data.model.AppSettings
import com.movienest.log.data.model.ContentType
import com.movienest.log.data.model.DEFAULT_GENRES
import com.movienest.log.data.model.MovieEntry
import com.movienest.log.data.model.WatchStatus
import com.movienest.log.util.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.UUID

private const val TAG = "MovieNestRepository"

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "movienest_prefs")

/**
 * Single source of truth for all locally-stored data.
 * Persists three JSON strings in DataStore Preferences and recovers safely
 * from empty / corrupted / partial data. No persistence exception is allowed
 * to reach the Compose layer.
 */
class MovieNestRepository(private val context: Context) {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
        isLenient = true
    }

    private object Keys {
        val ENTRIES = stringPreferencesKey("entries_json")
        val GENRES = stringPreferencesKey("genres_json")
        val SETTINGS = stringPreferencesKey("settings_json")
    }

    // ---- Observable flows -------------------------------------------------

    val entriesFlow: Flow<List<MovieEntry>> = context.dataStore.data
        .catch { e ->
            Log.w(TAG, "DataStore read failed for entries: ${e.javaClass.simpleName}")
            emit(androidx.datastore.preferences.core.emptyPreferences())
        }
        .map { prefs -> decodeEntries(prefs[Keys.ENTRIES]) }

    val genresFlow: Flow<List<String>> = context.dataStore.data
        .catch { e ->
            Log.w(TAG, "DataStore read failed for genres: ${e.javaClass.simpleName}")
            emit(androidx.datastore.preferences.core.emptyPreferences())
        }
        .map { prefs -> decodeGenres(prefs[Keys.GENRES]) }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data
        .catch { e ->
            Log.w(TAG, "DataStore read failed for settings: ${e.javaClass.simpleName}")
            emit(androidx.datastore.preferences.core.emptyPreferences())
        }
        .map { prefs -> decodeSettings(prefs[Keys.SETTINGS]) }

    // ---- Safe decoding ----------------------------------------------------

    private fun decodeEntries(raw: String?): List<MovieEntry> {
        val text = raw?.trim().orEmpty()
        if (text.isEmpty()) return emptyList()
        return try {
            json.decodeFromString<List<MovieEntry>>(text)
                .filter { it.id.isNotBlank() }
        } catch (e: Exception) {
            Log.w(TAG, "Corrupted entries JSON; returning empty list.")
            emptyList()
        }
    }

    private fun decodeGenres(raw: String?): List<String> {
        val text = raw?.trim().orEmpty()
        if (text.isEmpty()) return DEFAULT_GENRES
        return try {
            val list = json.decodeFromString<List<String>>(text)
            list.map { it.trim() }.filter { it.isNotEmpty() }
        } catch (e: Exception) {
            Log.w(TAG, "Corrupted genres JSON; returning defaults.")
            DEFAULT_GENRES
        }
    }

    private fun decodeSettings(raw: String?): AppSettings {
        val text = raw?.trim().orEmpty()
        if (text.isEmpty()) return AppSettings()
        return try {
            json.decodeFromString<AppSettings>(text)
        } catch (e: Exception) {
            Log.w(TAG, "Corrupted settings JSON; returning defaults.")
            AppSettings()
        }
    }

    // ---- Internal safe read for mutations --------------------------------

    private suspend fun currentEntries(): List<MovieEntry> =
        readOnce(Keys.ENTRIES) { decodeEntries(it) } ?: emptyList()

    private suspend fun currentGenres(): List<String> =
        readOnce(Keys.GENRES) { decodeGenres(it) } ?: DEFAULT_GENRES

    private suspend fun currentSettings(): AppSettings =
        readOnce(Keys.SETTINGS) { decodeSettings(it) } ?: AppSettings()

    private suspend fun <T> readOnce(
        key: Preferences.Key<String>,
        decode: (String?) -> T
    ): T? {
        return try {
            val prefs = context.dataStore.data
                .catch { emit(androidx.datastore.preferences.core.emptyPreferences()) }
                .first()
            decode(prefs[key])
        } catch (e: Exception) {
            Log.w(TAG, "readOnce failed: ${e.javaClass.simpleName}")
            null
        }
    }

    private suspend fun writeEntries(entries: List<MovieEntry>) {
        try {
            val encoded = json.encodeToString(entries)
            context.dataStore.edit { it[Keys.ENTRIES] = encoded }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to write entries: ${e.javaClass.simpleName}")
        }
    }

    private suspend fun writeGenres(genres: List<String>) {
        try {
            val cleaned = dedupeGenres(genres)
            context.dataStore.edit { it[Keys.GENRES] = json.encodeToString(cleaned) }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to write genres: ${e.javaClass.simpleName}")
        }
    }

    private suspend fun writeSettings(settings: AppSettings) {
        try {
            context.dataStore.edit { it[Keys.SETTINGS] = json.encodeToString(settings) }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to write settings: ${e.javaClass.simpleName}")
        }
    }

    private fun dedupeGenres(genres: List<String>): List<String> {
        val seen = LinkedHashMap<String, String>()
        genres.map { it.trim() }.filter { it.isNotEmpty() }.forEach { g ->
            val key = g.lowercase()
            if (!seen.containsKey(key)) seen[key] = g
        }
        return seen.values.toList()
    }

    // ---- Entry CRUD -------------------------------------------------------

    fun newEntryId(): String = UUID.randomUUID().toString()

    suspend fun addEntry(entry: MovieEntry): MovieEntry {
        val now = DateUtils.nowTimestamp()
        val safeId = entry.id.ifBlank { newEntryId() }
        val toStore = entry.copy(
            id = safeId,
            createdAt = entry.createdAt.ifBlank { now },
            updatedAt = now
        )
        val list = currentEntries().toMutableList()
        // Guard against duplicate IDs.
        list.removeAll { it.id == safeId }
        list.add(toStore)
        writeEntries(list)
        return toStore
    }

    suspend fun updateEntry(entry: MovieEntry) {
        val list = currentEntries().toMutableList()
        val index = list.indexOfFirst { it.id == entry.id }
        val updated = entry.copy(updatedAt = DateUtils.nowTimestamp())
        if (index >= 0) {
            list[index] = updated
            writeEntries(list)
        } else {
            Log.w(TAG, "updateEntry: id not found, ignoring.")
        }
    }

    suspend fun deleteEntry(entryId: String) {
        val list = currentEntries().filterNot { it.id == entryId }
        writeEntries(list)
    }

    suspend fun toggleFavorite(entryId: String) {
        val list = currentEntries().toMutableList()
        val index = list.indexOfFirst { it.id == entryId }
        if (index >= 0) {
            val e = list[index]
            list[index] = e.copy(isFavorite = !e.isFavorite, updatedAt = DateUtils.nowTimestamp())
            writeEntries(list)
        }
    }

    suspend fun changeStatus(entryId: String, status: WatchStatus, suggestWatchedDate: Boolean) {
        val list = currentEntries().toMutableList()
        val index = list.indexOfFirst { it.id == entryId }
        if (index < 0) return
        val e = list[index]
        var updated = e.copy(status = status, updatedAt = DateUtils.nowTimestamp())
        // Suggest dates without ever deleting existing data.
        if (status == WatchStatus.Watching && e.startDate.isNullOrBlank()) {
            updated = updated.copy(startDate = DateUtils.todayIso())
        }
        if (status == WatchStatus.Watched && suggestWatchedDate && e.watchedDate.isNullOrBlank()) {
            updated = updated.copy(watchedDate = DateUtils.todayIso())
        }
        list[index] = updated
        writeEntries(list)
    }

    /** Increment watched episodes by one, clamped to a known total. Series only. */
    suspend fun incrementEpisode(entryId: String) {
        val list = currentEntries().toMutableList()
        val index = list.indexOfFirst { it.id == entryId }
        if (index < 0) return
        val e = list[index]
        if (e.contentType != ContentType.Series) return
        val current = (e.watchedEpisodes ?: 0).coerceAtLeast(0)
        val total = e.totalEpisodes
        val next = if (total != null && total > 0) (current + 1).coerceAtMost(total) else current + 1
        list[index] = e.copy(watchedEpisodes = next, updatedAt = DateUtils.nowTimestamp())
        writeEntries(list)
    }

    suspend fun updateSeriesProgress(
        entryId: String,
        currentSeason: Int?,
        totalSeasons: Int?,
        watchedEpisodes: Int?,
        totalEpisodes: Int?
    ) {
        val list = currentEntries().toMutableList()
        val index = list.indexOfFirst { it.id == entryId }
        if (index < 0) return
        val e = list[index]
        list[index] = e.copy(
            currentSeason = currentSeason?.coerceAtLeast(0),
            totalSeasons = totalSeasons,
            watchedEpisodes = watchedEpisodes?.coerceAtLeast(0),
            totalEpisodes = totalEpisodes,
            updatedAt = DateUtils.nowTimestamp()
        )
        writeEntries(list)
    }

    // ---- Genre management -------------------------------------------------

    suspend fun addGenre(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        val genres = currentGenres().toMutableList()
        if (genres.none { it.equals(trimmed, ignoreCase = true) }) {
            genres.add(trimmed)
            writeGenres(genres)
        }
    }

    suspend fun renameGenre(oldName: String, newName: String) {
        val newTrim = newName.trim()
        if (newTrim.isEmpty()) return
        val genres = currentGenres().toMutableList()
        val idx = genres.indexOfFirst { it.equals(oldName, ignoreCase = true) }
        if (idx >= 0) {
            genres[idx] = newTrim
            writeGenres(genres)
        }
        // Update entries that used the old label (case-insensitive), preserving them.
        val entries = currentEntries().map { entry ->
            if (entry.genre.equals(oldName, ignoreCase = true)) {
                entry.copy(genre = newTrim, updatedAt = DateUtils.nowTimestamp())
            } else entry
        }
        writeEntries(entries)
    }

    /** Remove a genre from the label list only. Entries keep their genre text. */
    suspend fun removeGenreLabel(name: String) {
        val genres = currentGenres().filterNot { it.equals(name, ignoreCase = true) }
        writeGenres(genres)
    }

    /** Remove a genre and clear that genre from any entries that used it. */
    suspend fun removeGenreAndClearFromEntries(name: String) {
        val genres = currentGenres().filterNot { it.equals(name, ignoreCase = true) }
        writeGenres(genres)
        val entries = currentEntries().map { entry ->
            if (entry.genre.equals(name, ignoreCase = true)) {
                entry.copy(genre = "", updatedAt = DateUtils.nowTimestamp())
            } else entry
        }
        writeEntries(entries)
    }

    // ---- Settings ---------------------------------------------------------

    suspend fun updateSettings(transform: (AppSettings) -> AppSettings) {
        val current = currentSettings()
        writeSettings(transform(current))
    }

    suspend fun setOnboardingCompleted(completed: Boolean) {
        updateSettings { it.copy(onboardingCompleted = completed) }
    }

    suspend fun resetFilters() {
        updateSettings {
            it.copy(
                selectedSort = com.movienest.log.data.model.SortOption.RecentlyUpdated,
                selectedContentFilter = com.movienest.log.data.model.ContentFilter.All,
                selectedGenre = null
            )
        }
    }

    // ---- Destructive ------------------------------------------------------

    suspend fun deleteAllEntries() {
        writeEntries(emptyList())
    }

    suspend fun resetAllData() {
        try {
            context.dataStore.edit { it.clear() }
        } catch (e: Exception) {
            Log.w(TAG, "resetAllData failed: ${e.javaClass.simpleName}")
        }
    }
}
