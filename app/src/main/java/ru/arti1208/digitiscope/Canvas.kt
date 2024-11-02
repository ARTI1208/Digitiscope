@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package ru.arti1208.digitiscope

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.FloatState
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import kotlinx.coroutines.coroutineScope
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.collections.removeLast as removeFromEnd

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
    val drawingItem: DrawingItem,
)

private fun createNewBitmap(size: Size) = ImageBitmap(size.width.toInt(), size.height.toInt())
private fun createNewBitmap(size: IntSize) = ImageBitmap(size.width, size.height)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CanvasScreen(modifier: Modifier = Modifier) {

    val color = remember { mutableStateOf(Color.Black) }
    val strokeWidth = remember { mutableFloatStateOf(5f) }
    val items = remember { mutableStateListOf<DrawingItem>() }
    val selectedTool = remember { mutableStateOf<Tool>(Tool.Pencil) }

    val undoItems = remember { mutableStateListOf<HistoryItem>() }
    val redoItems = remember { mutableStateListOf<HistoryItem>() }

    val images = remember { mutableStateListOf<ImageBitmap>() }
    val currentImageIndex = remember { mutableIntStateOf(-1) }
    val currentImage = remember { derivedStateOf { images[currentImageIndex.intValue] } }

    val imageSize = remember { mutableStateOf(IntSize(0, 0)) }

    fun newImage() {
        images.add(createNewBitmap(imageSize.value))
        currentImageIndex.intValue = images.lastIndex
    }

    fun deleteCurrentImage() {
        val index = currentImageIndex.intValue
        images.removeAt(index)

        if (images.isEmpty()) {
            newImage()
        } else {
            currentImageIndex.intValue = (index - 1).coerceAtLeast(0)
        }
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
                        colorState = color,
                        toolState = selectedTool,
                        strokeWidthState = strokeWidth,
                        bitmapState = currentImage,
                        onDrawingItemAdd = {
                            redoItems.clear()
                            undoItems.add(HistoryItem((it)))
                        }
                    )
                }
            }

            Column {

                PreviewRow(
                    modifier = Modifier.padding(vertical = 4.dp),
                    images = images,
                    selectedIndexState = currentImageIndex,
                    newFrame = ::newImage,
                )

                Slider(
                    value = strokeWidth.floatValue,
                    onValueChange = { strokeWidth.floatValue = it },
                    valueRange = 1f..100f
                )

                ToolsRow(
                    modifier = Modifier.fillMaxWidth(),
                    colorState = color,
                    toolState = selectedTool,
                    undoItems = undoItems,
                    redoItems = redoItems,
                    newFrame = ::newImage,
                    deleteCurrentFrame = ::deleteCurrentImage,
                )
            }
        }
    }
}

