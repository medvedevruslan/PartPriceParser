package com.medvedev.parts.main.presentation.models

sealed interface ProductBrand {
    val name: String

    class Kamaz(override val name: String = "ПАО \"КАМАЗ\"") : ProductBrand
    class Repair(override val name: String = "Ремонтный") : ProductBrand
    class Unknown(override val name: String = "Неопределен") : ProductBrand
}