package com.mountainlabs.musix.domain

import com.mountainlabs.musix.network.Image

//All searches return this uniform class to make representation simpler
data class SearchItem(
    val type: ItemType,
    val id: String = "",         //Will be set when saving it in Azure
    val artist: String,
    val text: String,       //Album or Trackname or listeners for Artist
    val pictureURLs: List<Image>,
    val mbid: String,
)