package com.mountainlabs.musix.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mountainlabs.musix.domain.SearchItem
import com.mountainlabs.musix.network.CollectionDataRepository
import com.mountainlabs.musix.ui.state.SearchScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


class SearchScreenViewModel (
    private val collectionItemsRepository: CollectionDataRepository
) : ViewModel() {

    private val _isSearching = MutableStateFlow(false)
    val isSearching = _isSearching.asStateFlow()

    private val inputText = MutableStateFlow("")
    private val searchItems = MutableStateFlow<List<SearchItem>>(emptyList())

    val uiState =

        combine(inputText, searchItems,) {
                inputText, searchItems -> SearchScreenState(inputText, inputText.isNotEmpty(), searchItems)
        }.stateIn(viewModelScope, SharingStarted.Lazily, SearchScreenState("", false, emptyList()))

    public fun getArtistSearch(artist: String, limit: String? = null, page: String? = null) {
        viewModelScope.launch {
            searchItems.update {
                collectionItemsRepository.getArtistSearch(
                    artist = artist,
                    limit = limit,
                    page = page
                )
            }
        }
    }

    public fun getTrackSearch(
        track: String,
        artist: String? = null,
        limit: String? = null,
        page: String? = null
    ) {
        viewModelScope.launch {
            searchItems.update {
                val trackInfoResponse = collectionItemsRepository.getTrackSearch(
                    track = track,
                    artist = artist,
                    limit = limit,
                    page = page
                )
                Log.d("SearchViewModel: getTrackSearch Response", trackInfoResponse.toString())
                trackInfoResponse
            }
        }
    }

    public fun getAlbumSearch(album: String, limit: String? = null, page: String? = null) {
        viewModelScope.launch {
            searchItems.update {
                collectionItemsRepository.getAlbumSearch(album = album, limit = limit, page = page)
            }
        }
    }
}