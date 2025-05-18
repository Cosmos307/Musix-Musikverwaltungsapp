package com.mountainlabs.musix.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mountainlabs.musix.R
import org.koin.androidx.compose.koinViewModel

const val newCollectionScreenRouteDefinition = "NewCollectionScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewCollectionScreen(
    onCollectionItemClick: (String, String, Int, String, String) -> Unit,
    viewModel: NewCollectionScreenViewModel = koinViewModel()
) {
    val artistsBackend by viewModel.artistsBackend.collectAsStateWithLifecycle()
    val tracksBackend by viewModel.tracksBackend.collectAsStateWithLifecycle()
    val playlistsBackend by viewModel.playlistsBackend.collectAsStateWithLifecycle()

    var searchSort by remember { mutableStateOf("Artist") }
    viewModel.getBackendArtists()

    Column {
        CenterAlignedTopAppBar(
            title = { Text("Collection", fontSize = 22.sp) },
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Blue),
        )

        Row (
            modifier = Modifier
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ){
            listOf("Artist", "Song", "Playlist").forEach { label ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(2.dp, color = Color.Gray),
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable {
                            searchSort = label
                            when (label) {

                                "Artist" -> {
                                    viewModel.getBackendArtists()
                                }

                                "Song" -> {
                                    viewModel.getBackendTracks()

                                }
                                "Playlist" -> {
                                    viewModel.getBackendPlaylists()
                                }
                            }
                        }
                ) {
                    Text(
                        text = label,
                        modifier = Modifier
                            .padding(all = 8.dp),
                        color = Color.Black
                    )
                }
            }
        }
        Spacer(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.Gray)
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.87f)
        ) {
            when (searchSort) {

                "Artist" -> {
                    items(artistsBackend) { artist ->
                        CollectionItem(
                            collectionItemName = artist.Name,
                            collectionArtistName = artist.Name,
                            collectionItemDescription = artist.DescriptionSummary,
                            collectionItemListeners = artist.Listeners,
                            collectionItemTags = artist.Tags,
                            collectionItemId = artist.id,
                            searchSort,
                            onCollectionItemClick
                        )
                    }
                }

                "Song" -> {
                    items(tracksBackend) { track ->
                        CollectionItem(
                            collectionItemName = track.Name,
                            collectionArtistName = track.ArtistName,
                            collectionItemDescription = track.DescriptionSummary,
                            collectionItemListeners = track.Listeners,
                            collectionItemTags = track.Tags,
                            collectionItemId = track.id,
                            searchSort,
                            onCollectionItemClick
                        )
                    }

                }
                "Playlist" -> {
                    items(playlistsBackend) { playlist ->
                        CollectionItem(
                            collectionItemName = playlist.Name,
                            collectionArtistName = playlist.CreationDate,
                            collectionItemDescription = playlist.DescriptionSummary,
                            collectionItemListeners = 0,
                            collectionItemTags = playlist.Tags,
                            collectionItemId = playlist.id,
                            searchSort,
                            onCollectionItemClick
                        )
                    }
                }
            }

        }
    }
}

@Composable
fun CollectionItem (
    collectionItemName: String,
    collectionArtistName: String,
    collectionItemDescription: String,
    collectionItemListeners: Int,
    collectionItemTags: List<String>,
    collectionItemId: String,
    searchSort : String,
    onCollectionItemClick: (String, String, Int, String, String) -> Unit,
) {
    Row (
        modifier = Modifier
            .padding(12.dp)
            .clickable {
                when (searchSort) {
                    "Artist" -> {
                        onCollectionItemClick(collectionItemName, collectionItemDescription, collectionItemListeners, collectionItemId, searchSort)
                    }

                    "Song" -> {
                        onCollectionItemClick(collectionArtistName, collectionItemName, collectionItemListeners, collectionItemId, searchSort)
                    }
                    "Playlist" -> {
                        onCollectionItemClick(collectionArtistName, collectionItemName, collectionItemListeners, collectionItemId, searchSort)
                    }
                }

            }
    )
    {
        Image(
            painter = painterResource(R.drawable.placeholder_image),
            contentDescription = "Album Cover",
            modifier = Modifier
                .fillMaxWidth(0.3f)
                .aspectRatio(1f)
                .clip(shape = RoundedCornerShape(4.dp))
        )
        Column(
            modifier = Modifier
                .padding(start = 8.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = collectionItemName,
                fontSize = 22.sp,
                modifier = Modifier
                    .padding(5.dp)
            )
            if (searchSort == "Playlist") {
                Text(
                    text = "Creation Date: $collectionArtistName",
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    modifier = Modifier
                        .padding(5.dp)
                )
            }
            else{
                Text(
                    text = "Listeners: $collectionItemListeners",
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    modifier = Modifier
                        .padding(5.dp)
                )
            }

        }
    }
}
