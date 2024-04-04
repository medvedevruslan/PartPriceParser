package com.medvedev.parts_domain.models

sealed interface PartExistenceDTO {
    val description: String
    class Positive(override val description: String = "В наличии") : PartExistenceDTO
    class Negative(override val description: String = "Отсутствует") : PartExistenceDTO
    class Unknown(override val description: String = "наличие неизвестно") : PartExistenceDTO
}