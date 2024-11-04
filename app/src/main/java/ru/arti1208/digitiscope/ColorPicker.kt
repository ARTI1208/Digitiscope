@file:OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)

package ru.arti1208.digitiscope

import android.graphics.ComposeShader
import android.graphics.PorterDuff
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastAny

@Composable
fun ColorPickerBottomSheet(
    showColorPickerState: MutableState<Boolean>,
    colorState: MutableState<Color>,
) {
    if (!showColorPickerState.value) return
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        sheetState = state,
        onDismissRequest = { showColorPickerState.value = false },
    ) {
        ColorPicker(Modifier, colorState)
    }
}

@Composable
fun ColorPicker(
    modifier: Modifier,
    colorState: MutableState<Color>,
) {
    Column(
        modifier = modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        FlowRow {
            ColoredCircleButton(
                color = colorState.value,
                borderColor = MaterialTheme.colorScheme.onSurface,
                onClick = { },
            )
        }

        val staValSize = remember { mutableStateOf(IntSize.Zero) }

        val satValOffset = remember { derivedStateOf {
            val hsvArray = floatArrayOf(0f, 0f, 0f)
            android.graphics.Color.colorToHSV(colorState.value.toArgb(), hsvArray)
            val offsetX = hsvArray[1] * staValSize.value.width
            val offsetY = (1f - hsvArray[2]) * staValSize.value.width
            Offset(offsetX, offsetY)
        } }

        fun updateValueAndSaturation(offset: Offset, size: IntSize) {
            val satPoint = 1f / size.width * offset.x
            val valuePoint = 1f - 1f / size.height * offset.y
            val hsvArray = floatArrayOf(0f, 0f, 0f)
            android.graphics.Color.colorToHSV(colorState.value.toArgb(), hsvArray)
            hsvArray[1] = satPoint
            hsvArray[2] = valuePoint
            val color = android.graphics.Color.HSVToColor(hsvArray)
            colorState.value = Color(color).copy(alpha = colorState.value.alpha)
        }

        Canvas(
            modifier = Modifier
                .size(240.dp)
                .onSizeChanged {
                    staValSize.value = it
                }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val gesture = awaitFirstDown()
                        gesture.consume()
                        updateValueAndSaturation(gesture.position, size)
                        do {
                            size
                            val event = awaitPointerEvent()
                            val canceled = event.changes.fastAny { it.isConsumed }
                            if (!canceled) {
                                event.changes.forEach {
                                    updateValueAndSaturation(it.position, size)
                                    it.consume()
                                }
                            }
                        } while (!canceled && event.changes.fastAny { it.pressed })
                    }
                }
        ) {

            val hsvArray = FloatArray(3)
            android.graphics.Color.colorToHSV(
                colorState.value.toArgb(),
                hsvArray,
            )
            hsvArray[1] = 1f
            hsvArray[2] = 1f

            val saturationColor = Color(android.graphics.Color.HSVToColor(hsvArray))

            val saturationShader = LinearGradientShader(
                from = Offset.Zero,
                to = Offset(size.width, 0f),
                colors = listOf(Color(-0x1), saturationColor),
            )
            val valueShader = LinearGradientShader(
                from = Offset.Zero,
                to = Offset(0f, size.height),
                colors = listOf(Color(-0x1), Color(-0x1000000)),
            )

            drawRect(
                brush = ShaderBrush(
                    ComposeShader(
                        valueShader,
                        saturationShader,
                        PorterDuff.Mode.MULTIPLY,
                    )
                ).apply {

                }
            )

            drawCircle(
                color = Color.White,
                radius = 20f,
                center = satValOffset.value,
                style = Stroke(3f),
            )
        }

        val canvasSize = remember { mutableStateOf(IntSize.Zero) }

        val selectedColorIndex = remember { derivedStateOf {
            val hsv = FloatArray(3)
            android.graphics.Color.colorToHSV(colorState.value.toArgb(), hsv)
            val selectedHue = hsv[0]
            selectedHue * canvasSize.value.width / 360f
        } }


        fun updateColor(offset: Offset, size: IntSize) {
            val fixedOffset = offset.copy(
                x = offset.x.coerceIn(0f, size.width.toFloat()),
            )
            val colorIndex = fixedOffset.x.toInt().coerceAtMost(size.width - 1)

            val hue = colorIndex * 360f / size.width
            val hsvArray = floatArrayOf(hue, 1f, 1f)
            val color = android.graphics.Color.HSVToColor(hsvArray)
            colorState.value = Color(color).copy(alpha = colorState.value.alpha)
        }

        Canvas(
            modifier = Modifier.fillMaxWidth().height(20.dp)
                .onSizeChanged {
                    canvasSize.value = it
                }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val gesture = awaitFirstDown()
                        gesture.consume()
                        updateColor(gesture.position, size)
                        do {
                            val event = awaitPointerEvent()
                            val canceled = event.changes.fastAny { it.isConsumed }
                            if (!canceled) {
                                event.changes.forEach {
                                    updateColor(it.position, size)
                                    it.consume()
                                }
                            }
                        } while (!canceled && event.changes.fastAny { it.pressed })
                    }
                }
        ) {
            val hsvArray = floatArrayOf(0f, 1f, 1f)
            val intWidth = size.width.toInt()
            repeat(intWidth) {
                val hue = it * 360f / intWidth
                hsvArray[0] = hue
                val color = android.graphics.Color.HSVToColor(hsvArray)

                drawLine(
                    color = Color(color),
                    start = Offset(it.toFloat(), 0f),
                    end = Offset(it.toFloat(), size.height),
                )
            }
            drawCircle(
                color = Color.White,
                center = Offset(selectedColorIndex.value, size.height / 2),
                style = Stroke(3f),
            )
        }

        Slider(
            value = colorState.value.alpha,
            onValueChange = { colorState.value = colorState.value.copy(alpha = it) },
        )

        Text("Alpha", fontSize = 10.sp)
    }
}