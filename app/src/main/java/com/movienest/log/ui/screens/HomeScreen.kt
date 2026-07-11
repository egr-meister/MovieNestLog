package com.movienest.log.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.movienest.log.data.model.MovieEntry
import com.movienest.log.data.model.WatchStatus
import com.movienest.log.ui.components.EmptyStateBlock
import com.movienest.log.ui.components.PerforatedDivider
import com.movienest.log.ui.components.SectionHeading
import com.movienest.log.ui.components.ShelfRail
import com.movienest.log.ui.components.ShelfTicket
import com.movienest.log.ui.components.StatusStamp
import com.movienest.log.ui.components.TicketShape
import com.movienest.log.ui.state.HomeUiState
import com.movienest.log.ui.theme.MovieNestTheme
import com.movienest.log.ui.viewmodel.AppViewModelProvider
import com.movienest.log.ui.viewmodel.HomeViewModel
import com.movienest.log.util.DateUtils

@Composable
fun HomeScreen(
    onOpenEntry: (String) -> Unit,
    onAddEntry: (WatchStatus?) -> Unit,
    onOpenLibrary: () -> Unit,
    onOpenSettings: () -> Unit,
    viewModel: HomeViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = MovieNestTheme.colors

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.paper)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 24.dp)
    ) {
        MarqueeHeader(
            monthLabel = state.monthLabel,
            onSearch = onOpenLibrary,
            onAdd = { onAddEntry(state.selectedShelf) },
            onSettings = onOpenSettings
        )

        Spacer(Modifier.height(12.dp))
        ShelfSelector(
            selected = state.selectedShelf,
            onSelect = viewModel::selectShelf
        )

        Spacer(Modifier.height(4.dp))
        Box(Modifier.padding(horizontal = 16.dp)) { ShelfRail() }
        Spacer(Modifier.height(12.dp))

        if (state.totalCount == 0) {
            EmptyStateBlock(
                title = "Your cinema shelf is empty.",
                message = "Add a movie or series to begin your personal watchlist.",
                actionLabel = "Add First Title",
                onAction = { onAddEntry(WatchStatus.WantToWatch) }
            )
            Spacer(Modifier.height(24.dp))
            return@Column
        }

        // Now Showing on My Shelf (currently watching)
        SectionHeading("Now Showing on My Shelf")
        Spacer(Modifier.height(8.dp))
        if (state.nowWatching.isEmpty()) {
            InlineEmpty("Nothing is currently playing on your shelf.")
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.nowWatching, key = { it.id }) { entry ->
                    ShelfTicket(
                        entry = entry,
                        showRating = state.showRatingOnTickets,
                        onClick = { onOpenEntry(entry.id) }
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        AdmissionStrip(state)

        Spacer(Modifier.height(20.dp))
        // Selected shelf tickets
        SectionHeading(state.selectedShelf.displayLabel + " Shelf")
        Spacer(Modifier.height(8.dp))
        if (state.shelfEntries.isEmpty()) {
            InlineEmpty("No tickets on this shelf yet.")
        } else {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.shelfEntries, key = { it.id }) { entry ->
                    ShelfTicket(
                        entry = entry,
                        showRating = state.showRatingOnTickets,
                        onClick = { onOpenEntry(entry.id) }
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        // Up Next queue
        SectionHeading("Up Next")
        Spacer(Modifier.height(8.dp))
        if (state.upNext.isEmpty()) {
            InlineEmpty("Your Want to Watch queue is empty.")
        } else {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                state.upNext.forEach { entry ->
                    UpNextRow(
                        entry = entry,
                        onOpen = { onOpenEntry(entry.id) },
                        onStart = { viewModel.moveToWatching(entry.id) }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        // Recent viewing timeline
        SectionHeading("Recent Viewing")
        Spacer(Modifier.height(8.dp))
        if (state.recentlyWatched.isEmpty()) {
            InlineEmpty("No watched titles recorded yet.")
        } else {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                state.recentlyWatched.forEach { entry ->
                    TimelineRow(entry = entry, onOpen = { onOpenEntry(entry.id) })
                }
            }
        }

        Spacer(Modifier.height(20.dp))
        AddTitleTicket(onClick = { onAddEntry(state.selectedShelf) })
    }
}

@Composable
private fun MarqueeHeader(
    monthLabel: String,
    onSearch: () -> Unit,
    onAdd: () -> Unit,
    onSettings: () -> Unit
) {
    val colors = MovieNestTheme.colors
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "MovieNest Log",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = monthLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = colors.brass
                )
            }
            IconButton(onClick = onSearch) {
                Icon(Icons.Filled.Search, "Search titles", tint = MaterialTheme.colorScheme.onPrimary)
            }
            IconButton(onClick = onAdd) {
                Icon(Icons.Filled.Add, "Add title", tint = MaterialTheme.colorScheme.onPrimary)
            }
            IconButton(onClick = onSettings) {
                Icon(Icons.Filled.Settings, "Settings", tint = MaterialTheme.colorScheme.onPrimary)
            }
        }
    }
}

@Composable
private fun ShelfSelector(selected: WatchStatus, onSelect: (WatchStatus) -> Unit) {
    val colors = MovieNestTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        WatchStatus.entries.forEach { status ->
            val isSelected = status == selected
            val accent = colors.statusColor(status)
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp))
                    .background(if (isSelected) accent.copy(alpha = 0.16f) else colors.ticketStub)
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) accent else colors.divider,
                        shape = RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp)
                    )
                    .clickable { onSelect(status) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = status.displayLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) accent else colors.mutedInk,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun AdmissionStrip(state: HomeUiState) {
    val colors = MovieNestTheme.colors
    val avg = state.averageRatingThisMonth
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(colors.ticket)
            .border(1.dp, colors.brass, RoundedCornerShape(10.dp))
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        AdmissionCell("Watched", state.watchedThisMonth.toString())
        StripDivider()
        AdmissionCell("Movies", state.moviesThisMonth.toString())
        StripDivider()
        AdmissionCell("Series", state.seriesThisMonth.toString())
        StripDivider()
        AdmissionCell(
            "Avg rating",
            if (avg == null) "—" else com.movienest.log.util.RatingUtils.format(avg)
        )
        StripDivider()
        AdmissionCell("Favorites", state.favoritesThisMonth.toString())
    }
}

