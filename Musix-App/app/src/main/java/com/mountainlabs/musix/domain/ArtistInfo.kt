package com.mountainlabs.musix.domain

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class ArtistInfo(
    @Json(name = "type") var type: ItemType = ItemType.ARTIST,
    @Json(name = "id") var id: String = "",                        //Will be set when saving it in Azure
    @Json(name = "name") var Name: String,
    @Json(name = "mbid") var MBID: String = "",                           //Can be an empty string
    @Json(name = "descriptionSummary") var DescriptionSummary: String = "",             //Can be an empty string
    @Json(name = "descriptionLong") var DescriptionLong: String = "",                //Can be an empty string
    @Json(name = "descriptionDate") var DescriptionDate: String = "",                //Can be an empty string
    @Json(name = "imageURL") var ImageURL: String = "",
    @Json(name = "listeners") var Listeners: Int = 0,
    @Json(name = "tags") var Tags: List<String> = emptyList(),
    @Json(name = "similiarArtists") var SimiliarArtists: List<String?>? = emptyList()          //Can be an empty string
)