package com.mountainlabs.musix.ui

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mountainlabs.musix.domain.ArtistInfo
import com.mountainlabs.musix.domain.PlaylistInfo
import com.mountainlabs.musix.domain.Track
import com.mountainlabs.musix.domain.TrackInfo
import com.mountainlabs.musix.domain.UpdatedItem
import com.mountainlabs.musix.network.CollectionDataRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SongScreenViewModel (
    private val collectionItemsRepository: CollectionDataRepository,

) : ViewModel() {

    private val _artistInfo: MutableStateFlow<ArtistInfo?> = MutableStateFlow(null)
    val artistInfo: StateFlow<ArtistInfo?> = _artistInfo.asStateFlow()

    private val _expanded = MutableLiveData(false)
    var expanded = mutableStateOf(false)

    private val _updatedItems: MutableStateFlow<List<UpdatedItem>> = MutableStateFlow(emptyList())
    val updatedItem: StateFlow<List<UpdatedItem>> = _updatedItems.asStateFlow()

    fun toggleExpanded() {
        _expanded.value = _expanded.value?.not()
    }

    private val _trackInfo: MutableStateFlow<TrackInfo?> = MutableStateFlow(null)
    val trackInfo: StateFlow<TrackInfo?> = _trackInfo.asStateFlow()

    private val _playlists: MutableStateFlow<List<PlaylistInfo>> = MutableStateFlow(emptyList())
    val playlists: StateFlow<List<PlaylistInfo>> = _playlists.asStateFlow()

    //playlist from backend
    private val _playlistBackend: MutableStateFlow<PlaylistInfo?> = MutableStateFlow(null)
    val playlistBackend: StateFlow<PlaylistInfo?> = _playlistBackend.asStateFlow()

    var updatedTrackInfo by mutableStateOf<TrackInfo?>(null)
        private set

    var isDialogShown by mutableStateOf(false)
    var isFavorite by mutableStateOf(false)
        private set

    // Zustand für den Dialog
    var isNewDialogShown by mutableStateOf(false)

    // Zustand für das Eingabefeld und die Buttons
    var isInputShown by mutableStateOf(false)

    var playlistName by mutableStateOf("")
    // Funktion zum Verbergen des Eingabefelds und der Buttons


    // Funktion zum Anzeigen des Eingabefelds und der Buttons
    fun onAddPlaylistClick() {
        isInputShown = true
    }

    fun onCancelClick() {
        isInputShown = false
        playlistName = "" // Leert das Eingabefeld
    }
    fun playlistHinzufuegen(name: String, tracks: List<Track>, track: Track) {
        savePlaylistInfoToAzure(
            playlist = PlaylistInfo(
                Name = name,
                Tracks = tracks + track
            )
        )
        //playlist update
    }

    // Funktion zum Anzeigen des Dialogs
    fun onPlusClick() {
        isNewDialogShown = true
    }

    // Funktion zum Verbergen des Dialogs
    fun onDismissPlusClick() {
        isNewDialogShown = false
    }

    fun onConfirmClick(){
        isDialogShown = true
    }

    fun onDismissDialog(){
        isDialogShown = false
    }


    fun savePlaylistInfoToAzure(
        playlist: PlaylistInfo
    ) {
        viewModelScope.launch {
            _playlistBackend.update {
                collectionItemsRepository.savePlaylistInfoToAzure(
                    playlist = playlist
                )
            }
        }
    }

    fun getTrackInfo(
        track: String,
        artist: String,
        mbid: String? = null,
        autocorrect: Boolean? = null,
        username: String? = null
    ) {
        viewModelScope.launch {
            _trackInfo.update {

                if (updatedTrackInfo != null) {
                    // Use the updated metadata
                    updatedTrackInfo
                }
                else if(/*isFavorite*/true){
                    val trackInfoResponse = collectionItemsRepository.getTrackInfo(
                        track = track,              //Optional
                        artist = artist,            //Optional
                        mbid = mbid,                //Optional
                        autocorrect = autocorrect,
                        username = username
                    )
                    trackInfoResponse
                }
                else{ // Von DB laden
                    val trackInfoResponse = collectionItemsRepository.getTrackInfo(
                        track = track,              //Optional
                        artist = artist,            //Optional
                        mbid = mbid,                //Optional
                        autocorrect = autocorrect,
                        username = username
                    )
                    trackInfoResponse
                }
            }
        }
    }

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

    fun getBackendTrack(id: String) {
        viewModelScope.launch {
            _trackInfo.update {
                collectionItemsRepository.getTrackFromAzure(id = id)
            }
        }
    }

    public fun updateBackendTrack(track: TrackInfo){
        viewModelScope.launch {
            _updatedItems.update {
                collectionItemsRepository.updateTrackFromAzure(track = track)
            }
        }
    }



    fun onFavoriteClick() {
        isFavorite = !isFavorite
    }

    fun saveTrackInfoToAzure(
        track: TrackInfo
    ) {
        viewModelScope.launch {
            _trackInfo.update {
                collectionItemsRepository.saveTrackInfoToAzure(
                    track = track
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

    fun updateMetadata(name: String, interpret: String, duration: Int, tags: List<String>) {
        viewModelScope.launch {
            _trackInfo.update { trackInfo ->
                trackInfo?.copy(
                    Name = name,
                    ArtistName = interpret,
                    Duration = duration,
                    Tags = tags
                ).also { updatedTrackInfo = it } // Speichern der aktualisierten Metadaten
            }
            collectionItemsRepository.updateTrackFromAzure(updatedTrackInfo!!)
        }
        Log.w("ViewModel: TrackInfo", "Name: $name, Interpret: $interpret, Tags: $tags")
    }

    fun getBackendPlaylists() {
        viewModelScope.launch {
            _playlists.update {
                collectionItemsRepository.getPlaylistsFromAzure()
            }
        }
    }
    fun updateBackendPlaylist(playlist: PlaylistInfo){
        viewModelScope.launch {
            _updatedItems.update {
                collectionItemsRepository.updatePlaylistFromAzure(playlist = playlist)
            }
        }
    }

}