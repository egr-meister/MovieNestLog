package com.movienest.log

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import com.movienest.log.data.model.AppSettings
import com.movienest.log.ui.navigation.MovieNestApp
import com.movienest.log.ui.theme.MovieNestTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val repository = (application as MovieNestApplication).repository

        setContent {
            // Null keeps the static splash visible until the first settings emission,
            // so onboarding never briefly flashes for returning users.
            val settings by produceState<AppSettings?>(initialValue = null) {
                repository.settingsFlow.collect { value = it }
            }

            MovieNestTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    settings?.let { current ->
                        MovieNestApp(startAtOnboarding = !current.onboardingCompleted)
                    }
                }
            }
        }
    }
}
