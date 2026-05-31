# API Keys Setup Guide

CropDoctor AI relies on external APIs for Authentication and the AI Disease Analysis feature. These keys are kept out of source control for security. 

You must define these keys in the `local.properties` file located at the root of the project.

## 1. Google Sign-In (Firebase Authentication)

We use Firebase Authentication with the Google Identity Services credential manager.

*   **API Key Needed**: Firebase Web Client ID
*   **Where to get it**: 
    1. Go to your [Firebase Console](https://console.firebase.google.com/)
    2. Open your CropDoctorAI project.
    3. Go to **Authentication** -> **Sign-in method**.
    4. Enable the **Google** provider if you haven't already.
    5. Expand the **Web SDK configuration** section.
    6. Copy the string labeled **Web client ID**.

## 2. Gemini AI (Crop Disease Analysis)

We use Google's Generative AI (Gemini 2.0 Flash) to dynamically generate real organic remedies, chemical remedies, and prevention tips based on the TFLite model's predictions.

*   **API Key Needed**: Gemini API Key
*   **Where to get it**: 
    1. Go to [Google AI Studio](https://aistudio.google.com/app/apikey).
    2. Click **Create API key**.

## How to add them to the code

1. Open the file `local.properties` in the root of your Android Studio project. (If it doesn't exist, create it in `C:\Users\MOHD ABBAS\AndroidStudioProjects\CropDoctorAI\local.properties`).
2. Add the following two lines, replacing the placeholder text with your actual keys:

```properties
WEB_CLIENT_ID=your_firebase_web_client_id_here
GEMINI_API_KEY=your_gemini_api_key_here
```

### How the code reads them

The build system automatically reads these keys from `local.properties` during compile time and injects them into the app securely via `BuildConfig`.

**See:** `app/build.gradle.kts` (Lines 13-18 and 33-38)
```kotlin
// Load local.properties for API keys
val localProperties = Properties().apply {
    val localPropsFile = rootProject.file("local.properties")
    if (localPropsFile.exists()) {
        load(localPropsFile.inputStream())
    }
}

// ... inside android { defaultConfig { ... } }
buildConfigField("String", "WEB_CLIENT_ID", "\"${localProperties.getProperty("WEB_CLIENT_ID", "YOUR_WEB_CLIENT_ID_HERE")}\"")
buildConfigField("String", "GEMINI_API_KEY", "\"${localProperties.getProperty("GEMINI_API_KEY", "")}\"")
```

*   **Web Client ID Usage**: Used in `AuthViewModel.kt` to request Google Sign-In tokens.
*   **Gemini API Key Usage**: Used in `di/AnalysisModule.kt` which passes it to `data/remote/GeminiService.kt` to initialize the `GenerativeModel`.