@Composable
private fun AdmissionCell(label: String, value: String) {
    val colors = MovieNestTheme.colors
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = colors.mutedInk)
    }
}

@Composable
private fun StripDivider() {
    Box(
        Modifier
            .width(1.dp)
            .height(32.dp)
            .background(MovieNestTheme.colors.divider)
    )
}

@Composable
private fun UpNextRow(entry: MovieEntry, onOpen: () -> Unit, onStart: () -> Unit) {
    val colors = MovieNestTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(TicketShape(cornerRadius = 10.dp, notchRadius = 7.dp))
            .background(colors.ticket)
            .border(1.dp, colors.divider, TicketShape(cornerRadius = 10.dp, notchRadius = 7.dp))
            .clickable(onClick = onOpen)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(entry.title.ifBlank { "Untitled" }, style = MaterialTheme.typography.titleSmall, color = colors.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(2.dp))
            Text(
                if (entry.contentType == com.movienest.log.data.model.ContentType.Movie) "Movie" else "Series",
                style = MaterialTheme.typography.labelMedium,
                color = colors.mutedInk
            )
        }
        IconButton(onClick = onStart) {
            Icon(Icons.Filled.PlayArrow, "Move to Watching", tint = colors.statusWatching)
        }
    }
}

@Composable
private fun TimelineRow(entry: MovieEntry, onOpen: () -> Unit) {
    val colors = MovieNestTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(colors.statusWatched)
        )
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(entry.title.ifBlank { "Untitled" }, style = MaterialTheme.typography.bodyLarge, color = colors.ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                DateUtils.formatForDisplay(entry.watchedDate),
                style = MaterialTheme.typography.labelMedium,
                color = colors.mutedInk
            )
        }
        StatusStamp(status = entry.status)
    }
}

@Composable
private fun AddTitleTicket(onClick: () -> Unit) {
    val colors = MovieNestTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(TicketShape(cornerRadius = 14.dp, notchRadius = 10.dp))
            .background(MaterialTheme.colorScheme.primary)
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Filled.Add, null, tint = MaterialTheme.colorScheme.onPrimary)
        Spacer(Modifier.width(8.dp))
        Text("Add Title", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun InlineEmpty(text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MovieNestTheme.colors.mutedInk)
    }
}
