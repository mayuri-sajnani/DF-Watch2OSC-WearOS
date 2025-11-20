plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.elementz.heartrate2osc"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.elementz.heartrate2osc"
        minSdk = 30
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    // Mark as a Wear OS app
    defaultConfig {
        manifestPlaceholders["appLabel"] = "HR→OSC"
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Wear OS UI support (classic views are fine; this adds wear widget styles)
    implementation("androidx.wear:wear:1.3.0")

    // JavaOSC for sending OSC over UDP
    implementation("com.illposed.osc:javaosc-core:0.7")
}