package com.mountainlabs.musix.network

import com.mountainlabs.musix.domain.AlbumInfo
import com.mountainlabs.musix.domain.ArtistInfo
import com.mountainlabs.musix.domain.PlaylistInfo
import com.mountainlabs.musix.domain.SearchItem
import com.mountainlabs.musix.domain.TrackInfo
import com.mountainlabs.musix.domain.UpdatedItem

interface CollectionDataRepository {
    //Artist related
    suspend fun getArtistInfo(artist: String, mbid : String?, lang : String?, autocorrect : Boolean?, username : String?): ArtistInfo?
    //suspend fun getArtistSearch(artist: String, limit: String?, page: String?): APIArtistSearch?
    suspend fun getArtistSearch(artist: String, limit: String?, page: String?): List<SearchItem>

    //Azure-Artist
    suspend fun saveArtistInfoToAzure(artist: ArtistInfo): ArtistInfo?
    suspend fun getArtistsFromAzure() : List<ArtistInfo>
    suspend fun getArtistFromAzure(id: String): ArtistInfo?
    suspend fun deleteArtistFromAzure(id: String): List<UpdatedItem>
    suspend fun updateArtistFromAzure(artist: ArtistInfo): List<UpdatedItem>

    //Album related
    suspend fun getAlbumSearch(album: String, limit: String?, page: String?): List<SearchItem>
    suspend fun getAlbumInfo(artist: String, album: String, mbid : String?, lang : String?, username : String?, autocorrect : Boolean?): AlbumInfo?

    //Track related
    suspend fun getTrackSearch(track: String, artist: String?, limit : String?, page : String?): List<SearchItem>
    suspend fun getTrackInfo(track: String, artist: String, mbid : String?, autocorrect : Boolean?, username : String?): TrackInfo?

    //Azure-Track
    suspend fun saveTrackInfoToAzure(track: TrackInfo): TrackInfo?
    suspend fun getTracksFromAzure() : List<TrackInfo>
    suspend fun getTrackFromAzure(id: String): TrackInfo?
    suspend fun deleteTrackFromAzure(id: String): List<UpdatedItem>
    suspend fun updateTrackFromAzure(track: TrackInfo): List<UpdatedItem>


    //Azure-Playlist
    suspend fun savePlaylistInfoToAzure(playlist: PlaylistInfo): PlaylistInfo?
    suspend fun getPlaylistsFromAzure() : List<PlaylistInfo>
    suspend fun getPlaylistFromAzure(id: String): PlaylistInfo?
    suspend fun deletePlaylistFromAzure(id: String): List<UpdatedItem>
    suspend fun updatePlaylistFromAzure(playlist: PlaylistInfo): List<UpdatedItem>


    //Azure-Album
    suspend fun saveAlbumInfoToAzure(album: AlbumInfo): AlbumInfo?
    suspend fun getAlbumsFromAzure() : List<AlbumInfo>
    suspend fun getAlbumFromAzure(id: String): AlbumInfo?
    suspend fun deleteAlbumFromAzure(id: String): List<UpdatedItem>
}

