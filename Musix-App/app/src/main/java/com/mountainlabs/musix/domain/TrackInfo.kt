package com.mountainlabs.musix.domain

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass


//TODO: maybe remove MBIDs
@JsonClass(generateAdapter = true)
data class TrackInfo(
    @Json(name = "type") var type: ItemType = ItemType.TRACK,
    @Json(name = "id") var id: String = "",                //Will be set when saving it in Azure
    @Json(name = "name") var Name: String,
    @Json(name = "duration") var Duration: Int,
    @Json(name = "mbid") var MBID: String = "",                   //Can be an empty string
    @Json(name = "artistName") var ArtistName: String = "",
    @Json(name = "artistID") var ArtistID: String = "",          //Must be set to save it in Azure
    @Json(name = "artistMBID") var ArtistMBID: String = "",             //Can be an empty string
    @Json(name = "album") var Album: String = "",                  //Can be an empty string
    @Json(name = "albumMBID") var AlbumMBID: String = "",              //Can be an empty string
    @Json(name = "albumPos") var AlbumPos: String = "",               //Can be an empty string
    @Json(name = "playcount") var Playcount: Int = -1,
    @Json(name = "listeners") var Listeners: Int = -1,
    @Json(name = "imageURL") var ImageURL: String = "",               //Can be an empty string
    @Json(name = "tags") var Tags: List<String> = emptyList(),
    @Json(name = "descriptionSummary") var DescriptionSummary: String = "",     //Can be an empty string
    @Json(name = "descriptionLong") var DescriptionLong: String = "",        //Can be an empty string
    @Json(name = "descriptionDate") var DescriptionDate: String = "",        //Can be an empty string
)