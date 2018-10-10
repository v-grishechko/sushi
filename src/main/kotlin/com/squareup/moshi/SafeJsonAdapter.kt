package com.squareup.moshi

import com.github.vgrishechko.sushi.Sushi

internal class SafeJsonAdapter<T>(private val jsonAdapter: JsonAdapter<T>) : JsonAdapter<T>() {

    override fun fromJson(reader: JsonReader): T? {
        val stackSize = reader.stackSize
        return try {
            jsonAdapter.fromJson(reader)
        } catch (throwable: Throwable) {
            try {
                reader.skipToStackSize(stackSize)
                Sushi.throwError(throwable)
                null
            } catch (skipToStackSizeThrowable: Throwable) {
                Sushi.throwError(throwable)
                null
            }
        }
    }

    override fun isLenient(): Boolean {
        return !Sushi.isStrict
    }

    override fun toJson(writer: JsonWriter, value: T?) {
        jsonAdapter.toJson(writer, value)
    }
}