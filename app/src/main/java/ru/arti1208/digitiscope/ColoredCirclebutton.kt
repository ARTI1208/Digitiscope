package ru.arti1208.digitiscope

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun ColoredCircleButton(
    modifier: Modifier = Modifier,
    buttonSize: Dp = 32.dp,
    color: Color,
    borderColor: Color,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick) {
        val checkCount = 4
        Box(
            Modifier
                .size(buttonSize)
                .clip(CircleShape)
                .border(1.dp, borderColor, CircleShape)
                .drawBehind {
                    repeat(checkCount) { column ->
                        repeat(checkCount) { row ->
                            val w = size.width / checkCount
                            val h = size.height / checkCount
                            drawRect(
                                if ((column + row) % 2 == 0) {
                                    Color.Gray
                                } else {
                                    Color.White
                                },
                                topLeft = Offset(column * w, row * h),
                                size = Size(w, h),
                            )
                        }
                    }
                }
                .background(color)
                .then(modifier)
        )
    }
}