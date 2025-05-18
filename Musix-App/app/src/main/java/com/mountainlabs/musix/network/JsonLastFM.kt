package com.mountainlabs.musix.network

import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private const val functionKey_lfm: String = ""

private val retrofit = Retrofit.Builder()
    .baseUrl("https://ws.audioscrobbler.com/")
    .addConverterFactory(MoshiConverterFactory.create())
    .build()

val apiLastFM: JsonLastFM = retrofit.create(JsonLastFM::class.java)

//This interface implements all the relevant LastFM-Apis about an Artist
interface JsonLastFM {

    //Album related routes
    @GET("2.0/?method=album.search")
    suspend fun getAlbumSearch(
        @Query("album") album: String,
        @Query("limit") limit: String? = null,
        @Query("page") page: String? = null,
        @Query("api_key") apiKey: String = functionKey_lfm,
        @Query("format") format: String = "json"
    ): Response<APIAlbumSearch>

    @GET("2.0/?method=album.getinfo")
    suspend fun getAlbumInfo(
        @Query("artist") artist : String,
        @Query("album") album: String,
        @Query("mbid") mbid : String? = null,
        @Query("lang") lang : String? = null,
        @Query("autocorrect") autocorrect : Boolean? = null,
        @Query("username") username : String? = null,
        @Query("api_key") apiKey: String = functionKey_lfm,
        @Query("format") format: String = "json"
    ): Response<APIAlbumInfo>

    //Artist related routes
    @GET("2.0/?method=artist.search")
    suspend fun getArtistSearch(
        @Query("artist") artist: String,
        @Query("limit") limit: String? = null,
        @Query("page") page: String? = null,
        @Query("api_key") apiKey: String = functionKey_lfm,
        @Query("format") format: String = "json"
    ): Response<APIArtistSearch>

    @GET("2.0/?method=artist.getinfo")
    suspend fun getArtistInfo(
        @Query("artist") artist: String,
        @Query("mbid") mbid : String? = null,
        @Query("lang") lang : String? = null,
        @Query("autocorrect") autocorrect : Boolean? = null,
        @Query("username") username : String? = null,
        @Query("api_key") apiKey: String = functionKey_lfm,
        @Query("format") format: String = "json"
    ): Response<APIArtistInfo>

    //Track related routes
    @GET("2.0/?method=track.search")
    suspend fun getTrackSearch(
        @Query("track") track: String,
        @Query("artist") artist: String? = null,
        @Query("limit") limit: String? = null,
        @Query("page") page: String? = null,
        @Query("api_key") apiKey: String = functionKey_lfm,
        @Query("format") format: String = "json"
    ): Response<APITrackSearch>

    @GET("2.0/?method=track.getInfo")
    suspend fun getTrackInfo(
        @Query("track") track: String,
        @Query("artist") artist: String,
        @Query("mbid") mbid : String? = null,
        @Query("autocorrect") autocorrect : Boolean? = null,
        @Query("username") username : String? = null,
        @Query("api_key") apiKey: String = functionKey_lfm,
        @Query("format") format: String = "json"
    ): Response<APITrackInfo>
}