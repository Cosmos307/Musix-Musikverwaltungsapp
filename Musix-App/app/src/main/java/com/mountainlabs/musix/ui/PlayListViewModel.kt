package com.mountainlabs.musix.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mountainlabs.musix.domain.TrackInfo
import com.mountainlabs.musix.network.CollectionDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlayListViewModel (
    private val collectionItemsRepository: CollectionDataRepository
) : ViewModel() {

    private val _playListInfo: MutableStateFlow<TrackInfo?> = MutableStateFlow(null)
    val plaListInfo: StateFlow<TrackInfo?> = _playListInfo.asStateFlow()

    fun getPlayListInfo(
        track: String,
        artist: String,
        mbid: String? = null,
        autocorrect: Boolean? = null,
        username: String? = null
    ) {
        viewModelScope.launch {
            _playListInfo.update {
                collectionItemsRepository.getTrackInfo(
                    track = track,              //Optional
                    artist = artist,            //Optional
                    mbid = mbid,                //Optional
                    autocorrect = autocorrect,
                    username = username
                )
            }
        }
    }


    var isFavorite by mutableStateOf(false)
        private set

}
