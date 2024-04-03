package com.medvedev.partpriceparser.feature_parsers.domain.mappers

import com.medvedev.partpriceparser.feature_parsers.presentation.models.ProductBrand
import com.medvedev.partpriceparser.feature_parsers.presentation.models.ProductCart
import com.medvedev.partpriceparser.feature_parsers.presentation.models.filter.PartExistence
import com.medvedev.partsparser.models.PartExistenceDTO
import com.medvedev.partsparser.models.ProductBrandDTO
import com.medvedev.partsparser.models.ProductCartDTO
import com.medvedev.partsparser.utils.ResourceDTO
import com.medvedev.partpriceparser.feature_parsers.presentation.models.Resource


fun ResourceDTO<Set<ProductCartDTO>>.toResult(): Resource<Set<ProductCart>> {
    val newResource: Resource<Set<ProductCart>> = when (this) {
        is ResourceDTO.Loading -> Resource.Loading()
        is ResourceDTO.Error -> Resource.Error(message = message ?: "")
        is ResourceDTO.Success -> Resource.Success(
            data = data?.map { product ->
                ProductCart(
                    fullLinkToProduct = product.fullLinkToProduct,
                    fullImageUrl = product.fullImageUrl,
                    price = product.price,
                    name = product.name,
                    article = product.article,
                    additionalArticles = product.additionalArticles,
                    brand = product.brand.toProductBrand(),
                    quantity = product.quantity,
                    existence = product.existence.toPartExistence(),
                )
            }?.toSet()
        )
    }
    return newResource
}

fun ProductBrandDTO.toProductBrand(): ProductBrand {
    return when(this) {
        is ProductBrandDTO.Kamaz -> ProductBrand.Kamaz(name = this.name)
        is ProductBrandDTO.Repair -> ProductBrand.Repair(name = this.name)
        is ProductBrandDTO.Unknown -> ProductBrand.Unknown(name = this.name)
    }
}

fun PartExistenceDTO.toPartExistence(): PartExistence {
    return when (this) {
        is PartExistenceDTO.TrueExistence -> PartExistence.Positive(description = description)
        is PartExistenceDTO.FalseExistenceDTO -> PartExistence.Negative(description = description)
        else -> PartExistence.Unknown(description = description)
    }
}