package com.tkirchmann.animatedbrickwall.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill

sealed class PaintMode {
    data class ColorMode(val color: Color) : PaintMode()
    data class BrushMode(val brush: Brush) : PaintMode()
}

fun DrawScope.drawLineWithPaintMode(
    paintMode: PaintMode,
    start: Offset,
    end: Offset,
    strokeWidth: Float
) {
    when (paintMode) {
        is PaintMode.ColorMode -> drawLine(
            color = paintMode.color,
            start = start,
            end = end,
            strokeWidth = strokeWidth,
        )
        is PaintMode.BrushMode -> drawLine(
            brush = paintMode.brush,
            start = start,
            end = end,
            strokeWidth = strokeWidth,
        )
    }
}

fun DrawScope.drawRectWithPaintMode(
    paintMode: PaintMode,
    topLeft: Offset,
    size: Size
) {
    when (paintMode) {
        is PaintMode.ColorMode -> drawRect(
            color = paintMode.color,
            topLeft = topLeft,
            size = size,
            style = Fill
        )
        is PaintMode.BrushMode -> drawRect(
            brush = paintMode.brush,
            topLeft = topLeft,
            size = size,
            style = Fill
        )
    }
}