package com.medvedev.parts.main.presentation.models.filter

sealed interface PartExistence {
    val description: String
    class Positive(override val description: String = "В наличии") : PartExistence
    class Negative(override val description: String = "Отсутствует") : PartExistence
    class Unknown(override val description: String = "наличие неизвестно") : PartExistence
}