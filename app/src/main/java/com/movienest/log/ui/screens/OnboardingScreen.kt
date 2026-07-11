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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movienest.log.ui.components.DISCLAIMER_TEXT
import com.movienest.log.ui.components.PerforatedDivider
import com.movienest.log.ui.components.ShelfRail
import com.movienest.log.ui.components.TicketShape
import com.movienest.log.ui.theme.MovieNestTheme
import com.movienest.log.ui.viewmodel.AppViewModelProvider
import com.movienest.log.ui.viewmodel.OnboardingViewModel

private data class OnboardPage(val title: String, val body: String)

private val onboardPages = listOf(
    OnboardPage(
        "Build your personal cinema shelf.",
        "Add movies and series manually, then place each title on the shelf that matches your progress: Want to Watch, Watching, or Watched."
    ),
    OnboardPage(
        "Save the details that matter to you.",
        "Use ticket cards to record your personal rating, genre, viewing dates, season and episode progress, favorites, and notes."
    ),
    OnboardPage(
        "See your month at a glance.",
        "MovieNest Log gathers what you finished into a simple monthly summary — watched titles, average personal rating, and favorites added."
    ),
    OnboardPage(
        "Everything stays on this device.",
        "No posters, no streaming links, no automatic imports, and no internet access. Your library is yours and stays offline."
    )
)

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit,
    viewModel: OnboardingViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val colors = MovieNestTheme.colors
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.paper)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Ticket emblem
        Box(
            modifier = Modifier
                .width(120.dp)
                .height(74.dp)
                .clip(TicketShape(cornerRadius = 16.dp, notchRadius = 11.dp))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Movie,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    Icons.Filled.Star,
                    contentDescription = null,
                    tint = colors.brass,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Box(Modifier.width(150.dp)) { ShelfRail() }
        Spacer(Modifier.height(20.dp))
        Text(
            text = "MovieNest Log",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
        Text(
            text = "Your private movie & series diary",
            style = MaterialTheme.typography.bodyMedium,
            color = colors.mutedInk,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))

        onboardPages.forEachIndexed { index, page ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(TicketShape())
                    .background(colors.ticket)
                    .border(1.dp, colors.divider, TicketShape())
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "${index + 1}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(
                        page.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.ink
                    )
                }
                Spacer(Modifier.height(8.dp))
                PerforatedDivider()
                Spacer(Modifier.height(8.dp))
                Text(
                    page.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.mutedInk
                )
            }
            Spacer(Modifier.height(12.dp))
        }

        Spacer(Modifier.height(8.dp))
        Text(
            text = DISCLAIMER_TEXT,
            style = MaterialTheme.typography.bodySmall,
            color = colors.mutedInk,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = { viewModel.complete(); onFinish() },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Create My Shelf") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = { viewModel.complete(); onFinish() },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Explore Empty Shelf") }
        Spacer(Modifier.height(16.dp))
    }
}
