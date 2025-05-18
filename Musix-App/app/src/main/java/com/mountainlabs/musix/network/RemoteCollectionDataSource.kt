package com.mountainlabs.musix.network

import android.util.Log
import com.mountainlabs.musix.domain.AlbumInfo
import com.mountainlabs.musix.domain.ArtistInfo
import com.mountainlabs.musix.domain.ItemType
import com.mountainlabs.musix.domain.SearchItem
import com.mountainlabs.musix.domain.Track
import com.mountainlabs.musix.domain.TrackInfo

interface RemoteCollectionDataSource {
    //Artist related
    suspend fun getRemoteArtistSearch(artist: String, limit: String?, page: String?): List<SearchItem>
    suspend fun getRemoteArtistInfo(artist: String, mbid : String?, lang : String?, autocorrect : Boolean?, username : String?): ArtistInfo?

    //Album related
    suspend fun getRemoteAlbumSearch(album: String, limit: String?, page: String?): List<SearchItem>
    suspend fun getRemoteAlbumInfo(artist: String, album: String, mbid : String?, lang : String?, username : String?, autocorrect : Boolean?): AlbumInfo?

    //Track related
    suspend fun getRemoteTrackSearch(track: String, artist: String?, limit : String?, page : String?): List<SearchItem>
    suspend fun getRemoteTrackInfo(track: String, artist: String, mbid : String?, autocorrect : Boolean?, username : String?): TrackInfo?
}

class RemoteCollectionDataSourceImpl : RemoteCollectionDataSource {
    private val apiAboutArtists: JsonLastFM = apiLastFM

    private val regexEmbeddedHTML = Regex("<a.*")

    override suspend fun getRemoteArtistInfo(artist: String, mbid : String?, lang : String?, autocorrect : Boolean?, username : String?): ArtistInfo? {

        Log.i("APIArtistInfo", "getRemoteArtistInfo called")

        val response = apiAboutArtists.getArtistInfo(artist = artist, mbid = mbid, lang = lang, autocorrect = autocorrect,username = username)
        //TODO: add header-checking for 401 (forbidden => key is invalid)

        val responseBody = response.body()

        val artistInfo = if (response.isSuccessful && responseBody != null) {
            Log.i("APIArtistInfo", "Good response: The received response body is all good :)")
            ArtistInfo(
                Name = responseBody.artist.name,
                MBID = responseBody.artist.mbid ?: "",
                DescriptionSummary = responseBody.artist.bio?.summary?.replace(regexEmbeddedHTML, "") ?: "",
                DescriptionLong = responseBody.artist.bio?.content?.replace(regexEmbeddedHTML, "") ?: "",
                DescriptionDate = responseBody.artist.bio?.published ?: "",
                ImageURL = responseBody.artist.image[responseBody.artist.image.count()-1].url,
                Listeners = responseBody.artist.stats.listeners,
                Tags = responseBody.artist.tags.tag.map {it.name},
                SimiliarArtists = responseBody.artist.similar?.similarArtist?.map { it.name } ?: emptyList()
            )
        } else if (response.isSuccessful) {
            Log.e("APIArtistInfo", "Bad response: $responseBody")
            null
        } else
        {
            Log.w("APIArtistInfo", "Bad response: Body Empty :(")
            null
        }

        Log.i("APIArtistInfo", "getRemoteArtistInfo completed")
        return artistInfo
    }

    override suspend fun getRemoteArtistSearch(artist: String, limit: String?, page: String?): List<SearchItem> {
        Log.i("APIArtistSearch", "getRemoteArtistSearch called")
                val response = apiAboutArtists.getArtistSearch(artist = artist, limit = limit, page = page)
                //TODO: add header-checking for 401 (forbidden => key is invalid)

                val responseBody = response.body()

                val searchResult = if (response.isSuccessful && responseBody != null) {
                    Log.i("APIArtistSearch", "Good response: The received response body is all good :)")
                    //Mapping the result of the API call to the searchItem kotlin data class
                    responseBody.results.artistmatches.artist.map {
                        SearchItem(
                            artist = it.name,
                            text = it.listeners,
                            pictureURLs = it.image,      //TODO: get image from musicbrainz
                            mbid = it.mbid,
                            type = ItemType.ARTIST
                        )
                    }
                } else if (response.isSuccessful) {
                    Log.e("Bad response!", "Bad response: $responseBody")
                    emptyList()
                } else
                {
                    Log.w("APIArtistSearch", "Bad response: Body Empty :(")
                    emptyList()
                }
        Log.i("APIArtist", "getRemoteArtist completed")
        return searchResult
    }

    //Album related calls
    override suspend fun getRemoteAlbumSearch(album: String, limit: String?, page: String?): List<SearchItem> {
        Log.i("APIAlbumSearch", "getRemoteAlbumSearch called")
        val response = apiAboutArtists.getAlbumSearch(album = album, limit = limit, page = page)
        //TODO: add header-checking for 401 (forbidden => key is invalid)

        val responseBody = response.body()

        val searchResult = if (response.isSuccessful && responseBody != null) {
            Log.i("APIArtistSearch", "Good response: The received response body is all good :)")
            responseBody.results.albumMatches.album.map {
                SearchItem(
                    artist = it.artist,
                    text = it.name,
                    pictureURLs = it.image,      //TODO: get image from musicbrainz
                    mbid = it.mbid,
                    type = ItemType.ALBUM
                )
            }
        } else if (response.isSuccessful) {
            Log.e("Bad response!", "Bad response: $responseBody")
            emptyList()
        } else
        {
            Log.w("APIArtistSearch", "Bad response: Body Empty :(")
            emptyList()
        }
        Log.i("APIArtist", "getRemoteAlbumSearch completed")
        return searchResult
    }

