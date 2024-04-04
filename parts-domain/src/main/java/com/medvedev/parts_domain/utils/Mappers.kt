package com.medvedev.parts_domain.utils

import com.medvedev.parts_domain.models.PartExistenceDTO
import com.medvedev.parts_domain.models.PartsDataDTO
import com.medvedev.parts_domain.models.ProductBrandDTO
import com.medvedev.parts_domain.models.ProductCartDTO
import com.medvedev.partsparser.models.PartExistenceParse
import com.medvedev.partsparser.models.PartsDataParse
import com.medvedev.partsparser.models.ProductBrandParse
import com.medvedev.partsparser.models.ProductCartParse
import com.medvedev.partsparser.utils.ResourceParse

fun PartsDataParse.toPartsDataDTO(): PartsDataDTO {
    return PartsDataDTO(
        linkToSite = this.linkToSite,
        linkToSearchCatalog = this.linkToSearchCatalog,
        siteName = this.siteName,
        partsResultDTO = this.partsResultParse.toResourceDTO()
    )
}

fun ResourceParse<Set<ProductCartParse>>.toResourceDTO(): ResourceDTO<Set<ProductCartDTO>> {
    return when (this) {
        is ResourceParse.Loading -> ResourceDTO.Loading()
        is ResourceParse.Error -> ResourceDTO.Error(message = message ?: "")
        is ResourceParse.Success -> ResourceDTO.Success(data = data?.map { partsDataParse ->
            partsDataParse.toProductCartDTO()
        }?.toSet())
    }
}

fun ProductCartParse.toProductCartDTO(): ProductCartDTO {
    return ProductCartDTO(
        fullLinkToProduct = this.fullLinkToProduct,
        fullImageUrl = this.fullImageUrl,
        price = this.price,
        name = this.name,
        article = this.article,
        additionalArticles = this.additionalArticles,
        brand = this.brand.toProductBrandDTO(),
        quantity = this.quantity,
        existence = this.existence.toPartExistenceDto()
    )
}

fun PartExistenceParse.toPartExistenceDto(): PartExistenceDTO {
    return when (this) {
        is PartExistenceParse.FalseExistenceParse -> PartExistenceDTO.Negative(description = this.description)
        is PartExistenceParse.TrueExistence -> PartExistenceDTO.Positive(description = this.description)
        is PartExistenceParse.UnknownExistence -> PartExistenceDTO.Unknown(description = this.description)
    }

}


fun ProductBrandParse.toProductBrandDTO(): ProductBrandDTO {
    return when (this) {
        is ProductBrandParse.Kamaz -> ProductBrandDTO.Kamaz(name = this.name)
        is ProductBrandParse.Repair -> ProductBrandDTO.Repair(name = this.name)
        is ProductBrandParse.Unknown -> ProductBrandDTO.Unknown(name = this.name)
    }
}