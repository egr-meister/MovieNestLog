package com.movienest.log.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/** Extra cinema-shelf palette not covered by the Material color scheme. */
@Immutable
data class MovieNestColors(
    val paper: Color,
    val ticket: Color,
    val ticketStub: Color,
    val shelf: Color,
    val shelfEdge: Color,
    val brass: Color,
    val divider: Color,
    val ink: Color,
    val mutedInk: Color,
    val perforation: Color,
    val statusWantToWatch: Color,
    val statusWatching: Color,
    val statusWatched: Color,
    val favorite: Color,
    val warning: Color,
    val information: Color,
    val isDark: Boolean
) {
    fun statusColor(status: com.movienest.log.data.model.WatchStatus): Color =
        when (status) {
            com.movienest.log.data.model.WatchStatus.WantToWatch -> statusWantToWatch
            com.movienest.log.data.model.WatchStatus.Watching -> statusWatching
            com.movienest.log.data.model.WatchStatus.Watched -> statusWatched
        }
}

val LocalMovieNestColors = staticCompositionLocalOf {
    lightMovieNestColors
}

private val lightMovieNestColors = MovieNestColors(
    paper = PaperBackground,
    ticket = SurfaceCream,
    ticketStub = PaleStub,
    shelf = ShelfBrown,
    shelfEdge = DarkWalnut,
    brass = BrassDetail,
    divider = DividerBeige,
    ink = InkBlack,
    mutedInk = MutedInk,
    perforation = PaperBackground,
    statusWantToWatch = StatusWantToWatch,
    statusWatching = StatusWatching,
    statusWatched = StatusWatched,
    favorite = FavoriteColor,
    warning = WarningColor,
    information = InformationColor,
    isDark = false
)

private val darkMovieNestColors = MovieNestColors(
    paper = DarkBackground,
    ticket = DarkTicket,
    ticketStub = DarkSurface,
    shelf = ShelfBrown,
    shelfEdge = DarkWalnut,
    brass = BrassDetail,
    divider = DarkBorder,
    ink = DarkPrimaryText,
    mutedInk = DarkSecondaryText,
    perforation = DarkBackground,
    statusWantToWatch = Color(0xFF6E9DB6),
    statusWatching = Color(0xFFD69B54),
    statusWatched = Color(0xFF7BA98A),
    favorite = Color(0xFFCE6E85),
    warning = Color(0xFFD09253),
    information = Color(0xFF6E9DB6),
    isDark = true
)

private val LightColorScheme = lightColorScheme(
    primary = MarqueeBurgundy,
    onPrimary = WarmTicketCream,
    primaryContainer = TicketCoral,
    onPrimaryContainer = DeepCinemaRed,
    secondary = ShelfBrown,
    onSecondary = SurfaceCream,
    secondaryContainer = SoftWood,
    onSecondaryContainer = InkBlack,
    tertiary = BrassDetail,
    onTertiary = InkBlack,
    background = PaperBackground,
    onBackground = InkBlack,
    surface = SurfaceCream,
    onSurface = InkBlack,
    surfaceVariant = PaleStub,
    onSurfaceVariant = MutedInk,
    outline = DividerBeige,
    error = ErrorColor,
    onError = SurfaceCream
)

private val DarkColorScheme = darkColorScheme(
    primary = TicketCoral,
    onPrimary = DeepCinemaRed,
    primaryContainer = MarqueeBurgundy,
    onPrimaryContainer = WarmTicketCream,
    secondary = SoftWood,
    onSecondary = InkBlack,
    secondaryContainer = ShelfBrown,
    onSecondaryContainer = DarkPrimaryText,
    tertiary = BrassDetail,
    onTertiary = InkBlack,
    background = DarkBackground,
    onBackground = DarkPrimaryText,
    surface = DarkSurface,
    onSurface = DarkPrimaryText,
    surfaceVariant = DarkTicket,
    onSurfaceVariant = DarkSecondaryText,
    outline = DarkBorder,
    error = Color(0xFFCF6D6D),
    onError = DarkBackground
)

@Composable
fun MovieNestTheme(
    darkTheme: Boolean = androidx.compose.foundation.isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val extended = if (darkTheme) darkMovieNestColors else lightMovieNestColors

    androidx.compose.runtime.CompositionLocalProvider(
        LocalMovieNestColors provides extended
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = MovieNestTypography,
            shapes = MovieNestShapes,
            content = content
        )
    }
}

/** Convenience accessor for the cinema palette. */
object MovieNestTheme {
    val colors: MovieNestColors
        @Composable get() = LocalMovieNestColors.current
}
