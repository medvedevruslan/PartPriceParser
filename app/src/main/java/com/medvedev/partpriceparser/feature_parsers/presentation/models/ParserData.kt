package com.medvedev.partpriceparser.feature_parsers.presentation.models

data class ParserData(
    val linkToSearchCatalog: String = "",
    val linkToSite: String = "",
    val siteName: String = "",
    val productParserData: Resource<Set<ProductCart>> = Resource.Success(data = setOf())
)
