package com.medvedev.partsparser.models

sealed interface PartExistenceParse {
    val description: String
    val possibleDescription: ArrayList<String>

    class TrueExistence(override val description: String = "В наличии") : PartExistenceParse {
        override val possibleDescription: ArrayList<String> =
            arrayListOf(
                "в наличии",
                "мало в наличии",
                "есть в наличии",
                "в наличии:",
                "в наличии много",
            )
    }

    class FalseExistenceParse(override val description: String = "Отсутствует") : PartExistenceParse {
        override val possibleDescription: ArrayList<String> =
            arrayListOf("нет в наличии", "под заказ")
    }

    class UnknownExistence(override val description: String = "наличие неизвестно") :
        PartExistenceParse {
        override val possibleDescription: ArrayList<String> = arrayListOf("наличие уточняйте")
    }
}

val String.getExistence
    get() = this.trim().lowercase().let { lowerExistence ->
        when {
            PartExistenceParse.FalseExistenceParse().possibleDescription.contains(lowerExistence) ->
                PartExistenceParse.FalseExistenceParse(description = this)

            PartExistenceParse.TrueExistence().possibleDescription.contains(lowerExistence) ->
                PartExistenceParse.TrueExistence(description = this)

            else -> PartExistenceParse.UnknownExistence(description = this)
        }
    }