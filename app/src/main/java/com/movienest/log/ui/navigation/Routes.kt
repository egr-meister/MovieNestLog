package com.movienest.log.ui.navigation

/** All navigation routes and argument keys in one place. */
object Routes {
    const val ONBOARDING = "onboarding"
    const val HOME = "home"
    const val LIBRARY = "library"
    const val WATCHING = "watching"
    const val FAVORITES = "favorites"
    const val STATISTICS = "statistics"
    const val SETTINGS = "settings"
    const val GENRES = "genres"

    const val ARG_ENTRY_ID = "entryId"

    const val ENTRY_ADD = "entry/add"
    const val ENTRY_DETAIL = "entry/{$ARG_ENTRY_ID}"
    const val ENTRY_EDIT = "entry/{$ARG_ENTRY_ID}/edit"

    fun entryDetail(entryId: String) = "entry/$entryId"
    fun entryEdit(entryId: String) = "entry/$entryId/edit"

    /** Optional start-shelf argument for Add screen (status name). */
    const val ARG_START_STATUS = "startStatus"
    fun addEntry(startStatus: String? = null): String =
        if (startStatus == null) ENTRY_ADD else "entry/add?$ARG_START_STATUS=$startStatus"
    const val ENTRY_ADD_PATTERN = "entry/add?$ARG_START_STATUS={$ARG_START_STATUS}"
}

/** Bottom navigation destinations. */
enum class BottomDestination(val route: String, val label: String) {
    Home(Routes.HOME, "Home"),
    Library(Routes.LIBRARY, "Library"),
    Watching(Routes.WATCHING, "Watching"),
    Favorites(Routes.FAVORITES, "Favorites"),
    Stats(Routes.STATISTICS, "Stats")
}
