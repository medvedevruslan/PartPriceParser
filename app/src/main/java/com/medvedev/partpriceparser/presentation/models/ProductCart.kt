package com.medvedev.partpriceparser.presentation.models

data class ProductCart(
    val fullLinkToProduct: String,
    val fullImageUrl: String,
    val price: String?,
    val name: String,
    val article: String,
    val additionalArticles: String?,
    val brand: String?,
    val quantity: String?,
    val existence: String?
)
