package com.medvedev.partsparser

import com.medvedev.partsparser.models.PartsDataParse
import com.medvedev.partsparser.sources.AutoMotorsParser
import com.medvedev.partsparser.sources.AvtoKamaParser
import com.medvedev.partsparser.sources.KamaCenterParser
import com.medvedev.partsparser.sources.MarkParser
import com.medvedev.partsparser.sources.MidkamParser
import com.medvedev.partsparser.sources.NikoParser
import com.medvedev.partsparser.sources.ProductParser
import com.medvedev.partsparser.sources.RiatParser
import com.medvedev.partsparser.sources.SkladTfkParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.launch

class PartsParser {

    private val autoMotorsParser = AutoMotorsParser()

    private val skladTfkParser = SkladTfkParser()

    private val riatParser = RiatParser()

    private val markParser = MarkParser()

    private val kamaCenterParser = KamaCenterParser()

    private val avtoKamaParser = AvtoKamaParser()

    private val midkamParser = MidkamParser()

    private val nikoParser = NikoParser()

    private val parserSourcesList: ArrayList<ProductParser> = arrayListOf(
        autoMotorsParser,
        skladTfkParser,
        riatParser,
        markParser,
        kamaCenterParser,
        avtoKamaParser,
        midkamParser,
        nikoParser
    )

    suspend fun getPartsData(article: String): Flow<PartsDataParse> {
        return channelFlow {
            parserSourcesList.forEach { source ->
                launch(Dispatchers.IO) {
                    source.getProduct(article).collect { resultParse ->
                        val parserData = PartsDataParse(
                            linkToSearchCatalog = source.linkToSite +
                                    source.partOfLinkToCatalog(article),
                            linkToSite = source.linkToSite,
                            siteName = source.siteName,
                            partsResultParse = resultParse
                        )
                        send(parserData)
                    }
                }
            }
        }
    }
}