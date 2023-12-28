package com.medvedev.partpriceparser.feature_parsers.presentation.models.filter

sealed interface ProductExistence {
    val description: String
    val possibleDescription: ArrayList<String>

    class TrueExistence(override val description: String = "В наличии") : ProductExistence {
        override val possibleDescription: ArrayList<String> =
            arrayListOf(
                "в наличии",
                "мало в наличии",
                "есть в наличии",
                "в наличии:",
                "в наличии много",
            )
    }

    class FalseExistence(override val description: String = "Отсутствует") : ProductExistence {
        override val possibleDescription: ArrayList<String> =
            arrayListOf("нет в наличии", "под заказ")
    }

    class UnknownExistence(override val description: String = "наличие неизвестно") : ProductExistence {
        override val possibleDescription: ArrayList<String> = arrayListOf("наличие уточняйте")
    }
}

val String.getExistence
    get() = this.trim().lowercase().let { lowerExistence ->
        when {
            ProductExistence.FalseExistence().possibleDescription.contains(lowerExistence) ->
                ProductExistence.FalseExistence(description = this)

            ProductExistence.TrueExistence().possibleDescription.contains(lowerExistence) ->
                ProductExistence.TrueExistence(description = this)

            else -> ProductExistence.UnknownExistence(description = this)
        }
    }