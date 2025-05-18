package com.mountainlabs.musix.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class APITrackSearch(
    @Json(name = "results") val results: TrackSearchResults
)

@JsonClass(generateAdapter = true)
data class TrackSearchResults(
    @Json(name = "opensearch:Query")
    val openSearchQuery: OpenSearchQuery,

    @Json(name = "opensearch:totalResults")
    val totalResults: String,

    @Json(name = "opensearch:startIndex")
    val startIndex: String,

    @Json(name = "opensearch:itemsPerPage")
    val itemsPerPage: String,

    @Json(name = "trackmatches")
    val trackMatches: TrackMatches,
)

@JsonClass(generateAdapter = true)
data class OpenSearchQuery(
    @Json(name = "#text")
    val text: String,

    @Json(name = "role")
    val role: String,

    @Json(name = "startPage")
    val startPage: String
)

@JsonClass(generateAdapter = true)
data class TrackMatches(
    @Json(name = "track")
    val track: List<TrackSearchTrack>
)

@JsonClass(generateAdapter = true)
data class TrackSearchTrack(
    @Json(name = "name")
    val name: String,

    @Json(name = "artist")
    val artist: String,

    @Json(name = "url")
    val url: String,

    @Json(name = "streamable")
    val streamable: String,

    @Json(name = "listeners")
    val listeners: String,

    @Json(name = "image")
    val image: List<Image>,

    @Json(name = "mbid")
    val mbid: String
)
