package com.medvedev.partpriceparser

import android.app.Application
import android.content.pm.PackageManager
import com.medvedev.parts.main.utils.printE
import dagger.hilt.android.HiltAndroidApp
import io.sentry.android.core.SentryAndroid
import timber.log.Timber

@HiltAndroidApp
class PartPriceParserApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initTimber()
    }

    private fun initSentry() {
        // настройка лога, на сайт Sentry сообщения будут отправляться с актуальной версией VersionName из build.gradle
        try {
            val appVer = this.packageManager.getPackageInfo(this.packageName, 0).versionName
            SentryAndroid.init(this) { options ->
                options.release = "io.part_price@$appVer"
            }
        } catch (e: PackageManager.NameNotFoundException) {
            "Can`t catch versionName: $e".printE
        }
    }

    private fun initTimber() {
        Timber.plant(Timber.DebugTree())
    }
}