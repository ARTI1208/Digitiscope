@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package ru.arti1208.digitiscope

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ComposeShader
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandIn
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.GeneratingTokens
import androidx.compose.material.icons.outlined.Moving
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Rectangle
import androidx.compose.material.icons.outlined.RoundedCorner
import androidx.compose.material.icons.outlined.ShapeLine
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Square
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.FloatState
import androidx.compose.runtime.IntState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableFloatState
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.LinearGradientShader
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.ShaderBrush
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.PointerId
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.util.fastAny
import androidx.compose.ui.util.fastForEach
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupPositionProvider
import androidx.compose.ui.window.PopupProperties
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.random.Random
import kotlin.collections.removeLast as removeFromEnd


sealed interface DrawingShape {

    data class PathShape(
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
    data object Move : Tool
    data object Pencil : Tool
    data object Eraser : Tool

    sealed interface Shape : Tool {
        data object Line : Shape
        data object Rectangle : Shape
        data object Square : Shape
        data object Oval : Shape
        data object Circle : Shape
    }
}

enum class PaintOption {
    FILLED,
}

private fun createNewBitmap(size: IntSize) = ImageBitmap(size.width, size.height)

private fun Random.nextFloat(from: Float, until: Float) =
    nextInt(from.toInt(), until.toInt()) + nextFloat() // TODO approximation

private fun <T> Random.nextItem(list: List<T>) = list[nextInt(list.size)]

private fun <T> List<T>.random() = Random.nextItem(this)

const val MIN_STROKE_WIDTH = 1f
const val MAX_STROKE_WIDTH = 300f

const val MIN_DELAY_MS = 20f
const val MAX_DELAY_MS = 5000f

@Composable
fun CanvasScreen(
    modifier: Modifier = Modifier,
) {

    val colorState = remember { mutableStateOf(Color.Black) }
    val strokeWidthState = remember { mutableFloatStateOf(5f) }
    val toolState = remember { mutableStateOf<Tool>(Tool.Pencil) }

    val frames = remember { mutableStateListOf<ImageBitmap>() }
    val currentFrameIndexState = remember { mutableIntStateOf(-1) }
    val previousFrameState =
        remember { derivedStateOf { frames.getOrNull(currentFrameIndexState.intValue - 1) } }
    val currentFrameState = remember { derivedStateOf { frames[currentFrameIndexState.intValue] } }

    val drawings = remember { mutableStateListOf<SnapshotStateList<DrawingItem>>() }
    val currentDrawingsState =
        remember { derivedStateOf { drawings.getOrNull(currentFrameIndexState.intValue) } }

    val drawScope = remember { CanvasDrawScope() }
    val recordingCanvases = remember { derivedStateOf { frames.map { Canvas(it) } } }
    val currentRecordingCanvasState =
        remember { derivedStateOf { recordingCanvases.value.getOrNull(currentFrameIndexState.intValue) } }

    val redoItems = remember { mutableStateListOf<SnapshotStateList<DrawingItem>>() }
    val currentRedoItemsState =
        remember { derivedStateOf { redoItems.getOrNull(currentFrameIndexState.intValue) } }

    val frameSizeState = remember { mutableStateOf(IntSize(0, 0)) }

    val animationPlayingState = remember { mutableStateOf(false) }
    val animationDelayState = remember { mutableLongStateOf(500) }

    val drawingUpdater = remember { mutableIntStateOf(0) }

    fun addDrawingItem(drawingItem: DrawingItem) {
        redoItems[currentFrameIndexState.intValue].clear()
        currentDrawingsState.value?.add((drawingItem))
    }

    fun insertFrame(
        index: Int,
        frame: ImageBitmap,
        frameDrawings: SnapshotStateList<DrawingItem>,
        frameRedoItems: SnapshotStateList<DrawingItem>,
    ) {
        if (index == frames.size) {
            frames.add(frame)
            drawings.add(frameDrawings)
            redoItems.add(frameRedoItems)
        } else {
            frames.add(index, frame)
            drawings.add(index, frameDrawings)
            redoItems.add(index, frameRedoItems)
        }
    }

    fun newFrame() {
        val newIndex = currentFrameIndexState.intValue + 1
        val newFrame = createNewBitmap(frameSizeState.value)
        insertFrame(
            index = newIndex,
            frame = newFrame,
            frameDrawings = mutableStateListOf(),
            frameRedoItems = mutableStateListOf(),
        )
        currentFrameIndexState.intValue = newIndex
    }

    fun copyFrame() {
        val currentIndex = currentFrameIndexState.intValue
        val newIndex = currentIndex + 1

        val copyFrame = frames[currentIndex].run {
            val androidConfig = when (config) {
                ImageBitmapConfig.Argb8888 -> Bitmap.Config.ARGB_8888
                ImageBitmapConfig.Alpha8 -> Bitmap.Config.ALPHA_8
                ImageBitmapConfig.Rgb565 -> Bitmap.Config.RGB_565
                ImageBitmapConfig.F16 -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Bitmap.Config.RGBA_F16
                } else {
                    Bitmap.Config.RGB_565
                }

                ImageBitmapConfig.Gpu -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Bitmap.Config.HARDWARE
                } else {
                    Bitmap.Config.RGB_565
                }

                else -> Bitmap.Config.ARGB_8888
            }
            asAndroidBitmap().copy(androidConfig, true).asImageBitmap()
        }

