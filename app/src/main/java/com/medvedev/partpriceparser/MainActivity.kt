package com.medvedev.partpriceparser

import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.medvedev.partpriceparser.core.util.printE
import com.medvedev.partpriceparser.presentation.ParseScreen
import com.medvedev.partpriceparser.ui.theme.PartPriceParserTheme
import io.sentry.android.core.SentryAndroid

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PartPriceParserTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    ParseScreen(viewModel = hiltViewModel())
                }
            }
        }

        // настройка лога, на сайт Sentry сообщения будут отправляться с актуальной версией VersionName из build.gradle
        try {
            val appVer = this.packageManager.getPackageInfo(this.packageName, 0).versionName
            SentryAndroid.init(this) { options ->
                options.release = "io.part_price@$appVer"
            }
        } catch (e: PackageManager.NameNotFoundException) {
            "Can`t catch versionName: $e".printE
        }


        // viewModel.temporaryParseProducts("740-1003010")
        // viewModel.temporaryParseProducts("740.1003010-20")
        // viewModel.temporaryParseProducts("агрегат")
    }
}
