package com.medvedev.partsparser.models

sealed interface ProductBrandParse {
    val name: String
    val possibleNames: ArrayList<String>

    class Kamaz(override val name: String = "ПАО \"КАМАЗ\"") : ProductBrandParse {

        override val possibleNames: ArrayList<String> = arrayListOf(
            "пао",
            "камаз",
            "пао \"камаз\"",
            "ПАО КАМАЗ",
            "пао камаз",
            "паокамаз",
            "ПАОКАМАЗ"
        )
    }

    class Repair(override val name: String = "Ремонтный") : ProductBrandParse {
        override val possibleNames: ArrayList<String> = arrayListOf(
            "ремонтный",
            "ремонтированный",
            "восcтановленный",
            "ремонт под новый с гарантией",
            "ремонт с гарантией"
        )
    }

    class Unknown(override val name: String = "Неопределен") : ProductBrandParse {
        override val possibleNames: ArrayList<String> = arrayListOf("Прочие")
    }
}


// todo результат поиска брендов по названию: "головка". какие и стоит ли делать так много сортировок по производителям(брендам)?
// БелЗАН \ SORL \ CUMMINS \ РААЗ АМО ЗиЛ \ Литиз г. Набережные Челны \ БелЗАН г. Белебей \ РААЗ АМО ЗиЛ \ БелОМО \
// НЕФАЗ г. Нефтекамск \ КМД \ г. Ярославль \ Mercedes Benz \ Россия \ Китай \ OE Germany \ ПК АЙК \ МАРК \ FG (Китай) \
// MADARA \ АО "Строймаш" \ ОАО "Балаковорезинотехника" \ "SORL Ruili Group China" \ ГК Ростар \ ООО "ЕНА-холдинг" \
// TECNARI \ ПАО "НЕФАЗ" \   "ПК АЙК" \ "ZF", Германия \ ОАО "Шадринский автоагрегатный завод" \ ВЧ РУС \
// ООО "Рославльские тормозные системы" РААЗ \ ООО НПО "УРАЛ" \


val String.getBrand: ProductBrandParse
    get() = this.trim().lowercase().let { lowerString ->
        when {
            ProductBrandParse.Kamaz().possibleNames.contains(lowerString) ||
                    lowerString.contains("камаз") -> ProductBrandParse.Kamaz(name = this)

            ProductBrandParse.Repair().possibleNames.contains(lowerString) -> ProductBrandParse.Repair(name = this)
            else -> ProductBrandParse.Unknown(name = this)
        }
    }

