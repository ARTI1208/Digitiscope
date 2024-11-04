@file:OptIn(ExperimentalMaterial3Api::class)

package ru.arti1208.digitiscope

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

sealed interface Background {
    data class BitmapBackground(val bitmap: ImageBitmap) : Background

    data class ColorBackground(val color: Color): Background
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
                                                .border(2.dp, MaterialTheme.colorScheme.surface, CircleShape)
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

internal fun saveGif(
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

internal fun exportGif(
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
