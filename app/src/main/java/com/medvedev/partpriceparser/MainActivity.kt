package com.medvedev.partpriceparser

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.medvedev.partpriceparser.presentation.ParseScreen
import com.medvedev.partpriceparser.presentation.ParserViewModel
import com.medvedev.partpriceparser.ui.theme.PartPriceParserTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val viewModel = ParserViewModel()
        setContent {
            PartPriceParserTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface
                ) {
                    ParseScreen(viewModel)
                }
            }
        }

        // viewModel.temporaryParseProducts("740-1003010")
        // viewModel.temporaryParseProducts("740.1003010-20")
        // viewModel.temporaryParseProducts("агрегат")
    }
}
