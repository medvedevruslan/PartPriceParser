package com.medvedev.partsparser.models

sealed interface PartExistenceDTO {
    val description: String
    val possibleDescription: ArrayList<String>

    class TrueExistence(override val description: String = "В наличии") : PartExistenceDTO {
        override val possibleDescription: ArrayList<String> =
            arrayListOf(
                "в наличии",
                "мало в наличии",
                "есть в наличии",
                "в наличии:",
                "в наличии много",
            )
    }

    class FalseExistenceDTO(override val description: String = "Отсутствует") : PartExistenceDTO {
        override val possibleDescription: ArrayList<String> =
            arrayListOf("нет в наличии", "под заказ")
    }

    class UnknownExistence(override val description: String = "наличие неизвестно") :
        PartExistenceDTO {
        override val possibleDescription: ArrayList<String> = arrayListOf("наличие уточняйте")
    }
}

val String.getExistence
    get() = this.trim().lowercase().let { lowerExistence ->
        when {
            PartExistenceDTO.FalseExistenceDTO().possibleDescription.contains(lowerExistence) ->
                PartExistenceDTO.FalseExistenceDTO(description = this)

            PartExistenceDTO.TrueExistence().possibleDescription.contains(lowerExistence) ->
                PartExistenceDTO.TrueExistence(description = this)

            else -> PartExistenceDTO.UnknownExistence(description = this)
        }
    }