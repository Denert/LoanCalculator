package com.mickey.loan_calc.calculator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * Custom shape for bottom sheet with handle notch.
 *
 * Shape structure (top edge, left to right):
 * - Left corner: raised up with rounded corner
 * - Curve down (concave) to the dip
 * - Center bump up for handle
 * - Curve down (concave) to the dip
 * - Right corner: raised up with rounded corner
 */
class BottomSheetWithHandleShape(
    val cornerRadius: Dp = 16.dp,
    private val cornerSmallRadius: Dp = 8.dp,
    val handleBumpWidth: Dp = 100.dp,
    private val handleBumpHeight: Dp = 16.dp,
    private val dipDepth: Dp = 12.dp
) : Shape {

    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val path = Path()

        val cornerRadiusPx = with(density) { cornerRadius.toPx() }
        val cornerSmallRadiusPx = with(density) { cornerSmallRadius.toPx() }
        val handleBumpWidthPx = with(density) { handleBumpWidth.toPx() }


        val width = size.width
        val height = size.height
        val centerX = width / 2

        // Key Y positions (from top)
        val topY = 0f                           // Top of corners

        // Key X positions
        val handleLeft = centerX - handleBumpWidthPx / 2
        val handleRight = centerX + handleBumpWidthPx / 2

        path.moveTo(0f, cornerRadiusPx)

        // Left edge going up
        // Top-left corner arc
        path.quadraticTo(
            0f, 0f,
            cornerRadiusPx, topY
        )

        path.lineTo(handleLeft, 0f)
        path.quadraticTo( // полузакругление слева
            handleLeft + cornerSmallRadiusPx / 2, 0f,
            handleLeft + cornerSmallRadiusPx, cornerSmallRadiusPx
        )
        path.quadraticTo( // полузакругление слева
            handleLeft + cornerSmallRadiusPx + cornerSmallRadiusPx / 2, cornerRadiusPx,
            handleLeft + cornerRadiusPx, cornerRadiusPx
        )
        path.lineTo(handleRight - cornerRadiusPx, cornerRadiusPx)

        path.quadraticTo( // полузакругление справа
            handleRight - cornerSmallRadiusPx - cornerSmallRadiusPx / 2, cornerRadiusPx,
            handleRight - cornerSmallRadiusPx, cornerSmallRadiusPx
        )
        path.quadraticTo( // полузакругление справа
            handleRight - cornerSmallRadiusPx / 2, 0f,
            handleRight, 0f
        )

        path.lineTo(width - cornerRadiusPx, topY)

        // Top-right corner arc
        path.quadraticTo(
            width, 0f,
            width, cornerRadiusPx
        )

        // Right edge down
        path.lineTo(width, height)

        // Bottom edge
        path.lineTo(0f, height)

        // Left edge up to start
        path.close()

        return Outline.Generic(path)
    }
}

@Composable
fun BottomSheetContainer(
    modifier: Modifier = Modifier,
    shape: BottomSheetWithHandleShape = BottomSheetWithHandleShape(),
    color: Color = Color.White,
    shadowElevation: Dp = 4.dp,
    handleBarWidth: Dp = 40.dp,
    handleBarColor: Color = Color.White,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            shape = shape,
            color = color,
            shadowElevation = shadowElevation
        ) {
            Box(content = content)
        }

        // Handle bar drawn above the cutout (in the bump area)
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = (shape.cornerRadius - 4.dp) / 2 - 4.dp)
                .width(handleBarWidth)
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(handleBarColor)
        )
    }
}
