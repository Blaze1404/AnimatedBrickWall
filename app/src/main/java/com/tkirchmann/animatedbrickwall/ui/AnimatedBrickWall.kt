package com.tkirchmann.animatedbrickwall.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AnimatedBrickWall(
    modifier: Modifier = Modifier,
    wallDimensions: BrickWallDimensions = BrickWallDefaults.defaultDimensions(),
    mortar: BrickWallMortar? = BrickWallDefaults.defaultMortar(),
    colors: BrickWallColors = BrickWallDefaults.defaultColors(),
    startAnimation: Boolean = true,
    buildInstantly: Boolean = false,
    animationConfig: AnimationConfig = BrickWallDefaults.defaultAnimationConfig(),
    onAnimationFinished: () -> Unit
) {
    val colorsOrBrushes = rememberColorsOrBrushes(colors, wallDimensions)
    validateColorListSize(wallDimensions, colorsOrBrushes)

    val rawBricks = rememberBricks(wallDimensions, colorsOrBrushes)
    val animatedBricks = rememberAnimatedBricks(rawBricks, buildInstantly)

    BrickWallAnimator(animatedBricks, animationConfig, startAnimation) { onAnimationFinished() }
    BrickWall(animatedBricks, wallDimensions, mortar, modifier)
}

@Composable
fun rememberBricks(
    wallDimensions: BrickWallDimensions,
    colorsOrBrushes: ColorsOrBrushes,
): List<List<Brick>> {
    val (bottomLeft, wallWidthRatio, wallHeightRatio, brickRows, bricksPerRow) = wallDimensions
    val defaultBrickWidthRatio = wallWidthRatio / bricksPerRow
    val defaultBrickHeightRatio = wallHeightRatio / brickRows
    val halfBrickWidthRatio = defaultBrickWidthRatio / 2f

    return buildBricks(
        brickRows = brickRows,
        bricksPerRow = bricksPerRow,
        bottomLeft = bottomLeft,
        defaultBrickWidthRatio = defaultBrickWidthRatio,
        halfBrickWidthRatio = halfBrickWidthRatio,
        defaultBrickHeightRatio = defaultBrickHeightRatio,
        colorsOrBrushes = colorsOrBrushes
    )
}

@Composable
private fun BrickWallAnimator(
    bricks: List<List<AnimatedBrick>>,
    animationConfig: AnimationConfig,
    startAnimation: Boolean,
    onFinished: () -> Unit
) {
    if (startAnimation) {
        LaunchedEffect(Unit) {
            // Go bottom to top
            for (row in bricks.indices) {
                // Left to right per row
                for (brick in bricks[row]) {
                    launch {
                        brick.animatable.animateTo(
                            targetValue = 1f,
                            animationSpec = animationConfig.animationSpec
                        )
                    }
                    delay(animationConfig.delay)
                }
            }
            onFinished()
        }
    }
}

