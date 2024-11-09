plugins {
    alias(libs.plugins.android.application) // Applies the Android application plugin
    alias(libs.plugins.kotlin.android) // Applies the Kotlin plugin for Android
    alias(libs.plugins.kotlin.compose) // Enables Jetpack Compose support
}

android {
    namespace = "com.example.android_app" // Specifies the app's namespace, used for generated R classes
    compileSdk = 34 // Set the SDK level to compile against

    defaultConfig {
        applicationId = "com.example.android_app" // Unique identifier for your app
        minSdk = 24 // Minimum Android version supported
        targetSdk = 34 // Android version the app is targeting
        versionCode = 1 // Internal version number of the app
        versionName = "1.0" // User-visible version of the app

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner" // Test runner for instrumentation tests
    }

    buildTypes {
        release {
            isMinifyEnabled = false // Disable code shrinking and obfuscation for release builds
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"), // Default ProGuard rules for optimization
                "proguard-rules.pro" // Custom ProGuard rules
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8 // Source code compatibility with Java 8
        targetCompatibility = JavaVersion.VERSION_1_8 // Compiled bytecode compatibility with Java 8
    }

    kotlinOptions {
        jvmTarget = "1.8" // Use JVM target 1.8 to enable Java 8 features in Kotlin
    }

    buildFeatures {
        compose = true // Enables Jetpack Compose in the project
        viewBinding = true // Enables ViewBinding for easier view access
    }
}

dependencies {
    val cameraxVersion = "1.2.2"// Define the CameraX library version as a variable
    implementation("androidx.camera:camera-core:$cameraxVersion")// Core CameraX library for base functionality
    implementation("androidx.camera:camera-camera2:$cameraxVersion")// CameraX integration with the Camera2 API
    implementation("androidx.camera:camera-lifecycle:$cameraxVersion") // CameraX lifecycle-aware components for automatic lifecycle handling
    implementation("androidx.camera:camera-video:$cameraxVersion")// CameraX video capture library for video recording functionality
    implementation("androidx.camera:camera-view:$cameraxVersion")// CameraX View library to simplify displaying the camera feed
    implementation("androidx.camera:camera-extensions:$cameraxVersion") // CameraX Extensions library for advanced features like HDR and night mode

    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
}