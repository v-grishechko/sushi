package com.github.vgrishechko.sushi.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TestErrorHandlingData(val string: String,
                                 val nullableString: String? = null,
                                 val nullableNumber: Int? = null,
                                 val list: List<Simple>? = null,
                                 val map: Map<String, Simple>? = null)