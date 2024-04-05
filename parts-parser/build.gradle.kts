import com.medvedev.buildsrc.ProjectConfig.COMPILE_SDK
import com.medvedev.buildsrc.ProjectConfig.JAVA_VERSION
import com.medvedev.buildsrc.ProjectConfig.JVM_TARGET
import com.medvedev.buildsrc.ProjectConfig.MIN_SDK

plugins {
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.medvedev.partsparser"
    compileSdk = COMPILE_SDK

    defaultConfig {
        minSdk = MIN_SDK

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JAVA_VERSION
        targetCompatibility = JAVA_VERSION
    }
    kotlinOptions {
        jvmTarget = JVM_TARGET
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)


    // HTML parser
    implementation(libs.jsoup)

    // Timber for logging
    implementation(libs.timber)

    // Sentry
    implementation(libs.sentry)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)

}