package com.movienest.log.ui.components

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarHalf
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import com.movienest.log.ui.theme.MovieNestTheme
import com.movienest.log.util.DateUtils
import com.movienest.log.util.RatingUtils

/** Labeled single-line text field with optional inline error and helper text. */
@Composable
fun LabeledTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    helper: String? = null,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            isError = error != null,
            singleLine = singleLine,
            minLines = minLines,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            modifier = Modifier.fillMaxWidth()
        )
        val support = error ?: helper
        if (support != null) {
            Text(
                text = support,
                style = MaterialTheme.typography.bodySmall,
                color = if (error != null) MaterialTheme.colorScheme.error
                else MovieNestTheme.colors.mutedInk,
                modifier = Modifier.padding(start = 12.dp, top = 2.dp)
            )
        }
    }
}

/** Half-star rating selector (tap a star = full, long area handled by two taps).
 *  Provides an explicit "Clear" to return to Not rated. */
@Composable
fun RatingInputRow(
    rating: Double?,
    onRatingChange: (Double?) -> Unit,
    modifier: Modifier = Modifier
) {
    val colors = MovieNestTheme.colors
    Column(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Your rating",
                style = MaterialTheme.typography.titleSmall,
                color = colors.ink
            )
            Spacer(Modifier.width(8.dp))
            Text(
                text = RatingUtils.format(rating),
                style = MaterialTheme.typography.labelMedium,
                color = colors.mutedInk
            )
            Spacer(Modifier.weight(1f))
            if (rating != null) {
                TextButton(onClick = { onRatingChange(null) }) { Text("Clear") }
            }
        }
        Spacer(Modifier.height(4.dp))
        Row(
            modifier = Modifier.semantics {
                contentDescription = "Rating selector, current ${RatingUtils.format(rating)} of 5"
            }
        ) {
            for (i in 1..5) {
                val full = i.toDouble()
                val half = i - 0.5
                Row {
                    // Half-star tap zone
                    Box(
                        Modifier
                            .size(22.dp)
                            .clickable { onRatingChange(half) }
                            .clearAndSetSemantics { },
                        contentAlignment = Alignment.Center
                    ) {
                        StarIcon(indexValue = half, rating = rating, tint = colors.warning)
                    }
                    // Full-star tap zone
                    Box(
                        Modifier
                            .size(22.dp)
                            .clickable { onRatingChange(full) }
                            .clearAndSetSemantics { },
                        contentAlignment = Alignment.Center
                    ) {
                        StarIcon(indexValue = full, rating = rating, tint = colors.warning)
                    }
                }
                Spacer(Modifier.width(2.dp))
            }
        }
        Text(
            text = "Tap the left half of a star for a half rating.",
            style = MaterialTheme.typography.bodySmall,
            color = colors.mutedInk
        )
    }
}

@Composable
private fun StarIcon(indexValue: Double, rating: Double?, tint: Color) {
    val r = rating ?: 0.0
    val icon = when {
        r >= indexValue -> if (indexValue % 1.0 == 0.5) Icons.Filled.StarHalf else Icons.Filled.Star
        else -> Icons.Outlined.StarBorder
    }
    Icon(imageVector = icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
}

/** Date field: manual YYYY-MM-DD entry with quick Today / Clear actions. */
@Composable
fun DateInputField(
    label: String,
    value: String?,
    onValueChange: (String?) -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
    emphasized: Boolean = false
) {
    val colors = MovieNestTheme.colors
    Column(modifier = modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = value.orEmpty(),
            onValueChange = { raw -> onValueChange(raw.ifBlank { null }) },
            label = { Text(if (emphasized) "$label ★" else label) },
            placeholder = { Text("YYYY-MM-DD") },
            isError = error != null,
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(top = 2.dp)
        ) {
            TextButton(onClick = { onValueChange(DateUtils.todayIso()) }) { Text("Today") }
            if (!value.isNullOrBlank()) {
                TextButton(onClick = { onValueChange(null) }) { Text("Clear") }
            }
        }
        if (error != null) {
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(start = 12.dp)
            )
        }
    }
}

/** Reusable confirmation dialog. */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    dismissLabel: String = "Cancel",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    destructive: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    confirmLabel,
                    color = if (destructive) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.primary
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(dismissLabel) }
        }
    )
}

/** A small circular stepper used for +1 episode etc. */
@Composable
fun RoundIconAction(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MovieNestTheme.colors.statusWatching
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clickable(onClick = onClick)
            .semantics { this.contentDescription = contentDescription },
        contentAlignment = Alignment.Center
    ) {
        Box(
            Modifier
                .size(36.dp)
                .border(1.dp, tint, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        }
    }
}
