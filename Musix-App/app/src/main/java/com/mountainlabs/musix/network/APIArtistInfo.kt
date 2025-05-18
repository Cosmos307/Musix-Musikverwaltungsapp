package com.mountainlabs.musix.network

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class APIArtistInfo(
    @Json(name = "artist") val artist: APIArtistInfoChild
)

@JsonClass(generateAdapter = true)
data class APIArtistInfoChild(
    @Json(name = "name") val name: String,
    @Json(name = "mbid") val mbid: String?,
    @Json(name = "url") val url: String,
    @Json(name = "image") val image: List<Image>,
    @Json(name = "streamable") val streamable: String,
    @Json(name = "ontour") val ontour: String?,
    @Json(name = "stats") val stats: Stats,
    @Json(name = "similar") val similar: Similar?,
    @Json(name = "tags") val tags: Tags,
    @Json(name = "bio") val bio: Bio?
)


@JsonClass(generateAdapter = true)
data class Similar(
    @Json(name = "artist") val similarArtist: List<SimilarArtist>?
)

@JsonClass(generateAdapter = true)
data class SimilarArtist(
    @Json(name = "name") val name: String,
    @Json(name = "image") val image: List<Image>
)

@JsonClass(generateAdapter = true)
data class Stats(
    @Json(name = "listeners") val listeners: Int,
    @Json(name = "playcount") val playcount: Int
)

@JsonClass(generateAdapter = true)
data class Tags(
    @Json(name = "tag") val tag: List<Tag>
)

@JsonClass(generateAdapter = true)
data class Tag(
    @Json(name = "name") val name: String,
    @Json(name = "url") val url: String
)

@JsonClass(generateAdapter = true)
data class Link(
    @Json(name = "#text") val text: String,
    @Json(name = "rel") val rel: String,
    @Json(name = "href") val href: String
)

@JsonClass(generateAdapter = true)
data class Links(
    @Json(name = "link") val link: Link
)

@JsonClass(generateAdapter = true)
data class Bio(
    @Json(name = "links") val links: Links,
    @Json(name = "published") val published: String,
    @Json(name = "summary") val summary: String,
    @Json(name = "content") val content: String
)
