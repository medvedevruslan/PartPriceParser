package com.medvedev.parts_domain.models
import com.medvedev.parts_domain.utils.ResourceDTO

data class PartsDataDTO(
    val linkToSearchCatalog: String = "",
    val linkToSite: String = "",
    val siteName: String = "",
    val partsResultDTO: ResourceDTO<Set<ProductCartDTO>> = ResourceDTO.Success(data = setOf())
)
