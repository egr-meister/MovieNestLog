package com.movienest.log.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.movienest.log.ui.theme.MovieNestTheme

/** Shown when an entry route points at something that no longer exists. */
@Composable
fun MissingEntryScreen(onGoBack: () -> Unit, onOpenLibrary: () -> Unit) {
    val colors = MovieNestTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.paper)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "This title is no longer available.",
            style = MaterialTheme.typography.titleMedium,
            color = colors.ink,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.padding(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedButton(onClick = onGoBack) { Text("Go Back") }
            Spacer(Modifier.width(4.dp))
            Button(onClick = onOpenLibrary) { Text("Open Library") }
        }
    }
}
