package com.movienest.log.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.movienest.log.data.model.ContentType
import com.movienest.log.data.model.MovieEntry
import com.movienest.log.ui.theme.MovieNestTheme
import com.movienest.log.util.DateUtils
import com.movienest.log.util.ProgressUtils

private val ticketShape = TicketShape()

@Composable
private fun ContentTypeLabel(type: ContentType) {
    val colors = MovieNestTheme.colors
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = if (type == ContentType.Movie) Icons.Filled.Movie else Icons.Filled.Tv,
            contentDescription = null,
            tint = colors.mutedInk,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            text = if (type == ContentType.Movie) "Movie" else "Series",
            style = MaterialTheme.typography.labelMedium,
            color = colors.mutedInk
        )
    }
}

/** Full-width perforated ticket row used in the Library and lists. */
@Composable
fun TicketRow(
    entry: MovieEntry,
    showRating: Boolean,
    compact: Boolean,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null
) {
    val colors = MovieNestTheme.colors
    val accent = colors.statusColor(entry.status)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(ticketShape)
            .background(colors.ticket)
            .border(1.dp, colors.divider, ticketShape)
            .clickable(onClick = onClick)
            .padding(vertical = if (compact) 8.dp else 12.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Colored status spine on the left edge.
        Box(
            Modifier
                .width(4.dp)
                .height(if (compact) 34.dp else 46.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accent)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entry.title.ifBlank { "Untitled" },
                style = MaterialTheme.typography.titleMedium,
                color = colors.ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ContentTypeLabel(entry.contentType)
                StatusStamp(status = entry.status)
            }
            if (!compact) {
                Spacer(Modifier.height(6.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (entry.genre.isNotBlank()) GenreStamp(entry.genre)
                    val meta = ticketMetaLine(entry)
                    if (meta.isNotBlank()) {
                        Text(
                            text = meta,
                            style = MaterialTheme.typography.labelMedium,
                            color = colors.mutedInk,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                if (showRating && entry.rating != null) {
                    Spacer(Modifier.height(6.dp))
                    RatingStars(rating = entry.rating)
                }
            }
        }
        Spacer(Modifier.width(8.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            FavoriteToggle(isFavorite = entry.isFavorite, onToggle = onToggleFavorite)
            trailing?.invoke()
        }
    }
}

private fun ticketMetaLine(entry: MovieEntry): String {
    val parts = mutableListOf<String>()
    entry.releaseYear?.let { parts.add(it.toString()) }
    when (entry.contentType) {
        ContentType.Series -> {
            val pct = ProgressUtils.percentOrNull(entry.watchedEpisodes, entry.totalEpisodes)
            if (pct != null) parts.add("$pct%") else {
                val label = ProgressUtils.descriptiveLabel(entry)
                if (label.isNotBlank()) parts.add(label)
            }
        }
        ContentType.Movie -> {
            entry.durationMinutes?.let { if (it > 0) parts.add("$it min") }
        }
    }
    DateUtils.parseIsoOrNull(entry.watchedDate)?.let {
        parts.add("Watched ${DateUtils.formatForDisplay(entry.watchedDate)}")
    }
    return parts.joinToString("  •  ")
}

/** Compact vertical ticket used in the horizontal Home rail. */
@Composable
fun ShelfTicket(
    entry: MovieEntry,
    showRating: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MovieNestTheme.colors
    val accent = colors.statusColor(entry.status)
    Column(
        modifier = modifier
            .width(196.dp)
            .clip(ticketShape)
            .background(colors.ticket)
            .border(1.dp, colors.divider, ticketShape)
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            ContentTypeLabel(entry.contentType)
            if (entry.isFavorite) {
                Icon(
                    imageVector = androidx.compose.material.icons.Icons.Filled.Favorite,
                    contentDescription = "Favorite",
                    tint = colors.favorite,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Text(
            text = entry.title.ifBlank { "Untitled" },
            style = MaterialTheme.typography.titleMedium,
            color = colors.ink,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        PerforatedDivider()
        Spacer(Modifier.height(8.dp))
        StatusStamp(status = entry.status)
        if (entry.contentType == ContentType.Series) {
            val pct = ProgressUtils.percentOrNull(entry.watchedEpisodes, entry.totalEpisodes)
            Spacer(Modifier.height(8.dp))
            if (pct != null) {
                ProgressStrip(percent = pct)
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "$pct%  •  ${ProgressUtils.descriptiveLabel(entry)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.mutedInk
                )
            } else {
                Text(
                    text = ProgressUtils.descriptiveLabel(entry),
                    style = MaterialTheme.typography.labelSmall,
                    color = colors.mutedInk
                )
            }
        }
        if (showRating && entry.rating != null) {
            Spacer(Modifier.height(8.dp))
            RatingStars(rating = entry.rating, starSize = 14.dp)
        }
        Box(Modifier.height(2.dp))
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(accent.copy(alpha = 0.5f))
        )
    }
}
