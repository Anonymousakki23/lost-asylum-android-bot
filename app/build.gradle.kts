plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.lastasylum.automation"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.lastasylum.automation"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        viewBinding = true
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.8.1")
    implementation("androidx.lifecycle:lifecycle-service:2.8.7")

    // ML Kit Text Recognition for OCR (multi-language support built-in)
    implementation("com.google.mlkit:text-recognition:16.0.1")

    // CameraX for screen capture analysis (with ImageAnalysis)
    implementation("androidx.camera:camera-core:1.4.1")
    implementation("androidx.camera:camera-camera2:1.4.1")
    implementation("androidx.camera:camera-lifecycle:1.4.1")
    implementation("androidx.camera:camera-view:1.4.1")

    // Tflite for custom on-device image classification
    implementation("org.tensorflow:tensorflow-lite:2.16.1")

    // WorkManager for scheduled automation tasks
    implementation("androidx.work:work-runtime-ktx:2.9.0")

    // DataStore for user settings
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Image loading for bitmap processing
    implementation("androidx.media3:media3-exoplayer:1.3.1")
}
