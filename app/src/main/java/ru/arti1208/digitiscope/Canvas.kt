@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)

package ru.arti1208.digitiscope

import android.graphics.Bitmap
import android.os.Build
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
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.outlined.Circle
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.GeneratingTokens
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.Rectangle
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.Square
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.ImageBitmapConfig
import androidx.compose.ui.graphics.Path
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
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
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.arti1208.digitiscope.model.DrawingItem
import ru.arti1208.digitiscope.model.DrawingShape
import ru.arti1208.digitiscope.model.Tool
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlin.collections.removeLast as removeFromEnd

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

    val zoomState = remember { mutableFloatStateOf(1f) }
    val moveState = remember { mutableStateOf(Offset.Zero) }
    val rotationState = remember { mutableFloatStateOf(0f) }
    val pivotState = remember { mutableStateOf(Offset.Zero) }

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
        val newFrame = ImageBitmap(frameSizeState.value)
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

    fun draw() {
        val bitmap = currentFrameState.value
        drawScope.draw(
            Density(1f),
            LayoutDirection.Ltr,
            currentRecordingCanvasState.value!!,
            Size(bitmap.width.toFloat(), bitmap.height.toFloat()),
        ) {
            bitmap.asAndroidBitmap().eraseColor(Color.Transparent.value.toInt())
//
//            withTransform({
//                val zoom = zoomState.floatValue
//                val translationX = -moveState.value.x * zoom
//                val translationY = -moveState.value.y * zoom
//                translate(translationX, translationY)
//                scale(zoom, zoom)
//                rotate(rotationState.floatValue, pivotState.value)
//            }) {

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
//            }
        }

        drawingUpdater.intValue++
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
            }
            draw()
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
                },
                redraw = {
                    draw()
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
                                                Bitmap
                                                    .createBitmap(
                                                        oldFrame.asAndroidBitmap(),
                                                        0, 0,
                                                        newSize.width, newSize.height,
                                                    )
                                                    .asImageBitmap()
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

                        DrawingCanvas2(
                            modifier = Modifier.matchParentSize(),
                            colorState = colorState,
                            toolState = toolState,
                            strokeWidthState = strokeWidthState,
                            previousBitmapState = previousFrameState,
                            bitmapState = currentFrameState,
                            zoomState = zoomState,
                            moveState = moveState,
                            rotationState = rotationState,
                            pivotState = pivotState,
                            drawingUpdater = drawingUpdater,
                            isAnimationPlaying = animationPlayingState,
                            addDrawingItem = ::addDrawingItem,
                        ) {
                            draw()
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
                        ) {
                            zoomState.floatValue = 1f
                            moveState.value = Offset.Zero
                            rotationState.floatValue = 0f
                            pivotState.value = Offset.Zero
                        }
                    }
                }
            }
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
    rotationState: MutableFloatState,
    pivotState: MutableState<Offset>,
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
    val angleCenter by pivotState
    val angle by rotationState
    var zoom by zoomState

    val textMeasurer = rememberTextMeasurer()

    fun Offset.withTransforms(): Offset {
        val translatedX = x + (bitmap.width * (zoom - 1) / 2) + offset.x / 2
        val translatedY = y + (bitmap.height * (zoom - 1) / 2) + offset.y / 2
        val zoomedX = translatedX / zoom
        val zoomedY = translatedY / zoom
        val angleSin = sin(angle)
        val angleCos = cos(angle)
        val depivotedX = zoomedX - angleCenter.x
        val depivotedY = zoomedY - angleCenter.y
        val rotatedX = depivotedX * angleCos - depivotedY * angleSin
        val rotatedY = depivotedX * angleSin + depivotedY * angleCos
        val pivotedX = rotatedX + angleCenter.x
        val pivotedY = rotatedY + angleCenter.y

        return Offset(pivotedX, pivotedY)
    }

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

                                val fixed = firstDownChange.position.withTransforms()
                                getPath(firstDownChange.id).moveTo(
                                    fixed.x,
                                    fixed.y
                                )

                                do {
                                    val event = awaitPointerEvent()
                                    val canceled = event.changes.fastAny { it.isConsumed }
                                    if (!canceled) {
                                        event.changes.fastForEach { change ->
                                            if (change.pressed) {

                                                val path = getPath(change.id)

                                                if (change.previousPressed) {
                                                    path.applyDrawing(
                                                        toolState.value,
                                                        firstDownChange.position.withTransforms(),
                                                        change.previousPosition.withTransforms(),
                                                        change.position.withTransforms(),
                                                    )

                                                    val z = zoom
                                                    val o = offset

                                                    onDraw()
                                                } else {
                                                    val p = change.position.withTransforms()
                                                    path.moveTo(
                                                        p.x,
                                                        p.y,
                                                    )
                                                }
                                            }
                                        }
                                    }
                                } while (!canceled && event.changes.fastAny { it.pressed })

                                paths.clear()
                            }
                        }

                        if (toolState.value == Tool.Move) {
                            detectTransformGestures { centroid, pan, gestureZoom, rotation ->
                                val oldScale = zoom
                                val newScale = (oldScale * gestureZoom).coerceAtLeast(1f)

                                if (gestureZoom == 1f) {
                                    offset = ((offset + centroid / oldScale).rotateBy(rotation) -
                                            (centroid / newScale + pan / oldScale)).let {
                                                val maxOffsetX = (size.width * (newScale - 1)) / 2
                                                val maxOffsetY = (size.height * (newScale - 1)) / 2
                                                Offset(
                                                    it.x.coerceIn(-maxOffsetX, maxOffsetX),
                                                    it.y.coerceIn(-maxOffsetY, maxOffsetY),
                                                )
                                    }
                                }
                                zoom = newScale
                            }
                        }
                    }
                }
            }
    ) {
        withTransform({
            val translationX = -offset.x * zoom
            val translationY = -offset.y * zoom
            translate(translationX, translationY)
            scale(zoom, zoom)
            rotate(rotationState.floatValue, pivotState.value)
        }) {
            previousBitmap?.also { drawImage(it, alpha = 0.2f) }
            drawImage(bitmap)
        }
        // for redrawing
        drawText(textMeasurer, drawingUpdater.intValue.toString(), style = TextStyle(color = Color.Transparent))
    }
}

