package com.movienest.log.ui.components

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * A rounded rectangle with two semicircular notches carved out of the left and
 * right edges — the classic torn cinema-ticket silhouette. Purely decorative.
 */
class TicketShape(
    private val cornerRadius: Dp = 14.dp,
    private val notchRadius: Dp = 9.dp,
    private val notchCenterYFraction: Float = 0.5f
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val corner = with(density) { cornerRadius.toPx() }
        val notch = with(density) { notchRadius.toPx() }
        val cy = size.height * notchCenterYFraction

        val base = Path().apply {
            addRoundRect(
                androidx.compose.ui.geometry.RoundRect(
                    left = 0f,
                    top = 0f,
                    right = size.width,
                    bottom = size.height,
                    radiusX = corner,
                    radiusY = corner
                )
            )
        }
        val leftNotch = Path().apply {
            addOval(
                androidx.compose.ui.geometry.Rect(
                    center = Offset(0f, cy),
                    radius = notch
                )
            )
        }
        val rightNotch = Path().apply {
            addOval(
                androidx.compose.ui.geometry.Rect(
                    center = Offset(size.width, cy),
                    radius = notch
                )
            )
        }
        val result = Path().apply {
            op(base, leftNotch, PathOperation.Difference)
            op(this, rightNotch, PathOperation.Difference)
        }
        return Outline.Generic(result)
    }
}
