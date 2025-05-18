package com.mountainlabs.musix.domain

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Track(
    @Json(name = "name") val name: String,
    @Json(name = "trackID") val trackID: String = "",
    @Json(name = "duration") val duration: Int,      //In seconds
    @Json(name = "artistName") val artistName: String,
    @Json(name = "artistID") val artistID: String = ""
)