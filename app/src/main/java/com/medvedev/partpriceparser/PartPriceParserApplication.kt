package com.medvedev.partpriceparser

import android.app.Application
import timber.log.Timber

class PartPriceParserApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initTimber()
    }

    private fun initTimber() {
        Timber.plant(Timber.DebugTree())
    }

}