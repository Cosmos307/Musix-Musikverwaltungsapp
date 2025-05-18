package com.mountainlabs.musix.network

import android.util.Log
import com.mountainlabs.musix.domain.AlbumInfo
import com.mountainlabs.musix.domain.ArtistInfo
import com.mountainlabs.musix.domain.PlaylistInfo
import com.mountainlabs.musix.domain.TrackInfo
import com.mountainlabs.musix.domain.UpdatedItem

interface AzureDataSource {
    //Artist related
    suspend fun saveAzureArtist(artist: ArtistInfo): ArtistInfo?
    suspend fun getAzureArtists(): List<ArtistInfo>
    suspend fun getAzureArtist(id: String): ArtistInfo?
    suspend fun deleteAzureArtist(id: String): List<UpdatedItem>
    suspend fun updateAzureArtist(artist: ArtistInfo): List<UpdatedItem>

    //Track related
    suspend fun saveAzureTrack(track: TrackInfo): TrackInfo?
    suspend fun getAzureTracks(): List<TrackInfo>
    suspend fun getAzureTrack(id: String): TrackInfo?
    suspend fun deleteAzureTrack(id: String): List<UpdatedItem>
    suspend fun updateAzureTrack(track: TrackInfo): List<UpdatedItem>

    //Playlist related
    suspend fun saveAzurePlaylist(playlist: PlaylistInfo): PlaylistInfo?
    suspend fun getAzurePlaylists(): List<PlaylistInfo>
    suspend fun getAzurePlaylist(id: String): PlaylistInfo?
    suspend fun deleteAzurePlaylist(id: String): List<UpdatedItem>
    suspend fun updateAzurePlaylist(playlist: PlaylistInfo): List<UpdatedItem>


    //Album related
    suspend fun saveAzureAlbum(album: AlbumInfo): AlbumInfo?
    suspend fun getAzureAlbums(): List<AlbumInfo>
    suspend fun getAzureAlbum(id: String): AlbumInfo?
    suspend fun deleteAzureAlbum(id: String): List<UpdatedItem>
}

class AzureDataSourceImpl : AzureDataSource {

    private val regexEmbeddedHTML = Regex("<a.*")

    //Artist related
    override suspend fun saveAzureArtist(artist: ArtistInfo): ArtistInfo? {

        Log.i("AzureSaveArtist", "saveRemoteArtistInfoToAzure called")

        val response = apiAzure.postArtistInfo(artist = artist)
        //TODO: add header-checking for 401 (forbidden => key is invalid)

        val responseBody = response?.body()

        Log.w("AzureSaveArtist", "StatusCode: " + response?.code())

        if(response?.code() == 401)
        {
            Log.e("AzureSaveArtist", "Unauthorized!")
            return null;
        }

        if(responseBody == null || response == null){
            Log.e("AzureAPISave", "ERROR: response(.body) is null!")
            return null;
        }

        val artistInfo = if (response.isSuccessful && responseBody != null) {
            Log.i("AzureSaveArtist", "saveAzureArtist - Good response: The received response body is all good :)")
            responseBody
        } else if (response.isSuccessful) {
            Log.e("AzureSaveArtist", "saveAzureArtist - Bad response: $responseBody")
            null
        } else
        {
            Log.w("AzureSaveArtist", "saveAzureArtist - Bad response: Body Empty :(")
            null
        }

        Log.i("AzureSaveArtist", "saveAzureArtist completed")
        return artistInfo
    }
    override suspend fun getAzureArtists(): List<ArtistInfo> {
        Log.i("AzureGetArtists", "getAzureArtists called")
        val response = apiAzure.getArtists()
        //TODO: add header-checking for 401 (forbidden => key is invalid)

        val responseBody : List<ArtistInfo>? = response.body()

        Log.w("AzureGetArtists", "StatusCode: " + response?.code())

        if(response?.code() == 401)
        {
            Log.e("AzureGetArtists", "Unauthorized!")
            return emptyList();
        }

        val searchResult = if (response.isSuccessful && responseBody != null) {
            Log.i("AzureGetArtists", "getAzureArtists - Good response: The received response body is all good :)")
            responseBody
        }
         else if (response.isSuccessful) {
            Log.e("AzureGetArtists", "getAzureArtists - Bad response: $responseBody")
            emptyList()
        } else
        {
            Log.w("AzureGetArtists", "getAzureArtists -Bad response: Body Empty :(")
            emptyList()
        }
        Log.i("AzureGetArtists", "getAzureArtists completed")
        return searchResult
    }