@Composable
fun PreviewRow(
    modifier: Modifier = Modifier,
    images: SnapshotStateList<ImageBitmap>,
    selectedIndexState: MutableIntState,
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
    redraw: () -> Unit,
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
                        redraw()
                    }) {
                        Icon(Icons.AutoMirrored.Default.Undo, contentDescription = "Undo")
                    }

                    IconButton(enabled = redoItems.isNullOrEmpty().not(), onClick = {
                        val historyItem = redoItems?.removeFromEnd() ?: return@IconButton
                        undoItems?.add(historyItem)
                        redraw()
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
                        Icon(
                            painterResource(R.drawable.delete_frame),
                            contentDescription = "Delete"
                        )
                    }

                    IconButton(onClick = {
                        deleteAllFrames()
                    }) {
                        Icon(
                            painterResource(R.drawable.delete_all),
                            contentDescription = "Delete all"
                        )
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

@Composable
fun GenerateFrames(
    modifier: Modifier,
    generateFrames: (Int) -> Unit,
) {
    Column(modifier, horizontalAlignment = Alignment.End) {
        var frameCountString by remember { mutableStateOf("") }
        val focusRequester = remember { FocusRequester() }
        TextField(
            modifier = Modifier.fillMaxWidth().focusRequester(focusRequester),
            value = frameCountString,
            onValueChange = { frameCountString = it },
            singleLine = true,
            readOnly = false,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        )

        LaunchedEffect(Unit) {
            focusRequester.requestFocus()
        }

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
    clearTransforms: () -> Unit,
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
                borderColor = MaterialTheme.colorScheme.onSurface,
                onClick = { showColorPickerState.value = true },
            )

            listOf(
                ToolButtonData(Tool.Move, ImageVector.vectorResource(R.drawable.move), "Move"),
                ToolButtonData(Tool.Pencil, Icons.Outlined.Edit, "Pencil"),
                ToolButtonData(
                    Tool.Eraser,
                    ImageVector.vectorResource(R.drawable.eraser),
                    "Eraser"
                ),
                ToolButtonData(
                    Tool.Shape.Line,
                    ImageVector.vectorResource(R.drawable.line),
                    "Line"
                ),
                ToolButtonData(Tool.Shape.Rectangle, Icons.Outlined.Rectangle, "Rectangle"),
                ToolButtonData(Tool.Shape.Square, Icons.Outlined.Square, "Square"),
                ToolButtonData(
                    Tool.Shape.Oval,
                    ImageVector.vectorResource(R.drawable.oval),
                    "Oval"
                ),
                ToolButtonData(Tool.Shape.Circle, Icons.Outlined.Circle, "Circle"),
            ).forEach { (tool, icon, description) ->
                IconButton(modifier = Modifier.run {
                    if (toolState.value == tool) {
                        background(Color.Gray.copy(alpha = 0.5f), CircleShape)
                    } else {
                        this
                    }
                }, onClick = {
                    if (toolState.value == Tool.Move && tool == Tool.Move) {
                        clearTransforms()
                    }
                    toolState.value = tool
                }) {
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