        val copyDrawings =
            mutableStateListOf<DrawingItem>().apply { addAll(drawings[currentIndex]) }
        val copyRedoItems =
            mutableStateListOf<DrawingItem>().apply { addAll(redoItems[currentIndex]) }
        insertFrame(
            index = newIndex,
            frame = copyFrame,
            frameDrawings = copyDrawings,
            frameRedoItems = copyRedoItems,
        )
        currentFrameIndexState.intValue = newIndex
    }

    fun generateFrames(count: Int) {
        val tools = listOf(
            Tool.Pencil,
            Tool.Shape.Line,
            Tool.Shape.Oval,
            Tool.Shape.Circle,
            Tool.Shape.Rectangle,
            Tool.Shape.Square,
        )
        repeat(count) {
            newFrame()
            val figureCount = Random.nextInt(3, 11)
            repeat(figureCount) {
                val tool = tools.random()
                val color = Color(Random.nextInt(256), Random.nextInt(256), Random.nextInt(256))
                val width = Random.nextInt(MIN_STROKE_WIDTH.toInt(), MAX_STROKE_WIDTH.toInt()) +
                        Random.nextFloat()
                val drawingItem = createDrawingItem(tool, color, width)
                addDrawingItem(drawingItem)

                when (val shape = drawingItem.shape) {
                    is DrawingShape.PathShape -> {

                        fun randomOffsetOnFrame() = Offset(
                            Random.nextInt(0, frameSizeState.value.width).toFloat(),
                            Random.nextInt(0, frameSizeState.value.height).toFloat(),
                        )

                        val originalOffset = randomOffsetOnFrame()

                        when (tool) {
                            Tool.Pencil -> {
                                var previousOffset: Offset
                                var lastOffset = originalOffset
                                repeat(Random.nextInt(1, 6)) {
                                    previousOffset = lastOffset
                                    lastOffset = randomOffsetOnFrame()

                                    shape.path.applyDrawing(
                                        tool = tool,
                                        originalOffset = originalOffset,
                                        previousOffset = previousOffset,
                                        lastOffset = lastOffset,
                                    )
                                }
                            }

                            else -> {
                                val lastOffset = randomOffsetOnFrame()
                                shape.path.applyDrawing(
                                    tool = tool,
                                    originalOffset = originalOffset,
                                    previousOffset = originalOffset,
                                    lastOffset = lastOffset,
                                )
                            }
                        }
                    }
                }
                drawingUpdater.intValue++
            }
        }
    }

    fun deleteCurrentFrame() {
        val index = currentFrameIndexState.intValue
        frames.removeAt(index)
        drawings.removeAt(index)
        redoItems.removeAt(index)

        if (frames.isEmpty()) {
            currentFrameIndexState.intValue = -1
            newFrame()
        } else {
            currentFrameIndexState.intValue = (index - 1).coerceAtLeast(0)
        }
    }

    fun deleteAllFrames() {
        frames.clear()
        drawings.clear()
        redoItems.clear()

        currentFrameIndexState.intValue = -1

        newFrame()
    }

    Scaffold(modifier = modifier.fillMaxSize()) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
        ) {

            val coroutineScope = rememberCoroutineScope()
            val jobState = remember { mutableStateOf(null as Job?) }

            fun playOrPause() {
                if (jobState.value?.isActive == true) {
                    jobState.value?.cancel()
                    animationPlayingState.value = false
                    currentFrameIndexState.intValue = frames.size - 1
                } else {
                    jobState.value = coroutineScope.launch {
                        animationPlayingState.value = true
                        currentFrameIndexState.intValue = 0
                        while (true) {
                            delay(animationDelayState.longValue)
                            currentFrameIndexState.intValue =
                                (currentFrameIndexState.intValue + 1) % frames.size
                        }
                    }
                }
            }

            val context = LocalContext.current

            ControlsRow(
                modifier = Modifier.fillMaxWidth(),
                undoItemsState = currentDrawingsState,
                redoItemsState = currentRedoItemsState,
                animationDelayState = animationDelayState,
                isPlaying = animationPlayingState,
                playOrPause = ::playOrPause,
                newFrame = ::newFrame,
                copyFrame = ::copyFrame,
                generateFrames = ::generateFrames,
                deleteCurrentFrame = ::deleteCurrentFrame,
                deleteAllFrames = ::deleteAllFrames,
                export = { config ->
                    exportGif(
                        context = context,
                        coroutineScope = coroutineScope,
                        frames = frames.toList(),
                        delay = animationDelayState.longValue.toInt(),
                        config = config,
                    )
                },
                save = { config, path ->
                    saveGif(
                        context = context,
                        coroutineScope = coroutineScope,
                        frames = frames.toList(),
                        delay = animationDelayState.longValue.toInt(),
                        config = config,
                        filePath = path,
                    )
                }
            )

            AnimatedVisibility(animationPlayingState.value) {

                Column {

                    Slider(
                        value = 1000f / animationDelayState.longValue,
                        onValueChange = { animationDelayState.longValue = (1000f / it).toLong() },
                        valueRange = (1000f / MAX_DELAY_MS)..(1000f / MIN_DELAY_MS)
                    )

                    Text("Animation speed", fontSize = 10.sp)
                }

            }

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
            ) {

                Box(
                    modifier = Modifier
                        .run {
                            if (animationPlayingState.value) {
                                val s = frameSizeState.value.toSize() / LocalDensity.current.density
                                size(s.width.dp, s.height.dp)
                            } else {
                                fillMaxWidth()
                                    .weight(1f)
                                    .onSizeChanged { newSize ->
                                        frameSizeState.value = newSize
                                        if (newSize.height == 0 || newSize.width == 0) return@onSizeChanged
                                        if (frames.isEmpty()) {
                                            newFrame()
                                        } else {
                                            val oldFrames = frames.toList()
                                            frames.clear()

                                            frames.addAll(oldFrames.map { oldFrame ->
                                                Bitmap.createBitmap(
                                                    oldFrame.asAndroidBitmap(),
                                                    0, 0,
                                                    newSize.width, newSize.height,
                                                ).asImageBitmap()
                                            })
                                        }
                                    }
                            }
                        }
                ) {
                    if (currentFrameIndexState.intValue >= 0) {
//                        DrawingCanvas(
//                            modifier = Modifier.matchParentSize(),
//                            colorState = colorState,
//                            toolState = toolState,
//                            strokeWidthState = strokeWidthState,
//                            bitmapState = currentFrameState,
//                            drawingState = currentDrawingsState,
//                            isAnimationPlaying = animationPlayingState,
//                            addDrawingItem = ::addDrawingItem,
//                        )

                        val zoomState = remember { mutableFloatStateOf(1f) }
                        val moveState = remember { mutableStateOf(Offset.Zero) }

                        DrawingCanvas2(
                            modifier = Modifier.matchParentSize(),
                            colorState = colorState,
                            toolState = toolState,
                            strokeWidthState = strokeWidthState,
                            previousBitmapState = previousFrameState,
                            bitmapState = currentFrameState,
                            zoomState = zoomState,
                            moveState = moveState,
                            drawingUpdater = drawingUpdater,
                            isAnimationPlaying = animationPlayingState,
                            addDrawingItem = ::addDrawingItem,
                        ) {
                            val bitmap = currentFrameState.value
                            drawScope.draw(
                                Density(1f),
                                LayoutDirection.Ltr,
                                currentRecordingCanvasState.value!!,
                                Size(bitmap.width.toFloat(), bitmap.height.toFloat()),
                            ) {
                                bitmap.asAndroidBitmap().eraseColor(Color.Transparent.value.toInt())

                                withTransform({
                                    val zoom = zoomState.value
//                                    translate(-offset.x * zoom, -offset.y * zoom)
                                    scale(zoom, zoom)
//                                    rotate(angle, angleCenter)
                                }) {

                                    currentDrawingsState.value?.forEach {
                                        when (it.shape) {
                                            is DrawingShape.PathShape -> drawPath(
                                                it.shape.path,
                                                color = Color(it.color),
                                                style = Stroke(
                                                    width = it.shape.strokeWidth,
                                                    cap = StrokeCap.Round,
                                                ),
                                                blendMode = it.blendMode,
                                            )
                                        }
                                    }
                                }
                            }
                            drawingUpdater.intValue++
                        }
                    }
                }

                AnimatedVisibility(
                    !animationPlayingState.value,
                    enter = slideInVertically(initialOffsetY = { it }) + expandVertically() + fadeIn(),
                    exit = slideOutVertically(targetOffsetY = { it }) + shrinkVertically() + fadeOut(),
                ) {
                    Column {
                        PreviewRow(
                            modifier = Modifier.padding(vertical = 4.dp),
                            images = frames,
                            selectedIndexState = currentFrameIndexState,
                            newFrame = ::newFrame,
                        )

                        Slider(
                            value = strokeWidthState.floatValue,
                            onValueChange = { strokeWidthState.floatValue = it },
                            valueRange = MIN_STROKE_WIDTH..MAX_STROKE_WIDTH,
                        )

                        Text(
                            modifier = Modifier.padding(horizontal = 8.dp),
                            text = "Drawing width",
                            fontSize = 10.sp,
                        )

                        ToolsRow(
                            modifier = Modifier.fillMaxWidth(),
                            colorState = colorState,
                            toolState = toolState,
                        )
                    }
                }
            }
        }
    }
}

