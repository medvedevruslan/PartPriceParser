package com.medvedev.partsparser.models

import com.medvedev.partsparser.utils.ResourceParse

data class PartsDataParse(
    val linkToSearchCatalog: String = "",
    val linkToSite: String = "",
    val siteName: String = "",
    val partsResultParse: ResourceParse<Set<ProductCartParse>> = ResourceParse.Success(data = setOf())
)
