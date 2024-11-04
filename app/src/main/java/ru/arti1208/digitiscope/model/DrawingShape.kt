package ru.arti1208.digitiscope.model

import androidx.compose.ui.graphics.Path

sealed interface DrawingShape {

    data class PathShape(
        val path: Path,
        val strokeWidth: Float,
    ) : DrawingShape
}