package com.movienest.log.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movienest.log.data.model.ContentType
import com.movienest.log.data.model.WatchStatus
import com.movienest.log.ui.components.ConfirmDialog
import com.movienest.log.ui.components.DateInputField
import com.movienest.log.ui.components.LabeledTextField
import com.movienest.log.ui.components.PerforatedDivider
import com.movienest.log.ui.components.RatingInputRow
import com.movienest.log.ui.components.SectionHeading
import com.movienest.log.ui.state.EntryFormState
import com.movienest.log.ui.theme.MovieNestTheme
import com.movienest.log.ui.viewmodel.AppViewModelProvider
import com.movienest.log.ui.viewmodel.EntryEditorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryEditorScreen(
    onSaved: (String) -> Unit,
    onCancel: () -> Unit,
    onMissing: () -> Unit,
    viewModel: EntryEditorViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val colors = MovieNestTheme.colors

    androidx.compose.runtime.LaunchedEffect(state.saved, state.savedEntryId) {
        val id = state.savedEntryId
        if (state.saved && id != null) {
            viewModel.consumeSaved()
            onSaved(id)
        }
    }
    if (state.missing) {
        MissingEntryScreen(onGoBack = onMissing, onOpenLibrary = onMissing)
        return
    }

    val form = state.form
    var pendingTypeToMovie by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditMode) "Edit Title" else "Add Title") },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                windowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Required
            SectionHeading("Ticket Basics", Modifier.padding(start = 0.dp))
            LabeledTextField(
                label = "Title (required)",
                value = form.title,
                onValueChange = viewModel::setTitle,
                error = if (form.title.isNotEmpty()) form.titleError else null,
                helper = "${form.title.length}/120"
            )
            LabeledTextField(
                label = "Original title (optional)",
                value = form.originalTitle,
                onValueChange = viewModel::setOriginalTitle
            )

            // Type
            Text("Type", style = MaterialTheme.typography.titleSmall, color = colors.ink)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = form.contentType == ContentType.Movie,
                    onClick = {
                        if (form.contentType == ContentType.Series && viewModel.seriesProgressWouldBeLost()) {
                            pendingTypeToMovie = true
                        } else {
                            viewModel.setContentType(ContentType.Movie)
                        }
                    },
                    label = { Text("Movie") }
                )
                FilterChip(
                    selected = form.contentType == ContentType.Series,
                    onClick = { viewModel.setContentType(ContentType.Series) },
                    label = { Text("Series") }
                )
            }

            // Status
            Text("Status", style = MaterialTheme.typography.titleSmall, color = colors.ink)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                WatchStatus.entries.forEach { status ->
                    FilterChip(
                        selected = form.status == status,
                        onClick = { viewModel.setStatus(status) },
                        label = { Text(status.displayLabel) }
                    )
                }
            }

            PerforatedDivider()
            SectionHeading("Details", Modifier.padding(start = 0.dp))
            LabeledTextField(
                label = "Genre (optional)",
                value = form.genre,
                onValueChange = viewModel::setGenre,
                error = form.genreError
            )
            LabeledTextField(
                label = "Release year (optional)",
                value = form.releaseYear,
                onValueChange = viewModel::setReleaseYear,
                error = form.yearError,
                keyboardType = KeyboardType.Number
            )
            RatingInputRow(rating = form.rating, onRatingChange = viewModel::setRating)

            if (form.contentType == ContentType.Movie) {
                LabeledTextField(
                    label = "Duration in minutes (optional)",
                    value = form.durationMinutes,
                    onValueChange = viewModel::setDuration,
                    error = form.durationError,
                    keyboardType = KeyboardType.Number
                )
            }

            // Series progress
            if (form.contentType == ContentType.Series) {
                PerforatedDivider()
                SectionHeading("Series Progress", Modifier.padding(start = 0.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LabeledTextField(
                        label = "Current season",
                        value = form.currentSeason,
                        onValueChange = viewModel::setCurrentSeason,
                        error = form.currentSeasonError,
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                    LabeledTextField(
                        label = "Total seasons",
                        value = form.totalSeasons,
                        onValueChange = viewModel::setTotalSeasons,
                        error = form.totalSeasonsError,
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    LabeledTextField(
                        label = "Watched episodes",
                        value = form.watchedEpisodes,
                        onValueChange = viewModel::setWatchedEpisodes,
                        error = form.watchedEpisodesError,
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                    LabeledTextField(
                        label = "Total episodes",
                        value = form.totalEpisodes,
                        onValueChange = viewModel::setTotalEpisodes,
                        error = form.totalEpisodesError,
                        keyboardType = KeyboardType.Number,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            PerforatedDivider()
            SectionHeading("Dates", Modifier.padding(start = 0.dp))
            DateInputField(
                label = "Start date",
                value = form.startDate,
                onValueChange = viewModel::setStartDate,
                error = form.startDateError,
                emphasized = form.status == WatchStatus.Watching
            )
            DateInputField(
                label = "Watched date",
                value = form.watchedDate,
                onValueChange = viewModel::setWatchedDate,
                error = form.watchedDateError ?: form.dateOrderError,
                emphasized = form.status == WatchStatus.Watched
            )
            DateInputField(
                label = "Completion date",
                value = form.completionDate,
                onValueChange = viewModel::setCompletionDate,
                error = form.completionDateError
            )

            PerforatedDivider()
            SectionHeading("Extras", Modifier.padding(start = 0.dp))
            LabeledTextField(
                label = "Rewatch count (optional)",
                value = form.rewatchCount,
                onValueChange = viewModel::setRewatchCount,
                error = form.rewatchError,
                keyboardType = KeyboardType.Number
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Mark as favorite", style = MaterialTheme.typography.bodyLarge, color = colors.ink, modifier = Modifier.weight(1f))
                Switch(checked = form.isFavorite, onCheckedChange = viewModel::setFavorite)
            }
            LabeledTextField(
                label = "Notes (optional)",
                value = form.notes,
                onValueChange = viewModel::setNotes,
                error = form.notesError,
                helper = "${form.notesRemaining} characters remaining",
                singleLine = false,
                minLines = 3
            )

            Spacer(Modifier.height(8.dp))
            Button(
                onClick = viewModel::save,
                enabled = form.isSavable,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.isEditMode) "Save Changes" else "Add to Shelf")
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (pendingTypeToMovie) {
        ConfirmDialog(
            title = "Change to Movie?",
            message = "This title has series progress. Switching to Movie will not keep the season and episode values.",
            confirmLabel = "Change to Movie",
            onConfirm = {
                viewModel.setContentType(ContentType.Movie)
                pendingTypeToMovie = false
            },
            onDismiss = { pendingTypeToMovie = false },
            destructive = true
        )
    }
}
