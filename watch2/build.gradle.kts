plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
}

android {
    namespace = "com.example.smartpillow.watch2"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.smartpillow.watch2"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        vectorDrawables {
            useSupportLibrary = true
        }

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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.1"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // 1. Wear XML UI Libraries (Required for BoxInsetLayout and layout_box)
    // REMOVED QUOTES - This uses your libs.versions.toml
    implementation(libs.androidx.wear)

    implementation("androidx.wear:wear:1.3.0")
    implementation("androidx.wear:wear-input:1.2.0") // Updated as suggested
    implementation("androidx.percentlayout:percentlayout:1.0.0")
    // 2. Core UI & Shared Module
    implementation(project(":shared"))
    implementation(libs.appcompat)
    implementation(libs.constraintlayout)
    implementation("androidx.cardview:cardview:1.0.0")

    // 3. Google Play Services for Wear
    implementation(libs.play.services.wearable)

    // 4. Compose Libraries
    implementation(platform(libs.compose.bom))
    implementation(libs.ui)
    implementation(libs.ui.tooling.preview)
    implementation(libs.compose.material)
    implementation(libs.compose.foundation)
    implementation(libs.activity.compose)
    implementation(libs.core.splashscreen)

    // 5. Testing & Debug
    androidTestImplementation(platform(libs.compose.bom))
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.tooling)
    debugImplementation(libs.ui.test.manifest)
}