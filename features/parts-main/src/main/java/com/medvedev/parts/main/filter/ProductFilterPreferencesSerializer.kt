package com.medvedev.parts.main.filter

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.google.protobuf.InvalidProtocolBufferException
import com.medvedev.parts.main.ProductFilterPreferences
import java.io.InputStream
import java.io.OutputStream

object ProductFilterPreferencesSerializer : Serializer<ProductFilterPreferences> {

    override val defaultValue: ProductFilterPreferences =
        ProductFilterPreferences.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): ProductFilterPreferences {
        try {
            return ProductFilterPreferences.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto", exception)
        }
    }

    override suspend fun writeTo(t: ProductFilterPreferences, output: OutputStream) =
        t.writeTo(output)
}
