package com.medvedev.partpriceparser.feature_parsers.presentation.models

data class PartsData(
    val linkToSearchCatalog: String = "",
    val linkToSite: String = "",
    val siteName: String = "",
    val partsResult: Resource<Set<ProductCart>> = Resource.Success(data = setOf())
)
