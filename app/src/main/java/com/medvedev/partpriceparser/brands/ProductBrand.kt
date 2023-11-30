package com.medvedev.partpriceparser.brands

sealed interface ProductBrand {
    val name: String
    val possibleNames: ArrayList<String>

    class Kamaz(override val name: String = "ПАО \"КАМАЗ\"") : ProductBrand {

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

    class Repair(override val name: String = "Ремонтный") : ProductBrand {
        override val possibleNames: ArrayList<String> = arrayListOf(
            "ремонтный",
            "ремонтированный",
            "восcтановленный",
            "ремонт под новый с гарантией",
            "ремонт с гарантией"
        )
    }

    class Unknown(override val name: String = "Неопределен") : ProductBrand {
        override val possibleNames: ArrayList<String> = arrayListOf("Прочие")
    }
}


// todo результат поиска брендов по названию: "головка". какие и стоит ли делать так много сортировок по производителям(брендам)?
// БелЗАН \ SORL \ CUMMINS \ РААЗ АМО ЗиЛ \ Литиз г. Набережные Челны \ БелЗАН г. Белебей \ РААЗ АМО ЗиЛ \ БелОМО \
// НЕФАЗ г. Нефтекамск \ КМД \ г. Ярославль \ Mercedes Benz \ Россия \ Китай \ OE Germany \ ПК АЙК \ МАРК \ FG (Китай) \
// MADARA \ АО "Строймаш" \ ОАО "Балаковорезинотехника" \ "SORL Ruili Group China" \ ГК Ростар \ ООО "ЕНА-холдинг" \
// TECNARI \ ПАО "НЕФАЗ" \   "ПК АЙК" \ "ZF", Германия \ ОАО "Шадринский автоагрегатный завод" \ ВЧ РУС \
// ООО "Рославльские тормозные системы" РААЗ \ ООО НПО "УРАЛ" \


val String.getBrand: ProductBrand
    get() = this.trim().lowercase().let { lowerString ->
        when {
            ProductBrand.Kamaz().possibleNames.contains(lowerString) ||
                    lowerString.contains("камаз") -> ProductBrand.Kamaz(name = this)

            ProductBrand.Repair().possibleNames.contains(lowerString) -> ProductBrand.Repair(name = this)
            else -> ProductBrand.Unknown(name = this)
        }
    }

