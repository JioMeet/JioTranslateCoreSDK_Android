package com.jio.jiotranslatecoresdk

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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
import com.jio.jiotranslatecoresdk.ui.theme.AudioPlayerManager
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


    fun updateInputLang(inputLanguage: SupportedLanguage){
        _selectedInputLanguage.value = inputLanguage
    }

    fun updateOutputLang(outputLanguage: SupportedLanguage){
        _selectedOutputLanguage.value = outputLanguage

    }
    fun initJioTranslate(context: Context) {
        val builder = JioTranslate.Builder()
        val accessToken =
            "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VySWQiOiJ1LTA4NDM3NGJjLTAxOGQtNGQ3ZS04MTM3LTgzYzNjNTFlY2VhNiIsInRva2VuSWQiOiJvdC1jMjMxOTc4OS0yZWNkLTQ5OGQtYjRlZC1jZTRhYjQ5MzBkNGMiLCJzb3VyY2UiOiJtb2JpbGUiLCJpYXQiOjE3MTYzNjcxNjQsImV4cCI6MTcxNjQ1MzU2NH0.dupQmzfkRhIsyHDc3bhBOD4TF39Wb-pBlzKVYsUM7flZdUTshMAxod4Hzz3P2bFyAe1yRap9ZDV3RcX2m_XVr89yfQgMKS8txCiYLK0a0q-EfzPxT9QKFLYKqaec76YeWxATFiTi1_bVrE4-c5Z2Y4VqZZnFb4W0q1s4uwMTy64"
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
                inputLanguage = inputLanguage,
                translationLanguage = translationLanguage,
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

    @RequiresApi(Build.VERSION_CODES.O)
    fun synthesisToSpeaker(
        text: String,
        isFemale: Boolean,
        language: SupportedLanguage,
        translationEngine: TranslateEngineType
    ) {
        viewModelScope.launch {
            jioTranslate.startTextToSpeech(
                textSupportedLanguage = language,
                inputText = text,
                gender = if (isFemale) Gender.FEMALE else Gender.MALE ,
                translateEngine = translationEngine
            ) { result ->
                when(result){
                    is Completion.Success -> {
                        AudioPlayerManager.playAudioBytes(
                            audioBytes = Base64.getDecoder().decode(result.result)  ?: byteArrayOf(),
                            onStopPlaying = {}
                        )
                    }
                    is Completion.Error -> {
                        _result.value = result.errorMessage
                    }

                }
            }
        }
    }

    fun speechToText(
        filePath: String,
        inputLanguage: SupportedLanguage,
        translationEngine: TranslateEngineType
    ) {
        viewModelScope.launch {
            jioTranslate.startSpeechToText(
                audioFilePath = filePath,
                inputLanguage = inputLanguage,
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