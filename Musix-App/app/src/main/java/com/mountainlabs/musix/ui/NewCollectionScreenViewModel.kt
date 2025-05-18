package com.mountainlabs.musix.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mountainlabs.musix.domain.ArtistInfo
import com.mountainlabs.musix.domain.PlaylistInfo
import com.mountainlabs.musix.domain.TrackInfo
import com.mountainlabs.musix.network.CollectionDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NewCollectionScreenViewModel (
    private val collectionItemsRepository: CollectionDataRepository
) : ViewModel() {

    private val _artistsBackend: MutableStateFlow<List<ArtistInfo>> = MutableStateFlow(emptyList())
    val artistsBackend: StateFlow<List<ArtistInfo>> = _artistsBackend.asStateFlow()

    private val _tracksBackend: MutableStateFlow<List<TrackInfo>> = MutableStateFlow(emptyList())
    val tracksBackend: StateFlow<List<TrackInfo>> = _tracksBackend.asStateFlow()

    private val _playlistsBackend: MutableStateFlow<List<PlaylistInfo>> = MutableStateFlow(emptyList())
    val playlistsBackend: StateFlow<List<PlaylistInfo>> = _playlistsBackend.asStateFlow()

    public fun getBackendArtists() {
        viewModelScope.launch {
            _artistsBackend.update {
                collectionItemsRepository.getArtistsFromAzure()
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

    public fun getBackendPlaylists() {
        viewModelScope.launch {
            _playlistsBackend.update {
                collectionItemsRepository.getPlaylistsFromAzure()
            }
        }
    }
}