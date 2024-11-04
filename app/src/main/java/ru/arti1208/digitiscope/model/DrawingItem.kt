package ru.arti1208.digitiscope.model

import androidx.compose.ui.graphics.BlendMode

data class DrawingItem(
    val shape: DrawingShape,
    val color: Long,
    val blendMode: BlendMode = BlendMode.SrcOver,
)