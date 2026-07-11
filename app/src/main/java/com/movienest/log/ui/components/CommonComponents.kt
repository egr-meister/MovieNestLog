package com.movienest.log.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.movienest.log.data.model.WatchStatus
import com.movienest.log.ui.theme.MovieNestTheme
import com.movienest.log.util.RatingUtils

/** Horizontal dashed perforation line, decorative and ignored by a11y. */
@Composable
fun PerforatedDivider(
    modifier: Modifier = Modifier,
    color: Color = MovieNestTheme.colors.divider
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .clearAndSetSemantics { }
            .drawBehind {
                val dash = PathEffect.dashPathEffect(floatArrayOf(10f, 8f), 0f)
                drawLine(
                    color = color,
                    start = Offset(0f, size.height / 2f),
                    end = Offset(size.width, size.height / 2f),
                    strokeWidth = 2f,
                    pathEffect = dash,
                    cap = StrokeCap.Round
                )
            }
    )
}

/** A wood shelf rail with a brass edge. Decorative. */
@Composable
fun ShelfRail(modifier: Modifier = Modifier) {
    val colors = MovieNestTheme.colors
    Column(modifier = modifier.clearAndSetSemantics { }) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(colors.shelf)
        )
        Spacer(Modifier.height(2.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(colors.shelfEdge)
        )
    }
}

/** A small uppercase status stamp with a colored border and dot label. */
@Composable
fun StatusStamp(
    status: WatchStatus,
    modifier: Modifier = Modifier
) {
    val colors = MovieNestTheme.colors
    val stampColor = colors.statusColor(status)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, stampColor, RoundedCornerShape(4.dp))
            .background(stampColor.copy(alpha = 0.10f))
            .padding(horizontal = 8.dp, vertical = 3.dp)
            .semantics { contentDescription = "Status: ${status.displayLabel}" }
    ) {
        Box(
            Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(stampColor)
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = status.displayLabel.uppercase(),
            style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
            color = stampColor,
            fontWeight = FontWeight.Bold
        )
    }
}

/** A small outlined genre stamp. */
@Composable
fun GenreStamp(genre: String, modifier: Modifier = Modifier) {
    if (genre.isBlank()) return
    val colors = MovieNestTheme.colors
    Text(
        text = genre.trim(),
        style = androidx.compose.material3.MaterialTheme.typography.labelSmall,
        color = colors.mutedInk,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, colors.divider, RoundedCornerShape(4.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    )
}

/** Read-only star display with a text label for accessibility. */
@Composable
fun RatingStars(
    rating: Double?,
    modifier: Modifier = Modifier,
    starSize: androidx.compose.ui.unit.Dp = 16.dp
) {
    val colors = MovieNestTheme.colors
    if (rating == null) {
        Text(
            text = RatingUtils.NOT_RATED_LABEL,
            style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
            color = colors.mutedInk,
            modifier = modifier
        )
        return
    }
    val full = RatingUtils.fullStars(rating)
    val half = RatingUtils.hasHalfStar(rating)
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.semantics {
            contentDescription = "Your rating: ${RatingUtils.format(rating)} out of 5"
        }
    ) {
        for (i in 1..5) {
            val icon = when {
                i <= full -> Icons.Filled.Star
                i == full + 1 && half -> Icons.Filled.StarHalf
                else -> Icons.Outlined.StarBorder
            }
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.warning,
                modifier = Modifier.size(starSize)
            )
        }
        Spacer(Modifier.width(6.dp))
        Text(
            text = RatingUtils.format(rating),
            style = androidx.compose.material3.MaterialTheme.typography.labelMedium,
            color = colors.mutedInk
        )
    }
}

/** Heart toggle for favorite state; uses shape + label, not color alone. */
@Composable
fun FavoriteToggle(
    isFavorite: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MovieNestTheme.colors
    IconButton(onClick = onToggle, modifier = modifier) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            contentDescription = if (isFavorite) "Favorite. Tap to remove from favorites."
            else "Not a favorite. Tap to add to favorites.",
            tint = if (isFavorite) colors.favorite else colors.mutedInk
        )
    }
}

/** Generic empty-state block preserving the cinema shelf mood. */
@Composable
fun EmptyStateBlock(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    val colors = MovieNestTheme.colors
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        EmptyTicketGlyph()
        Text(
            text = title,
            style = androidx.compose.material3.MaterialTheme.typography.titleMedium,
            color = colors.ink,
            textAlign = TextAlign.Center
        )
        Text(
            text = message,
            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
            color = colors.mutedInk,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            Spacer(Modifier.height(4.dp))
            androidx.compose.material3.Button(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}

/** Small drawn empty ticket outline used inside empty states. */
@Composable
private fun EmptyTicketGlyph() {
    val colors = MovieNestTheme.colors
    Box(
        modifier = Modifier
            .width(88.dp)
            .height(56.dp)
            .clip(TicketShape(cornerRadius = 12.dp, notchRadius = 8.dp))
            .background(colors.ticketStub)
            .border(
                1.dp,
                colors.divider,
                TicketShape(cornerRadius = 12.dp, notchRadius = 8.dp)
            )
            .clearAndSetSemantics { },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.StarBorder,
            contentDescription = null,
            tint = colors.brass,
            modifier = Modifier.size(22.dp)
        )
    }
}

/** Thin proportional progress bar with a text percentage fallback handled by caller. */
@Composable
fun ProgressStrip(
    percent: Int?,
    modifier: Modifier = Modifier
) {
    val colors = MovieNestTheme.colors
    val fraction = ((percent ?: 0).coerceIn(0, 100)) / 100f
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(colors.ticketStub)
            .clearAndSetSemantics { }
    ) {
        Box(
            Modifier
                .fillMaxWidth(fraction)
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(colors.statusWatching)
        )
    }
}

@Composable
fun SectionHeading(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text.uppercase(),
        style = androidx.compose.material3.MaterialTheme.typography.titleSmall,
        color = MovieNestTheme.colors.mutedInk,
        modifier = modifier.padding(horizontal = 16.dp)
    )
}
