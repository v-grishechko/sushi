package com.squareup.moshi

import com.github.vgrishechko.sushi.Sushi
import java.io.IOException
import java.lang.reflect.Type

internal class SafeMapJsonAdapter<K, V>(moshi: Moshi, keyType: Type, valueType: Type) : JsonAdapter<MutableMap<K, V>>() {

    private val keyAdapter: JsonAdapter<K> = moshi.adapter(keyType)
    private val valueAdapter: JsonAdapter<V> = moshi.adapter(valueType)

    @Throws(IOException::class)
    override fun toJson(writer: JsonWriter, map: MutableMap<K, V>?) {
        writer.beginObject()
        if (map != null) {
            for ((key, value) in map) {
                if (key == null) {
                    Sushi.throwError(JsonDataException("Map key is null at " + writer.path))
                }

                writer.promoteValueToName()
                keyAdapter.toJson(writer, key)
                valueAdapter.toJson(writer, value)
            }
        }
        writer.endObject()
    }

    @Throws(IOException::class)
    override fun fromJson(reader: JsonReader): MutableMap<K, V>? {
        val result = LinkedHashTreeMap<K, V>()
        reader.beginObject()
        while (reader.hasNext()) {
            reader.promoteNameToValue()
            val name = keyAdapter.fromJson(reader)
            val value = valueAdapter.fromJson(reader)

            var replaced: V? = null
            if (name != null && value != null) {
                replaced = result.put(name, value)
            }

            if (replaced != null) {
                Sushi.throwError(JsonDataException("Map key '" + name + "' has multiple values at path "
                        + reader.path + ": " + replaced + " and " + value))
            }
        }
        reader.endObject()
        return result
    }

    override fun toString(): String {
        return "JsonAdapter($keyAdapter=$valueAdapter)"
    }

    companion object {
        val FACTORY: Factory = Factory { type, annotations, moshi ->
            if (!annotations.isEmpty()) return@Factory null
            val rawType = Types.getRawType(type)
            if (rawType != Map::class.java) return@Factory null
            val keyAndValue = Types.mapKeyAndValueTypes(type, rawType)
            SafeMapJsonAdapter<Any, Any>(moshi, keyAndValue[0], keyAndValue[1]).nullSafe()
        }
    }
}
