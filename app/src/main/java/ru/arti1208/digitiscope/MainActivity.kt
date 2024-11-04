@file:OptIn(ExperimentalMaterial3Api::class)

package ru.arti1208.digitiscope

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.preference.PreferenceManager
import ru.arti1208.digitiscope.ui.theme.DigitiscopeTheme

class MainActivity : ComponentActivity() {

    private val preferences by lazy { PreferenceManager.getDefaultSharedPreferences(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DigitiscopeTheme {
                CanvasScreen(
                    modifier = Modifier.fillMaxSize(),
                )
                var showManual by remember { mutableStateOf(!preferences.getBoolean(MANUAL_SHOWN_KEY, false)) }
                if (showManual) {
                    fun closeDialog() {
                        showManual = false
                        preferences.edit { putBoolean(MANUAL_SHOWN_KEY, true) }
                    }
                    BasicAlertDialog(onDismissRequest = ::closeDialog) {
                        Surface(shape = RoundedCornerShape(8.dp)) {
                            Column(modifier = Modifier.padding(4.dp)) {

                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = """
                                        Welcome to Digiscope!
                                        Panels on top and bottom are scrollable
                                    """.trimIndent(),
                                    textAlign = TextAlign.Center,
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                ) {
                                    TextButton(onClick = ::closeDialog) {
                                        Text("OK")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val MANUAL_SHOWN_KEY = "manual_shown"
    }
}
