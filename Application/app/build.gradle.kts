plugins {
    alias(libs.plugins.android.application) // Applies the Android application plugin
    alias(libs.plugins.kotlin.android) // Applies the Kotlin plugin for Android
    alias(libs.plugins.kotlin.compose) // Enables Jetpack Compose support
}

buildscript {
    dependencies {
        classpath(libs.google.services)
    }
}

android {
    namespace = "com.example.android_app" // Specifies the app's namespace, used for generated R classes
    compileSdk = 35 // Set the SDK level to compile against

    defaultConfig {
        applicationId = "com.example.android_app" // Unique identifier for your app
        minSdk = 24 // Minimum Android version supported
        //noinspection OldTargetApi
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
        sourceCompatibility = JavaVersion.VERSION_11 // Source code compatibility with Java 8
        targetCompatibility = JavaVersion.VERSION_11 // Compiled bytecode compatibility with Java 8
    }

    kotlinOptions {
        jvmTarget = "11" // Use JVM target 1.8 to enable Java 8 features in Kotlin
    }

    buildFeatures {
        compose = true // Enables Jetpack Compose in the project
        viewBinding = true // Enables ViewBinding for easier view access
    }
}

dependencies {
    // Core Android dependencies
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)

    // CameraX dependencies
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.video)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.camera.extensions)

    // Jetpack Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.activity.ktx)

    // Testing dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.ui.test.manifest)

    implementation(libs.tensorflow.lite)
    implementation(libs.tensorflow.lite.support)

    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)

    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)

    implementation(libs.firebase.firestore.ktx)

}