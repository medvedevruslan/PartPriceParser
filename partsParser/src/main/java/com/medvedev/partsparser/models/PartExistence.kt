package com.medvedev.partsparser.models

sealed interface PartExistence {
    val description: String
    val possibleDescription: ArrayList<String>

    class TrueExistence(override val description: String = "В наличии") : PartExistence {
        override val possibleDescription: ArrayList<String> =
            arrayListOf(
                "в наличии",
                "мало в наличии",
                "есть в наличии",
                "в наличии:",
                "в наличии много",
            )
    }

    class FalseExistence(override val description: String = "Отсутствует") : PartExistence {
        override val possibleDescription: ArrayList<String> =
            arrayListOf("нет в наличии", "под заказ")
    }

    class UnknownExistence(override val description: String = "наличие неизвестно") :
        PartExistence {
        override val possibleDescription: ArrayList<String> = arrayListOf("наличие уточняйте")
    }
}

val String.getExistence
    get() = this.trim().lowercase().let { lowerExistence ->
        when {
            PartExistence.FalseExistence().possibleDescription.contains(lowerExistence) ->
                PartExistence.FalseExistence(description = this)

            PartExistence.TrueExistence().possibleDescription.contains(lowerExistence) ->
                PartExistence.TrueExistence(description = this)

            else -> PartExistence.UnknownExistence(description = this)
        }
    }