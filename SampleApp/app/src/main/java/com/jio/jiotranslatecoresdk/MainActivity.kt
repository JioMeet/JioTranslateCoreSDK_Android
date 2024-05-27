package com.jio.jiotranslatecoresdk

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import com.jio.jiotranslate.model.SupportedLanguage
import com.jio.jiotranslate.model.TranslateEngineType
import com.jio.jiotranslatecoresdk.ui.theme.MyApplicationTheme
import com.jio.jiotranslatecoresdk.util.AudioPlayerManager
import com.jio.jiotranslatecoresdk.util.AudioRecorder
import com.jio.jiotranslatecoresdk.util.ComposableLifecycle


class MainActivity : ComponentActivity() {

    private val viewModel : MainViewModel by viewModels { MainViewModel.Factory }

    override fun onCreate(savedInstanceState : Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            var isRecording by remember { mutableStateOf(false) }
            val audioRecorder = remember { AudioRecorder(context) }

            val permissions = arrayOf(
                android.Manifest.permission.RECORD_AUDIO,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            )

            val requestPermissionLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                    permissions.entries.forEach {
                        Log.d("Permissions", "${it.key} = ${it.value}")
                    }
                }

            ComposableLifecycle(onDispose = {
            }, onEvent = { _, event ->
                when (event) {
                    //handle app background case
                    Lifecycle.Event.ON_PAUSE -> {
                        AudioPlayerManager.stopPlayback()
                        viewModel.updateMediaPlayerState(false)
                    }

                    else -> {}
                }
            })

            LaunchedEffect(Unit) {
                val allPermissionsGranted : Boolean =
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) { // Below Android 13
                        permissions.all {
                            ContextCompat.checkSelfPermission(
                                context,
                                it
                            ) == PackageManager.PERMISSION_GRANTED
                        }
                    } else {
                        ContextCompat.checkSelfPermission(
                            context,
                            android.Manifest.permission.RECORD_AUDIO,
                        ) == PackageManager.PERMISSION_GRANTED
                    }
                if (!allPermissionsGranted) {
                    requestPermissionLauncher.launch(permissions)
                }
                viewModel.initJioTranslate(context)
            }

            MyApplicationTheme {
                val context = LocalContext.current
                val textState by viewModel.text.collectAsState()
                val resultState by viewModel.result.collectAsState()
                val supportedLanguages by viewModel.listOfLanguages.collectAsState()
                val selectedInputLanguage by viewModel.selectedInputLanguage.collectAsState()
                val selectedOutputLanguage by viewModel.selectedOutputLanguage.collectAsState()
                val isPlaying by viewModel.isMediaPlayerPlaying.collectAsState()
                MainScreen(
                    textState = textState,
                    resultState = resultState,
                    onTextChange = {
                        viewModel.updateText(it)
                    },
                    onTextTranslate = {
                        if (textState.isEmpty()) {
                            showToast(context, "Please Enter text to speak in input box")
                            return@MainScreen
                        }
                        viewModel.textTranslate(
                            text = textState,
                            inputLanguage = selectedInputLanguage,
                            translationLanguage = selectedOutputLanguage,
                            translationEngine = TranslateEngineType.TRANSLATE_ENGINE_1
                        )
                    },
                    onStartSynthesis = {
                        if (textState.isEmpty()) {
                            showToast(context, "Please Enter text to speak in input box")
                            return@MainScreen
                        }
                        viewModel.updateMediaPlayerState(!isPlaying)
                        viewModel.synthesisToSpeaker(
                            text = textState,
                            isFemale = false,
                            language = selectedInputLanguage,
                            translationEngine = TranslateEngineType.TRANSLATE_ENGINE_1
                        )
                    },
                    speechToText = {
                        val allPermissionsGranted : Boolean =
                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) { // Below Android 13
                                permissions.all {
                                    ContextCompat.checkSelfPermission(
                                        context,
                                        it
                                    ) == PackageManager.PERMISSION_GRANTED
                                }
                            } else {
                                ContextCompat.checkSelfPermission(
                                    context,
                                    android.Manifest.permission.RECORD_AUDIO,
                                ) == PackageManager.PERMISSION_GRANTED
                            }
                        if (allPermissionsGranted) {
                            if (isRecording) {
                                audioRecorder.stopRecording()
                                viewModel.speechToText(
                                    filePath = audioRecorder.wavFilePath,
                                    inputLanguage = selectedInputLanguage,
                                    translationEngine = TranslateEngineType.TRANSLATE_ENGINE_1
                                )
                            } else {
                                audioRecorder.startRecording()
                            }
                            isRecording = !isRecording
                        } else {
                            requestPermissionLauncher.launch(permissions)
                        }
                    },
                    speechToTextTitle = if (isRecording) "Stop Speech To Text" else "Start Speech To Text",
                    listSupportedLanguages = supportedLanguages,
                    updateInputLang = { viewModel.updateInputLang(it) },
                    updateOutputLang = { viewModel.updateOutputLang(it) },
                    selectedInputLanguage = selectedInputLanguage,
                    selectedOutputLanguage = selectedOutputLanguage,
                    isPlaying = isPlaying,
                    updateAudioPlayerState = {
                        viewModel.updateMediaPlayerState(false)
                    }
                )
            }
        }
    }
}

