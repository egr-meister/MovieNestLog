package com.movienest.log

import android.app.Application
import com.movienest.log.data.repository.MovieNestRepository

/**
 * Holds the single repository instance for the whole app.
 * No dependency-injection framework is used by design.
 */
class MovieNestApplication : Application() {

    lateinit var repository: MovieNestRepository
        private set

    override fun onCreate() {
        super.onCreate()
        repository = MovieNestRepository(applicationContext)
    }
}
