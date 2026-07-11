package com.movienest.log.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movienest.log.data.model.ContentType
import com.movienest.log.data.model.MovieEntry
import com.movienest.log.ui.components.EmptyStateBlock
import com.movienest.log.ui.components.PerforatedDivider
import com.movienest.log.ui.components.ProgressStrip
import com.movienest.log.ui.theme.MovieNestTheme
import com.movienest.log.ui.viewmodel.AppViewModelProvider
import com.movienest.log.ui.viewmodel.WatchingViewModel
import com.movienest.log.util.DateUtils
import com.movienest.log.util.ProgressUtils

@Composable
fun WatchingScreen(
    onOpenEntry: (String) -> Unit,
    onEditEntry: (String) -> Unit,
    viewModel: WatchingViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = MovieNestTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.paper)
    ) {
        Text(
            "Watching",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(16.dp)
        )
        if (state.entries.isEmpty() && !state.loading) {
            EmptyStateBlock(
                title = "Nothing is currently playing on your shelf.",
                message = "Move a title from Want to Watch when you begin."
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.entries, key = { it.id }) { entry ->
                    WatchingCard(
                        entry = entry,
                        onOpen = { onOpenEntry(entry.id) },
                        onPlusOne = { viewModel.incrementEpisode(entry.id) },
                        onEditProgress = { onEditEntry(entry.id) },
                        onMarkWatched = { viewModel.markWatched(entry.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WatchingCard(
    entry: MovieEntry,
    onOpen: () -> Unit,
    onPlusOne: () -> Unit,
    onEditProgress: () -> Unit,
    onMarkWatched: () -> Unit
) {
    val colors = MovieNestTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.ticket)
            .border(1.dp, colors.divider, RoundedCornerShape(12.dp))
            .clickable(onClick = onOpen)
            .padding(16.dp)
    ) {
        Text(
            entry.title.ifBlank { "Untitled" },
            style = MaterialTheme.typography.titleMedium,
            color = colors.ink,
            fontWeight = FontWeight.SemiBold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(4.dp))
        Text(
            if (entry.contentType == ContentType.Movie) "Movie" else "Series",
            style = MaterialTheme.typography.labelMedium,
            color = colors.mutedInk
        )
        Spacer(Modifier.height(10.dp))

        if (entry.contentType == ContentType.Series) {
            val pct = ProgressUtils.percentOrNull(entry.watchedEpisodes, entry.totalEpisodes)
            if (pct != null) {
                ProgressStrip(percent = pct)
                Spacer(Modifier.height(6.dp))
                Text(
                    "$pct%  •  ${ProgressUtils.descriptiveLabel(entry)}" +
                        (entry.currentSeason?.let { "  •  Season $it" } ?: ""),
                    style = MaterialTheme.typography.labelMedium,
                    color = colors.mutedInk
                )
            } else {
                Text(
                    ProgressUtils.descriptiveLabel(entry),
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.mutedInk
                )
            }
        } else {
            Text(
                "Started ${DateUtils.formatForDisplay(entry.startDate, "not set")}",
                style = MaterialTheme.typography.labelMedium,
                color = colors.mutedInk
            )
            if (entry.notes.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    entry.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = colors.mutedInk,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        PerforatedDivider()
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            if (entry.contentType == ContentType.Series) {
                AssistChip(
                    onClick = onPlusOne,
                    label = { Text("+1 Episode") },
                    leadingIcon = { Icon(Icons.Filled.Add, null, Modifier.height(18.dp)) }
                )
                AssistChip(onClick = onEditProgress, label = { Text("Edit Progress") })
            }
            AssistChip(onClick = onMarkWatched, label = { Text("Mark Watched") })
        }
    }
}