@Composable
fun MainScreen(
    textState : String,
    resultState : String,
    onTextChange : (String) -> Unit,
    onTextTranslate : () -> Unit,
    onStartSynthesis : () -> Unit,
    speechToText : () -> Unit,
    speechToTextTitle : String,
    listSupportedLanguages : List<SupportedLanguage>?,
    updateInputLang : (SupportedLanguage) -> Unit,
    updateOutputLang : (SupportedLanguage) -> Unit,
    selectedInputLanguage : SupportedLanguage,
    selectedOutputLanguage : SupportedLanguage,
    isPlaying: Boolean,
    updateAudioPlayerState: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        MultilineTextField(textState, onTextChange)
        Spacer(modifier = Modifier.height(16.dp))
        TextViewBelowTextField(resultState)
        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDropdowns(
            listSupportedLanguages,
            updateInputLang,
            updateOutputLang,
            selectedInputLanguage,
            selectedOutputLanguage
        )
        Spacer(modifier = Modifier.height(16.dp))
        FullWidthVerticalButtons(
            onTextTranslate,
            onStartSynthesis,
            speechToText,
            speechToTextTitle,
            isPlaying = isPlaying,
            updateAudioPlayerState = updateAudioPlayerState
        )
    }
}

@Composable
fun MultilineTextField(textState : String, onTextChange : (String) -> Unit) {
    TextField(
        value = textState,
        onValueChange = { onTextChange(it) },
        label = { Text("Enter text here") },
        modifier = Modifier.fillMaxWidth(),
        maxLines = 4,
        singleLine = false
    )
}

@Composable
fun TextViewBelowTextField(resultState : String) {
    Column {
        Text(
            text = "Result",
            color = Color.Black,
            fontSize = 12.sp,
        )
        Text(
            text = resultState,
            modifier = Modifier.fillMaxWidth()
        )
    }

}

@Composable
fun HorizontalDropdowns(
    listSupportedLanguages : List<SupportedLanguage>?,
    updateInputLang : (SupportedLanguage) -> Unit,
    updateOutputLang : (SupportedLanguage) -> Unit,
    selectedInputLanguage : SupportedLanguage,
    selectedOutputLanguage : SupportedLanguage
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly
    ) {
        Column {
            Text(
                text = "Input Language",
                color = Color.Black,
                fontSize = 12.sp,
            )
            DropdownMenuSample(
                listSupportedLanguages,
                updateInputLang,
                true,
                selectedInputLanguage,
                selectedOutputLanguage
            )
        }
        Column {
            Text(
                text = "Output Language",
                color = Color.Black,
                fontSize = 12.sp,
            )
            DropdownMenuSample(
                listSupportedLanguages,
                updateOutputLang,
                false,
                selectedInputLanguage,
                selectedOutputLanguage
            )
        }

    }
}

@Composable
fun DropdownMenuSample(
    listSupportedLanguages : List<SupportedLanguage>?,
    updateLang : (SupportedLanguage) -> Unit,
    isInput : Boolean,
    selectedInputLanguage : SupportedLanguage,
    selectedOutputLanguage : SupportedLanguage
) {
    var expanded by remember { mutableStateOf(false) }
//
    Box(modifier = Modifier.wrapContentSize()) {
        Button(onClick = { expanded = true }) {
            if (isInput) {
                Text(selectedInputLanguage.languageName)
            } else {
                Text(selectedOutputLanguage.languageName)
            }
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            listSupportedLanguages?.forEachIndexed { index, language ->
                DropdownMenuItem(
                    text = { Text(text = language.languageName) },
                    onClick = {
                        updateLang(language)
                        expanded = false
                    })
            }
        }
    }
}

@Composable
fun FullWidthVerticalButtons(
    onTextTranslate : () -> Unit,
    onStartSynthesis : () -> Unit,
    speechToText : () -> Unit,
    speechToTextTitle : String,
    isPlaying : Boolean,
    updateAudioPlayerState:(Boolean) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Button(onClick = { onTextTranslate() }, modifier = Modifier.fillMaxWidth()) {
            Text("Translate Text")
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = {
            if (isPlaying) {
                AudioPlayerManager.stopPlayback()
                updateAudioPlayerState(false)
            } else onStartSynthesis()
        }, modifier = Modifier.fillMaxWidth()) {
           if(!isPlaying) Text("Start Text To Speech")  else Text("Stop Text to Speech")
        }

        AudioControlRow(
            onPauseClick = { AudioPlayerManager.pausePlayback() },
            onReplayClick = { AudioPlayerManager.replay() },
            onResumeClick = { AudioPlayerManager.resumePlayback() })
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = { speechToText() }, modifier = Modifier.fillMaxWidth()) {
            Text(speechToTextTitle)
        }
    }
}

@Composable
fun AudioControlRow(
    onPauseClick : () -> Unit,
    onReplayClick : () -> Unit,
    onResumeClick : () -> Unit,
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = onPauseClick) {
            Text("Pause")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = onReplayClick) {
            Text("Replay")
        }
        Spacer(modifier = Modifier.width(8.dp))
        Button(onClick = onResumeClick) {
            Text("Resume")
        }
    }
}

fun showToast(context : Context, message : String) {
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}
