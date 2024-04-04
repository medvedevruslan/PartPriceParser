package com.medvedev.parts_domain.usecases

import com.medvedev.parts_domain.PartsRepository
import com.medvedev.parts_domain.models.PartsDataDTO
import com.medvedev.parts_domain.utils.toPartsDataDTO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetPartsDataUseCase(private val partsRepository: PartsRepository) {

    suspend fun getPartsData(article: String): Flow<PartsDataDTO> {
        return partsRepository.getPartsData(article).map { partsDataParse ->
            partsDataParse.toPartsDataDTO()
        }
    }

}