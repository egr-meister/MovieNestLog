package com.movienest.log.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.movienest.log.data.model.ContentType
import com.movienest.log.data.model.WatchStatus
import com.movienest.log.data.repository.MovieNestRepository
import com.movienest.log.ui.navigation.Routes
import com.movienest.log.ui.state.EntryFormState
import com.movienest.log.util.DateUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Wraps the form plus load status for the shared Add / Edit screen. */
data class EditorUiState(
    val loading: Boolean = true,
    val isEditMode: Boolean = false,
    val missing: Boolean = false,
    val form: EntryFormState = EntryFormState(),
    val saved: Boolean = false,
    val savedEntryId: String? = null
)

class EntryEditorViewModel(
    private val repository: MovieNestRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val entryId: String? =
        savedStateHandle.get<String>(Routes.ARG_ENTRY_ID)?.takeIf { it.isNotBlank() }
    private val startStatus: String? = savedStateHandle.get<String>(Routes.ARG_START_STATUS)

    private val _state = MutableStateFlow(EditorUiState())
    val state: StateFlow<EditorUiState> = _state.asStateFlow()

    init {
        if (entryId != null) {
            loadForEdit(entryId)
        } else {
            val status = WatchStatus.fromNameSafe(startStatus)
            _state.value = EditorUiState(
                loading = false,
                isEditMode = false,
                form = EntryFormState(status = status)
            )
        }
    }

    private fun loadForEdit(id: String) {
        viewModelScope.launch {
            val entries = repository.entriesFlow.first()
            val entry = entries.firstOrNull { it.id == id }
            if (entry == null) {
                _state.value = EditorUiState(loading = false, isEditMode = true, missing = true)
            } else {
                _state.value = EditorUiState(
                    loading = false,
                    isEditMode = true,
                    form = EntryFormState.fromEntry(entry)
                )
            }
        }
    }

    private fun updateForm(transform: (EntryFormState) -> EntryFormState) {
        _state.value = _state.value.copy(form = transform(_state.value.form))
    }

    // ---- Field setters -----------------------------------------------------
    fun setTitle(v: String) = updateForm { it.copy(title = v.take(200)) }
    fun setOriginalTitle(v: String) = updateForm { it.copy(originalTitle = v.take(200)) }
    fun setGenre(v: String) = updateForm { it.copy(genre = v.take(60)) }
    fun setReleaseYear(v: String) = updateForm { it.copy(releaseYear = v.filterDigits(4)) }
    fun setRating(v: Double?) = updateForm { it.copy(rating = v) }
    fun setNotes(v: String) = updateForm { it.copy(notes = v.take(1000)) }
    fun setDuration(v: String) = updateForm { it.copy(durationMinutes = v.filterDigits(5)) }
    fun setTotalSeasons(v: String) = updateForm { it.copy(totalSeasons = v.filterDigits(4)) }
    fun setCurrentSeason(v: String) = updateForm { it.copy(currentSeason = v.filterDigits(4)) }
    fun setTotalEpisodes(v: String) = updateForm { it.copy(totalEpisodes = v.filterDigits(5)) }
    fun setWatchedEpisodes(v: String) = updateForm {
        val next = it.copy(watchedEpisodes = v.filterDigits(5))
        next.copy(watchedExceedsWarning = next.watchedEpisodesError != null)
    }
    fun setRewatchCount(v: String) = updateForm { it.copy(rewatchCount = v.filterDigits(4)) }
    fun setFavorite(v: Boolean) = updateForm { it.copy(isFavorite = v) }
    fun setStartDate(v: String?) = updateForm { it.copy(startDate = v) }
    fun setWatchedDate(v: String?) = updateForm { it.copy(watchedDate = v) }
    fun setCompletionDate(v: String?) = updateForm { it.copy(completionDate = v) }

    fun setContentType(type: ContentType) = updateForm { form ->
        if (type == ContentType.Movie) {
            // Keep series values in memory in case the user switches back, but
            // they will not be persisted for a movie.
            form.copy(contentType = ContentType.Movie)
        } else {
            form.copy(contentType = ContentType.Series)
        }
    }

    /** True when switching to Movie would drop existing series progress. */
    fun seriesProgressWouldBeLost(): Boolean {
        val f = _state.value.form
        if (f.contentType != ContentType.Series) return false
        return listOf(f.totalSeasons, f.currentSeason, f.totalEpisodes, f.watchedEpisodes)
            .any { it.trim().isNotEmpty() }
    }

    fun setStatus(status: WatchStatus) = updateForm { form ->
        var next = form.copy(status = status)
        if (status == WatchStatus.Watching && next.startDate.isNullOrBlank()) {
            next = next.copy(startDate = DateUtils.todayIso())
        }
        if (status == WatchStatus.Watched && next.watchedDate.isNullOrBlank()) {
            next = next.copy(watchedDate = DateUtils.todayIso())
        }
        next
    }

    /** For Watching→Watched with a known total: optionally fill watched episodes. */
    fun fillEpisodesToTotalOnComplete() = updateForm { form ->
        val total = form.totalEpisodes.trim().toIntOrNull()
        if (form.isSeries && total != null && total > 0) {
            form.copy(watchedEpisodes = total.toString(), watchedExceedsWarning = false)
        } else form
    }

    fun save() {
        val form = _state.value.form
        if (!form.isSavable) return
        viewModelScope.launch {
            val savedId: String
            if (_state.value.isEditMode && form.id != null) {
                // Confirm the entry still exists before saving.
                val exists = repository.entriesFlow.first().any { it.id == form.id }
                if (!exists) {
                    _state.value = _state.value.copy(missing = true)
                    return@launch
                }
                repository.updateEntry(form.toEntry(form.id))
                savedId = form.id
            } else {
                val newId = repository.newEntryId()
                val created = repository.addEntry(form.toEntry(newId))
                savedId = created.id
            }
            _state.value = _state.value.copy(saved = true, savedEntryId = savedId)
        }
    }

    fun consumeSaved() { _state.value = _state.value.copy(saved = false) }
}

private fun String.filterDigits(max: Int): String =
    filter { it.isDigit() }.take(max)
