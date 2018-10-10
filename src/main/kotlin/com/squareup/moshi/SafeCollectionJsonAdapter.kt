package com.squareup.moshi

import com.github.vgrishechko.sushi.Sushi
import java.io.IOException
import java.lang.reflect.Type

internal abstract class SafeCollectionJsonAdapter<C : MutableCollection<T>, T> private constructor(private val elementAdapter: JsonAdapter<T>) : JsonAdapter<C>() {

    internal abstract fun newCollection(): C

    @Throws(IOException::class)
    override fun fromJson(reader: JsonReader): C? {
        val result = newCollection()
        reader.beginArray()
        while (reader.hasNext()) {
            var value: T?
            try {
                reader.peek()
                value = elementAdapter.fromJson(reader)
            } catch (ex: Exception) {
                Sushi.throwError(ex)
                continue
            }

            if (value != null) {
                result.add(value)
            }
        }
        reader.endArray()
        return result
    }

    @Throws(IOException::class)
    override fun toJson(writer: JsonWriter, value: C?) {
        writer.beginArray()
        if (value != null) {
            for (element in value) {
                elementAdapter.toJson(writer, element)
            }
        }
        writer.endArray()
    }

    override fun toString(): String {
        return elementAdapter.toString() + ".collection()"
    }

    companion object {
        val FACTORY: Factory = Factory { type, annotations, moshi ->
            val rawType = Types.getRawType(type)
            if (!annotations.isEmpty()) return@Factory null
            if (rawType == List::class.java || rawType == Collection::class.java) {
                return@Factory newArrayListAdapter<Any>(type, moshi).nullSafe()
            } else if (rawType == Set::class.java) {
                return@Factory newLinkedHashSetAdapter<Any>(type, moshi).nullSafe()
            }
            null
        }

        private fun <T> newArrayListAdapter(type: Type, moshi: Moshi): JsonAdapter<MutableCollection<T>> {
            val elementType = Types.collectionElementType(type, Collection::class.java)
            val elementAdapter = moshi.adapter<T>(elementType)
            return object : SafeCollectionJsonAdapter<MutableCollection<T>, T>(elementAdapter) {
                override fun newCollection(): MutableCollection<T> {
                    return ArrayList()
                }
            }
        }

        private fun <T> newLinkedHashSetAdapter(type: Type, moshi: Moshi): JsonAdapter<MutableSet<T>> {
            val elementType = Types.collectionElementType(type, Collection::class.java)
            val elementAdapter = moshi.adapter<T>(elementType)
            return object : SafeCollectionJsonAdapter<MutableSet<T>, T>(elementAdapter) {
                override fun newCollection(): MutableSet<T> {
                    return LinkedHashSet()
                }
            }
        }
    }
}