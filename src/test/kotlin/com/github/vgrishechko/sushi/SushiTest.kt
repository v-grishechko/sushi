package com.github.vgrishechko.sushi

import com.github.vgrishechko.sushi.data.Simple
import com.github.vgrishechko.sushi.data.TestErrorHandlingData
import com.squareup.moshi.Moshi
import com.squareup.moshi.SafeCollectionJsonAdapter
import com.squareup.moshi.SafeMapJsonAdapter
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe

object SushiTest : Spek({

    val moshi = Moshi.Builder()
            .add(Sushi.FACTORY)
            .add(SafeCollectionJsonAdapter.FACTORY)
            .add(SafeMapJsonAdapter.FACTORY)
            .build()


    class StubErrorHandlingData : ErrorHandler {
        val errors: MutableList<Throwable> = ArrayList()

        override fun handleError(throwable: Throwable) {
            errors.add(throwable)
        }
    }

    lateinit var errorHandler: StubErrorHandlingData


    Sushi.isStrict = true
    errorHandler = StubErrorHandlingData()
    Sushi.registerErrorHandler(errorHandler)

    describe("safely parse json") {

        describe("with incorrect type of field") {
            errorHandler.errors.clear()

            val json = """{
                "string": "test",
                "nullableNumber": "number"
            }
            """

            val data = moshi.adapter(TestErrorHandlingData::class.java).fromJson(json)

            val expected = TestErrorHandlingData("test", nullableNumber = null)

            it("should throw exception") {
                assertThat(errorHandler.errors[0]).isInstanceOf(Exception::class.java)
            }

            it("return null") {
                assertThat(data).isEqualTo(expected)
            }
        }

        describe("with nonnulable field") {
            errorHandler.errors.clear()

            val json = """{
                "string": null
            }
            """.trimIndent()

            val data = moshi.adapter(TestErrorHandlingData::class.java).fromJson(json)

            it("should throw exception") {
                assertThat(errorHandler.errors[0]).isInstanceOf(Exception::class.java)
            }

            it("return null") {
                assertThat(data).isNull()
            }
        }

        describe("with incorrect element in list") {
            errorHandler.errors.clear()

            val json = """{
                "string": "test",
                "list": [
                    {"prop":"test"},
                    {"prop": null },
                    {"prop": "test"}
                ],
                "map": {"key": {"prop": "test"}}
            }
            """.trimIndent()

            val data = moshi.adapter(TestErrorHandlingData::class.java).fromJson(json)

            val expected = TestErrorHandlingData(
                    "test",
                    list = listOf(Simple("test"), Simple("test")),
                    map = mapOf("key" to Simple("test")))

            it("should throw exception") {
                assertThat(errorHandler.errors[0]).isInstanceOf(Exception::class.java)
            }

            it("filter nullable elements in list") {
                assertThat(data).isEqualTo(expected)
            }
        }

        describe("with incorrect element in map") {
            errorHandler.errors.clear()

            val json = """{
                "string": "test",
                "list": [
                    {"prop":"test"},
                    {"prop": null },
                    {"prop": "test"}
                ],
                "map": {
                    "key": {"prop": "test"},
                    "key2": {"prop": null},
                    "key3": {"prop": "test"}
                }
            }
            """.trimIndent()

            val data = moshi.adapter(TestErrorHandlingData::class.java).fromJson(json)

            val expected = TestErrorHandlingData(
                    "test",
                    list = listOf(Simple("test"), Simple("test")),
                    map = mapOf("key" to Simple("test"), "key3" to Simple("test"))
            )

            it("should throw exception") {
                assertThat(errorHandler.errors[0]).isInstanceOf(Exception::class.java)
            }

            it("filter nullable elements in map") {
                assertThat(data).isEqualTo(expected)
            }
        }

        describe("when json corrupted") {

            val json = """{
                "string": "test",
                "list": [
                    {"prop":"test"},
                    {"prop": null },
                    {"prop": "test"}
                ], }}

                "map": {
                    "key": {"prop": "test"},
                    "key2": {"prop": null},
                    "key3": {"prop": "test"}
                }
                }
            }
            """.trimIndent()

            describe("strict mode enabled") {
                errorHandler.errors.clear()

                it("should throw exception") {
                    Sushi.isStrict = true

                    assertThatThrownBy {
                        moshi.adapter(TestErrorHandlingData::class.java).fromJson(json)
                    }
                }
            }

            describe("strict mode disabled") {
                errorHandler.errors.clear()

                Sushi.isStrict = false

                val data = moshi.adapter(TestErrorHandlingData::class.java).fromJson(json)

                it("should throw exception") {
                    assertThat(errorHandler.errors[0]).isInstanceOf(Exception::class.java)
                }

                it("return null") {
                    assertThat(data).isNull()
                }
            }
        }
    }
})