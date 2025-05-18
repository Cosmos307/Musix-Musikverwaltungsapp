package com.mountainlabs.musix.domain

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class AlbumInfo(
    @Json(name = "type") val type: ItemType = ItemType.ALBUM,
    @Json(name = "id") val id: String = "",                //Will be set when saving it in Azure
    @Json(name = "mbid") val MBID: String,                   //Can be an empty string
    @Json(name = "name") val Name: String,
    @Json(name = "artistName") val ArtistName: String,
    @Json(name = "artistID") val ArtistID: String = "",
    @Json(name = "releaseDate") val ReleaseDate: String,
    @Json(name = "imageURL") val ImageURL: String,
    @Json(name = "playcount") val Playcount: Int,
    @Json(name = "listeners") val Listeners: Int,
    @Json(name = "tags") val Tags: List<String>,
    @Json(name = "tracks") val Tracks: List<Track>,
    @Json(name = "descriptionSummary") val DescriptionSummary: String,     //Can be an empty string
    @Json(name = "descriptionLong") val DescriptionLong: String,        //Can be an empty string
    @Json(name = "descriptionDate") val DescriptionDate: String,        //Can be an empty string
)