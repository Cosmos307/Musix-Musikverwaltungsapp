package com.mountainlabs.musix.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mountainlabs.musix.domain.AlbumInfo
import com.mountainlabs.musix.domain.ArtistInfo
import com.mountainlabs.musix.domain.PlaylistInfo
import com.mountainlabs.musix.domain.SearchItem
import com.mountainlabs.musix.domain.TrackInfo
import com.mountainlabs.musix.domain.UpdatedItem
import com.mountainlabs.musix.network.APIAlbumSearch
import com.mountainlabs.musix.network.APIArtistSearch
import com.mountainlabs.musix.network.APICollectionItem
import com.mountainlabs.musix.network.APITrackSearch
import com.mountainlabs.musix.network.CollectionDataRepository
import com.mountainlabs.musix.ui.state.SearchScreenState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * This whole ViewModel was used for testing Purposes,
 * it contains nearly every call to the LastFMAPI and AzureStorageTable
 */
class CallinApiPlaceholderViewModel(
    private val collectionItemsRepository: CollectionDataRepository
) : ViewModel() {

    private val inputText = MutableStateFlow("")                            //Search input from user
    private val searchItems = MutableStateFlow<List<SearchItem>>(emptyList())     //Search result

    /** uiState is a hot flow that just needs to be collected in
     * order to get the latest results list from a SearchScreenState */
    val uiState =
        combine(inputText, searchItems,) {
            inputText, searchItems -> SearchScreenState(inputText, inputText.isNotEmpty(), searchItems)
        }.stateIn(viewModelScope, SharingStarted.Lazily, SearchScreenState("", false, emptyList()))

    //Placeholder
    private val _collectionItems: MutableStateFlow<List<APICollectionItem>> =
        MutableStateFlow(emptyList())
    val collectionItems: StateFlow<List<APICollectionItem>> = _collectionItems.asStateFlow()

    //updatedItem is used to store the latest updatedItem that was send back by Azure => use it to get precisely what was updated
    private val _updatedItems: MutableStateFlow<List<UpdatedItem>> = MutableStateFlow(emptyList())
    val updatedItem: StateFlow<List<UpdatedItem>> = _updatedItems.asStateFlow()


    //artist-save to azure
    /*private val _artistInfo: MutableStateFlow<ArtistInfo?> = MutableStateFlow(null)
    val artistInfo: StateFlow<ArtistInfo?> = _artistInfo.asStateFlow()*/

    //artist-info
    private val _artistInfo: MutableStateFlow<ArtistInfo?> = MutableStateFlow(null)
    val artistInfo: StateFlow<ArtistInfo?> = _artistInfo.asStateFlow()

    //artist from backend
    private val _artistBackend: MutableStateFlow<ArtistInfo?> = MutableStateFlow(null)
    val artistBackend: StateFlow<ArtistInfo?> = _artistBackend.asStateFlow()

    //artists from backend
    private val _artistsBackend: MutableStateFlow<List<ArtistInfo>> = MutableStateFlow(emptyList())
    val artistsBackend: StateFlow<List<ArtistInfo>> = _artistsBackend.asStateFlow()

    //artist-search
    private val _artistSearch: MutableStateFlow<APIArtistSearch?> = MutableStateFlow(null)
    val artistSearch: StateFlow<APIArtistSearch?> = _artistSearch.asStateFlow()

    //album-search
    private val _albumSearch: MutableStateFlow<APIAlbumSearch?> = MutableStateFlow(null)
    val albumSearch: StateFlow<APIAlbumSearch?> = _albumSearch.asStateFlow()

    //album-info
    private val _albumInfo: MutableStateFlow<AlbumInfo?> = MutableStateFlow(null)
    val albumInfo: StateFlow<AlbumInfo?> = _albumInfo.asStateFlow()

    //track-search
    private val _trackSearch: MutableStateFlow<APITrackSearch?> = MutableStateFlow(null)
    val trackSearch: StateFlow<APITrackSearch?> = _trackSearch.asStateFlow()

    //track-info
    private val _trackInfo: MutableStateFlow<TrackInfo?> = MutableStateFlow(null)
    val trackInfo: StateFlow<TrackInfo?> = _trackInfo.asStateFlow()

    //track from backend
    private val _trackBackend: MutableStateFlow<TrackInfo?> = MutableStateFlow(null)
    val trackBackend: StateFlow<TrackInfo?> = _trackBackend.asStateFlow()

    //tracks from backend
    private val _tracksBackend: MutableStateFlow<List<TrackInfo>> = MutableStateFlow(emptyList())
    val tracksBackend: StateFlow<List<TrackInfo>> = _tracksBackend.asStateFlow()


    //playlist from backend
    private val _playlistBackend: MutableStateFlow<PlaylistInfo?> = MutableStateFlow(null)
    val playlistBackend: StateFlow<PlaylistInfo?> = _playlistBackend.asStateFlow()

    //playlists from backend
    private val _playlistsBackend: MutableStateFlow<List<PlaylistInfo>> = MutableStateFlow(emptyList())
    val playlistsBackend: StateFlow<List<PlaylistInfo>> = _playlistsBackend.asStateFlow()


    //album from backend
    private val _albumBackend: MutableStateFlow<AlbumInfo?> = MutableStateFlow(null)
    val albumBackend: StateFlow<AlbumInfo?> = _albumBackend.asStateFlow()

    //albums from backend
    private val _albumsBackend: MutableStateFlow<List<AlbumInfo>> = MutableStateFlow(emptyList())
    val albumsBackend: StateFlow<List<AlbumInfo>> = _albumsBackend.asStateFlow()


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

    public fun getBackendArtist(id: String) {
        viewModelScope.launch {
            _artistBackend.update {
                collectionItemsRepository.getArtistFromAzure(id = id)
            }
        }
    }

    public fun getBackendArtists() {
        viewModelScope.launch {
            _artistsBackend.update {
                collectionItemsRepository.getArtistsFromAzure()
            }
        }
    }

    public fun updateBackendArtist(artist: ArtistInfo){
        viewModelScope.launch {
            _updatedItems.update {
                collectionItemsRepository.updateArtistFromAzure(artist = artist)
            }
        }
    }

    public fun deleteBackendArtist(id: String){
        viewModelScope.launch {
            _updatedItems.update {
                collectionItemsRepository.deleteArtistFromAzure(id = id)
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

    public fun getAlbumSearch(album: String, limit: String? = null, page: String? = null) {
        viewModelScope.launch {
            searchItems.update {
                collectionItemsRepository.getAlbumSearch(album = album, limit = limit, page = page)
            }
        }
    }

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


    //Track-Related

    public fun getTrackSearch(
        track: String,
        artist: String? = null,
        limit: String? = null,
        page: String? = null
    ) {
        viewModelScope.launch {
            searchItems.update {
                collectionItemsRepository.getTrackSearch(
                    track = track,
                    artist = artist,
                    limit = limit,
                    page = page
                )
            }
        }
    }

    public fun getTrackInfo(
        track: String,
        artist: String,
        mbid: String? = null,
        autocorrect: Boolean? = null,
        username: String? = null
    ) {
        viewModelScope.launch {
            _trackInfo.update {
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

    //Azure-Track
    public fun saveTrackInfoToAzure(
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

    public fun getBackendTrack(id: String) {
        viewModelScope.launch {
            _trackBackend.update {
                collectionItemsRepository.getTrackFromAzure(id = id)
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

    public fun updateBackendTrack(track: TrackInfo){
        viewModelScope.launch {
            _updatedItems.update {
                collectionItemsRepository.updateTrackFromAzure(track = track)
            }
        }
    }

    public fun deleteBackendTrack(id: String){
        viewModelScope.launch {
            _updatedItems.update {
                collectionItemsRepository.deleteTrackFromAzure(id = id)
            }
        }
    }


    //Azure-Playlist
    public fun savePlaylistInfoToAzure(
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

    public fun getBackendPlaylist(id: String) {
        viewModelScope.launch {
            _playlistBackend.update {
                collectionItemsRepository.getPlaylistFromAzure(id = id)
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

    public fun updateBackendPlaylist(playlist: PlaylistInfo){
        viewModelScope.launch {
            _updatedItems.update {
                collectionItemsRepository.updatePlaylistFromAzure(playlist = playlist)
            }
        }
    }

    public fun deleteBackendPlaylist(id: String){
        viewModelScope.launch {
            _updatedItems.update {
                collectionItemsRepository.deletePlaylistFromAzure(id = id)
            }
        }
    }



    //Azure-Album
    public fun saveAlbumInfoToAzure(
        album: AlbumInfo
    ) {
        viewModelScope.launch {
            _albumBackend.update {
                collectionItemsRepository.saveAlbumInfoToAzure(
                    album = album
                )
            }
        }
    }

    public fun getBackendAlbum(id: String) {
        viewModelScope.launch {
            _albumBackend.update {
                collectionItemsRepository.getAlbumFromAzure(id = id)
            }
        }
    }

    public fun getBackendAlbums() {
        viewModelScope.launch {
            _albumsBackend.update {
                collectionItemsRepository.getAlbumsFromAzure()
            }
        }
    }

    public fun deleteBackendAlbum(id: String){
        viewModelScope.launch {
            _updatedItems.update {
                collectionItemsRepository.deleteAlbumFromAzure(id = id)
            }
        }
    }


    fun onInputTextChange(text: String) {
        inputText.update { text }
    }
}