package com.mountainlabs.musix.network

import com.mountainlabs.musix.domain.AlbumInfo
import com.mountainlabs.musix.domain.ArtistInfo
import com.mountainlabs.musix.domain.PlaylistInfo
import com.mountainlabs.musix.domain.TrackInfo
import com.mountainlabs.musix.domain.UpdatedItem
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

private const val functionKey_az: String = ""

private val retrofit = Retrofit.Builder()
    .baseUrl("https://func-moco-lome.azurewebsites.net/api/")
    .addConverterFactory(MoshiConverterFactory.create())
    .build()

val apiAzure: JsonAzure = retrofit.create(JsonAzure::class.java)

interface JsonAzure {

    //Artist related
    //Save info about an artist
    @POST("artist")
    suspend fun postArtistInfo(
        @Body artist: ArtistInfo,
        @Query("code") code: String = functionKey_az
    ): Response<ArtistInfo?>?

    //Get a specified artist by their ID
    @GET("artist/{artistID}")
    suspend fun getArtist(
        @Path("artistID") id: String,
        @Query("code") code: String = functionKey_az
    ): Response<ArtistInfo?>

    //Get all artists from Azure
    @GET("artist")
    suspend fun getArtists(
        @Query("code") code: String = functionKey_az
    ): Response<List<ArtistInfo>>

    //Update a specific artist
    @POST("artist/update/")
    suspend fun updateArtist(
        @Body artist: ArtistInfo,
        @Query("code") code: String = functionKey_az
    ): Response<List<UpdatedItem>>

    //Delete a specified artist by their ID
    @DELETE("artist/delete/{artistID}")
    suspend fun deleteArtist(
        @Path("artistID") id: String,
        @Query("code") code: String = functionKey_az
    ): Response<List<UpdatedItem>>

    //Track related
    //Save info about an track
    @POST("track")
    suspend fun postTrackInfo(
        @Body track: TrackInfo,
        @Query("code") code: String = functionKey_az
    ): Response<TrackInfo?>?

    //Get a specified track by their ID
    @GET("track/{trackID}")
    suspend fun getTrack(
        @Path("trackID") id: String,
        @Query("code") code: String = functionKey_az
    ): Response<TrackInfo?>

    //Get all tracks from Azure
    @GET("track")
    suspend fun getTracks(
        @Query("code") code: String = functionKey_az
    ): Response<List<TrackInfo>>

    //Update a specific track
    @POST("track/update/")
    suspend fun updateTrack(
        @Body track: TrackInfo,
        @Query("code") code: String = functionKey_az
    ): Response<List<UpdatedItem>>

    //Delete a specified track by their ID
    @DELETE("track/delete/{trackID}")
    suspend fun deleteTrack(
        @Path("trackID") id: String,
        @Query("code") code: String = functionKey_az
    ): Response<List<UpdatedItem>>



    //Playlist related
    //Save info about an playlist
    @POST("playlist")
    suspend fun postPlaylistInfo(
        @Body playlist: PlaylistInfo,
        @Query("code") code: String = functionKey_az
    ): Response<PlaylistInfo?>?

    //Get a specified playlist by their ID
    @GET("playlist/{playlistID}")
    suspend fun getPlaylist(
        @Path("playlistID") id: String,
        @Query("code") code: String = functionKey_az
    ): Response<PlaylistInfo?>

    //Get all playlists from Azure
    @GET("playlist")
    suspend fun getPlaylists(
        @Query("code") code: String = functionKey_az
    ): Response<List<PlaylistInfo>>

    //Update a specific playlist
    @POST("playlist/update/")
    suspend fun updatePlaylist(
        @Body playlist: PlaylistInfo,
        @Query("code") code: String = functionKey_az
    ): Response<List<UpdatedItem>>

    //Delete a specified playlist by their ID
    @DELETE("playlist/delete/{playlistID}")
    suspend fun deletePlaylist(
        @Path("playlistID") id: String,
        @Query("code") code: String = functionKey_az
    ): Response<List<UpdatedItem>>



    //Album related
    //Save info about an album
    @POST("album")
    suspend fun postAlbumInfo(
        @Body album: AlbumInfo,
        @Query("code") code: String = functionKey_az
    ): Response<AlbumInfo?>?

    //Get a specified album by their ID
    @GET("album/{albumID}")
    suspend fun getAlbum(
        @Path("albumID") id: String,
        @Query("code") code: String = functionKey_az
    ): Response<AlbumInfo?>

    //Get all albums from Azure
    @GET("album")
    suspend fun getAlbums(
        @Query("code") code: String = functionKey_az
    ): Response<List<AlbumInfo>>

    //Delete a specified album by their ID
    @DELETE("album/delete/{albumID}")
    suspend fun deleteAlbum(
        @Path("albumID") id: String,
        @Query("code") code: String = functionKey_az
    ): Response<List<UpdatedItem>>
}