package com.medvedev.parts.main.presentation.models

import com.medvedev.parts.main.presentation.models.filter.PartExistence

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