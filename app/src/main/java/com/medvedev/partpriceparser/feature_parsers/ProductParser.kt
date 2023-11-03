package com.medvedev.partpriceparser.feature_parsers

import com.medvedev.partpriceparser.core.util.Resource
import com.medvedev.partpriceparser.presentation.models.ProductCart
import io.sentry.Sentry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

abstract class ProductParser {

    abstract val linkToSite: String
    abstract val siteName: String
    abstract val partOfLinkToCatalog: (String) -> String

    protected val productList: MutableList<ProductCart> = mutableListOf()

    suspend fun getProduct(articleToSearch: String): Flow<Resource<List<ProductCart>>> = flow {
        try {
            if (productList.size > 0) productList.clear()
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
    protected abstract val workWithServer: (String) -> Flow<Resource<List<ProductCart>>>
}