    //Get artist by id
    override suspend fun getAzureArtist(id: String): ArtistInfo? {
        Log.i("AzureGetArtist", "getAzureArtist called")
        val response = apiAzure.getArtist(id = id)
        //TODO: add header-checking for 401 (forbidden => key is invalid)

        val responseBody : ArtistInfo? = response.body()

        Log.w("AzureGetArtist", "StatusCode: " + response?.code())

        if(response?.code() == 401)
        {
            Log.e("AzureGetArtist", "Unauthorized!")
            return null
        }

        val searchResult = if (response.isSuccessful && responseBody != null) {
            Log.i("AzureGetArtist", "getAzureArtist - Good response: The received response body is all good :)")
            responseBody
        }
        else if (response.isSuccessful) {
            Log.e("AzureGetArtist", "getAzureArtist - Bad response: $responseBody")
            null
        } else
        {
            Log.w("AzureGetArtist", "getAzureArtist -Bad response: Body Empty :(")
            null
        }

        Log.i("AzureGetArtist", "getAzureArtist completed")
        return searchResult
    }

    //Update artist by id
    override suspend fun updateAzureArtist(artist: ArtistInfo): List<UpdatedItem> {
        Log.i("AzureUpdateArtist", "UpdateAzureArtist called")
        val response = apiAzure.updateArtist(artist = artist)
        //TODO: add header-checking for 401 (forbidden => key is invalid)

        val responseBody = response.body()

        Log.w("AzureUpdateArtist", "StatusCode: " + response?.code())

        if(response?.code() == 401)
        {
            Log.e("AzureUpdateArtist", "Unauthorized!")
            return emptyList()
        }

        val searchResult = if (response.isSuccessful && responseBody != null) {
            Log.i("AzureUpdateArtist", "UpdateAzureArtist - Good response: The received response body is all good :)")
            responseBody
        }
        else if (response.isSuccessful) {
            Log.e("AzureUpdateArtist", "UpdateAzureArtist - Bad response: $responseBody")
            emptyList()
        } else
        {
            Log.w("AzureUpdateArtist", "UpdateAzureArtist -Bad response: Body Empty :(")
            emptyList()
        }

        Log.i("AzureUpdateArtist", "UpdateAzureArtist completed")
        return searchResult
    }

    //Delete artist by id
    override suspend fun deleteAzureArtist(id: String): List<UpdatedItem> {
        Log.i("AzureDeleteArtist", "DeleteAzureArtist called")
        val response = apiAzure.deleteArtist(id = id)
        //TODO: add header-checking for 401 (forbidden => key is invalid)

        val responseBody = response.body()

        Log.w("AzureDeleteArtist", "StatusCode: " + response?.code())

        if(response?.code() == 401)
        {
            Log.e("AzureDeleteArtist", "Unauthorized!")
            return emptyList()
        }

        val searchResult = if (response.isSuccessful && responseBody != null) {
            Log.i("AzureDeleteArtist", "DeleteAzureArtist - Good response: The received response body is all good :)")
            responseBody
        }
        else if (response.isSuccessful) {
            Log.e("AzureDeleteArtist", "DeleteAzureArtist - Bad response: $responseBody")
            emptyList()
        } else
        {
            Log.w("AzureDeleteArtist", "DeleteAzureArtist -Bad response: Body Empty :(")
            emptyList()
        }

        Log.i("AzureDeleteArtist", "DeleteAzureArtist completed")
        return searchResult
    }


