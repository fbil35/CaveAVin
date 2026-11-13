plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "online.billard35.caveavin"
    compileSdk = 34

    defaultConfig {
        applicationId = "online.billard35.caveavin"
        minSdk = 26
        targetSdk = 34
        versionCode = (System.getenv("VERSION_CODE") ?: "1").toInt()
        versionName = "2." + (System.getenv("VERSION_CODE") ?: "0")
    }

    // Clé de signature fixe, versionnée avec le projet : chaque nouvel APK
    // s'installe par-dessus le précédent sans perdre la cave.
    signingConfigs {
        create("cave") {
            storeFile = rootProject.file("signature/cave.jks")
            storePassword = "caveavin"
            keyAlias = "cave"
            keyPassword = "caveavin"
        }
    }

    buildTypes {
        getByName("debug") {
            signingConfig = signingConfigs.getByName("cave")
        }
        getByName("release") {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("cave")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf(
            "-opt-in=androidx.compose.material3.ExperimentalMaterial3Api",
        )
    }
    buildFeatures {
        compose = true
    }
    lint {
        checkReleaseBuilds = false
        abortOnError = false
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.2")
    implementation("androidx.exifinterface:exifinterface:1.3.7")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.1")
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    // Lecture de texte sur l'appareil, gratuite et hors ligne (ML Kit).
    implementation("com.google.mlkit:text-recognition:16.0.1")
}
