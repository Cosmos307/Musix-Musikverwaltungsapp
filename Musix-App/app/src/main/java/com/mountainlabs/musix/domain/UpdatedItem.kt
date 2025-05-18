package com.mountainlabs.musix.domain

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class UpdatedItem(
    @Json(name = "type") val type: ItemType,
    @Json(name = "id") val id: String
)