    //Track related
    //Create/save a new playlist
    override suspend fun saveAzureTrack(track: TrackInfo): TrackInfo? {

        Log.i("AzureSaveTrack", "saveRemoteTrackInfoToAzure called")

        val response = apiAzure.postTrackInfo(track = track)
        //TODO: add header-checking for 401 (forbidden => key is invalid)

        val responseBody = response?.body()

        Log.w("AzureSaveTrack", "StatusCode: " + response?.code())

        if(response?.code() == 401)
        {
            Log.e("AzureSaveTrack", "Unauthorized!")
            return null;
        }

        if(responseBody == null || response == null){
            Log.e("AzureAPISave", "ERROR: response(.body) is null!")
            return null;
        }

        val trackInfo = if (response.isSuccessful && responseBody != null) {
            Log.i("AzureSaveTrack", "saveAzureTrack - Good response: The received response body is all good :)")
            responseBody
        } else if (response.isSuccessful) {
            Log.e("AzureSaveTrack", "saveAzureTrack - Bad response: $responseBody")
            null
        } else
        {
            Log.w("AzureSaveTrack", "saveAzureTrack - Bad response: Body Empty :(")
            null
        }

        Log.i("AzureSaveTrack", "saveAzureTrack completed")
        return trackInfo
    }

    //Get all Tracks from Azure
    override suspend fun getAzureTracks(): List<TrackInfo> {
        Log.i("AzureGetTracks", "getAzureTracks called")
        val response = apiAzure.getTracks()
        //TODO: add header-checking for 401 (forbidden => key is invalid)

        val responseBody : List<TrackInfo>? = response.body()

        Log.w("AzureGetTracks", "StatusCode: " + response?.code())

        if(response?.code() == 401)
        {
            Log.e("AzureGetTracks", "Unauthorized!")
            return emptyList();
        }

        val searchResult = if (response.isSuccessful && responseBody != null) {
            Log.i("AzureGetTracks", "getAzureTracks - Good response: The received response body is all good :)")
            responseBody
        }
        else if (response.isSuccessful) {
            Log.e("AzureGetTracks", "getAzureTracks - Bad response: $responseBody")
            emptyList()
        } else
        {
            Log.w("AzureGetTracks", "getAzureTracks -Bad response: Body Empty :(")
            emptyList()
        }
        Log.i("AzureGetTracks", "getAzureTracks completed")
        return searchResult
    }

    //Get track by id
    override suspend fun getAzureTrack(id: String): TrackInfo? {
        Log.i("AzureGetTrack", "getAzureTrack called")
        val response = apiAzure.getTrack(id = id)
        //TODO: add header-checking for 401 (forbidden => key is invalid)

        val responseBody : TrackInfo? = response.body()

        Log.w("AzureGetTrack", "StatusCode: " + response?.code())

        if(response?.code() == 401)
        {
            Log.e("AzureGetTrack", "Unauthorized!")
            return null
        }

        val searchResult = if (response.isSuccessful && responseBody != null) {
            Log.i("AzureGetTrack", "getAzureTrack - Good response: The received response body is all good :)")
            responseBody
        }
        else if (response.isSuccessful) {
            Log.e("AzureGetTrack", "getAzureTrack - Bad response: $responseBody")
            null
        } else
        {
            Log.w("AzureGetTrack", "getAzureTrack -Bad response: Body Empty :(")
            null
        }

        Log.i("AzureGetTrack", "getAzureTrack completed")
        return searchResult
    }

    //Update track by id
    override suspend fun updateAzureTrack(track: TrackInfo): List<UpdatedItem> {
        Log.i("AzureUpdateTrack", "UpdateAzureTrack called")
        val response = apiAzure.updateTrack(track = track)
        //TODO: add header-checking for 401 (forbidden => key is invalid)

        val responseBody = response.body()

        Log.w("AzureUpdateTrack", "StatusCode: " + response?.code())

        if(response?.code() == 401)
        {
            Log.e("AzureUpdateTrack", "Unauthorized!")
            return emptyList()
        }

        val searchResult = if (response.isSuccessful && responseBody != null) {
            Log.i("AzureUpdateTrack", "UpdateAzureTrack - Good response: The received response body is all good :)")
            responseBody
        }
        else if (response.isSuccessful) {
            Log.e("AzureUpdateTrack", "UpdateAzureTrack - Bad response: $responseBody")
            emptyList()
        } else
        {
            Log.w("AzureUpdateTrack", "UpdateAzureTrack -Bad response: Body Empty :(")
            emptyList()
        }

        Log.i("AzureUpdateTrack", "UpdateAzureTrack completed")
        return searchResult
    }

