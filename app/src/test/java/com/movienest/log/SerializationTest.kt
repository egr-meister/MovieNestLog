package com.movienest.log

import com.movienest.log.data.model.AppSettings
import com.movienest.log.data.model.ContentType
import com.movienest.log.data.model.MovieEntry
import com.movienest.log.data.model.WatchStatus
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SerializationTest {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true; isLenient = true }

    @Test fun entry_round_trip() {
        val entry = MovieEntry(
            id = "abc", title = "Weekend Mystery", contentType = ContentType.Series,
            status = WatchStatus.Watching, genre = "Mystery", rating = 4.5,
            totalEpisodes = 20, watchedEpisodes = 8, createdAt = "t", updatedAt = "t"
        )
        val text = json.encodeToString(listOf(entry))
        val back = json.decodeFromString<List<MovieEntry>>(text)
        assertEquals(entry, back.first())
    }

    @Test fun missing_fields_merge_defaults() {
        // Simulates an older stored entry that lacks newer fields.
        val partial = """[{"id":"x1","title":"Old Title"}]"""
        val back = json.decodeFromString<List<MovieEntry>>(partial)
        val e = back.first()
        assertEquals("x1", e.id)
        assertEquals(ContentType.Movie, e.contentType)
        assertEquals(WatchStatus.WantToWatch, e.status)
        assertEquals(0, e.rewatchCount)
        assertTrue(e.notes.isEmpty())
    }

    @Test fun unknown_keys_are_ignored() {
        val withExtra = """[{"id":"x2","title":"Future","surpriseField":123}]"""
        val back = json.decodeFromString<List<MovieEntry>>(withExtra)
        assertEquals("Future", back.first().title)
    }

    @Test fun settings_round_trip() {
        val settings = AppSettings(onboardingCompleted = true, defaultHomeShelf = WatchStatus.Watching)
        val back = json.decodeFromString<AppSettings>(json.encodeToString(settings))
        assertEquals(settings, back)
    }
}
