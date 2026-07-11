package com.movienest.log.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movienest.log.data.model.WatchStatus
import com.movienest.log.ui.components.AFFILIATION_TEXT
import com.movienest.log.ui.components.ConfirmDialog
import com.movienest.log.ui.components.DISCLAIMER_TEXT
import com.movienest.log.ui.components.PRIVACY_TEXT
import com.movienest.log.ui.theme.MovieNestTheme
import com.movienest.log.ui.viewmodel.AppViewModelProvider
import com.movienest.log.ui.viewmodel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onManageGenres: () -> Unit,
    onReplayOnboarding: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = MovieNestTheme.colors
    var showDeleteAll by remember { mutableStateOf(false) }
    var showResetAll by remember { mutableStateOf(false) }

    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back") }
                },
                windowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(colors.paper)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SettingsCard("Home shelf") {
                Text("Default shelf shown on Home", style = MaterialTheme.typography.bodyMedium, color = colors.mutedInk)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    WatchStatus.entries.forEach { status ->
                        FilterChip(
                            selected = state.settings.defaultHomeShelf == status,
                            onClick = { viewModel.setDefaultShelf(status) },
                            label = { Text(status.displayLabel) }
                        )
                    }
                }
            }

            SettingsCard("Tickets") {
                ToggleRow(
                    label = "Show ratings on tickets",
                    checked = state.settings.showRatingOnTickets,
                    onCheckedChange = viewModel::setShowRatingOnTickets
                )
                ToggleRow(
                    label = "Compact ticket mode",
                    checked = state.settings.compactTicketMode,
                    onCheckedChange = viewModel::setCompactTicketMode
                )
            }

            SettingsCard("Library") {
                OutlinedButton(onClick = onManageGenres, modifier = Modifier.fillMaxWidth()) { Text("Manage Genres") }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = viewModel::clearFilters, modifier = Modifier.fillMaxWidth()) { Text("Clear Current Filters") }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { viewModel.replayOnboarding(); onReplayOnboarding() },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Show Onboarding Again") }
            }

            SettingsCard("Your data (${state.entryCount} titles, ${state.genreCount} genres)") {
                OutlinedButton(
                    onClick = { showDeleteAll = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Delete All Titles", color = MaterialTheme.colorScheme.error) }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { showResetAll = true },
                    modifier = Modifier.fillMaxWidth()
                ) { Text("Reset All Local Data", color = MaterialTheme.colorScheme.error) }
            }

            SettingsCard("About MovieNest Log") {
                Text("Version 1.0.0", style = MaterialTheme.typography.bodyMedium, color = colors.ink)
                Spacer(Modifier.height(10.dp))
                Text("Privacy", style = MaterialTheme.typography.titleSmall, color = colors.ink)
                Text(PRIVACY_TEXT, style = MaterialTheme.typography.bodySmall, color = colors.mutedInk)
                Spacer(Modifier.height(10.dp))
                Text("Manual tracking", style = MaterialTheme.typography.titleSmall, color = colors.ink)
                Text(DISCLAIMER_TEXT, style = MaterialTheme.typography.bodySmall, color = colors.mutedInk)
                Spacer(Modifier.height(10.dp))
                Text(AFFILIATION_TEXT, style = MaterialTheme.typography.bodySmall, color = colors.mutedInk)
            }
            Spacer(Modifier.height(24.dp))
        }
    }

    if (showDeleteAll) {
        ConfirmDialog(
            title = "Delete all titles?",
            message = "This will permanently remove all movies, series, ratings, progress, dates, favorites, and notes stored by MovieNest Log.",
            confirmLabel = "Delete All",
            onConfirm = { viewModel.deleteAllEntries(); showDeleteAll = false },
            onDismiss = { showDeleteAll = false },
            destructive = true
        )
    }
    if (showResetAll) {
        ConfirmDialog(
            title = "Reset MovieNest Log?",
            message = "This will remove the complete local library, custom genres, filters, preferences, and onboarding state.",
            confirmLabel = "Reset Everything",
            onConfirm = { viewModel.resetAllData(); showResetAll = false; onReplayOnboarding() },
            onDismiss = { showResetAll = false },
            destructive = true
        )
    }
}

@Composable
private fun SettingsCard(title: String, content: @Composable () -> Unit) {
    val colors = MovieNestTheme.colors
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(colors.ticket)
            .border(1.dp, colors.divider, RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text(title, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(10.dp))
        content()
    }
}

@Composable
private fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        Text(label, style = MaterialTheme.typography.bodyLarge, color = MovieNestTheme.colors.ink, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
