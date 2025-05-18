package com.mountainlabs.musix.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class APITrackInfo(
    @Json(name = "track") val track: APITrackInfoChild

    //TODO: also accept missing route
)

@JsonClass(generateAdapter = true)
data class APITrackInfoChild(
    @Json(name = "name") val name: String,
    @Json(name = "mbid") val mbid: String?,
    @Json(name = "url") val url: String,
    @Json(name = "duration") val duration: String,
    @Json(name = "streamable") val streamable: StreamableTrackInfo,
    @Json(name = "listeners") val listeners: Int,
    @Json(name = "playcount") val playcount: Int,
    @Json(name = "artist") val artist: ArtistTrackInfo,
    @Json(name = "album") val album: AlbumTrackInfo?,
    @Json(name = "toptags") val toptags: Toptags,
    @Json(name = "wiki") val wiki: WikiTrackInfo?
)

@JsonClass(generateAdapter = true)
data class StreamableTrackInfo(
    @Json(name = "#text") val text: String,
    @Json(name = "fulltrack") val fulltrack: String
)

@JsonClass(generateAdapter = true)
data class ArtistTrackInfo(
    @Json(name = "name") val name: String,
    @Json(name = "mbid") val mbid: String?,
    @Json(name = "url") val url: String
)

@JsonClass(generateAdapter = true)
data class AlbumTrackInfo(
    @Json(name = "artist") val artist: String,
    @Json(name = "title") val title: String,
    @Json(name = "mbid") val mbid: String?,
    @Json(name = "url") val url: String,
    @Json(name = "image") val image: List<Image>,
    @Json(name = "@attr") val attr: AttrTrackInfo?
)

@JsonClass(generateAdapter = true)
data class AttrTrackInfo(
    @Json(name = "position") val position: String
)

@JsonClass(generateAdapter = true)
data class Toptags(
    @Json(name = "tag") val tag: List<TagTrackInfo>
)

@JsonClass(generateAdapter = true)
data class TagTrackInfo(
    @Json(name = "name") val name: String,
    @Json(name = "url") val url: String
)

@JsonClass(generateAdapter = true)
data class WikiTrackInfo(
    @Json(name = "published") val published: String,
    @Json(name = "summary") val summary: String,
    @Json(name = "content") val content: String
)
