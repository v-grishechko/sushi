package com.squareup.moshi

fun JsonReader.skipToStackSize(stackSize: Int) {
    val currentStackSize = this.stackSize

    if (currentStackSize < stackSize) {
        return
    }

    while (stackSize < this.stackSize) {
        if (peek() == JsonReader.Token.END_DOCUMENT) {
            break
        }

        when (peek()) {
            JsonReader.Token.END_OBJECT -> endObject()
            JsonReader.Token.END_ARRAY -> endArray()
            JsonReader.Token.NAME -> skipName()
            else -> skipValue()
        }
    }

    if (peek().isPrimitiveValue()) {
        skipValue()
    }
}


fun JsonReader.Token.isPrimitiveValue(): Boolean {
    return this == JsonReader.Token.STRING ||
            this == JsonReader.Token.BOOLEAN ||
            this == JsonReader.Token.NULL ||
            this == JsonReader.Token.NUMBER
}