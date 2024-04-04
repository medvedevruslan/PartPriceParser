package com.medvedev.partsparser.utils

sealed class ResourceParse<T>(val data: T? = null, val message: String? = null) {
    class Loading<T>(data: T? = null) : ResourceParse<T>(data = data)
    class Success<T>(data: T?) : ResourceParse<T>(data = data)
    class Error<T>(message: String, data: T? = null) : ResourceParse<T>(data = data, message = message)
}