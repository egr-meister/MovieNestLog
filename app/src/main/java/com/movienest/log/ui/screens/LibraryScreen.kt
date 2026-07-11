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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.movienest.log.data.model.SortOption
import com.movienest.log.data.model.WatchStatus
import com.movienest.log.ui.components.ConfirmDialog
import com.movienest.log.ui.components.EmptyStateBlock
import com.movienest.log.ui.components.TicketRow
import com.movienest.log.ui.theme.MovieNestTheme
import com.movienest.log.ui.viewmodel.AppViewModelProvider
import com.movienest.log.ui.viewmodel.LibraryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onOpenEntry: (String) -> Unit,
    onAddEntry: () -> Unit,
    viewModel: LibraryViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = MovieNestTheme.colors
    var sortMenu by remember { mutableStateOf(false) }
    var genreMenu by remember { mutableStateOf(false) }
    var pendingDelete by remember { mutableStateOf<MovieEntry?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.paper)
    ) {
        Text(
            "Library",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp, end = 16.dp)
        )
        // Search
        OutlinedTextField(
            value = state.query,
            onValueChange = viewModel::setQuery,
            leadingIcon = { Icon(Icons.Filled.Search, null) },
            trailingIcon = {
                if (state.query.isNotBlank()) {
                    IconButton(onClick = { viewModel.setQuery("") }) {
                        Icon(Icons.Filled.Clear, "Clear search")
                    }
                }
            },
            placeholder = { Text("Search titles and notes") },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

        // Status + content filters (horizontally scrollable)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ContentFilter.entries.forEach { filter ->
                FilterChip(
                    selected = state.contentFilter == filter,
                    onClick = { viewModel.setContentFilter(filter) },
                    label = { Text(filter.displayLabel) }
                )
            }
            WatchStatus.entries.forEach { status ->
                FilterChip(
                    selected = state.statusFilter == status,
                    onClick = {
                        viewModel.setStatusFilter(if (state.statusFilter == status) null else status)
                    },
                    label = { Text(status.displayLabel) }
                )
            }
        }

        // Sort + genre + clear
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box {
                TextButton(onClick = { sortMenu = true }) {
                    Icon(Icons.Filled.Sort, null)
                    Text("  ${state.sort.displayLabel}")
                }
                DropdownMenu(expanded = sortMenu, onDismissRequest = { sortMenu = false }) {
                    SortOption.entries.forEach { opt ->
                        DropdownMenuItem(
                            text = { Text(opt.displayLabel) },
                            onClick = { viewModel.setSort(opt); sortMenu = false }
                        )
                    }
                }
            }
            Box {
                TextButton(onClick = { genreMenu = true }) {
                    Icon(Icons.Filled.FilterList, null)
                    Text("  ${state.genreFilter ?: "All genres"}")
                }
                DropdownMenu(expanded = genreMenu, onDismissRequest = { genreMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("All genres") },
                        onClick = { viewModel.setGenreFilter(null); genreMenu = false }
                    )
                    state.availableGenres.forEach { g ->
                        DropdownMenuItem(
                            text = { Text(g) },
                            onClick = { viewModel.setGenreFilter(g); genreMenu = false }
                        )
                    }
                }
            }
            Spacer(Modifier.weight(1f))
            if (state.hasActiveFilters) {
                TextButton(onClick = viewModel::clearFilters) { Text("Clear") }
            }
        }

        if (state.results.isEmpty() && !state.loading) {
            if (state.totalEntries == 0) {
                EmptyStateBlock(
                    title = "Your cinema shelf is empty.",
                    message = "Add a movie or series to start your library.",
                    actionLabel = "Add First Title",
                    onAction = onAddEntry
                )
            } else {
                EmptyStateBlock(
                    title = "No titles match these filters.",
                    message = "Try changing or clearing your search and filters.",
                    actionLabel = "Clear Filters",
                    onAction = viewModel::clearFilters
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(state.results, key = { it.id }) { entry ->
                    LibraryRow(
                        entry = entry,
                        showRating = state.showRatingOnTickets,
                        compact = state.compactTicketMode,
                        onOpen = { onOpenEntry(entry.id) },
                        onToggleFavorite = { viewModel.toggleFavorite(entry.id) },
                        onDelete = { pendingDelete = entry }
                    )
                }
            }
        }
    }

    pendingDelete?.let { entry ->
        ConfirmDialog(
            title = "Delete this title?",
            message = "This will permanently remove \"${entry.title}\" and all of its details from this device.",
            confirmLabel = "Delete",
            onConfirm = {
                viewModel.deleteEntry(entry.id)
                pendingDelete = null
            },
            onDismiss = { pendingDelete = null },
            destructive = true
        )
    }
}

@Composable
private fun LibraryRow(
    entry: MovieEntry,
    showRating: Boolean,
    compact: Boolean,
    onOpen: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit
) {
    var menu by remember { mutableStateOf(false) }
    TicketRow(
        entry = entry,
        showRating = showRating,
        compact = compact,
        onClick = onOpen,
        onToggleFavorite = onToggleFavorite,
        trailing = {
            Box {
                IconButton(onClick = { menu = true }) {
                    Icon(Icons.Filled.MoreVert, "More actions")
                }
                DropdownMenu(expanded = menu, onDismissRequest = { menu = false }) {
                    DropdownMenuItem(text = { Text("Open") }, onClick = { menu = false; onOpen() })
                    DropdownMenuItem(text = { Text("Delete") }, onClick = { menu = false; onDelete() })
                }
            }
        }
    )
}
