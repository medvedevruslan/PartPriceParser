package com.medvedev.parts_domain.models

data class ProductCartDTO(
    val fullLinkToProduct: String,
    val fullImageUrl: String,
    val price: Float?,
    val name: String,
    val article: String,
    val additionalArticles: String?,
    val brand: ProductBrandDTO,
    val quantity: String?,
    val existence: PartExistenceDTO
)