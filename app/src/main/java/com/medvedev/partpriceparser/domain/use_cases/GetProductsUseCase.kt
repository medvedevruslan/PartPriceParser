package com.medvedev.partpriceparser.domain.use_cases

import com.medvedev.partpriceparser.feature_parsers.sources.AutoMotorsParser
import com.medvedev.partpriceparser.presentation.models.ParserData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow


class GetProductsUseCase {

    private val autoMotorsParser = AutoMotorsParser()

    suspend fun execute(article: String): Flow<ParserData> {
        return flow {
            autoMotorsParser.getProduct(article).collect { result ->
                val parserData = ParserData(
                    halfLinkSearchCatalog = autoMotorsParser.linkToSite + autoMotorsParser.partOfLinkToCatalog,
                    linkToSite = autoMotorsParser.linkToSite,
                    siteName = autoMotorsParser.siteName,
                    productList = result
                )
                emit(parserData)
            }
        }
    }
}