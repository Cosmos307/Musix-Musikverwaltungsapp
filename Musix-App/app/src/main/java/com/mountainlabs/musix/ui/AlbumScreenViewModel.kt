package com.mountainlabs.musix.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mountainlabs.musix.domain.AlbumInfo
import com.mountainlabs.musix.network.CollectionDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AlbumScreenViewModel (
    private val collectionItemsRepository: CollectionDataRepository
    ) : ViewModel() {

    //track-info
    private val _albumInfo: MutableStateFlow<AlbumInfo?> = MutableStateFlow(null)
    val albumInfo: StateFlow<AlbumInfo?> = _albumInfo.asStateFlow()

    public fun getAlbumInfo(
        artist: String,
        album: String,
        mbid: String? = null,
        lang: String? = null,
        username: String? = null,
        autocorrect: Boolean? = null
    ) {
        viewModelScope.launch {
            _albumInfo.update {
                collectionItemsRepository.getAlbumInfo(
                    artist = artist,
                    album = album,
                    mbid = mbid,
                    lang = lang,
                    username = username,
                    autocorrect = autocorrect
                )
            }
        }
    }

}