private fun saveGif(
    context: Context,
    coroutineScope: CoroutineScope,
    frames: List<ImageBitmap>,
    delay: Int,
    config: GifConfig,
    filePath: String,
) {
    coroutineScope.launch(Dispatchers.IO) {
        context.contentResolver.openOutputStream(Uri.parse(filePath))?.use { stream ->
            stream.writeGif(frames, delay, config)
        }
    }
}

private fun exportGif(
    context: Context,
    coroutineScope: CoroutineScope,
    frames: List<ImageBitmap>,
    delay: Int,
    config: GifConfig,
) {
    coroutineScope.launch(Dispatchers.IO) {
        val filesDir = context.applicationContext.filesDir
        val exportDir = File(filesDir, "export").also { it.mkdirs() }
        val imageFile = File(exportDir, "Digitiscope.gif")

        FileOutputStream(imageFile).use { stream ->
            stream.writeGif(frames, delay, config)
        }

        val authority = BuildConfig.APPLICATION_ID + ".ExportProvider"
        val imageUri = FileProvider.getUriForFile(context, authority, imageFile)

        val exportIntent = Intent(Intent.ACTION_SEND).apply {
            setType("image/gif")
            setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            putExtra(Intent.EXTRA_STREAM, imageUri)
        }
        val chooser = Intent.createChooser(exportIntent, "Share GIF")

        withContext(Dispatchers.Main) {
            context.startActivity(chooser)
        }
    }
}