class CollectionDataRepositoryImpl(
    private val remotePostsDataSource: RemoteCollectionDataSource,
    private val azureDataSource: AzureDataSource
) : CollectionDataRepository {

    //Artist related
    override suspend fun getArtistInfo(artist: String, mbid : String?, lang : String?, autocorrect : Boolean?, username : String?): ArtistInfo? {
        return remotePostsDataSource.getRemoteArtistInfo(artist = artist, mbid = mbid, lang = lang, autocorrect = autocorrect,username = username)
    }

    override suspend fun getArtistSearch(artist: String, limit: String?, page: String?): List<SearchItem> {
        return remotePostsDataSource.getRemoteArtistSearch(artist = artist, limit = limit, page = page)
    }

    //Azure-Artist
    override suspend fun saveArtistInfoToAzure(artist: ArtistInfo): ArtistInfo? {
        return azureDataSource.saveAzureArtist(artist = artist)
    }

    override suspend fun getArtistsFromAzure() : List<ArtistInfo>{
        return azureDataSource.getAzureArtists()
    }
    override suspend fun getArtistFromAzure(id: String): ArtistInfo?{
        return azureDataSource.getAzureArtist(id = id)
    }
    override suspend fun updateArtistFromAzure(artist: ArtistInfo): List<UpdatedItem>{
        return azureDataSource.updateAzureArtist(artist = artist)
    }
    override suspend fun deleteArtistFromAzure(id: String): List<UpdatedItem>{
        return azureDataSource.deleteAzureArtist(id = id)
    }


    //Album related
    override suspend fun getAlbumSearch(album: String, limit: String?, page: String?): List<SearchItem>  {
        return remotePostsDataSource.getRemoteAlbumSearch(album = album, limit = limit, page = page)
    }
    override suspend fun getAlbumInfo(artist: String, album: String, mbid : String?, lang : String?, username : String?, autocorrect : Boolean?): AlbumInfo? {
        return remotePostsDataSource.getRemoteAlbumInfo(artist = artist, album = album, mbid = mbid, lang = lang, username = username, autocorrect = autocorrect)
    }

    //Track related
    override suspend fun getTrackSearch(track: String, artist: String?, limit : String?, page : String?): List<SearchItem> {
        return remotePostsDataSource.getRemoteTrackSearch(track = track, artist  = artist, limit = limit, page = page)
    }

    override suspend fun getTrackInfo(track: String, artist: String, mbid : String?, autocorrect : Boolean?, username : String?): TrackInfo? {
        return remotePostsDataSource.getRemoteTrackInfo(track = track, artist  = artist, mbid = mbid, autocorrect = autocorrect,username = username)
    }

    //Track-Azure
    override suspend fun saveTrackInfoToAzure(track: TrackInfo): TrackInfo? {
        return azureDataSource.saveAzureTrack(track = track)
    }

    override suspend fun getTracksFromAzure() : List<TrackInfo>{
        return azureDataSource.getAzureTracks()
    }
    override suspend fun getTrackFromAzure(id: String): TrackInfo?{
        return azureDataSource.getAzureTrack(id = id)
    }
    override suspend fun updateTrackFromAzure(track: TrackInfo): List<UpdatedItem>{
        return azureDataSource.updateAzureTrack(track = track)
    }
    override suspend fun deleteTrackFromAzure(id: String): List<UpdatedItem>{
        return azureDataSource.deleteAzureTrack(id = id)
    }

    //Playlist-Azure
    override suspend fun savePlaylistInfoToAzure(playlist: PlaylistInfo): PlaylistInfo? {
        return azureDataSource.saveAzurePlaylist(playlist = playlist)
    }
    override suspend fun getPlaylistsFromAzure() : List<PlaylistInfo>{
        return azureDataSource.getAzurePlaylists()
    }
    override suspend fun getPlaylistFromAzure(id: String): PlaylistInfo?{
        return azureDataSource.getAzurePlaylist(id = id)
    }
    override suspend fun updatePlaylistFromAzure(playlist: PlaylistInfo): List<UpdatedItem>{
        return azureDataSource.updateAzurePlaylist(playlist = playlist)
    }
    override suspend fun deletePlaylistFromAzure(id: String): List<UpdatedItem>{
        return azureDataSource.deleteAzurePlaylist(id = id)
    }

    //Album-Azure
    override suspend fun saveAlbumInfoToAzure(album: AlbumInfo): AlbumInfo? {
        return azureDataSource.saveAzureAlbum(album = album)
    }
    override suspend fun getAlbumsFromAzure() : List<AlbumInfo>{
        return azureDataSource.getAzureAlbums()
    }
    override suspend fun getAlbumFromAzure(id: String): AlbumInfo?{
        return azureDataSource.getAzureAlbum(id = id)
    }
    override suspend fun deleteAlbumFromAzure(id: String): List<UpdatedItem>{
        return azureDataSource.deleteAzureAlbum(id = id)
    }
}