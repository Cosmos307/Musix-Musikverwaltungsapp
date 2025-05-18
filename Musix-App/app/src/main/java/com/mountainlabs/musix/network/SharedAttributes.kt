package com.mountainlabs.musix.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Image(
    @Json(name = "#text") val url: String,
    @Json(name = "size") val size: String
)