    //Delete track by id
    override suspend fun deleteAzureTrack(id: String): List<UpdatedItem> {
        Log.i("AzureDeleteTrack", "DeleteAzureTrack called")
        val response = apiAzure.deleteTrack(id = id)
        //TODO: add header-checking for 401 (forbidden => key is invalid)

        val responseBody = response.body()

        Log.w("AzureDeleteTrack", "StatusCode: " + response?.code())

        if(response?.code() == 401)
        {
            Log.e("AzureDeleteTrack", "Unauthorized!")
            return emptyList()
        }

        val searchResult = if (response.isSuccessful && responseBody != null) {
            Log.i("AzureDeleteTrack", "DeleteAzureTrack - Good response: The received response body is all good :)")
            responseBody
        }
        else if (response.isSuccessful) {
            Log.e("AzureDeleteTrack", "DeleteAzureTrack - Bad response: $responseBody")
            emptyList()
        } else
        {
            Log.w("AzureDeleteTrack", "DeleteAzureTrack -Bad response: Body Empty :(")
            emptyList()
        }

        Log.i("AzureDeleteTrack", "DeleteAzureTrack completed")
        return searchResult
    }


    //Playlist related
    //Create/save a new playlist
    override suspend fun saveAzurePlaylist(playlist: PlaylistInfo): PlaylistInfo? {

        Log.i("AzureSavePlaylist", "saveRemotePlaylistInfoToAzure called")

        val response = apiAzure.postPlaylistInfo(playlist = playlist)
        //TODO: add header-checking for 401 (forbidden => key is invalid)

        val responseBody = response?.body()

        Log.w("AzureSavePlaylist", "StatusCode: " + response?.code())

        if(response?.code() == 401)
        {
            Log.e("AzureSavePlaylist", "Unauthorized!")
            return null;
        }

        if(responseBody == null || response == null){
            Log.e("AzureAPISave", "ERROR: response(.body) is null!")
            return null;
        }

        val playlistInfo = if (response.isSuccessful && responseBody != null) {
            Log.i("AzureSavePlaylist", "saveAzurePlaylist - Good response: The received response body is all good :)")
            responseBody
        } else if (response.isSuccessful) {
            Log.e("AzureSavePlaylist", "saveAzurePlaylist - Bad response: $responseBody")
            null
        } else
        {
            Log.w("AzureSavePlaylist", "saveAzurePlaylist - Bad response: Body Empty :(")
            null
        }

        Log.i("AzureSavePlaylist", "saveAzurePlaylist completed")
        return playlistInfo
    }

    //Get all Playlists from Azure
    override suspend fun getAzurePlaylists(): List<PlaylistInfo> {
        Log.i("AzureGetPlaylists", "getAzurePlaylists called")
        val response = apiAzure.getPlaylists()
        //TODO: add header-checking for 401 (forbidden => key is invalid)

        val responseBody : List<PlaylistInfo>? = response.body()

        Log.w("AzureGetPlaylists", "StatusCode: " + response?.code())

        if(response?.code() == 401)
        {
            Log.e("AzureGetPlaylists", "Unauthorized!")
            return emptyList();
        }

        val searchResult = if (response.isSuccessful && responseBody != null) {
            Log.i("AzureGetPlaylists", "getAzurePlaylists - Good response: The received response body is all good :)")
            responseBody
        }
        else if (response.isSuccessful) {
            Log.e("AzureGetPlaylists", "getAzurePlaylists - Bad response: $responseBody")
            emptyList()
        } else
        {
            Log.w("AzureGetPlaylists", "getAzurePlaylists -Bad response: Body Empty :(")
            emptyList()
        }
        Log.i("AzureGetPlaylists", "getAzurePlaylists completed")
        return searchResult
    }

