import com.medvedev.buildsrc.ProjectConfig.ANDROID
import com.medvedev.buildsrc.ProjectConfig.COMPILE_SDK
import com.medvedev.buildsrc.ProjectConfig.JAVA_VERSION
import com.medvedev.buildsrc.ProjectConfig.JVM_TARGET
import com.medvedev.buildsrc.ProjectConfig.KOTLIN_COMPILER_EXTENSION_VERSION
import com.medvedev.buildsrc.ProjectConfig.MIN_SDK

val sentryToken = providers.gradleProperty("SENTRY_AUTH_TOKEN").get()


plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.kapt)
    alias(libs.plugins.sentry.android.gradle)
    alias(libs.plugins.google.dagger.hilt.android)
}

android {
    namespace = "com.medvedev.partpriceparser"
    compileSdk = COMPILE_SDK

    defaultConfig {
        applicationId = "com.medvedev.partpriceparser"
        minSdk = MIN_SDK
        targetSdk = COMPILE_SDK
        versionCode = 37
        versionName = "0.1.37"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file("$rootDir/key/platform1.jks")
            keyAlias = ANDROID
            keyPassword = ANDROID
            storePassword = ANDROID
        }
    }

    compileOptions {
        sourceCompatibility = JAVA_VERSION
        targetCompatibility = JAVA_VERSION
    }

    kotlinOptions {
        jvmTarget = JVM_TARGET
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion =  KOTLIN_COMPILER_EXTENSION_VERSION
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

sentry {
    authToken.set(sentryToken)
    experimentalGuardsquareSupport.set(true)
    projectName.set("smart_truck")
    org.set("medvedev91")
}

dependencies {

    implementation(project(":features:parts-main"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)

    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)

    // Timber for logging
    implementation(libs.timber)

    // Hilt for DI
    implementation(libs.hilt.android)
    implementation(libs.androidx.hilt.navigation.compose)
    kapt(libs.hilt.compiler)

    // Sentry
    implementation(libs.sentry.android)
    implementation(libs.sentry)
}

kapt {
    correctErrorTypes = true
}




