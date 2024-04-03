package com.medvedev.partpriceparser.feature_parsers.presentation.models

import com.medvedev.partpriceparser.feature_parsers.presentation.models.filter.PartExistence

data class ProductCart(
    val fullLinkToProduct: String,
    val fullImageUrl: String,
    val price: Float?,
    val name: String,
    val article: String,
    val additionalArticles: String?,
    val brand: ProductBrand,
    val quantity: String?,
    val existence: PartExistence
)