@Composable
private fun DrawingCanvas(
    modifier: Modifier = Modifier,
    colorState: State<Color>,
    toolState: State<Tool>,
    strokeWidthState: FloatState,
    bitmapState: State<ImageBitmap>,
    onDrawingItemAdd: (DrawingItem) -> Unit,
) {

    val bitmap = bitmapState.value

    var isDrawing = remember { mutableStateOf(false) }

    val drawings = remember(bitmap) { mutableStateListOf<DrawingItem>() }
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

        var lastUpdate by remember { mutableStateOf(0) }

        Canvas(
            modifier = modifier
                .clip(RoundedCornerShape(16.dp))
                .drawBehind { drawImage(back) }
                .pointerInput(Unit) {

                coroutineScope {
                    awaitEachGesture {
                        val paths = mutableMapOf<PointerId, Path>()
                        val change = awaitFirstDown(requireUnconsumed = false)

                        fun getPath(id: PointerId) = paths.getOrPut(id) {
                            Path().also { path ->

                                val color = if (toolState.value is Tool.Eraser) Color.White else colorState.value
                                val mode = if (toolState.value is Tool.Eraser) BlendMode.Clear else BlendMode.SrcOver

                                val drawingItem = DrawingItem(
                                    DrawingShape.Line(path, strokeWidthState.floatValue),
                                    color.toArgb().toLong(),
                                    mode
                                )

                                drawings.add(drawingItem)

                                onDrawingItemAdd(drawingItem)
                            }
                        }

                        getPath(change.id).moveTo(change.position.x, change.position.y)

                        println("Starttt at ${change.position}")
                        do {
                            val event = awaitPointerEvent()
                            val canceled = event.changes.fastAny { it.isConsumed }
                            if (!canceled) {
                                event.changes.fastForEach { change ->
                                    if (change.pressed) {
                                        val path = getPath(change.id)
                                        if (change.previousPressed) {
                                            path.lineTo(change.position.x, change.position.y)
//                                            path.quadraticTo(
//                                                change.previousPosition.x,
//                                                change.previousPosition.y,
//                                                (change.previousPosition.x + change.position.x) / 2,
//                                                (change.previousPosition.y + change.position.y) / 2,
//                                            )
                                        } else {
                                            path.moveTo(change.position.x, change.position.y)
                                        }
                                        lastUpdate++
                                    }
                                }
                            }
                        } while (!canceled && event.changes.fastAny { it.pressed })

//                        paths.forEach { (_, path) ->
//                            if (path.isEmpty.not()) {
//
//                                val color = if (toolState.value is Tool.Eraser) Color.White else Color.Blue
//                                val mode = if (toolState.value is Tool.Eraser) BlendMode.Clear else BlendMode.SrcOver
//
//                                drawings.add(
//                                    DrawingItem(
//                                        DrawingShape.Line(path, strokeWidthState.floatValue),
//                                        color.toArgb().toLong(),
//                                        mode
//                                    )
//                                )
//                            }
//                        }

                        paths.clear()
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

                    lastUpdate
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

@Composable
fun PreviewRow(
    modifier: Modifier = Modifier,
    images: SnapshotStateList<ImageBitmap>,
    selectedIndexState: MutableIntState,
    newFrame: () -> Unit,
) {
    LazyRow(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        itemsIndexed(images) { index, image ->
            val height = image.height.coerceAtMost(200)
            val width = (image.width * (height.toDouble() / image.height)).toInt()

            val heightDp = with(LocalDensity.current) { height.toDp() }
            val widthDp = with(LocalDensity.current) { width.toDp() }

            val borderColor = when (index) {
                selectedIndexState.intValue -> Color.Blue
                else -> Color.Gray
            }

            Canvas(
                modifier = Modifier.size(widthDp, heightDp)
                    .border(1.dp, borderColor, RoundedCornerShape(8.dp))
                    .clickable {
                        selectedIndexState.intValue = index
                    },
            ) {
                drawImage(image, dstSize = IntSize(width, height))
            }
        }
    }
}

@Composable
fun ToolsRow(
    modifier: Modifier,
    colorState: MutableState<Color>,
    toolState: MutableState<Tool>,
    undoItems: SnapshotStateList<HistoryItem>,
    redoItems: SnapshotStateList<HistoryItem>,
    newFrame: () -> Unit,
    deleteCurrentFrame: () -> Unit,
) {

    var showColorSelector by remember { mutableStateOf(false) }
    if (showColorSelector) {
        ColorSelectorBottomSheet(colorState) {showColorSelector = false }
    }

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {

        item {
            ColoredCircleButton(
                modifier = Modifier.size(32.dp),
                color = colorState.value,
                onClick = { showColorSelector = true },
            )

//            VerticalDivider()

            TextButton(onClick = { toolState.value = Tool.Pencil }) {
                Text("Line")
            }

            TextButton(onClick = { toolState.value = Tool.Eraser }) {
                Text("Eraser")
            }

            TextButton(onClick = {
                deleteCurrentFrame()
            }) {
                Text("Delete image")
            }

            TextButton(onClick = {
                newFrame()
            }) {
                Text("New image")
            }

            TextButton(enabled = undoItems.isNotEmpty(), onClick = {
                val historyItem = undoItems.removeFromEnd()
                redoItems.add(historyItem)
            }) {
                Text("Undo")
            }

            TextButton(enabled = redoItems.isNotEmpty(), onClick = {
                val historyItem = redoItems.removeFromEnd()
                undoItems.add(historyItem)
            }) {
                Text("Redo")
            }
        }
    }
}

@Composable
fun ColorSelectorBottomSheet(
    colorState: MutableState<Color>,
    closeBottomSheet: () -> Unit,
) {
    ModalBottomSheet(closeBottomSheet) {
        FlowRow {

            ColoredCircleButton(
                modifier = Modifier.border(1.dp, colorState.value.inverted),
                color = colorState.value,
                onClick = {  },
            )

            listOf(
                Color.Black,
                Color.White,
                Color.Blue,
                Color.Red,
                Color.Green,
                Color.Yellow,
                Color.Magenta,
                Color.Cyan,
            ).forEach {
                ColoredCircleButton(
                    color = it,
                    onClick = { colorState.value = it },
                )
            }
        }
    }
}

@Composable
fun ColoredCircleButton(
    modifier: Modifier = Modifier,
    color: Color,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick) {
        Box(Modifier.size(32.dp).clip(CircleShape).background(color).then(modifier))
    }
}

private val Color.inverted: Color
    get() = Color.White // TODO
