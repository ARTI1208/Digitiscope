package ru.arti1208.digitiscope

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.FloatState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.DrawStyle
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

data class Point(
    val x: Int,
    val u: Int,
)

sealed interface DrawingShape {

    data class Rectangle(
        val topLeftX: Int,
        val topLeftY: Int,
        val bottomRightX: Int,
        val bottomRightY: Int,
    ): DrawingShape

    data class Line(
        val path: Path,
        val strokeWidth: Float,
    ) : DrawingShape
}

data class DrawingItem(
    val shape: DrawingShape,
    val color: Long,
    val blendMode: BlendMode = BlendMode.SrcOver,
)

sealed interface Tool {
    data object Pencil : Tool
    data object Eraser : Tool
}

enum class PaintOption {

}

data class HistoryItem(
    val v: Int,
)

private fun createNewBitmap(size: Size) = ImageBitmap(size.width.toInt(), size.height.toInt())
private fun createNewBitmap(size: IntSize) = ImageBitmap(size.width, size.height)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanvasScreen(modifier: Modifier = Modifier) {

    val color = remember { mutableStateOf(Color.Black) }
    val strokeWidth = remember { mutableFloatStateOf(5f) }
    val items = remember { mutableStateListOf<DrawingItem>() }
    val undoItems = remember { mutableStateListOf<HistoryItem>() }
    val redoItems = remember { mutableStateListOf<HistoryItem>() }
    val selectedTool = remember { mutableStateOf<Tool>(Tool.Pencil) }


    val images = remember { mutableStateListOf<ImageBitmap>() }
    val currentImageIndex = remember { mutableIntStateOf(-1) }
    val currentImage = remember { derivedStateOf { images[currentImageIndex.intValue] } }

    val imageSize = remember { mutableStateOf(IntSize(0, 0)) }

    fun newImage() {
        images.add(createNewBitmap(imageSize.value))
        currentImageIndex.intValue = images.lastIndex
    }

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding).padding(16.dp)) {

            Box(
                modifier = Modifier.fillMaxWidth().weight(1f).onSizeChanged {
                    imageSize.value = it
                    if (images.isEmpty()) {
                        newImage()
                    }
                }
            ) {
                if (currentImageIndex.intValue >= 0) {
                    DrawingCanvas(
                        modifier = Modifier.matchParentSize(),
                        toolState = selectedTool,
                        strokeWidthState = strokeWidth,
                        bitmapState = currentImage,
                    )
                }
            }

            Column {

                LazyRow {
                    itemsIndexed(images) { index, image ->
                        val height = image.height.coerceAtMost(200)
                        val width = (image.width * (height.toDouble() / image.height)).toInt()

                        val heightDp = with(LocalDensity.current) { height.toDp() }
                        val widthDp = with(LocalDensity.current) { width.toDp() }

                        Canvas(
                            modifier = Modifier.size(widthDp, heightDp)
                                .clickable {
                                    currentImageIndex.intValue = index
                                }.drawWithContent {
                                    drawContent()
                                    if (index == currentImageIndex.intValue) {
                                        drawRoundRect(Color.Blue, style = Stroke(12f), cornerRadius = CornerRadius(16f, 16f))
                                    }
                                },
                        ) {
                            drawImage(image, dstSize = IntSize(width, height))
                        }
                    }
                }

                Slider(
                    value = strokeWidth.floatValue,
                    onValueChange = { strokeWidth.floatValue = it },
                    valueRange = 1f..100f
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    TextButton(onClick = { selectedTool.value = Tool.Pencil }) {
                        Text("Line")
                    }

                    TextButton(onClick = { selectedTool.value = Tool.Eraser }) {
                        Text("Eraser")
                    }

                    TextButton(onClick = {
                        newImage()
                    }) {
                        Text("New image")
                    }
                }


            }
        }
    }
}

