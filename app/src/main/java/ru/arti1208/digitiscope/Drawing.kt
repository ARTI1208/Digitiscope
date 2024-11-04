package ru.arti1208.digitiscope

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import ru.arti1208.digitiscope.model.Tool
import kotlin.math.max

fun Path.applyDrawing(
    tool: Tool,
    originalOffset: Offset,
    previousOffset: Offset,
    lastOffset: Offset,
) {
    when (tool) {
        Tool.Move -> Unit
        Tool.Pencil, Tool.Eraser -> {
            quadraticTo(
                previousOffset.x,
                previousOffset.y,
                (previousOffset.x + lastOffset.x) / 2,
                (previousOffset.y + lastOffset.y) / 2,
            )
        }

        is Tool.Shape -> {
            reset()
            moveTo(originalOffset.x, originalOffset.y)
            when (tool) {
                Tool.Shape.Line -> lineTo(
                    lastOffset.x,
                    lastOffset.y
                )

                Tool.Shape.Oval -> addOval(Rect(originalOffset, lastOffset))
                Tool.Shape.Circle -> addOval(
                    Rect(originalOffset, lastOffset.run {
                        max(x - originalOffset.x, y - originalOffset.y)
                    })
                )

                Tool.Shape.Rectangle -> addRect(Rect(originalOffset, lastOffset))
                Tool.Shape.Square -> addRect(
                    Rect(originalOffset, lastOffset.run {
                        max(x - originalOffset.x, y - originalOffset.y)
                    })
                )
            }
        }
    }
}