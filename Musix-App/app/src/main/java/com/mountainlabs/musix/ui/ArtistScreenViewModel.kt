package com.mountainlabs.musix.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mountainlabs.musix.domain.ArtistInfo
import com.mountainlabs.musix.domain.TrackInfo
import com.mountainlabs.musix.network.CollectionDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ArtistScreenViewModel (
    private val collectionItemsRepository: CollectionDataRepository
) :ViewModel() {

    private val _artistInfo: MutableStateFlow<ArtistInfo?> = MutableStateFlow(null)
    val artistInfo: StateFlow<ArtistInfo?> = _artistInfo.asStateFlow()

    private val _tracksBackend: MutableStateFlow<List<TrackInfo>> = MutableStateFlow(emptyList())
    val tracksBackend: StateFlow<List<TrackInfo>> = _tracksBackend.asStateFlow()

    public fun getArtistInfo(
        artist: String,
        mbid: String? = null,
        lang: String? = null,
        autocorrect: Boolean? = null,
        username: String? = null
    ) {
        viewModelScope.launch {
            _artistInfo.update {
                collectionItemsRepository.getArtistInfo(
                    artist = artist,
                    mbid = mbid,
                    lang = lang,
                    autocorrect = autocorrect,
                    username = username
                )
            }
        }
    }

    public fun saveArtistInfoToAzure(
        artist: ArtistInfo
    ) {
        viewModelScope.launch {
            _artistInfo.update {
                collectionItemsRepository.saveArtistInfoToAzure(
                    artist = artist
                )
            }
        }
    }

    public fun getBackendTracks() {
        viewModelScope.launch {
            _tracksBackend.update {
                collectionItemsRepository.getTracksFromAzure()
            }
        }
    }


}