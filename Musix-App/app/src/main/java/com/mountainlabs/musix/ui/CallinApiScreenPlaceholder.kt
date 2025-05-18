@file:OptIn(ExperimentalMaterial3Api::class)

package com.mountainlabs.musix.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.mountainlabs.musix.domain.PlaylistInfo
import com.mountainlabs.musix.domain.TrackInfo
import org.koin.androidx.compose.koinViewModel


const val collectionsScreenRouteDefinition = "CollectionScreen"


/**
 * This was used just to display and test different API calls to Azure and LastFMAPI
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallinApiScreenPlaceholder(
    viewModel: CallinApiPlaceholderViewModel = koinViewModel()
) {

    val collectionItems by viewModel.collectionItems.collectAsStateWithLifecycle()

    //Artist related
    viewModel.getArtistInfo(artist = "Cher")
    val artistInfo by viewModel.artistInfo.collectAsStateWithLifecycle()

    //API-Search Requests (only the last one called decides the ItemsList in the SearchState which one can get through the uiState (located: CallinApiPlaceholderViewModel=
    //viewModel.getArtistSearch("Cher")
    //viewModel.getAlbumSearch(album = "Reputation")
    //viewModel.getTrackSearch(track = "Believe")

    val thisIsMyUIState by viewModel.uiState.collectAsStateWithLifecycle()
    //val artistSearch by viewModel.artistSearch.collectAsStateWithLifecycle()

    //Album related
    //viewModel.getAlbumSearch(album = "Reputation")
    val albumSearch by viewModel.albumSearch.collectAsStateWithLifecycle()

    //viewModel.getAlbumInfo(artist = "Cher", album = "Believe")
    //viewModel.getAlbumInfo(artist = "Cher", album = "Believe")
    val albumInfo by viewModel.albumInfo.collectAsStateWithLifecycle()

    //viewModel.getTrackSearch(track = "Believe")
    val trackSearch by viewModel.trackSearch.collectAsStateWithLifecycle()

    //viewModel.getTrackInfo(track = "Believe", artist = "Cher")f
    val trackInfo by viewModel.trackInfo.collectAsStateWithLifecycle()

    val updatedItem by viewModel.updatedItem.collectAsStateWithLifecycle()

    //Azure Artist-Related
    viewModel.getBackendArtists()
    val artistsBackend by viewModel.artistsBackend.collectAsStateWithLifecycle()

    val artistBackend by viewModel.artistBackend.collectAsStateWithLifecycle()


    //Azure Track-Related
    viewModel.getBackendTracks()
    val tracksBackend by viewModel.tracksBackend.collectAsStateWithLifecycle()

    val trackBackend by viewModel.trackBackend.collectAsStateWithLifecycle()


    //Azure Track-Related
    viewModel.getBackendPlaylists()
    val playlistsBackend by viewModel.playlistsBackend.collectAsStateWithLifecycle()

    val playlistBackend by viewModel.playlistBackend.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .fillMaxWidth()
    ) {

        CenterAlignedTopAppBar(
            title = { Text("Collections Screen", fontSize = 25.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Green),
            )
        LazyColumn(
            modifier = Modifier
                .fillMaxSize(0.5f)
                .background(Color.DarkGray)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            /*
            //Placeholder API item list
            items(collectionItems) { item ->
                Column(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .fillParentMaxHeight(0.5f)
                        //.clickable { onCollectionClick(item) }
                        .padding(16.dp)
                        .background(Color.LightGray)
                ) {
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = item.author,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = item.title,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "placeholder", //item.publishYear.toString(),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
             */

            }
        }

            LazyColumn(
            modifier = Modifier
                .fillMaxSize(0.8f)
                .background(Color.DarkGray)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(thisIsMyUIState.item) { result ->
                val imageListLength = result.pictureURLs.count()
                Column(
                    modifier = Modifier
                        .fillParentMaxWidth()
                        .fillParentMaxHeight(0.5f)
                        .padding(16.dp)
                        .background(Color.LightGray)
                ) {
                    Text(
                        text = "Result: ${result.artist}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "Listeners: ${result.text}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Picture: ${result.pictureURLs[imageListLength-1].url}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                    AsyncImage(
                        model = result.pictureURLs[imageListLength-1].url,
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .aspectRatio(1f)
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {

            //Azure-Track-Related
            if(false){
            Column(
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Button(onClick = {
                    /*if (trackInfo != null)
                        viewModel.saveTrackInfoToAzure(track = trackInfo!!)*/
                    viewModel.saveTrackInfoToAzure(track = TrackInfo(Name = "Test", Duration = 100, ArtistID = "7ec6895920b4451a9d2bd5e1bbb11dc2") )
                })
                {
                    Text(text = "Save the trackInfo")
                }

                Button(onClick = {
                    viewModel.getBackendTrack(id = "98731fd5495244108723c65897f74378")
                }) {
                    Text(text = "Get track with id 98731fd5495244108723c65897f74378 (TestTrack by Cher)")
                }

                Button(onClick = {
                    if (trackBackend != null) {
                        trackBackend!!.Listeners =
                            if (trackBackend!!.Listeners >= 0) -100 else if (trackBackend!!.Listeners < 0) 9001 else 0
                        trackBackend!!.ArtistName = ""
                        viewModel.updateBackendTrack(track = trackBackend!!)
                    }
                }) {
                    Text(text = "Update track with id 98731fd5495244108723c65897f74378 (TestTrack by Cher) to have -100 or 9001 listeners")
                }

                Button(onClick = {
                    viewModel.deleteBackendTrack(id = "40b99ac1ddd2408482db1aa88c57b93d")
                }) {
                    Text(text = "Delete track id 5d16098be5ab42fb97b9d36dfe2feb03 (DeleteMe)")
                }

                //Display the track from the Azure-Backend
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .background(Color.LightGray)
                ) {

                    if (trackBackend != null) {
                        Text(text = "AZURE Track Info", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = trackBackend!!.Name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontStyle = FontStyle.Italic
                        )
                        Text(
                            text = "Listeners: ${trackBackend!!.Listeners}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Description summary: ${trackBackend!!.DescriptionSummary}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    } else {
                        Column(
                            modifier = Modifier
                                .padding(16.dp)
                                .background(Color.Red)
                        ) {
                            Text(text = "Track Backend info: No track info found")
                        }
                    }


                    if (false) {
                        //Updated Items
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize(0.5f)
                                .background(Color.DarkGray)
                                .fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            //List of tracks from backend
                            items(updatedItem) { item ->
                                Column(
                                    modifier = Modifier
                                        .fillParentMaxWidth()
                                        .fillParentMaxHeight(0.5f)
                                        //.clickable { onCollectionClick(item) }
                                        .padding(16.dp)
                                        .background(Color.LightGray)
                                ) {
                                    Text(
                                        text = item.type.toString(),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = item.id,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(0.5f)
                            .background(Color.DarkGray)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        //List of tracks from backend
                        items(tracksBackend) { item ->
                            Column(
                                modifier = Modifier
                                    .fillParentMaxWidth()
                                    .fillParentMaxHeight(0.5f)
                                    //.clickable { onCollectionClick(item) }
                                    .padding(16.dp)
                                    .background(Color.LightGray)
                            ) {
                                Text(
                                    text = item.Name,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = item.id,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = item.DescriptionLong,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
            }

            //Playlist-related
            if(true) {
                Column(
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    Button(onClick = {
                        viewModel.savePlaylistInfoToAzure(playlist = PlaylistInfo(Name = "Test"))
                    })
                    {
                        Text(text = "Save the playlistInfo")
                    }

                    Button(onClick = {
                        viewModel.getBackendPlaylists()
                        for (playlist in playlistsBackend){
                            if(playlist.Name == "Test"){
                                viewModel.getBackendPlaylist(id = playlist.id)
                            }
                        }
                    }) {
                        Text(text = "Get playlist 'Test' from Azure")
                    }

                    Button(onClick = {
                        viewModel.getBackendPlaylists()
                        for (playlist in playlistsBackend){
                            if(playlist.Name == "Test"){
                                playlist.Tags =
                                    if (playlist.Tags.isNotEmpty()) emptyList() else listOf("Tag1", "Tag2", "Tag3")

                                viewModel.updateBackendPlaylist(playlist = playlist)
                            }
                        }
                    }) {
                        Text(text = "Update playlist to have either 0 or 3 Tags")
                    }

                    Button(onClick = {
                        viewModel.getBackendPlaylists()
                        for (playlist in playlistsBackend){
                            if(playlist.Name == "Test"){
                                viewModel.deleteBackendPlaylist(id = playlist.id)
                            }
                        }
                    }) {
                        Text(text = "Delete playlist 'Test'")
                    }

                    //Display the playlist from the Azure-Backend
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .background(Color.LightGray)
                    ) {
                        var testPlaylist : PlaylistInfo? = null
                        for (playlist in playlistsBackend){
                            if(playlist.Name == "Test"){
                                testPlaylist = playlist
                            }
                        }

                        if (testPlaylist != null) {
                            Text(
                                text = "AZURE Playlist Info",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = "Name : ${testPlaylist.Name}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontStyle = FontStyle.Italic
                            )

                            for(tag in testPlaylist.Tags){
                                Text(
                                    text = "Tag: $tag",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }

                            Text(
                                text = "Description summary: ${testPlaylist.DescriptionSummary}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .background(Color.Red)
                            ) {
                                Text(text = "Playlist info: No playlists info found")
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(0.5f)
                            .background(Color.DarkGray)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        //List of playlists from backend
                        items(playlistsBackend) { item ->
                            Column(
                                modifier = Modifier
                                    .fillParentMaxWidth()
                                    .fillParentMaxHeight(0.5f)
                                    //.clickable { onCollectionClick(item) }
                                    .padding(16.dp)
                                    .background(Color.LightGray)
                            ) {
                                Text(
                                    text = item.Name,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = item.id,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = item.DescriptionDate,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }


            //Artist-related
            if(false) {
                Column(
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    Button(onClick = {
                        if (artistInfo != null)
                            viewModel.saveArtistInfoToAzure(artist = artistInfo!!)
                    })
                    {
                        Text(text = "Save the artistInfo")
                    }

                    Button(onClick = {
                        viewModel.getBackendArtist(id = "7ec6895920b4451a9d2bd5e1bbb11dc2")
                    }) {
                        Text(text = "Get artist with id 7ec6895920b4451a9d2bd5e1bbb11dc2 (Cher)")
                    }

                    Button(onClick = {
                        if (artistBackend != null) {
                            artistBackend!!.Listeners =
                                if (artistBackend!!.Listeners >= 0) -10 else if (artistBackend!!.Listeners < 0) 9001 else 10
                            viewModel.updateBackendArtist(artist = artistBackend!!)
                        }
                    }) {
                        Text(text = "Update artist with id 7ec6895920b4451a9d2bd5e1bbb11dc2 (Cher) to have -100 or 9000 listeners")
                    }

                    Button(onClick = {
                        viewModel.deleteBackendArtist(id = "40b99ac1ddd2408482db1aa88c57b93d")
                    }) {
                        Text(text = "Delete artist id 5d16098be5ab42fb97b9d36dfe2feb03 (DeleteMe)")
                    }

                    //Display the artist from the Azure-Backend
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .background(Color.LightGray)
                    ) {

                        if (artistBackend != null) {
                            Text(
                                text = "AZURE Artist Info",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = artistBackend!!.Name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontStyle = FontStyle.Italic
                            )
                            Text(
                                text = "Listeners: ${artistBackend!!.Listeners}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Description summary: ${artistBackend!!.DescriptionSummary}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        } else {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .background(Color.Red)
                            ) {
                                Text(text = "Artist info: No artists info found")
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize(0.5f)
                            .background(Color.DarkGray)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        //List of artists from backend
                        items(artistsBackend) { item ->
                            Column(
                                modifier = Modifier
                                    .fillParentMaxWidth()
                                    .fillParentMaxHeight(0.5f)
                                    //.clickable { onCollectionClick(item) }
                                    .padding(16.dp)
                                    .background(Color.LightGray)
                            ) {
                                Text(
                                    text = item.Name,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Text(
                                    text = item.id,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = item.DescriptionLong,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                item.SimiliarArtists?.get(0)?.let {
                                    Text(
                                        text = it, //item.publishYear.toString(),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
            }



            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .background(Color.LightGray)
                    .align(Alignment.Center)
            ) {

                if (artistInfo != null) {
                    Text(text = "Artist Info", style = MaterialTheme.typography.bodyLarge)
                    Text(
                        text = artistInfo!!.Name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic
                    )
                    Text(
                        text = "Listeners: ${artistInfo!!.Listeners}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = "Description summary: ${artistInfo!!.DescriptionSummary}",
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .background(Color.Red)
                    ) {
                        Text(text = "Artist info: No artists info found")
                    }
                }
            }
        


                /*
        if (artistSearch != null) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .background(Color.LightGray)
            ) {
                Text(text = "Artist Search", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "Total Results: ${artistSearch!!.results.totalResults}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Search-Term-Query: ",//${artistSearch!!.results.query.searchTerms}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Items per page: ${artistSearch!!.results.itemsPerPage}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .background(Color.Red)
            ) {
                Text(text = "Search: mo artists found")
            }
        }
        */

        //Album related api-calls
        if (albumSearch != null) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .background(Color.Cyan)
            ) {
                Text(text = "Album Search", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "Total Results: ${albumSearch!!.results.totalResults}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Search-Term-Query: ${albumSearch!!.results.query.searchTerms}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Items per page: ${albumSearch!!.results.itemsPerPage}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .background(Color.Red)
            ) {
                Text(text = "Album search: No albums found")
            }
        }

        //Album related api-calls
        if (albumInfo != null) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .background(Color.Cyan)
            ) {
                Text(text = "Album-Info", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "Album name: ${albumInfo!!.Name}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Artist name: ${albumInfo!!.ArtistName}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Wiki publish date: ${albumInfo!!.DescriptionDate}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .background(Color.Red)
            ) {
                Text(text = "Album-Info: No albums found")
            }
        }


        //Track related api-calls
        if (trackSearch != null) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .background(Color.Cyan)
            ) {
                Text(text = "Track search results", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "First track name: ${trackSearch!!.results.trackMatches.track[0].name}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Total results: ${trackSearch!!.results.totalResults}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Item per page: ${trackSearch!!.results.itemsPerPage}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .background(Color.Red)
            ) {
                Text(text = "Track search: No results")
            }
        }

        if (trackInfo != null) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .background(Color.Cyan)
            ) {
                Text(text = "Track search results", style = MaterialTheme.typography.bodyLarge)
                Text(
                    text = "Track name: ${trackInfo!!.Name}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = "Artist: ${trackInfo!!.ArtistName}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Wiki summary: ${trackInfo!!.DescriptionSummary}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .background(Color.Red)
            ) {
                Text(text = "Track search: No results")
            }
        }
    }
}