@Composable
private fun DrawingCanvas(
    modifier: Modifier = Modifier,
    toolState: State<Tool>,
    strokeWidthState: FloatState,
    bitmapState: State<ImageBitmap>,
) {

    val bitmap = bitmapState.value

    var isDrawing = remember { mutableStateOf(false) }

    val drawings = remember(bitmap) { mutableStateListOf<DrawingItem>().apply {
        add(DrawingItem(color = Color.Red.toArgb().toLong(), shape = DrawingShape.Rectangle(400, 800, 700, 1100)))
    } }
    val scope = remember(bitmap) { CanvasDrawScope() }
    val canvas = remember(bitmap) { androidx.compose.ui.graphics.Canvas(bitmap) }


        val back = ImageBitmap.imageResource(R.drawable.background)

//    val coroutineScope = rememberCoroutineScope()

        fun Offset.rotateBy(angle: Float): Offset {
            val angleInRadians = angle * PI / 180
            return Offset(
                (x * cos(angleInRadians) - y * sin(angleInRadians)).toFloat(),
                (x * sin(angleInRadians) + y * cos(angleInRadians)).toFloat(),
            )
        }

        var offset by remember { mutableStateOf(Offset.Zero) }
        var angleCenter by remember { mutableStateOf(Offset(bitmap.width.toFloat() / 2, bitmap.height.toFloat() / 2)) }
        var angle by remember { mutableStateOf(0f) }
        var zoom by remember { mutableStateOf(1f) }

        val fingersDown = remember { BooleanArray(10) }

        Canvas(
            modifier = modifier
                .clip(RoundedCornerShape(16.dp))
                .drawBehind { drawImage(back) }
                .pointerInput(Unit) {

                coroutineScope {
                    awaitEachGesture {
                        val paths = Array(10) { Path() }
                        val change = awaitFirstDown(requireUnconsumed = false)
                        paths[change.id.value.toInt()].moveTo(change.position.x, change.position.y)

                        println("Starttt at ${change.position}")
                        do {
                            val event = awaitPointerEvent()
                            val canceled = event.changes.fastAny { it.isConsumed }
                            if (!canceled) {
                                event.changes.fastForEach { change ->
                                    if (change.pressed) {
                                        val path = paths[change.id.value.toInt()]
                                        if (change.previousPressed) {
                                            path.lineTo(change.position.x, change.position.y)
                                        } else {
                                            path.moveTo(change.position.x, change.position.y)
                                        }
                                    }
                                }
                            }
                        } while (!canceled && event.changes.fastAny { it.pressed })

                        paths.forEach { path ->
                            if (path.isEmpty.not()) {

                                val color = if (toolState.value is Tool.Eraser) Color.White else Color.Blue
                                val mode = if (toolState.value is Tool.Eraser) BlendMode.Clear else BlendMode.SrcOver

                                drawings.add(
                                    DrawingItem(
                                        DrawingShape.Line(path, strokeWidthState.floatValue),
                                        color.toArgb().toLong(),
                                        mode
                                    )
                                )
                            }
                        }
                    }
                }

                coroutineScope {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        do {
                            val event = awaitPointerEvent()
                            val canceled = event.changes.fastAny { it.isConsumed }
                            if (!canceled) {
                                event.changes.forEach {
                                    if (it.pressed != it.previousPressed) {
                                        fingersDown[it.id.value.toInt()] = it.pressed
                                        println("FAFFAFA: ${it.id.value}: ${it.pressed}")
                                    }
                                }
                            }
                        } while (!canceled && event.changes.fastAny { it.pressed })
                    }
                }

                coroutineScope {
                    detectTransformGestures { centroid, pan, gestZoom, rotation ->
                        val oldScale = zoom
                        val newScale = zoom * gestZoom
                        // For natural zooming and rotating, the centroid of the gesture should
                        // be the fixed point where zooming and rotating occurs.
                        // We compute where the centroid was (in the pre-transformed coordinate
                        // space), and then compute where it will be after this delta.
                        // We then compute what the new offset should be to keep the centroid
                        // visually stationary for rotating and zooming, and also apply the pan.

//                        if (gestZoom != 1f) {
//                            angleCenter = (angleCenter + centroid / oldScale).rotateBy(rotation) //-
////                                (centroid / newScale + pan / oldScale)
//                        }
//
//                    if (fingersDown.count() >= 2) {
//                        offset = (offset + centroid / oldScale).rotateBy(rotation) -
//                                (centroid / newScale + pan / oldScale)
//                    }
                        zoom = newScale
//                        angle += rotation
                        println("FAFFAFA: $angleCenter")
                    }
                }
            },
        ) {
            scope.draw(
                Density(1f),
                LayoutDirection.Ltr,
                canvas,
                size,
            ) {
                withTransform({
                    translate(-offset.x * zoom, -offset.y * zoom)
                    scale(zoom, zoom)
                    rotate(angle, angleCenter)
                }) {

                    drawings.forEach {
                        when (it.shape) {
                            is DrawingShape.Line -> drawPath(
                                it.shape.path,
                                color = Color(it.color),
                                style = Stroke(width = it.shape.strokeWidth, cap = StrokeCap.Round),
                                blendMode = it.blendMode
                            )

                            is DrawingShape.Rectangle -> drawRect(
                                color = Color(it.color),
                                topLeft = Offset(
                                    it.shape.topLeftX.toFloat(),
                                    it.shape.topLeftY.toFloat()
                                ),
                                size = Size(
                                    it.shape.run { bottomRightX - topLeftX }.toFloat(),
                                    it.shape.run { bottomRightY - topLeftY }.toFloat()
                                ),
                            )
                        }
                    }
                }
            }

            drawImage(bitmap)
    }
}