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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.movienest.log.ui.components.ConfirmDialog
import com.movienest.log.ui.state.GenreItem
import com.movienest.log.ui.theme.MovieNestTheme
import com.movienest.log.ui.viewmodel.AppViewModelProvider
import com.movienest.log.ui.viewmodel.GenreViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenreScreen(
    onBack: () -> Unit,
    viewModel: GenreViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val colors = MovieNestTheme.colors
    var newGenre by remember { mutableStateOf("") }
    var renameTarget by remember { mutableStateOf<GenreItem?>(null) }
    var deleteTarget by remember { mutableStateOf<GenreItem?>(null) }

    Scaffold(
        contentWindowInsets = androidx.compose.foundation.layout.WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Manage Genres") },
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
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newGenre,
                    onValueChange = { if (it.length <= 40) newGenre = it },
                    label = { Text("New genre") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.padding(4.dp))
                IconButton(
                    onClick = {
                        if (newGenre.trim().isNotEmpty()) {
                            viewModel.addGenre(newGenre)
                            newGenre = ""
                        }
                    }
                ) { Icon(Icons.Filled.Add, "Add genre", tint = MaterialTheme.colorScheme.primary) }
            }

            if (state.genres.isEmpty() && !state.loading) {
                Text(
                    "No genres yet. Add one above to start labelling your titles.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.mutedInk,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.genres, key = { it.name }) { item ->
                        GenreRow(
                            item = item,
                            onRename = { renameTarget = item },
                            onDelete = { deleteTarget = item }
                        )
                    }
                }
            }
        }
    }

    renameTarget?.let { item ->
        RenameGenreDialog(
            currentName = item.name,
            onConfirm = { newName ->
                viewModel.renameGenre(item.name, newName)
                renameTarget = null
            },
            onDismiss = { renameTarget = null }
        )
    }

    deleteTarget?.let { item ->
        if (item.usageCount > 0) {
            AlertDialog(
                onDismissRequest = { deleteTarget = null },
                title = { Text("Genre in use") },
                text = { Text("This genre is used by ${item.usageCount} ${if (item.usageCount == 1) "title" else "titles"}.") },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.removeGenreAndClear(item.name)
                        deleteTarget = null
                    }) { Text("Remove Genre Label from Titles") }
                },
                dismissButton = {
                    Row {
                        TextButton(onClick = { deleteTarget = null }) { Text("Keep Genre") }
                    }
                }
            )
        } else {
            ConfirmDialog(
                title = "Delete genre?",
                message = "Remove \"${item.name}\" from your genre list. This genre is not used by any title.",
                confirmLabel = "Remove Genre",
                onConfirm = {
                    viewModel.removeGenreLabelOnly(item.name)
                    deleteTarget = null
                },
                onDismiss = { deleteTarget = null },
                destructive = true
            )
        }
    }
}

@Composable
private fun GenreRow(item: GenreItem, onRename: () -> Unit, onDelete: () -> Unit) {
    val colors = MovieNestTheme.colors
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(colors.ticket)
            .border(1.dp, colors.divider, RoundedCornerShape(10.dp))
            .padding(start = 14.dp, top = 6.dp, bottom = 6.dp, end = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(item.name, style = MaterialTheme.typography.titleSmall, color = colors.ink)
            Text(
                "Used by ${item.usageCount} ${if (item.usageCount == 1) "title" else "titles"}",
                style = MaterialTheme.typography.labelSmall,
                color = colors.mutedInk
            )
        }
        IconButton(onClick = onRename) { Icon(Icons.Filled.Edit, "Rename", tint = colors.mutedInk) }
        IconButton(onClick = onDelete) { Icon(Icons.Filled.Delete, "Delete", tint = colors.favorite) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RenameGenreDialog(currentName: String, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    var text by remember { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Rename genre") },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { if (it.length <= 40) text = it },
                singleLine = true,
                label = { Text("Genre name") }
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (text.trim().isNotEmpty()) onConfirm(text.trim()) },
                enabled = text.trim().isNotEmpty()
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
