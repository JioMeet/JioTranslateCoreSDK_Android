package com.jio.jiotranslatecoresdk

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.jio.jiotranslate.JioTranslate
import com.jio.jiotranslate.model.Completion
import com.jio.jiotranslate.model.Gender
import com.jio.jiotranslate.model.Server
import com.jio.jiotranslate.model.SupportedLanguage
import com.jio.jiotranslate.model.TranslateEngineType
import com.jio.jiotranslatecoresdk.util.AudioPlayerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Base64

class MainViewModel : ViewModel() {

    private lateinit var jioTranslate: JioTranslate

    private val _text = MutableStateFlow("")
    val text = _text.asStateFlow()

    private val _result = MutableStateFlow("")
    val result = _result.asStateFlow()

    private val _listOfLanguages = MutableStateFlow<List<SupportedLanguage>?>(null)
    val listOfLanguages = _listOfLanguages.asStateFlow()

    private val _selectedInputLanguage = MutableStateFlow<SupportedLanguage>(SupportedLanguage.English)
    val selectedInputLanguage = _selectedInputLanguage.asStateFlow()

    private val _selectedOutputLanguage = MutableStateFlow<SupportedLanguage>(SupportedLanguage.Hindi)
    val selectedOutputLanguage = _selectedOutputLanguage.asStateFlow()

    private val _isMediaPlayerPlaying = MutableStateFlow(false)
    val isMediaPlayerPlaying = _isMediaPlayerPlaying.asStateFlow()

    fun updateInputLang(inputLanguage: SupportedLanguage){
        _selectedInputLanguage.value = inputLanguage
    }

    fun updateOutputLang(outputLanguage: SupportedLanguage){
        _selectedOutputLanguage.value = outputLanguage

    }
    fun initJioTranslate(context: Context) {
        val builder = JioTranslate.Builder()
        val accessToken =
            "QnG9gW7xAW67NC73HWyfydjnbs2zJudeQ3y7asAkZQtfVFy7s8ygC25j2DXX4nkk"
        builder.apply {
            init(context, Server.SIT.baseURL)
            setJwt(accessToken)
            setUserId("userId")

        }
        jioTranslate = builder.build()
        viewModelScope.launch {
            jioTranslate.loadConfig { result ->
                when (result) {
                    is Completion.Success -> {
                        // Upon successful configuration retrieval, fetch the list of supported languages
                        _listOfLanguages.value = jioTranslate.getListOfSupportedLanguage()
                    }
                    is Completion.Error -> {
                        Log.e("TAG", "initJioTranslate: " + "Error fetching config")
                    }
                }
            }
        }

    }

    fun textTranslate(
        text: String,
        inputLanguage: SupportedLanguage,
        translationLanguage: SupportedLanguage,
        translationEngine: TranslateEngineType,
        isIndirectTranslation: Boolean = false
    ) {
        viewModelScope.launch {
            jioTranslate.startTextTranslate(
                inputText = text,
                inputLanguage = inputLanguage.languageName,
                translationLanguage = translationLanguage.languageName,
                translateEngine = translationEngine,
                isIndirectTranslation = isIndirectTranslation,
            ) { result ->
                when(result){
                    is Completion.Success -> {
                        _result.value = result.result
                    }
                    is Completion.Error -> {
                        _result.value = result.errorMessage
                    }

                }
            }
        }
    }

    fun synthesisToSpeaker(
        text: String,
        isFemale: Boolean,
        language: SupportedLanguage,
        translationEngine: TranslateEngineType
    ) {
        viewModelScope.launch {
            jioTranslate.startTextToSpeech(
                textSupportedLanguage = language.languageName,
                inputText = text,
                gender = if (isFemale) Gender.FEMALE else Gender.MALE ,
                translateEngine = translationEngine
            ) { result ->
                when(result){
                    is Completion.Success -> {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            AudioPlayerManager.playAudioBytes(
                                audioBytes = Base64.getDecoder().decode(result.result)  ?: byteArrayOf(),
                                onStopPlaying = {
                                    updateMediaPlayerState(false)
                                }
                            )
                            AudioPlayerManager.setProgressListener { currentPosition, duration ->
                                Log.d("TAG", "synthesisToSpeaker: " + currentPosition + " " + duration )
                                // Update UI or perform any action based on the current playback progress
                            }
                        } else {
                            AudioPlayerManager.playAudioBytes(
                            audioBytes = android.util.Base64.decode(result.result,0)  ?: byteArrayOf(),
                            onStopPlaying = {
                                updateMediaPlayerState(false)
                            })
                            AudioPlayerManager.setProgressListener { currentPosition, duration ->
                                Log.d("TAG", "synthesisToSpeaker: " + currentPosition + " " + duration )
                                // Update UI or perform any action based on the current playback progress
                            }
                        }
                    }
                    is Completion.Error -> {
                        _result.value = result.errorMessage
                    }

                }
            }
        }
    }

    fun updateMediaPlayerState(isPlaying: Boolean){
        _isMediaPlayerPlaying.value = isPlaying
    }
    fun speechToText(
        filePath: String,
        inputLanguage: SupportedLanguage,
        translationEngine: TranslateEngineType
    ) {
        viewModelScope.launch {
            jioTranslate.startSpeechToText(
                audioFilePath = filePath,
                inputLanguage = inputLanguage.languageName,
                translateEngine = translationEngine,
            ) { result ->
                when (result) {
                    is Completion.Success -> {
                        _result.value = result.result
                    }

                    is Completion.Error -> {
                        _result.value = result.errorMessage
                    }

                }
            }
        }
    }

    fun updateText(text: String) {
        _text.value = text
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                MainViewModel()
            }
        }
    }
}