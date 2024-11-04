package ru.arti1208.digitiscope

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import ru.arti1208.digitiscope.model.Tool
import kotlin.math.max
import kotlin.math.min

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

                Tool.Shape.Oval -> {
                    val left = min(originalOffset.x, lastOffset.x)
                    val top = min(originalOffset.y, lastOffset.y)
                    val right = max(originalOffset.x, lastOffset.x)
                    val bottom = max(originalOffset.y, lastOffset.y)
                    val rect = Rect(left, top, right, bottom)

                    addOval(rect)
                }
                Tool.Shape.Circle -> addOval(
                    Rect(originalOffset, lastOffset.run {
                        max(x - originalOffset.x, y - originalOffset.y)
                    })
                )

                Tool.Shape.Rectangle -> {
                    val left = min(originalOffset.x, lastOffset.x)
                    val top = min(originalOffset.y, lastOffset.y)
                    val right = max(originalOffset.x, lastOffset.x)
                    val bottom = max(originalOffset.y, lastOffset.y)
                    val rect = Rect(left, top, right, bottom)

                    addRect(rect)
                }
                Tool.Shape.Square -> addRect(
                    Rect(originalOffset, lastOffset.run {
                        max(x - originalOffset.x, y - originalOffset.y)
                    })
                )
            }
        }
    }
}