    //Get playlist by id
    override suspend fun getAzurePlaylist(id: String): PlaylistInfo? {
        Log.i("AzureGetPlaylist", "getAzurePlaylist called")
        val response = apiAzure.getPlaylist(id = id)
        //TODO: add header-checking for 401 (forbidden => key is invalid)

        val responseBody : PlaylistInfo? = response.body()

        Log.w("AzureGetPlaylist", "StatusCode: " + response?.code())

        if(response?.code() == 401)
        {
            Log.e("AzureGetPlaylist", "Unauthorized!")
            return null
        }

        val searchResult = if (response.isSuccessful && responseBody != null) {
            Log.i("AzureGetPlaylist", "getAzurePlaylist - Good response: The received response body is all good :)")
            responseBody
        }
        else if (response.isSuccessful) {
            Log.e("AzureGetPlaylist", "getAzurePlaylist - Bad response: $responseBody")
            null
        } else
        {
            Log.w("AzureGetPlaylist", "getAzurePlaylist -Bad response: Body Empty :(")
            null
        }

        Log.i("AzureGetPlaylist", "getAzurePlaylist completed")
        return searchResult
    }

    //Update playlist by id
    override suspend fun updateAzurePlaylist(playlist: PlaylistInfo): List<UpdatedItem> {
        Log.i("AzureUpdatePlaylist", "UpdateAzurePlaylist called")
        val response = apiAzure.updatePlaylist(playlist = playlist)
        //TODO: add header-checking for 401 (forbidden => key is invalid)

        val responseBody = response.body()

        Log.w("AzureUpdatePlaylist", "StatusCode: " + response?.code())

        if(response?.code() == 401)
        {
            Log.e("AzureUpdatePlaylist", "Unauthorized!")
            return emptyList()
        }

        val searchResult = if (response.isSuccessful && responseBody != null) {
            Log.i("AzureUpdatePlaylist", "UpdateAzurePlaylist - Good response: The received response body is all good :)")
            responseBody
        }
        else if (response.isSuccessful) {
            Log.e("AzureUpdatePlaylist", "UpdateAzurePlaylist - Bad response: $responseBody")
            emptyList()
        } else
        {
            Log.w("AzureUpdatePlaylist", "UpdateAzurePlaylist -Bad response: Body Empty :(")
            emptyList()
        }

        Log.i("AzureUpdatePlaylist", "UpdateAzurePlaylist completed")
        return searchResult
    }

    //Delete playlist by id
    override suspend fun deleteAzurePlaylist(id: String): List<UpdatedItem> {
        Log.i("AzureDeletePlaylist", "DeleteAzurePlaylist called")
        val response = apiAzure.deletePlaylist(id = id)
        //TODO: add header-checking for 401 (forbidden => key is invalid)

        val responseBody = response.body()

        Log.w("AzureDeletePlaylist", "StatusCode: " + response?.code())

        if(response?.code() == 401)
        {
            Log.e("AzureDeletePlaylist", "Unauthorized!")
            return emptyList()
        }

        val searchResult = if (response.isSuccessful && responseBody != null) {
            Log.i("AzureDeletePlaylist", "DeleteAzurePlaylist - Good response: The received response body is all good :)")
            responseBody
        }
        else if (response.isSuccessful) {
            Log.e("AzureDeletePlaylist", "DeleteAzurePlaylist - Bad response: $responseBody")
            emptyList()
        } else
        {
            Log.w("AzureDeletePlaylist", "DeleteAzurePlaylist -Bad response: Body Empty :(")
            emptyList()
        }

        Log.i("AzureDeletePlaylist", "DeleteAzurePlaylist completed")
        return searchResult
    }


