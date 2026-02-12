plugins {
    alias(libs.plugins.androidApplication)
    id("com.google.gms.google-services")  // Add this for Firebase
}

android {
    namespace = "com.example.watch"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.watch"
        minSdk = 26
        targetSdk = 34
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
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.play.services.wearable)

    // Add these Firebase dependencies
    implementation(platform("com.google.firebase:firebase-bom:32.0.0"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")

    // Wear OS support
    implementation("androidx.wear:wear:1.3.0")

    // For background work
    implementation("androidx.work:work-runtime:2.8.1")
}
