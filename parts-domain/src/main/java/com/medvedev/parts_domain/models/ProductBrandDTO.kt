package com.medvedev.parts_domain.models

sealed interface ProductBrandDTO {
    val name: String

    class Kamaz(override val name: String = "ПАО \"КАМАЗ\"") : ProductBrandDTO
    class Repair(override val name: String = "Ремонтный") : ProductBrandDTO
    class Unknown(override val name: String = "Неопределен") : ProductBrandDTO
}