private fun createDrawingItem(
    tool: Tool,
    color: Color,
    width: Float,
): DrawingItem {
    val mode = if (tool is Tool.Eraser) BlendMode.Clear else BlendMode.SrcOver

    val path = Path()

    return DrawingItem(
        DrawingShape.PathShape(
            path,
            width,
        ),
        color.toArgb().toLong(),
        mode,
    )
}

@Composable
private fun DrawingCanvas(
    modifier: Modifier = Modifier,
    colorState: State<Color>,
    toolState: State<Tool>,
    drawingState: State<SnapshotStateList<DrawingItem>?>,
    strokeWidthState: FloatState,
    bitmapState: State<ImageBitmap>,
    isAnimationPlaying: State<Boolean>,
    addDrawingItem: (DrawingItem) -> Unit,
) {

    val bitmap = bitmapState.value

    val scope = remember(bitmap) { CanvasDrawScope() }
    val canvas = remember(bitmap) { Canvas(bitmap) }


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
    var angleCenter by remember {
        mutableStateOf(
            Offset(
                bitmap.width.toFloat() / 2,
                bitmap.height.toFloat() / 2
            )
        )
    }
    var angle by remember { mutableFloatStateOf(0f) }
    var zoom by remember { mutableFloatStateOf(1f) }

    val fingersDown = remember { BooleanArray(10) }

    var lastUpdate by remember { mutableStateOf(0) }

    var fingersDownCount = remember { mutableIntStateOf(0) }

    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .drawBehind { drawImage(back) }
            .run {
                if (isAnimationPlaying.value) {
                    this
                } else {
                    pointerInput(Unit) {

                        coroutineScope {
                            awaitEachGesture {
                                if (toolState.value == Tool.Move) return@awaitEachGesture

                                val paths = mutableMapOf<PointerId, Path>()
                                val firstDownChange = awaitFirstDown(requireUnconsumed = false)

                                fun getPath(id: PointerId) = paths.getOrPut(id) {
                                    val drawingItem = createDrawingItem(
                                        tool = toolState.value,
                                        color = colorState.value,
                                        width = strokeWidthState.floatValue,
                                    )

                                    addDrawingItem(drawingItem)

                                    when (val shape = drawingItem.shape) {
                                        is DrawingShape.PathShape -> shape.path
                                    }
                                }

                                getPath(firstDownChange.id).moveTo(
                                    firstDownChange.position.x,
                                    firstDownChange.position.y
                                )

                                println("Starttt at ${firstDownChange.position}")
                                do {
                                    val event = awaitPointerEvent()
                                    val canceled = event.changes.fastAny { it.isConsumed }
                                    if (!canceled) {
                                        event.changes.fastForEach { change ->

                                            if (change.pressed != change.previousPressed) {
                                                fingersDownCount.intValue += if (change.pressed) 1 else -1

                                                if (fingersDownCount.intValue >= 2) {
                                                    return@fastForEach
                                                }
                                            }


                                            if (change.pressed) {

                                                val path = getPath(change.id)

                                                if (change.previousPressed) {
                                                    path.applyDrawing(
                                                        toolState.value,
                                                        firstDownChange.position,
                                                        change.previousPosition,
                                                        change.position,
                                                    )
                                                } else {
                                                    path.moveTo(
                                                        change.position.x,
                                                        change.position.y
                                                    )
                                                }
                                                lastUpdate++
                                            }
                                        }
                                    }
                                } while (!canceled && event.changes.fastAny { it.pressed } && fingersDownCount.intValue < 2)

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

//                        coroutineScope {
//                            awaitEachGesture {
//                                awaitFirstDown(requireUnconsumed = false)
//                                do {
//                                    val event = awaitPointerEvent()
//                                    val canceled = event.changes.fastAny { it.isConsumed }
//                                    if (!canceled) {
//                                        event.changes.forEach {
//                                            if (it.pressed != it.previousPressed) {
//                                                fingersDownCount.intValue += if (it.pressed) 1 else -1
//                                            }
//                                        }
//                                    }
//                                } while (!canceled && event.changes.fastAny { it.pressed })
//                            }
//                        }

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
                    }
                }
            }
    ) {
        scope.draw(
            Density(1f),
            LayoutDirection.Ltr,
            canvas,
            size,
        ) {
            bitmap.asAndroidBitmap().eraseColor(Color.Transparent.value.toInt())

            lastUpdate
            withTransform({
                translate(-offset.x * zoom, -offset.y * zoom)
                scale(zoom, zoom)
                rotate(angle, angleCenter)
            }) {
                drawingState.value?.forEach {
                    when (it.shape) {
                        is DrawingShape.PathShape -> drawPath(
                            it.shape.path,
                            color = Color(it.color),
                            style = Stroke(width = it.shape.strokeWidth, cap = StrokeCap.Round),
                            blendMode = it.blendMode
                        )
                    }
                }
            }
        }


        drawImage(bitmap)
    }
}

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
                        val length = max(
                            x - originalOffset.x,
                            y - originalOffset.y,
                        )
                        Size(length, length)
                    })
                )
            }
        }
    }
}

