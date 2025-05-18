package com.mountainlabs.musix.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class APIAlbumSearch(
    @Json(name = "results") val results: AlbumSearchResults,
    //@Json(name = "@attr")
    //val attr: AttrAlbumSearch
)

@JsonClass(generateAdapter = true)
data class AlbumSearchResults(
    @Json(name = "opensearch:Query")
    val query: AlbumSearchQuery,
    @Json(name = "opensearch:totalResults")
    val totalResults: String,
    @Json(name = "opensearch:startIndex")
    val startIndex: String,
    @Json(name = "opensearch:itemsPerPage")
    val itemsPerPage: String,
    @Json(name = "albummatches")
    val albumMatches: AlbumMatches
)

@JsonClass(generateAdapter = true)
data class AlbumSearchQuery(
    @Json(name = "#text")
    val text: String,
    @Json(name = "role")
    val role: String,
    @Json(name = "searchTerms")
    val searchTerms: String,
    @Json(name = "startPage")
    val startPage: String
)

@JsonClass(generateAdapter = true)
data class AlbumMatches(
    @Json(name = "album")
    val album: List<Album>
)

@JsonClass(generateAdapter = true)
data class Album(
    @Json(name = "name")
    val name: String,
    @Json(name = "artist")
    val artist: String,
    @Json(name = "url")
    val url: String,
    @Json(name = "image")
    val image: List<Image>,
    @Json(name = "streamable")
    val streamable: String,
    @Json(name = "mbid")
    val mbid: String
)

@JsonClass(generateAdapter = true)
data class AttrAlbumSearch(
    @Json(name = "for")
    val forAttr: String
)
