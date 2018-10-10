package com.squareup.moshi

import com.squareup.moshi.JsonReader
import com.squareup.moshi.skipToStackSize
import okio.Buffer
import org.assertj.core.api.Assertions.assertThat
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object JsonReaderTest : Spek({

    fun String.toJsonReader(): JsonReader {
        return JsonReader.of(Buffer().writeUtf8(this))
    }

    lateinit var jsonReader: JsonReader

    describe("json reader") {

        describe("skip to stack size") {

            describe("deep level of stack size") {


                jsonReader = """{
                    "prop1": {
                        "prop2": {
                            "prop3": {
                                "prop4": [
                                    {"elem_prop": "elem_prop"},
                                    {"elem_prop2": "elem_prop2"}
                                ]
                            }
                        }
                    }
                }
                 """.toJsonReader()

                for (step in 1..4) {
                    jsonReader.beginObject()
                    jsonReader.nextName()
                }

                jsonReader.beginArray()
                jsonReader.beginObject()

                jsonReader.skipToStackSize(2)

                it("stack size skipped") {
                    assertThat(jsonReader.stackSize).isEqualTo(2)
                }
            }

            describe("same level of stack size") {

                jsonReader = """{
                    "string": "name",
                    "number": 2,
                    "boolean": true
                }
                """.toJsonReader()

                jsonReader.beginObject()

                jsonReader.nextName()
                jsonReader.nextString()

                jsonReader.nextName()

                jsonReader.skipToStackSize(2)

                val name = jsonReader.nextName()
                val value = jsonReader.nextBoolean()
                it("stack size skipped") {
                    assertThat(name).isEqualTo("boolean")
                    assertThat(value).isEqualTo(true)
                }
            }
        }
    }
})