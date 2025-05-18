package com.mountainlabs.musix.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class APICollectionItem(
    @Json(name = "isbn") val isbn: String,
    @Json(name = "author") val author: String,
    @Json(name = "title") val title: String,
    //@Json(name = "publishYear") val publishYear: Int
)


