package com.mountainlabs.musix.domain

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import java.time.LocalDate

//Only from azure search
@JsonClass(generateAdapter = true)
data class PlaylistInfo(
    @Json(name = "type") var type: ItemType = ItemType.PLAYLIST,
    @Json(name = "id") var id: String = "",     //Will be set when saving it in Azure
    @Json(name = "name") var Name: String,
    @Json(name = "creationDate") var CreationDate: String = LocalDate.now().toString(),
    @Json(name = "imageURL") var ImageURL: String = "",          //Can be an empty string
    @Json(name = "tags") var Tags: List<String> = emptyList(),
    @Json(name = "tracks") var Tracks: List<Track> = emptyList(),
    @Json(name = "descriptionSummary") var DescriptionSummary: String = "", //Can be an empty string
    @Json(name = "descriptionDate") var DescriptionDate: String = "",    //Can be an empty string
)