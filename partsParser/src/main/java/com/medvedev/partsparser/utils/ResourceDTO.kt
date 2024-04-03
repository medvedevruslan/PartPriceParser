package com.medvedev.partsparser.utils

sealed class ResourceDTO<T>(val data: T? = null, val message: String? = null) {
    class Loading<T>(data: T? = null) : ResourceDTO<T>(data = data)
    class Success<T>(data: T?) : ResourceDTO<T>(data = data)
    class Error<T>(message: String, data: T? = null) : ResourceDTO<T>(data = data, message = message)
}


