package com.medvedev.partpriceparser.domain.use_cases

import com.medvedev.partpriceparser.feature_parsers.sources.AutoMotorsParser
import com.medvedev.partpriceparser.feature_parsers.sources.SkladTfkParser
import com.medvedev.partpriceparser.presentation.models.ParserData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class GetProductsUseCase {

    private val autoMotorsParser = AutoMotorsParser()

    private val skladTfkParser = SkladTfkParser()

    suspend fun execute(article: String): Flow<ParserData> {
        return flow {

            autoMotorsParser.getProduct(article).collect { result ->
                val parserData = ParserData(
                    linkToSearchCatalog = autoMotorsParser.linkToSite + autoMotorsParser.partOfLinkToCatalog(article),
                    linkToSite = autoMotorsParser.linkToSite,
                    siteName = autoMotorsParser.siteName,
                    productList = result
                )
                emit(parserData)
            }

            skladTfkParser.getProduct(article).collect { result ->
                val parserData = ParserData(
                    linkToSearchCatalog = skladTfkParser.linkToSite + skladTfkParser.partOfLinkToCatalog(article),
                    linkToSite = skladTfkParser.linkToSite,
                    siteName = skladTfkParser.siteName,
                    productList = result
                )
                emit(parserData)
            }
        }
    }
}