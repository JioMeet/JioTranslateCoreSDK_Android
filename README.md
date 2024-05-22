# JioTranslateCoreSDK_Android
JioTranslate is a package that provides functionality for translating text and speech between different languages. It offers features like text translation, text to speech, and speech to text.

# JioTranslate CoreSDK Quickstart

## Table of Contents

1. [Introduction](#introduction)
2. [Features](#features)
3. [Prerequisites](#prerequisites)
4. [Project Settings](#project-settings)
5. [Integration Steps](#integration-steps)
   - [Configure SDK](#configure-jiomeet-core-sdk-inside-your-app)
   - [Add Permissions](#add-permissions-for-network-and-device-access)
   - [Integrate SDK](#integrate-sdk)
   - [Speech to Text Translation](#speech-to-text-translation)
   - [Text to Text Translation](#text-to-text-translation)
   - [Text to Speech Translation](#text-to-speech-translation)

## Introduction

In this documentation, we'll guide you through the process of installation, Let's get started on your journey to Break Language barriers with translation of any language into your native tongue with JioTranslate CoreSDK!

---

## Features

In JioTranslate Core SDK, you'll find a range of powerful features designed to enhance your iOS application's translation needs. These features include:

1. **Speech to Text Translation**: Experience seamless conversion of spoken language into written text.

2. **Text to Text Translation**: Effortlessly translate text from one language to another.

3. **Text to Speech Translation**: Transform written text into spoken language.

## Prerequisites

Before getting started with this example app, please ensure you have the following software installed on your machine:

- Android Studio
- Support for Java 11

## Project Settings

## Configure JioMeet Core SDK inside your app

i.: Generate a Personal Access Token for GitHub

- Settings -> Developer Settings -> Personal Access Tokens -> Generate new token
- Make sure you select the following scopes (“ read:packages”) and Generate a token
- After Generating make sure to copy your new personal access token. You cannot see it again! The only option is to generate a new key.

ii. Update build.gradle inside the application module

```kotlin
    repositories {
    maven {
        credentials {
            <!--github user name-->
                username = ""
            <!--github user token-->
                password = ""
        }
        url = uri("https://maven.pkg.github.com/JioMeet/JioTranslateCoreSDK_Android")
    }
    google()
    mavenCentral()
}
```

iii. In Gradle Scripts/build.gradle (Module: <projectname>) add the CORE SDK dependency. The dependencies
section should look like the following:

```gradle
dependencies {
    ...
    implementation " com.jio.translation.jiomeetcoretranslationsdk:<version>"
    ...
}
```

Find the [Latest version](https://github.com/JioMeet/JioTranslateCoreSDK_Android/releases) of the Core
SDK and replace <version> with the one you want to use. For example: 1.0.0.

### Add permissions for network and device access.

In /app/Manifests/AndroidManifest.xml, add the following permissions after </application>:

```gradle
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"

```

### Integrate SDK

Create and configure the instance of `JioTranslateManager`. 

```kotlin
        val builder = JioTranslate.Builder()
        builder.apply {
            init(context, Server.SIT.baseURL)
            setJwt(accessToken)
            setUserId("userId")
        }
        jioTranslate = builder.build()
    
```


### Speech to Text Translation

Use this function to convert spoken language into written text.
```kotlin
 fun startSpeechToText(
        audioFilePath : String,
        inputLanguage : SupportedLanguage,
        translateEngine : TranslateEngineType? = null,
        completion : (Completion<String>) -> Unit,
    ) 
```


| Property Name | Type  | Description  |
| ------- | --- | --- |
| audioFilePath | String | Send recorded audio file path (Ex: recorded.wav) |
| inputLanguage | SupportedLanguage | Language name of the recorded audio, Ex: 'English', 'Telugu' |
| translateEngine | TranlsateEngineType | TRANSLATE_ENGINE_1, TRANSLATE_ENGINE_2, TRANSLATE_ENGINE_3 |
| completion | Completion<T> | The completion result. This is of type Completion, which is a sealed class with two possible types: Success or Error. The Success type holds a result of type T, while the Error type holds an error message as a String. |


### Text to Text Translation

Use this function to translate text from one language to another.
```kotlin
  suspend fun startTextTranslate(
        inputText : String,
        inputLanguage : SupportedLanguage,
        translationLanguage : SupportedLanguage,
        translateEngine :  TranslateEngineType? = null,
        isIndirectTranslation : Boolean = false,
        completion : (Completion<String>) -> Unit,
    ) 
```

| Property Name | Type  | Description  |
| ------- | --- | --- |
| inputText | String | Input text to translate |
| inputLanguage | SupportedLanguage | Language name of the input text, Ex: 'English', 'Telugu' |
| translationLanguage | SupportedLanguage | Language name of the output translation text, Ex: 'Hindi', 'Telugu' |
| translateEngine | TranlsateEngineType | TRANSLATE_ENGINE_1, TRANSLATE_ENGINE_2, TRANSLATE_ENGINE_3  |
| isIndirectTranslation | Bool | true or false |
| completion | Completion<T> | The completion result. This is of type Completion, which is a sealed class with two possible types: Success or Error. The Success type holds a result of type T, while the Error type holds an error message as a String. |


### Text to Speech Translation

Use this function to translate written text into spoken language.

```kotlin
suspend fun startTextToSpeech(
        inputText : String,
        gender: Gender,
        translateEngine :  TranslateEngineType? = null,
        textSupportedLanguage : SupportedLanguage,
        completion : (Completion<String>) -> Unit,
    )
```

| Property Name | Type  | Description  |
| ------- | --- | --- |
| inputText | String | Input text to translate |
| textSupportedLanguage | SupportedLanguage | Language name of the input text, Ex: 'English', 'Telugu' |
| translateEngine | TranlsateEngineType | TRANSLATE_ENGINE_1, TRANSLATE_ENGINE_2, TRANSLATE_ENGINE_3 |
| gender | Gender | MALE or FEMALE |
| completion | Completion<T> | The completion result. This is of type Completion, which is a sealed class with two possible types: Success or Error. The Success type holds a result of type T, while the Error type holds an error message as a String. |

## Troubleshooting

Facing any issues while integrating or installing the JioTranslate android CoreSDK kit, please connect with us via real time support present in JioTranslate.AppSupport@jio.com or https://translate.jio/contact-us.html
