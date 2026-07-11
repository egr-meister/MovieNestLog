package com.movienest.log.ui.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import com.movienest.log.MovieNestApplication
import com.movienest.log.data.repository.MovieNestRepository

private fun CreationExtras.app(): MovieNestApplication =
    (this[APPLICATION_KEY] as MovieNestApplication)

private fun CreationExtras.repo(): MovieNestRepository = app().repository

/** Central factory wiring the single repository into every ViewModel. */
object AppViewModelProvider {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
        initializer { HomeViewModel(repo()) }
        initializer { LibraryViewModel(repo()) }
        initializer { WatchingViewModel(repo()) }
        initializer { FavoritesViewModel(repo()) }
        initializer { StatisticsViewModel(repo()) }
        initializer { SettingsViewModel(repo()) }
        initializer { GenreViewModel(repo()) }
        initializer { OnboardingViewModel(repo()) }
        initializer {
            val handle: SavedStateHandle = createSavedStateHandle()
            EntryDetailViewModel(repo(), handle)
        }
        initializer {
            val handle: SavedStateHandle = createSavedStateHandle()
            EntryEditorViewModel(repo(), handle)
        }
    }
}
