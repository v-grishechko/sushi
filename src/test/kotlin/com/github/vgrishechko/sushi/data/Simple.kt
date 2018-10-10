package com.github.vgrishechko.sushi.data

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Simple(val prop: String)