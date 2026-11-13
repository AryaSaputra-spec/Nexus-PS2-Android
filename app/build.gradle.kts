
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.nexus.ps2android"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.nexus.ps2android"
        minSdk = 23
        targetSdk = 35

        versionCode = 1
        versionName = "0.1"

        externalNativeBuild {
            cmake {
                cppFlags += "-std=c++17"
            }
        }
    }

    externalNativeBuild {
        cmake {
            path = file("src/main/cpp/CMakeLists.txt")
            version = "3.22.1"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
    }
}
dependencies {
    implementation("androidx.activity:activity-ktx:1.9.3")
}
