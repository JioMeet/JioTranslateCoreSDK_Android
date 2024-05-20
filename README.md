
# JioTranslate

JioTranslate is a package that provides functionality for translating text and speech between different languages. It offers features like image to text conversion, text translation, text to speech, and more.

## Installation

To use JioTranslate in your project, include the following dependency in your `build.gradle` file:


dependencies {
    implementation 'com.jio.translation.jiomeetcoretranslationsdk:1.0.0'
}

## Usage
##### TranslationManager
TranslationManager is an interface that provides methods for managing translations.

* **Methods**
`startRecording`: Starts recording audio for translation.
    ```
        fun startRecording(
            filePath: String,
            inputLanguage: SupportedLanguage,
            isLiveTranslation: Boolean,
            platform: String,
            onTextTranslationState: (TextTranslationState) -> Unit,
            onError: (String) -> Unit
        )
    
    ```

    `stopRecording`: Stops recording audio.
    ```
    fun stopRecording(
        onTextTranslationState: (TextTranslationState) -> Unit,
        isLiveTranslation: Boolean = false,
        platform: String
    )
    ```
    
    `translateText`: Translates input text from one language to another.
    ```
    suspend fun translateText(
    inputText: String,
    inputLanguage: SupportedLanguage,
    translationLanguage: SupportedLanguage,
    isIndirectTranslation: Boolean,
    platform: String?,
    onError: (String) -> Unit,
    onSuccess: (String) -> Unit
    )
    ```
    `textToSpeech`: Converts text to speech in the specified language.
    ```
        suspend fun textToSpeech(
        textSupportedLanguage: SupportedLanguage,
        text: String,
        isFemale: Boolean,
        translateEngine: TranslationEngine,
        onTextToSpeechState: (TextToSpeechState) -> Unit
    )
    ```
    
    `getListOfSupportedLanguage`: Retrieves a list of supported languages.
    ```
    fun getListOfSupportedLanguage(onSuccess: (List<SupportedLanguage>) -> Unit, onError: (String) -> Unit)
    ```


