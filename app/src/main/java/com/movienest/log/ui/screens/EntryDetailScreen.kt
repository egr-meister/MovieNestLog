package com.movienest.log.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movienest.log.data.model.ContentType
import com.movienest.log.data.model.MovieEntry
import com.movienest.log.data.model.WatchStatus
import com.movienest.log.ui.components.ConfirmDialog
import com.movienest.log.ui.components.FavoriteToggle
import com.movienest.log.ui.components.GenreStamp
import com.movienest.log.ui.components.PerforatedDivider
import com.movienest.log.ui.components.ProgressStrip
import com.movienest.log.ui.components.RatingStars
import com.movienest.log.ui.components.StatusStamp
import com.movienest.log.ui.components.TicketShape
import com.movienest.log.ui.theme.MovieNestTheme
import com.movienest.log.ui.viewmodel.AppViewModelProvider
import com.movienest.log.ui.viewmodel.EntryDetailViewModel
import com.movienest.log.util.DateUtils
import com.movienest.log.util.ProgressUtils

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntryDetailScreen(
    onBack: () -> Unit,
    onEdit: (String) -> Unit,
    onOpenLibrary: () -> Unit,
    viewModel: EntryDetailViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = MovieNestTheme.colors

    if (state.missing && !state.loading) {
        MissingEntryScreen(onGoBack = onBack, onOpenLibrary = onOpenLibrary)
        return
    }
    val entry = state.entry ?: return

    var showDelete by remember { mutableStateOf(false) }
    var showStatusMenu by remember { mutableStateOf(false) }
    var showCompletePrompt by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Ticket Details", maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                actions = {
                    FavoriteToggle(isFavorite = entry.isFavorite, onToggle = viewModel::toggleFavorite)
                    IconButton(onClick = { onEdit(entry.id) }) { Icon(Icons.Filled.Edit, "Edit") }
                    IconButton(onClick = { showDelete = true }) { Icon(Icons.Filled.Delete, "Delete") }
                },
                windowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.paper)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Big ticket header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(TicketShape(cornerRadius = 18.dp, notchRadius = 12.dp))
                    .background(colors.ticket)
                    .border(1.dp, colors.divider, TicketShape(cornerRadius = 18.dp, notchRadius = 12.dp))
                    .padding(18.dp)
            ) {
                Text(
                    entry.title.ifBlank { "Untitled" },
                    style = MaterialTheme.typography.headlineSmall,
                    color = colors.ink,
                    fontWeight = FontWeight.Bold
                )
                if (entry.originalTitle.isNotBlank()) {
                    Text(entry.originalTitle, style = MaterialTheme.typography.bodyMedium, color = colors.mutedInk)
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    StatusStamp(status = entry.status)
                    Text(
                        if (entry.contentType == ContentType.Movie) "Movie" else "Series",
                        style = MaterialTheme.typography.labelMedium,
                        color = colors.mutedInk
                    )
                    if (entry.genre.isNotBlank()) GenreStamp(entry.genre)
                }
                Spacer(Modifier.height(12.dp))
                PerforatedDivider()
                Spacer(Modifier.height(12.dp))
                RatingStars(rating = entry.rating, starSize = 20.dp)
            }

            // Series progress card
            if (entry.contentType == ContentType.Series) {
                SeriesProgressCard(entry)
            }

            // Watching quick controls
            if (entry.status == WatchStatus.Watching) {
                WatchingControls(
                    entry = entry,
                    onPlusOne = {
                        val total = entry.totalEpisodes
                        val current = entry.watchedEpisodes ?: 0
                        if (entry.contentType == ContentType.Series &&
                            total != null && total > 0 && current + 1 >= total
                        ) {
                            showCompletePrompt = true
                        } else {
                            viewModel.incrementEpisode()
                        }
                    },
                    onEditProgress = { onEdit(entry.id) },
                    onMarkWatched = { viewModel.markWatched() }
                )
            }

            // Info fields
            InfoCard(entry)

            // Change status
            Box {
                OutlinedButton(onClick = { showStatusMenu = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Change Status")
                }
                DropdownMenu(expanded = showStatusMenu, onDismissRequest = { showStatusMenu = false }) {
                    WatchStatus.entries.forEach { status ->
                        DropdownMenuItem(
                            text = { Text(status.displayLabel) },
                            onClick = {
                                viewModel.changeStatus(status)
                                showStatusMenu = false
                            }
                        )
                    }
                }
            }

            Button(onClick = { onEdit(entry.id) }, modifier = Modifier.fillMaxWidth()) {
                Icon(Icons.Filled.Edit, null)
                Spacer(Modifier.height(0.dp))
                Text("  Edit Title")
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showDelete) {
        ConfirmDialog(
            title = "Delete this title?",
            message = "This will permanently remove the title, rating, progress, dates, favorite state, and notes stored on this device.",
            confirmLabel = "Delete",
            onConfirm = {
                showDelete = false
                viewModel.delete(onDone = onBack)
            },
            onDismiss = { showDelete = false },
            destructive = true
        )
    }

    if (showCompletePrompt) {
        ConfirmDialog(
            title = "Final episode reached",
            message = "You've reached the last episode. Add this episode and mark the series as Watched?",
            confirmLabel = "Mark Watched",
            dismissLabel = "Just add episode",
            onConfirm = {
                showCompletePrompt = false
                viewModel.incrementEpisode()
                viewModel.markWatched()
            },
            onDismiss = {
                showCompletePrompt = false
                viewModel.incrementEpisode()
            }
        )
    }
}

