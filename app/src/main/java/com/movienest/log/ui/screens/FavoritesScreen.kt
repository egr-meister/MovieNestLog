package com.movienest.log.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movienest.log.data.model.ContentFilter
import com.movienest.log.data.model.MovieEntry
import com.movienest.log.data.model.WatchStatus
import com.movienest.log.ui.components.EmptyStateBlock
import com.movienest.log.ui.components.TicketRow
import com.movienest.log.ui.theme.MovieNestTheme
import com.movienest.log.ui.viewmodel.AppViewModelProvider
import com.movienest.log.ui.viewmodel.FavoritesViewModel

@Composable
fun FavoritesScreen(
    onOpenEntry: (String) -> Unit,
    viewModel: FavoritesViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = MovieNestTheme.colors

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.paper)
    ) {
        Text(
            "Your Favorites",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(16.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            listOf(ContentFilter.All, ContentFilter.Movies, ContentFilter.Series).forEach { filter ->
                FilterChip(
                    selected = state.contentFilter == filter,
                    onClick = { viewModel.setContentFilter(filter) },
                    label = { Text(filter.displayLabel) }
                )
            }
            Spacer(Modifier.weight(1f))
            TextButton(onClick = { viewModel.setSortByRatingDesc(!state.sortByRatingDesc) }) {
                Text(if (state.sortByRatingDesc) "Rating ↓" else "Rating ↑")
            }
        }

        if (state.entries.isEmpty() && !state.loading) {
            EmptyStateBlock(
                title = "No favorites saved yet.",
                message = "Use the heart on any ticket to keep it here."
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.entries, key = { it.id }) { entry ->
                    FavoriteRow(
                        entry = entry,
                        showRating = state.showRatingOnTickets,
                        onOpen = { onOpenEntry(entry.id) },
                        onToggleFavorite = { viewModel.toggleFavorite(entry.id) },
                        onChangeStatus = { viewModel.changeStatus(entry.id, it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FavoriteRow(
    entry: MovieEntry,
    showRating: Boolean,
    onOpen: () -> Unit,
    onToggleFavorite: () -> Unit,
    onChangeStatus: (WatchStatus) -> Unit
) {
    var menu by remember { mutableStateOf(false) }
    TicketRow(
        entry = entry,
        showRating = showRating,
        compact = false,
        onClick = onOpen,
        onToggleFavorite = onToggleFavorite,
        trailing = {
            Box {
                IconButton(onClick = { menu = true }) {
                    Icon(Icons.Filled.MoreVert, "Change status")
                }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    WatchStatus.entries.forEach { status ->
                        DropdownMenuItem(
                            text = { Text("Move to ${status.displayLabel}") },
                            onClick = { menu = false; onChangeStatus(status) }
                        )
                    }
                }
            }
        }
    )
}