    override suspend fun getRemoteAlbumInfo(artist: String, album: String, mbid : String?, lang : String?, username : String?, autocorrect : Boolean?): AlbumInfo? {
        Log.i("APIAlbumInfo", "getRemoteAlbumInfo called")
        val response = apiAboutArtists.getAlbumInfo(artist = artist, album = album, mbid = mbid, lang = lang, autocorrect = autocorrect,username = username)
        //TODO: add header-checking for 401 (forbidden => key is invalid)

        val responseBody = response.body()

        val searchResult = if (response.isSuccessful && responseBody != null) {
            Log.i("APIAlbumInfo", "Good response: The received response body is all good :)")
            AlbumInfo(
                MBID = responseBody.album.mbid ?: "",
                Name = responseBody.album.name,
                ArtistName = responseBody.album.artist,
                ReleaseDate = responseBody.album.name,
                ImageURL = responseBody.album.image[responseBody.album.image.count()-1].url,
                Playcount = responseBody.album.playcount,
                Listeners = responseBody.album.listeners,
                Tags = responseBody.album.tags.tag.map { it.name },
                Tracks = responseBody.album.tracks.track.map {
                    Track(
                        name = it.name,
                        duration = it.duration,
                        artistName = it.artist.name
                    )
                },
                DescriptionSummary = responseBody.album.wiki?.summary?.replace(regexEmbeddedHTML, "") ?: "",
                DescriptionLong = responseBody.album.wiki?.content?.replace(regexEmbeddedHTML, "") ?: "",
                DescriptionDate = responseBody.album.wiki?.published ?: "",
            )
        } else if (response.isSuccessful) {
            Log.e("Bad response!", "Bad response: $responseBody")
            null
        } else
        {
            Log.w("APIAlbumInfo", "Bad response: Body Empty :(")
            null
        }
        Log.i("APIAlbumInfo", "getRemoteAlbumInfo completed")
        return searchResult
    }

    //Track related calls
    override suspend fun getRemoteTrackSearch(track: String, artist: String?, limit : String?, page : String?): List<SearchItem> {
        Log.i("APILastFM", "getRemoteTrackSearch called")
        val response = apiAboutArtists.getTrackSearch(track = track, artist  = artist, limit = limit, page = page)
        //TODO: add header-checking for 401 (forbidden => key is invalid)

        val responseBody = response.body()

        val searchResult = if (response.isSuccessful && responseBody != null) {
            Log.i("APILastFM", "TrackSearch - Good response: The received response body is all good :)")
            Log.i("Test", "Track[0].mbid: ${responseBody.results.trackMatches.track[0].streamable}")
            responseBody.results.trackMatches.track.map {
                SearchItem(
                    artist = it.artist,
                    text = it.name,
                    pictureURLs = it.image,      //TODO: get image from musicbrainz
                    mbid = it.mbid,
                    type = ItemType.TRACK
                )
            }
        } else if (response.isSuccessful) {
            Log.e("APILastFM", "TrachSearch - Bad response: $responseBody")
            emptyList()
        } else
        {
            Log.w("APILastFM", "TrachSearch -Bad response: Body Empty :(")
            emptyList()
        }
        Log.i("APILastFM", "getRemoteTrackSearch completed")
        return searchResult
    }

    override suspend fun getRemoteTrackInfo(track: String, artist: String, mbid : String?, autocorrect : Boolean?, username : String?): TrackInfo? {
        Log.i("APITrackInfo", "getRemoteAlbumInfo called")
        val response = apiAboutArtists.getTrackInfo(track = track, artist  = artist, mbid = mbid, autocorrect = autocorrect,username = username)
        //TODO: add header-checking for 401 (forbidden => key is invalid)

        val responseBody = response.body()

        val searchResult = if (response.isSuccessful && responseBody != null) {
            Log.i("APITrackInfo", "Good response: The received response body is all good :)")
            TrackInfo(
                Name = responseBody.track.name,
                Duration = responseBody.track.duration.toInt(),
                MBID = responseBody.track.mbid ?: "",
                ArtistName = responseBody.track.artist.name,
                ArtistMBID = responseBody.track.artist.mbid ?: "",
                Album = responseBody.track.album?.title ?: "",
                AlbumMBID = responseBody.track.album?.mbid ?: "",
                AlbumPos = responseBody.track.album?.attr?.position ?: "",
                DescriptionSummary = responseBody.track.wiki?.summary?.replace(regexEmbeddedHTML, "")?: "",
                DescriptionLong = responseBody.track.wiki?.content?.replace(regexEmbeddedHTML, "") ?: "",
                DescriptionDate = responseBody.track.wiki?.published ?: "",
                ImageURL = responseBody.track.album?.image?.get(responseBody.track.album.image.count()-1)?.url ?: "",
                Playcount = responseBody.track.playcount,
                Listeners = responseBody.track.listeners,
                Tags = responseBody.track.toptags.tag.map { it.name }
            )
        } else if (response.isSuccessful) {
            Log.e("Bad response!", "Bad response: $responseBody")
            null
        } else
        {
            Log.w("APITrackInfo", "Bad response: Body Empty :(")
            null
        }
        Log.i("APITrackInfo", "getRemoteAlbumInfo completed")
        return searchResult
    }
}