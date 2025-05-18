package com.mountainlabs.musix.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)
data class APIAlbumInfo(
    @Json(name = "album") val album : APIAlbumInfoChild
)

@JsonClass(generateAdapter = true)
data class APIAlbumInfoChild(
    @Json(name = "artist") val artist: String,
    @Json(name = "mbid") val mbid: String?,
    @Json(name = "tags") val tags: TagsAlbumInfo,
    @Json(name = "playcount") val playcount: Int,
    @Json(name = "image") val image: List<Image>,
    @Json(name = "tracks") val tracks: Tracks,
    @Json(name = "url") val url: String,
    @Json(name = "name") val name: String,
    @Json(name = "listeners") val listeners: Int,
    @Json(name = "wiki") val wiki: Wiki?
)

@JsonClass(generateAdapter = true)
data class TagAlbumInfo(
    @Json(name = "url") val url: String,
    @Json(name = "name") val name: String
)

@JsonClass(generateAdapter = true)
data class Track(
    @Json(name = "streamable") val streamable: Streamable,
    @Json(name = "duration") val duration: Int,
    @Json(name = "url") val url: String,
    @Json(name = "name") val name: String,
    @Json(name = "@attr") val attr: TrackAttribute,
    @Json(name = "artist") val artist: Artist
)

@JsonClass(generateAdapter = true)
data class Streamable(
    @Json(name = "fulltrack") val fullTrack: String,
    @Json(name = "#text") val text: String
)

@JsonClass(generateAdapter = true)
data class TrackAttribute(
    @Json(name = "rank") val rank: Int      //position in album
)

@JsonClass(generateAdapter = true)
data class Artist(
    @Json(name = "url") val url: String,
    @Json(name = "name") val name: String,
    @Json(name = "mbid") val mbid: String?
)

@JsonClass(generateAdapter = true)
data class TagsAlbumInfo(@Json(name = "tag") val tag: List<TagAlbumInfo>)

@JsonClass(generateAdapter = true)
data class Tracks(@Json(name = "track") val track: List<Track>)

@JsonClass(generateAdapter = true)
data class Wiki(
    @Json(name = "published") val published: String,
    @Json(name = "summary") val summary: String,
    @Json(name = "content") val content: String
)
