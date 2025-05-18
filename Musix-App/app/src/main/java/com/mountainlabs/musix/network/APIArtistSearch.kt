package com.mountainlabs.musix.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class APIArtistSearch(
    @Json(name = "results") val results: ArtistSearchResults,
)

@JsonClass(generateAdapter = true)
data class ArtistSearchResults(
    @Json(name = "opensearch:Query") val query: ArtistSearchQuery,
    @Json(name = "opensearch:totalResults") val totalResults: String,
    @Json(name = "opensearch:startIndex") val startIndex: String,
    @Json(name = "opensearch:itemsPerPage") val itemsPerPage: String,
    @Json(name = "artistmatches") val artistmatches: ArtistMatches,
    @Json(name = "@attr") val attr: Attr
)

@JsonClass(generateAdapter = true)
data class ArtistMatches(
    @Json(name = "artist") val artist: List<ArtistMatch>
)

@JsonClass(generateAdapter = true)
data class ArtistMatch(
    @Json(name = "name") val name: String,
    @Json(name = "listeners") val listeners: String,
    @Json(name = "mbid") val mbid: String,
    @Json(name = "url") val url: String,
    @Json(name = "streamable") val streamable: String,
    @Json(name = "image") val image: List<Image>
)

@JsonClass(generateAdapter = true)
data class ArtistSearchQuery(
    @Json(name = "#text") val text: String,
    @Json(name = "role") val role: String,
    @Json(name = "searchTerms") val searchTerms: String,
    @Json(name = "startPage") val startPage: String
)

@JsonClass(generateAdapter = true)
data class Attr(
    @Json(name = "for") val forAttr: String
)
