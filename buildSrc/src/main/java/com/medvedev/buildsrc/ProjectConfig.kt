package com.medvedev.buildsrc

import org.gradle.api.JavaVersion

object ProjectConfig {

    const val MIN_SDK = 24
    const val COMPILE_SDK = 34

    const val ANDROID = "android"

    val JAVA_VERSION = JavaVersion.VERSION_17
    const val KOTLIN_COMPILER_EXTENSION_VERSION = "1.5.11"

    const val JVM_TARGET = "17"
}
