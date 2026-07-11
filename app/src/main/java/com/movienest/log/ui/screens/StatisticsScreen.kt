package com.movienest.log.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movienest.log.data.model.MovieEntry
import com.movienest.log.ui.components.EmptyStateBlock
import com.movienest.log.ui.components.PerforatedDivider
import com.movienest.log.ui.components.RatingStars
import com.movienest.log.ui.theme.MovieNestTheme
import com.movienest.log.ui.viewmodel.AppViewModelProvider
import com.movienest.log.ui.viewmodel.StatisticsViewModel
import com.movienest.log.util.MonthlyStatistics
import com.movienest.log.util.RatingUtils

@Composable
fun StatisticsScreen(
    onOpenEntry: (String) -> Unit,
    viewModel: StatisticsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = MovieNestTheme.colors
    val stats = state.stats

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.paper)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            "Monthly Admissions",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 16.dp, top = 16.dp)
        )
        // Month navigator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = viewModel::previousMonth) {
                Icon(Icons.Filled.ChevronLeft, "Previous month")
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(state.monthLabel, style = MaterialTheme.typography.titleMedium, color = colors.ink, fontWeight = FontWeight.Bold)
                TextButton(onClick = viewModel::currentMonth) { Text("This month") }
            }
            IconButton(onClick = viewModel::nextMonth) {
                Icon(Icons.Filled.ChevronRight, "Next month")
            }
        }

        if (stats == null || !stats.hasData) {
            EmptyStateBlock(
                title = "No watched titles recorded for this month.",
                message = "Mark titles as Watched with a watched date to see them here."
            )
            Spacer(Modifier.height(24.dp))
            return@Column
        }

        StatGrid(stats)
        Spacer(Modifier.height(16.dp))
        MovieSeriesBars(stats)
        Spacer(Modifier.height(16.dp))
        GenreAndExtras(stats)
        Spacer(Modifier.height(16.dp))
        TopRatedList(stats, onOpenEntry)
        Spacer(Modifier.height(16.dp))
        WatchedTimeline(stats, onOpenEntry)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun StatGrid(stats: MonthlyStatistics) {
    val colors = MovieNestTheme.colors
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile("Watched", stats.totalWatched.toString(), Modifier.weight(1f))
            StatTile("Movies", stats.moviesWatched.toString(), Modifier.weight(1f))
            StatTile("Series", stats.seriesCompleted.toString(), Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(
                "Avg rating",
                if (stats.averageRating == null) "No rated titles" else RatingUtils.format(stats.averageRating),
                Modifier.weight(1f)
            )
            StatTile("Rated", stats.ratedCount.toString(), Modifier.weight(1f))
            StatTile("Favorites", stats.favoritesCompleted.toString(), Modifier.weight(1f))
        }
        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(
                "Movie minutes",
                if (stats.totalMovieMinutes == 0) "—" else stats.totalMovieMinutes.toString(),
                Modifier.weight(1f)
            )
            StatTile("Rewatches", stats.totalRewatches.toString(), Modifier.weight(1f))
            StatTile("Genre", stats.mostUsedGenre ?: "No genre data", Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    val colors = MovieNestTheme.colors
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(colors.ticket)
            .border(1.dp, colors.divider, RoundedCornerShape(10.dp))
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(value, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = colors.mutedInk)
    }
}

@Composable
private fun MovieSeriesBars(stats: MonthlyStatistics) {
    val colors = MovieNestTheme.colors
    val total = (stats.moviesWatched + stats.seriesCompleted).coerceAtLeast(1)
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text("Movies vs Series", style = MaterialTheme.typography.titleSmall, color = colors.ink)
        Spacer(Modifier.height(8.dp))
        ProportionBar("Movies", stats.moviesWatched, total, colors.statusWatching)
        Spacer(Modifier.height(6.dp))
        ProportionBar("Series", stats.seriesCompleted, total, colors.statusWatched)
    }
}

@Composable
private fun ProportionBar(label: String, value: Int, total: Int, color: androidx.compose.ui.graphics.Color) {
    val colors = MovieNestTheme.colors
    val fraction = value.toFloat() / total.toFloat()
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(label, style = MaterialTheme.typography.labelMedium, color = colors.mutedInk, modifier = Modifier.width(60.dp))
        Box(
            Modifier
                .weight(1f)
                .height(14.dp)
                .clip(RoundedCornerShape(7.dp))
                .background(colors.ticketStub)
        ) {
            Box(
                Modifier
                    .fillMaxWidth(fraction)
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(color)
            )
        }
        Spacer(Modifier.width(8.dp))
        Text(value.toString(), style = MaterialTheme.typography.labelMedium, color = colors.ink)
    }
}

@Composable
private fun GenreAndExtras(stats: MonthlyStatistics) {
    val colors = MovieNestTheme.colors
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(colors.ticket)
                .border(1.dp, colors.brass, RoundedCornerShape(10.dp))
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Most used genre", style = MaterialTheme.typography.bodyLarge, color = colors.mutedInk)
            Text(stats.mostUsedGenre ?: "No genre data", style = MaterialTheme.typography.titleSmall, color = colors.ink, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun TopRatedList(stats: MonthlyStatistics, onOpenEntry: (String) -> Unit) {
    if (stats.topRated.isEmpty()) return
    val colors = MovieNestTheme.colors
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text("Highest-rated this month", style = MaterialTheme.typography.titleSmall, color = colors.ink)
        Spacer(Modifier.height(8.dp))
        stats.topRated.forEach { entry ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenEntry(entry.id) }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    entry.title.ifBlank { "Untitled" },
                    style = MaterialTheme.typography.bodyLarge,
                    color = colors.ink,
                    modifier = Modifier.weight(1f)
                )
                RatingStars(rating = entry.rating, starSize = 14.dp)
            }
            PerforatedDivider()
        }
    }
}

@Composable
private fun WatchedTimeline(stats: MonthlyStatistics, onOpenEntry: (String) -> Unit) {
    val colors = MovieNestTheme.colors
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        Text("Watched timeline", style = MaterialTheme.typography.titleSmall, color = colors.ink)
        Spacer(Modifier.height(8.dp))
        stats.watchedEntries.forEach { entry ->
            TimelineStub(entry = entry, onOpen = { onOpenEntry(entry.id) })
        }
    }
}

@Composable
private fun TimelineStub(entry: MovieEntry, onOpen: () -> Unit) {
    val colors = MovieNestTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onOpen)
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .width(6.dp)
                .height(28.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(colors.statusWatched)
        )
        Spacer(Modifier.width(10.dp))
        Text(
            entry.title.ifBlank { "Untitled" },
            style = MaterialTheme.typography.bodyMedium,
            color = colors.ink,
            modifier = Modifier.weight(1f)
        )
        Text(
            com.movienest.log.util.DateUtils.formatForDisplay(entry.watchedDate),
            style = MaterialTheme.typography.labelSmall,
            color = colors.mutedInk
        )
    }
}