@Composable
fun BrickWall(
    bricks: List<List<AnimatedBrick>>,
    wallDimensions: BrickWallDimensions,
    mortar: BrickWallMortar?,
    modifier: Modifier = Modifier
) {
    val (_, wallWidthRatio, wallHeightRatio, brickRows, bricksPerRow) = wallDimensions

    Canvas(modifier) {
        val strokeWidthX = size.width * (mortar?.thicknessRatio ?: 0f)
        val strokeWidthY = size.height * (mortar?.thicknessRatio ?: 0f)

        for (row in bricks) {
            for (animatedBrick in row) {
                val brick = animatedBrick.brick
                val progress = animatedBrick.animatable.value

                val ratioBrickWidth  = wallWidthRatio / bricksPerRow
                val ratioBrickHeight = wallHeightRatio / brickRows

                val isHalfBrick = brick.isHalf
                val brickWidthRatio = if (isHalfBrick) ratioBrickWidth / 2f else ratioBrickWidth

                val topLeft = Offset(
                    x = size.width * brick.position.x,
                    y = size.height * (brick.position.y - ratioBrickHeight * progress)
                )

                val brickWidth = size.width * brickWidthRatio
                val brickHeight = size.height * ratioBrickHeight * progress

                // Draw brick
                drawRectWithPaintMode(
                    paintMode = brick.paintMode,
                    topLeft = topLeft,
                    size = Size(brickWidth, brickHeight)
                )

                // Draw mortar if enabled
                mortar?.let { mortar ->
                    drawMortar(
                        mortar = mortar,
                        brick = brick,
                        brickRows = brickRows,
                        bricksPerRow = bricksPerRow,
                        progress = progress,
                        topLeft = topLeft,
                        brickWidth = brickWidth,
                        brickHeight = brickHeight,
                        strokeWidthX = strokeWidthX,
                        strokeWidthY = strokeWidthY
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawMortar(
    mortar: BrickWallMortar,
    brick: Brick,
    brickRows: Int,
    bricksPerRow: Int,
    progress: Float,
    topLeft: Offset,
    brickWidth: Float,
    brickHeight: Float,
    strokeWidthX: Float,
    strokeWidthY: Float
) {
    val (paintMode, _, drawOuterEdges) = mortar

    val isBottomRow = brick.row == brickRows - 1
    val isRightmost = brick.col == (if (brick.isHalf) bricksPerRow else bricksPerRow - 1)

    if (progress == 1f) {
        // Right edge mortar
        if (drawOuterEdges || !isRightmost) {
            drawLineWithPaintMode(
                paintMode = paintMode,
                start = topLeft + Offset(brickWidth, 0f),
                end = topLeft + Offset(brickWidth, brickHeight),
                strokeWidth = strokeWidthX
            )
        }

        // Bottom edge mortar
        if (drawOuterEdges || !isBottomRow) {
            drawLineWithPaintMode(
                paintMode = paintMode,
                start = topLeft + Offset(0f, brickHeight),
                end = topLeft + Offset(brickWidth, brickHeight),
                strokeWidth = strokeWidthY
            )
        }

        // Left edge mortar
        if (drawOuterEdges || brick.col > 0) {
            drawLineWithPaintMode(
                paintMode = paintMode,
                start = topLeft,
                end = topLeft + Offset(0f, brickHeight),
                strokeWidth = strokeWidthX
            )
        }

        // Top edge mortar
        if (drawOuterEdges || brick.row > 0) {
            drawLineWithPaintMode(
                paintMode = paintMode,
                start = topLeft + Offset(0f, 0f),
                end = topLeft + Offset(brickWidth, 0f),
                strokeWidth = strokeWidthY
            )
        }
    }
}

@Composable
private fun rememberAnimatedBricks(rawBricks: List<List<Brick>>, buildInstantly: Boolean) = remember {
    rawBricks.map { row ->
        row.map { brick ->
            AnimatedBrick(
                brick = brick,
                animatable = Animatable(if (buildInstantly) 1f else 0f)
            )
        }
    }
}

private fun buildBricks(
    brickRows: Int,
    bricksPerRow: Int,
    bottomLeft: Offset,
    defaultBrickWidthRatio: Float,
    halfBrickWidthRatio: Float,
    defaultBrickHeightRatio: Float,
    colorsOrBrushes: ColorsOrBrushes
) = List(brickRows) { row ->
    val isHalfBrickRow = row % 2 != 0

    List(if (isHalfBrickRow) bricksPerRow + 1 else bricksPerRow) { col ->
        val isFirstBrick = col == 0
        val isLastBrick = col == bricksPerRow

        val x = getBrickX(
            isHalfBrickRow = isHalfBrickRow,
            bottomLeft = bottomLeft,
            col = col,
            defaultBrickWidthRatio = defaultBrickWidthRatio,
            isFirstBrick = isFirstBrick,
            isLastBrick = isLastBrick,
            halfBrickWidthRatio = halfBrickWidthRatio
        )
        val y = bottomLeft.y - row * defaultBrickHeightRatio

        val paintMode = when (colorsOrBrushes) {
            is ColorsOrBrushes.Colors -> {
                val color = colorsOrBrushes.colors.get(row, col)
                PaintMode.ColorMode(color)
            }
            is ColorsOrBrushes.Brushes -> {
                val brush = colorsOrBrushes.brushes.get(row, col)
                PaintMode.BrushMode(brush)
            }
        }

        Brick(
            row = row,
            col = col,
            isHalf = isHalfBrickRow && (isFirstBrick || isLastBrick),
            paintMode = paintMode,
            position = Offset(x, y),
        )
    }
}

private fun <T> List<List<T>>.get(row: Int, col: Int): T = this[row][col]

fun getBrickX(
    isHalfBrickRow: Boolean,
    bottomLeft: Offset,
    col: Int,
    defaultBrickWidthRatio: Float,
    isFirstBrick: Boolean,
    isLastBrick: Boolean,
    halfBrickWidthRatio: Float
) = when {
    !isHalfBrickRow -> bottomLeft.x + col * defaultBrickWidthRatio
    else -> {
        when {
            isFirstBrick -> bottomLeft.x
            isLastBrick -> bottomLeft.x + (col - 1) * defaultBrickWidthRatio + halfBrickWidthRatio
            else -> bottomLeft.x + col * defaultBrickWidthRatio - halfBrickWidthRatio
        }
    }
}

object BrickWallDefaults {

    fun defaultAnimationConfig(
        animationSpec: AnimationSpec<Float> = tween(
            durationMillis = 200,
            easing = FastOutSlowInEasing
        ),
        delay: Long = 60L
    ) = AnimationConfig(
        animationSpec = animationSpec,
        delay = delay
    )

    fun defaultDimensions(
        bottomLeft: Offset = Offset(0f, 0f),
        wallWidthRatio: Float = 0.9f,
        wallHeightRatio: Float = 0.9f,
        brickRows: Int = 8,
        bricksPerRow: Int = 10
    ) = BrickWallDimensions(
        bottomLeft = bottomLeft,
        wallWidthRatio = wallWidthRatio,
        wallHeightRatio = wallHeightRatio,
        brickRows = brickRows,
        bricksPerRow = bricksPerRow
    )

    fun defaultMortar(
        paintMode: PaintMode = PaintMode.ColorMode(Color.LightGray),
        thicknessRatio: Float = 0.005f,
        drawOuterEdges: Boolean = true
    ) = BrickWallMortar(
        paintMode = paintMode,
        thicknessRatio = thicknessRatio,
        drawOuterEdges = drawOuterEdges
    )

    fun defaultColors(
        colorDispersion: ColorDispersion = ColorDispersion.Alternating(
            setOf(
                Color(0xFFB0B0B0), // medium gray
                Color(0xFFA0A0A0), // slightly darker
                Color(0xFFC0C0C0), // slightly lighter
                Color(0xFF909090), // darker tone
                Color(0xFFD0D0D0)  // brighter tone
            )
        )
    ) = BrickWallColors.Color(
        colorDispersion = colorDispersion
    )
}

data class AnimationConfig(
    val animationSpec: AnimationSpec<Float>,
    val delay: Long
)

data class BrickWallDimensions(
    val bottomLeft: Offset,
    val wallWidthRatio: Float,
    val wallHeightRatio: Float,
    val brickRows: Int,
    val bricksPerRow: Int,
)

data class BrickWallMortar(
    val paintMode: PaintMode,
    val thicknessRatio: Float, // Ratio of the WIDTH of the canvas
    val drawOuterEdges: Boolean = true
)

data class Brick(
    val row: Int,
    val col: Int,
    val isHalf: Boolean,
    val paintMode: PaintMode,
    val position: Offset,
)

data class AnimatedBrick(
    val brick: Brick,
    val animatable: Animatable<Float, AnimationVector1D>
)

sealed class ColorDispersion {
    data class Random(val colors: Set<Color>) : ColorDispersion()
    data class Alternating(val colors: Set<Color>) : ColorDispersion()
    data class Custom(val colors: List<List<Color>>) : ColorDispersion()
}

sealed class BrushPaintDispersion {
    data class Random(val brushes: Set<Brush>) : BrushPaintDispersion()
    data class Alternating(val brushes: Set<Brush>) : BrushPaintDispersion()
    data class Custom(val brushes: List<List<Brush>>) : BrushPaintDispersion()
}

sealed class BrickWallColors {
    data class Color(val colorDispersion: ColorDispersion) : BrickWallColors()
    data class Brush(val brushDispersion: BrushPaintDispersion) : BrickWallColors()
}

sealed class ColorsOrBrushes {
    data class Colors(val colors: List<List<Color>>) : ColorsOrBrushes()
    data class Brushes(val brushes: List<List<Brush>>) : ColorsOrBrushes()
}