@Composable
private fun DrawingCanvas2(
    modifier: Modifier = Modifier,
    colorState: State<Color>,
    toolState: State<Tool>,
    drawingUpdater: IntState,
    strokeWidthState: FloatState,
    previousBitmapState: State<ImageBitmap?>,
    bitmapState: State<ImageBitmap>,
    zoomState: MutableFloatState,
    moveState: MutableState<Offset>,
    isAnimationPlaying: State<Boolean>,
    addDrawingItem: (DrawingItem) -> Unit,
    onDraw: () -> Unit,
) {
    val previousBitmap = previousBitmapState.value
    val bitmap = bitmapState.value
    val backgroundImage = ImageBitmap.imageResource(R.drawable.background)

    fun Offset.rotateBy(angle: Float): Offset {
        val angleInRadians = angle * PI / 180
        return Offset(
            (x * cos(angleInRadians) - y * sin(angleInRadians)).toFloat(),
            (x * sin(angleInRadians) + y * cos(angleInRadians)).toFloat(),
        )
    }

    var offset by moveState
    var angleCenter by remember {
        mutableStateOf(
            Offset(
                bitmap.width.toFloat() / 2,
                bitmap.height.toFloat() / 2
            )
        )
    }
    var angle by remember { mutableFloatStateOf(0f) }
//    var zoom by remember { mutableFloatStateOf(1f) }

    val fingersDown = remember { BooleanArray(10) }

    var lastUpdate by remember { mutableStateOf(0) }

    var fingersDownCount = remember { mutableIntStateOf(0) }

    val textMeasurer = rememberTextMeasurer()

    Canvas(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .drawBehind { drawImage(backgroundImage) }
            .run {
                if (isAnimationPlaying.value) {
                    this
                } else {
                    pointerInput(toolState.value) {

                        if (toolState.value != Tool.Move) {
//                            coroutineScope {
                                awaitEachGesture {

                                    val paths = mutableMapOf<PointerId, Path>()
                                    val firstDownChange = awaitFirstDown(requireUnconsumed = false)

                                    fun getPath(id: PointerId) = paths.getOrPut(id) {
                                        val drawingItem = createDrawingItem(
                                            tool = toolState.value,
                                            color = colorState.value,
                                            width = strokeWidthState.floatValue,
                                        )

                                        addDrawingItem(drawingItem)

                                        when (val shape = drawingItem.shape) {
                                            is DrawingShape.PathShape -> shape.path
                                        }
                                    }

                                    getPath(firstDownChange.id).moveTo(
                                        firstDownChange.position.x,
                                        firstDownChange.position.y
                                    )

                                    do {
                                        val event = awaitPointerEvent()
                                        val canceled = event.changes.fastAny { it.isConsumed }
                                        if (!canceled) {
                                            event.changes.fastForEach { change ->

                                                if (change.pressed != change.previousPressed) {
                                                    fingersDownCount.intValue += if (change.pressed) 1 else -1

                                                    if (fingersDownCount.intValue >= 2) {
                                                        return@fastForEach
                                                    }
                                                }


                                                if (change.pressed) {

                                                    val path = getPath(change.id)

                                                    if (change.previousPressed) {
                                                        path.applyDrawing(
                                                            toolState.value,
                                                            firstDownChange.position,
                                                            change.previousPosition,
                                                            change.position,
                                                        )

                                                        onDraw()
                                                    } else {
                                                        path.moveTo(
                                                            change.position.x,
                                                            change.position.y
                                                        )
                                                    }
                                                    lastUpdate++
                                                }
                                            }
                                        }
                                    } while (!canceled && event.changes.fastAny { it.pressed } && fingersDownCount.intValue < 2)

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
//                        }

//                        coroutineScope {
//                            awaitEachGesture {
//                                awaitFirstDown(requireUnconsumed = false)
//                                do {
//                                    val event = awaitPointerEvent()
//                                    val canceled = event.changes.fastAny { it.isConsumed }
//                                    if (!canceled) {
//                                        event.changes.forEach {
//                                            if (it.pressed != it.previousPressed) {
//                                                fingersDownCount.intValue += if (it.pressed) 1 else -1
//                                            }
//                                        }
//                                    }
//                                } while (!canceled && event.changes.fastAny { it.pressed })
//                            }
//                        }

                        if (toolState.value == Tool.Move) {
//                            return@pointerInput
                            println("FAFFAFA: move")
//                            coroutineScope {
                                println("FAFFAFA: scope in")
                                detectTransformGestures { centroid, pan, gestZoom, rotation ->
                                    println("FAFFAFA: detected")
                                    if (toolState.value != Tool.Move) return@detectTransformGestures
                                    val oldScale = zoomState.floatValue
                                    val newScale = (oldScale * gestZoom).coerceAtLeast(1f)
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
                        offset = (offset + centroid / oldScale).rotateBy(rotation) -
                                (centroid / newScale + pan / oldScale)
//                    }
                                    zoomState.floatValue = newScale
//                        angle += rotation
                                    println("FAFFAFA: $angleCenter")
                                }
//                            }
                        }
                    }
                }
            }
    ) {
        withTransform({
            scale(zoomState.floatValue, zoomState.floatValue)
        }) {
            previousBitmap?.also { drawImage(it, alpha = 0.2f) }
            drawImage(bitmap)
        }
        drawText(textMeasurer, drawingUpdater.intValue.toString())
    }
}

@Composable
fun PreviewRow(
    modifier: Modifier = Modifier,
    images: SnapshotStateList<ImageBitmap>,
    selectedIndexState: MutableIntState,
    newFrame: () -> Unit,
) {
    val state = rememberLazyListState()
    val scope = rememberCoroutineScope()
    LaunchedEffect(selectedIndexState.intValue) {
        scope.launch {
            state.animateScrollToItem(selectedIndexState.intValue)
        }
    }
    val background = ImageBitmap.imageResource(R.drawable.background)
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        state = state,
    ) {
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
                modifier = Modifier
                    .size(widthDp, heightDp)
                    .clip(RoundedCornerShape(8.dp))
                    .drawBehind { drawImage(background) }
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
fun ControlsRow(
    modifier: Modifier,
    isPlaying: State<Boolean>,
    animationDelayState: MutableLongState,
    playOrPause: () -> Unit,
    undoItemsState: State<SnapshotStateList<DrawingItem>?>,
    redoItemsState: State<SnapshotStateList<DrawingItem>?>,
    newFrame: () -> Unit,
    copyFrame: () -> Unit,
    generateFrames: (Int) -> Unit,
    deleteCurrentFrame: () -> Unit,
    deleteAllFrames: () -> Unit,
    export: (config: GifConfig) -> Unit,
    save: (config: GifConfig, path: String) -> Unit,
) {
    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
    ) {
        item {

            AnimatedVisibility(
                !isPlaying.value,
                enter = expandHorizontally() + fadeIn(),
                exit = shrinkHorizontally() + fadeOut(),
            ) {

                Row(horizontalArrangement = Arrangement.Center) {
                    val undoItems = undoItemsState.value
                    val redoItems = redoItemsState.value

                    IconButton(enabled = undoItems.isNullOrEmpty().not(), onClick = {
                        val historyItem = undoItems?.removeFromEnd() ?: return@IconButton
                        redoItems?.add(historyItem)
                    }) {
                        Icon(Icons.AutoMirrored.Default.Undo, contentDescription = "Undo")
                    }

                    IconButton(enabled = redoItems.isNullOrEmpty().not(), onClick = {
                        val historyItem = redoItems?.removeFromEnd() ?: return@IconButton
                        undoItems?.add(historyItem)
                    }) {
                        Icon(Icons.AutoMirrored.Default.Redo, contentDescription = "Redo")
                    }

                    IconButton(onClick = {
                        newFrame()
                    }) {
                        Icon(painterResource(R.drawable.create_frame), contentDescription = "Add")
                    }

                    var isGeneratePopupShown by remember { mutableStateOf(false) }


                    val generateIconOffset = remember { mutableStateOf(Offset.Zero) }
                    val generateIconSize = remember { mutableStateOf(IntSize.Zero) }

                    IconButton(
                        modifier = Modifier.onPlaced {
                            generateIconOffset.value = it.positionInParent()
                            generateIconSize.value = it.size
                        },
                        onClick = {
                            isGeneratePopupShown = true
                        }
                    ) {
                        Icon(Icons.Outlined.GeneratingTokens, contentDescription = "Generate")
                    }


                    val width = 200.dp
                    val widthPx = with(LocalDensity.current) { width.toPx() }
                    if (isGeneratePopupShown) {
                        Popup(
                            popupPositionProvider = object : PopupPositionProvider {
                                override fun calculatePosition(
                                    anchorBounds: IntRect,
                                    windowSize: IntSize,
                                    layoutDirection: LayoutDirection,
                                    popupContentSize: IntSize
                                ): IntOffset {
                                    val popupX = windowSize.width / 2 - widthPx / 2
                                    return IntOffset(popupX.toInt(), anchorBounds.bottom)
                                }
                            },
                            onDismissRequest = { isGeneratePopupShown = false },
                            properties = PopupProperties(focusable = true),
                        ) {

                            // FIXME visibility doesn't work for parent popup because of the offset
                            // but without 'if' on popup it consumes click events after closing
                            AnimatedVisibility(
                                isGeneratePopupShown,
                                enter = expandIn() + slideInVertically(),
                                exit = shrinkOut() + slideOutVertically(),
                            ) {
                                Surface(shape = RoundedCornerShape(8.dp)) {
                                    GenerateFrames(
                                        modifier = Modifier.width(width),
                                        generateFrames = {
                                            generateFrames(it)
                                            isGeneratePopupShown = false
                                        },
                                    )
                                }
                            }
                        }
                    }

                    IconButton(onClick = {
                        copyFrame()
                    }) {
                        Icon(Icons.Outlined.ContentCopy, contentDescription = "Duplicate")
                    }

                    IconButton(onClick = {
                        deleteCurrentFrame()
                    }) {
                        Icon(painterResource(R.drawable.delete_frame), contentDescription = "Delete")
                    }

                    IconButton(onClick = {
                        deleteAllFrames()
                    }) {
                        Icon(painterResource(R.drawable.delete_all), contentDescription = "Delete all")
                    }
                }
            }

            IconButton(onClick = {
                playOrPause()
            }) {
                Icon(
                    if (isPlaying.value) Icons.Outlined.Pause else ImageVector.vectorResource(R.drawable.animation_play),
                    contentDescription = if (isPlaying.value) "Pause" else "Play",
                )
            }

            val isExportDialogShowing = remember { mutableStateOf(false) }

            ExportDialog(
                isShowingState = isExportDialogShowing,
                animationDelayState = animationDelayState,
                export = export,
                save = save,
            )

            IconButton(onClick = {
                isExportDialogShowing.value = true
            }) {
                Icon(
                    Icons.Outlined.Share,
                    contentDescription = "Export",
                )
            }
        }
    }
}

data class GifConfig(
    val background: Background,
    val isCyclic: Boolean,
)

@Composable
fun ExportDialog(
    isShowingState: MutableState<Boolean>,
    animationDelayState: MutableLongState,
    export: (config: GifConfig) -> Unit,
    save: (config: GifConfig, path: String) -> Unit,
) {
    AnimatedVisibility(isShowingState.value) {
        BasicAlertDialog(
            onDismissRequest = { isShowingState.value = false },
        ) {
            Surface(shape = RoundedCornerShape(8.dp)) {

                Column(
                    modifier = Modifier.padding(vertical = 4.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {

                    val editorBackground = ImageBitmap.imageResource(R.drawable.background)

                    var isBackgroundFromEditor by remember { mutableStateOf(true) }
                    val backgroundColorState = remember { mutableStateOf(Color.White) }
                    val colorPickerShowingState = remember { mutableStateOf(false) }

                    ColorPickerBottomSheet(colorPickerShowingState, backgroundColorState)

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {

                        IconButton(onClick = {
                            isBackgroundFromEditor = true
                        }) {
                            Box(
                                Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .run {
                                        if (isBackgroundFromEditor) {
                                            border(1.dp, MaterialTheme.colorScheme.onSurface, CircleShape)
                                        } else {
                                            this
                                        }
                                    }
                                    .drawBehind {
                                        drawImage(editorBackground)
                                    }
                            )
                        }



                        ColoredCircleButton(
                            buttonSize = 64.dp,
                            color = backgroundColorState.value,
                            borderColor = if (!isBackgroundFromEditor) MaterialTheme.colorScheme.onSurface else Color.Transparent,
                            onClick = {
                                isBackgroundFromEditor = false
                               colorPickerShowingState.value = true
                            },
                        )

                    }

                    Text(if (isBackgroundFromEditor) "Use editor background" else "Color background")

                    Slider(
                        value = 1000f / animationDelayState.longValue,
                        onValueChange = { animationDelayState.longValue = (1000f / it).toLong() },
                        valueRange = (1000f / MAX_DELAY_MS)..(1000f / MIN_DELAY_MS)
                    )

                    Text("Animation speed", fontSize = 10.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp, alignment = Alignment.End),
                    ) {
                        val saveFile = rememberLauncherForActivityResult(
                            ActivityResultContracts.CreateDocument("image/gif")
                        ) {
                            val uriPath = it?.toString() ?: return@rememberLauncherForActivityResult
                            isShowingState.value = false

                            val background = when (isBackgroundFromEditor) {
                                true -> Background.BitmapBackground(editorBackground)
                                false -> Background.ColorBackground(backgroundColorState.value)
                            }

                            save(GifConfig(background, isCyclic = true), uriPath)
                        }

                        TextButton(onClick = {
                            saveFile.launch("Digitiscope.gif")
                        }) {
                            Text("Save to file")
                        }

                        TextButton(onClick = {
                            isShowingState.value = false

                            val background = when (isBackgroundFromEditor) {
                                true -> Background.BitmapBackground(editorBackground)
                                false -> Background.ColorBackground(backgroundColorState.value)
                            }

                            export(GifConfig(background, isCyclic = true))
                        }) {
                            Text("Share")
                        }
                    }
                }
            }
        }
    }
}

sealed interface Background {
    data class BitmapBackground(val bitmap: ImageBitmap) : Background

    data class ColorBackground(val color: Color): Background
}

fun OutputStream.writeGif(
    frames: List<ImageBitmap>,
    delay: Int,
    config: GifConfig,
) {
    val encoder = AnimatedGifEncoder().apply {
        setRepeat(if (config.isCyclic) 0 else 1)
        setDelay(delay)
    }
    encoder.start(this)

    frames.forEach { frame ->
        val frameWithBackground = ImageBitmap(frame.width, frame.height)
        val drawScope = CanvasDrawScope()
        val canvas = Canvas(frameWithBackground)
        drawScope.draw(
            Density(1f),
            LayoutDirection.Ltr,
            canvas,
            Size(frameWithBackground.width.toFloat(), frameWithBackground.height.toFloat()),
        ) {
            when (val background = config.background) {
                is Background.BitmapBackground -> drawImage(background.bitmap)
                is Background.ColorBackground -> drawRect(background.color)
            }
            drawImage(frame)
        }

        encoder.addFrame(frameWithBackground.asAndroidBitmap())
    }

    encoder.finish()
}

@Composable
fun GenerateFrames(
    modifier: Modifier,
    generateFrames: (Int) -> Unit,
) {
    Column(modifier, horizontalAlignment = Alignment.End) {
        var frameCountString by remember { mutableStateOf("") }
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = frameCountString,
            onValueChange = { frameCountString = it },
            singleLine = true,
            readOnly = false,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )

        TextButton(
            enabled = frameCountString.toIntOrNull()?.takeIf { it > 0 } != null,
            onClick = {
                val frameCount = frameCountString.toInt()
                generateFrames(frameCount)
            },
        ) {
            Text("Generate")
        }
    }
}

@Composable
fun ToolsRow(
    modifier: Modifier,
    colorState: MutableState<Color>,
    toolState: MutableState<Tool>,
) {

    val showColorPickerState = remember { mutableStateOf(false) }
    ColorPickerBottomSheet(showColorPickerState, colorState)

    LazyRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {

        item {
            ColoredCircleButton(
                color = colorState.value,
                borderColor = Color.Transparent,
                onClick = { showColorPickerState.value = true },
            )

            listOf(
                ToolButtonData(Tool.Move, ImageVector.vectorResource(R.drawable.move), "Move"),
                ToolButtonData(Tool.Pencil, Icons.Outlined.Edit, "Pencil"),
                ToolButtonData(Tool.Eraser, ImageVector.vectorResource(R.drawable.eraser), "Eraser"),
                ToolButtonData(Tool.Shape.Line, ImageVector.vectorResource(R.drawable.line), "Line"),
                ToolButtonData(Tool.Shape.Rectangle, Icons.Outlined.Rectangle, "Rectangle"),
                ToolButtonData(Tool.Shape.Square, Icons.Outlined.Square, "Square"),
                ToolButtonData(Tool.Shape.Oval, ImageVector.vectorResource(R.drawable.oval), "Oval"),
                ToolButtonData(Tool.Shape.Circle, Icons.Outlined.Circle, "Circle"),
            ).forEach { (tool, icon, description) ->
                IconButton(modifier = Modifier.run {
                    if (toolState.value == tool) {
                        background(Color.Gray.copy(alpha = 0.5f), CircleShape)
                    } else {
                        this
                    }
                }, onClick = { toolState.value = tool }) {
                    Icon(icon, contentDescription = description)
                }
            }
        }
    }
}

data class ToolButtonData(
    val tool: Tool,
    val image: ImageVector,
    val description: String,
)

@Composable
fun ColorPickerBottomSheet(
    showColorPickerState: MutableState<Boolean>,
    colorState: MutableState<Color>,
) {
    if (!showColorPickerState.value) return
    val state = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(sheetState = state, onDismissRequest = { showColorPickerState.value = false }) {
        ColorPicker(Modifier, colorState)
    }
}

@Composable
fun ColorPicker(
    modifier: Modifier,
    colorState: MutableState<Color>,
) {
    Column(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        FlowRow {
            ColoredCircleButton(
                color = colorState.value,
                borderColor = Color.Transparent,
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

            drawCircle(Color.White, radius = 20f, center = satValOffset.value, style = Stroke(3f))
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
            drawCircle(Color.White, center = Offset(selectedColorIndex.value.toFloat(), size.height / 2), style = Stroke(3f))
        }

        Slider(
            value = colorState.value.alpha,
            onValueChange = { colorState.value = colorState.value.copy(alpha = it) },
        )

        Text("Alpha", fontSize = 10.sp)
    }
}

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

private val Color.inverted: Color
    get() = Color(1f - red, 1f - green, 1f - blue)