    //Album related
    //Create/save a new album
    override suspend fun saveAzureAlbum(album: AlbumInfo): AlbumInfo? {

        Log.i("AzureSaveAlbum", "saveRemoteAlbumInfoToAzure called")

        val response = apiAzure.postAlbumInfo(album = album)
        //TODO: add header-checking for 401 (forbidden => key is invalid)

        val responseBody = response?.body()

        Log.w("AzureSaveAlbum", "StatusCode: " + response?.code())

        if(response?.code() == 401)
        {
            Log.e("AzureSaveAlbum", "Unauthorized!")
            return null;
        }

        if(responseBody == null || response == null){
            Log.e("AzureAPISave", "ERROR: response(.body) is null!")
            return null;
        }

        val albumInfo = if (response.isSuccessful && responseBody != null) {
            Log.i("AzureSaveAlbum", "saveAzureAlbum - Good response: The received response body is all good :)")
            responseBody
        } else if (response.isSuccessful) {
            Log.e("AzureSaveAlbum", "saveAzureAlbum - Bad response: $responseBody")
            null
        } else
        {
            Log.w("AzureSaveAlbum", "saveAzureAlbum - Bad response: Body Empty :(")
            null
        }

        Log.i("AzureSaveAlbum", "saveAzureAlbum completed")
        return albumInfo
    }

    //Get all Albums from Azure
    override suspend fun getAzureAlbums(): List<AlbumInfo> {
        Log.i("AzureGetAlbums", "getAzureAlbums called")
        val response = apiAzure.getAlbums()
        //TODO: add header-checking for 401 (forbidden => key is invalid)

        val responseBody : List<AlbumInfo>? = response.body()

        Log.w("AzureGetAlbums", "StatusCode: " + response?.code())

        if(response?.code() == 401)
        {
            Log.e("AzureGetAlbums", "Unauthorized!")
            return emptyList();
        }

        val searchResult = if (response.isSuccessful && responseBody != null) {
            Log.i("AzureGetAlbums", "getAzureAlbums - Good response: The received response body is all good :)")
            responseBody
        }
        else if (response.isSuccessful) {
            Log.e("AzureGetAlbums", "getAzureAlbums - Bad response: $responseBody")
            emptyList()
        } else
        {
            Log.w("AzureGetAlbums", "getAzureAlbums -Bad response: Body Empty :(")
            emptyList()
        }
        Log.i("AzureGetAlbums", "getAzureAlbums completed")
        return searchResult
    }

    //Get album by id
    override suspend fun getAzureAlbum(id: String): AlbumInfo? {
        Log.i("AzureGetAlbum", "getAzureAlbum called")
        val response = apiAzure.getAlbum(id = id)
        //TODO: add header-checking for 401 (forbidden => key is invalid)

        val responseBody : AlbumInfo? = response.body()

        Log.w("AzureGetAlbum", "StatusCode: " + response?.code())

        if(response?.code() == 401)
        {
            Log.e("AzureGetAlbum", "Unauthorized!")
            return null
        }

        val searchResult = if (response.isSuccessful && responseBody != null) {
            Log.i("AzureGetAlbum", "getAzureAlbum - Good response: The received response body is all good :)")
            responseBody
        }
        else if (response.isSuccessful) {
            Log.e("AzureGetAlbum", "getAzureAlbum - Bad response: $responseBody")
            null
        } else
        {
            Log.w("AzureGetAlbum", "getAzureAlbum -Bad response: Body Empty :(")
            null
        }

        Log.i("AzureGetAlbum", "getAzureAlbum completed")
        return searchResult
    }

    //Delete album by id
    override suspend fun deleteAzureAlbum(id: String): List<UpdatedItem> {
        Log.i("AzureDeleteAlbum", "DeleteAzureAlbum called")
        val response = apiAzure.deleteAlbum(id = id)
        //TODO: add header-checking for 401 (forbidden => key is invalid)

        val responseBody = response.body()

        Log.w("AzureDeleteAlbum", "StatusCode: " + response?.code())

        if(response?.code() == 401)
        {
            Log.e("AzureDeleteAlbum", "Unauthorized!")
            return emptyList()
        }

        val searchResult = if (response.isSuccessful && responseBody != null) {
            Log.i("AzureDeleteAlbum", "DeleteAzureAlbum - Good response: The received response body is all good :)")
            responseBody
        }
        else if (response.isSuccessful) {
            Log.e("AzureDeleteAlbum", "DeleteAzureAlbum - Bad response: $responseBody")
            emptyList()
        } else
        {
            Log.w("AzureDeleteAlbum", "DeleteAzureAlbum -Bad response: Body Empty :(")
            emptyList()
        }

        Log.i("AzureDeleteAlbum", "DeleteAzureAlbum completed")
        return searchResult
    }
}