@Composable
private fun SeriesProgressCard(entry: MovieEntry) {
    val colors = MovieNestTheme.colors
    val pct = ProgressUtils.percentOrNull(entry.watchedEpisodes, entry.totalEpisodes)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.ticket)
            .border(1.dp, colors.divider, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text("Series Progress", style = MaterialTheme.typography.titleSmall, color = colors.ink)
        Spacer(Modifier.height(8.dp))
        if (pct != null) {
            ProgressStrip(percent = pct)
            Spacer(Modifier.height(6.dp))
            Text("$pct%  •  ${ProgressUtils.descriptiveLabel(entry)}", style = MaterialTheme.typography.labelMedium, color = colors.mutedInk)
        } else {
            Text(ProgressUtils.descriptiveLabel(entry), style = MaterialTheme.typography.bodyMedium, color = colors.mutedInk)
        }
        Spacer(Modifier.height(8.dp))
        val season = entry.currentSeason
        val totalSeasons = entry.totalSeasons
        if (season != null || totalSeasons != null) {
            Text(
                buildString {
                    append("Season ")
                    append(season ?: 0)
                    if (totalSeasons != null) append(" of $totalSeasons")
                },
                style = MaterialTheme.typography.labelMedium,
                color = colors.mutedInk
            )
        }
    }
}

@Composable
private fun WatchingControls(
    entry: MovieEntry,
    onPlusOne: () -> Unit,
    onEditProgress: () -> Unit,
    onMarkWatched: () -> Unit
) {
    val colors = MovieNestTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.statusWatching.copy(alpha = 0.08f))
            .border(1.dp, colors.statusWatching.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Currently Watching", style = MaterialTheme.typography.titleSmall, color = colors.ink)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            if (entry.contentType == ContentType.Series) {
                AssistChip(onClick = onPlusOne, label = { Text("+1 Episode") }, leadingIcon = {
                    Icon(Icons.Filled.Add, null, modifier = Modifier.height(18.dp))
                })
                AssistChip(onClick = onEditProgress, label = { Text("Edit Progress") })
            }
            AssistChip(onClick = onMarkWatched, label = { Text("Mark Watched") })
        }
    }
}

@Composable
private fun InfoCard(entry: MovieEntry) {
    val colors = MovieNestTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.ticket)
            .border(1.dp, colors.divider, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        InfoRow("Release year", entry.releaseYear?.toString() ?: "Not set")
        if (entry.contentType == ContentType.Movie) {
            InfoRow("Duration", entry.durationMinutes?.let { "$it min" } ?: "Not set")
        }
        InfoRow("Start date", DateUtils.formatForDisplay(entry.startDate))
        InfoRow(
            "Watched date",
            if (entry.status == WatchStatus.Watched && entry.watchedDate.isNullOrBlank())
                "Not set (won't appear in monthly stats)"
            else DateUtils.formatForDisplay(entry.watchedDate)
        )
        InfoRow("Completion date", DateUtils.formatForDisplay(entry.completionDate))
        InfoRow("Rewatch count", entry.rewatchCount.toString())
        InfoRow("Favorite", if (entry.isFavorite) "Yes" else "No")
        if (entry.notes.isNotBlank()) {
            PerforatedDivider()
            Text("Notes", style = MaterialTheme.typography.titleSmall, color = colors.ink)
            Text(entry.notes, style = MaterialTheme.typography.bodyMedium, color = colors.mutedInk)
        }
        PerforatedDivider()
        InfoRow("Added", DateUtils.formatForDisplay(entry.createdAt.take(10), "Unknown"))
        InfoRow("Last updated", DateUtils.formatForDisplay(entry.updatedAt.take(10), "Unknown"))
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    val colors = MovieNestTheme.colors
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(label, style = MaterialTheme.typography.labelLarge, color = colors.mutedInk, modifier = Modifier.width(140.dp))
        Text(value, style = MaterialTheme.typography.bodyMedium, color = colors.ink, modifier = Modifier.weight(1f))
    }
}
