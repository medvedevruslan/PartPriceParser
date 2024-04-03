package com.medvedev.partsparser.sources


import com.medvedev.partsparser.models.ReceivedProductData
import com.medvedev.partsparser.utils.Resource
import io.sentry.Sentry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

abstract class ProductParser {

    abstract val linkToSite: String
    abstract val siteName: String
    abstract val partOfLinkToCatalog: (String) -> String


    suspend fun getProduct(articleToSearch: String): Flow<Resource<Set<ReceivedProductData>>> = flow {
        try {
            emit(Resource.Loading())
            workWithServer(articleToSearch).collect {
                emit(it)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Sentry.captureException(e)
            emit(Resource.Error(e.toString()))
        }
    }
    protected abstract val workWithServer: (String) -> Flow<Resource<Set<ReceivedProductData>>>
}