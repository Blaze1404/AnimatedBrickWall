package com.tkirchmann.animatedbrickwall.ui

import android.content.Context
import android.graphics.Matrix
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.TileMode
import androidx.compose.ui.res.imageResource

fun textureBrush(
    context: Context,
    @DrawableRes textureId: Int,
): Brush {
    val imageBitmap = ImageBitmap.imageResource(context.resources, textureId)
    val shader = ImageShader(imageBitmap, TileMode.Repeated, TileMode.Repeated)

    // Create a transformation matrix
    val matrix = Matrix()

    // Scale (e.g., 2x larger texture)
    matrix.preScale(0.1f, 0.1f)

    // Apply the matrix to the shader
    shader.setLocalMatrix(matrix)

    return ShaderBrush(shader)
}

val randomColorDispersion = ColorDispersion.Random(
    setOf(
        Color(0xFF8D6E63), // soft brown
        Color(0xFF795548), // chocolate brown
        Color(0xFFA1887F), // taupe
        Color(0xFF6D4C41), // dark cocoa
        Color(0xFFD7CCC8)  // light beige
    )
)

val alternatingColorDispersion = ColorDispersion.Alternating(
    setOf(
        Color(0xFF607D8B), // blue-gray
        Color(0xFF9E9E9E), // gray
        Color(0xFFCFD8DC)  // light gray
    )
)

// Sample color shortcuts for readability
private val B = Color(0xFF3E2723) // Dark brown border
private val W = Color.White
private val R = Color.Red
private val O = Color(0xFFFF9800) // Orange

val customColorDispersion = ColorDispersion.Custom(
    listOf(
        listOf(B, B, B, B, B, B, B, B, B, B),
        listOf(B, W, W, W, W, W, W, W, W, W, B),
        listOf(B, W, O, O, O, O, O, O, W, B),
        listOf(B, W, O, R, R, R, R, R, O, W, B),
        listOf(B, W, O, R, R, R, R, O, W, B),
        listOf(B, W, W, O, R, R, R, O, W, W, B),
        listOf(B, W, W, O, R, R, O, W, W, B),
        listOf(B, W, W, W, O, O, O, W, W, W, B),
        listOf(B, W, W, W, W, W, W, W, W, B),
        listOf(B, B, B, B, B, B, B, B, B, B, B),
    )
)

@Composable
fun rememberColorsOrBrushes(
    colors: BrickWallColors,
    wallDimensions: BrickWallDimensions,
) = remember {
    val (_, _, _, brickRows, bricksPerRow) = wallDimensions

    when (colors) {
        is BrickWallColors.Color -> {
            val brickColors = when (colors.colorDispersion) {
                is ColorDispersion.Random -> randomColorDispersion(brickRows, bricksPerRow, colors.colorDispersion.colors)
                is ColorDispersion.Alternating -> alternatingColorDispersion(brickRows, bricksPerRow, colors.colorDispersion.colors)
                is ColorDispersion.Custom -> colors.colorDispersion.colors
            }
            ColorsOrBrushes.Colors(brickColors)
        }
        is BrickWallColors.Brush -> {
            val brickBrushes = when (colors.brushDispersion) {
                is BrushPaintDispersion.Random -> randomBrushDispersion(brickRows, bricksPerRow, colors.brushDispersion.brushes)
                is BrushPaintDispersion.Alternating -> alternatingBrushDispersion(brickRows, bricksPerRow, colors.brushDispersion.brushes)
                is BrushPaintDispersion.Custom -> colors.brushDispersion.brushes
            }
            ColorsOrBrushes.Brushes(brickBrushes)
        }
    }
}

fun validateColorListSize(
    wallDimensions: BrickWallDimensions,
    colorsOrBrushes: ColorsOrBrushes,
) {
    val bricksPerRow = wallDimensions.bricksPerRow
    val brickRows = wallDimensions.brickRows

    val list = when (colorsOrBrushes) {
        is ColorsOrBrushes.Colors -> colorsOrBrushes.colors
        is ColorsOrBrushes.Brushes -> colorsOrBrushes.brushes
    }

    val oddBricksPerRow = bricksPerRow + 1
    Log.d("validateColorListSize", "Expected: $brickRows rows, $bricksPerRow columns for even rows and $oddBricksPerRow for odd rows")
    require(list.size == brickRows) { "Color list size must match the number of rows" }
    list.forEachIndexed { index, row ->
        val expectedColumns = if (index % 2 == 0) bricksPerRow else oddBricksPerRow
        require(row.size == expectedColumns) {
            "Row $index must have $expectedColumns columns, but has ${row.size}"
        }
    }
}

private fun alternatingBrushDispersion(
    brickRows: Int,
    bricksPerRow: Int,
    brushes: Set<Brush>
) = List(brickRows) { row ->
    val isHalfBrickRow = row % 2 != 0
    val totalCols = if (isHalfBrickRow) bricksPerRow + 1 else bricksPerRow
    List(totalCols) { col ->
        val index = if (row % 2 == 0) col else col + 1 // simple offset for variety
        brushes.elementAt(index % brushes.size)
    }
}

private fun randomBrushDispersion(
    brickRows: Int,
    bricksPerRow: Int,
    brushes: Set<Brush>
) = List(brickRows) { row ->
    val isHalfBrickRow = row % 2 != 0
    val columns = if (isHalfBrickRow) bricksPerRow + 1 else bricksPerRow
    List(columns) { _ -> brushes.random() }
}

private fun alternatingColorDispersion(
    brickRows: Int,
    bricksPerRow: Int,
    colors: Set<Color>
) = List(brickRows) { row ->
    val isHalfBrickRow = row % 2 != 0
    val totalCols = if (isHalfBrickRow) bricksPerRow + 1 else bricksPerRow
    List(totalCols) { col ->
        val index = if (row % 2 == 0) col else col + 1 // simple offset for variety
        colors.elementAt(index % colors.size)
    }
}

private fun randomColorDispersion(
    brickRows: Int,
    bricksPerRow: Int,
    colors: Set<Color>
) = List(brickRows) { row ->
    val isHalfBrickRow = row % 2 != 0
    val columns = if (isHalfBrickRow) bricksPerRow + 1 else bricksPerRow
    List(columns) { _ -> colors.random() }
}