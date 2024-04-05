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
    compileSdk = 34

    defaultConfig {
        applicationId = "com.medvedev.partpriceparser"
        minSdk = 24
        targetSdk = 34
        versionCode = 36
        versionName = "0.1.36"

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
            keyAlias = "android"
            keyPassword = "android"
            storeFile = file("$rootDir/key/platform1.jks")
            storePassword = "android"
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
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.11"
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




