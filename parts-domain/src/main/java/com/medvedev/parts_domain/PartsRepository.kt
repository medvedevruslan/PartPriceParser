package com.medvedev.parts_domain

import com.medvedev.partsparser.PartsParser
import com.medvedev.partsparser.models.PartsDataParse
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

class PartsRepository @Inject constructor(
    private val parser: PartsParser
) {

    suspend fun getPartsData(article: String): Flow<PartsDataParse> {
        return parser.getPartsData(article = article)
    }
}