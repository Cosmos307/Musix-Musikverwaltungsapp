package com.mountainlabs.musix.ui


import android.annotation.SuppressLint
import android.os.Handler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.mountainlabs.musix.R
import com.mountainlabs.musix.domain.PlaylistInfo
import com.mountainlabs.musix.domain.Track
import com.mountainlabs.musix.domain.TrackInfo
import org.koin.androidx.compose.koinViewModel



var songScreenRouteDefinition = "SongScreen/{artistName}/{trackName}/{trackId}/{fromCollection}"

fun songScreenRoute(artistName: String, trackName: String, trackId: String, fromCollection: Boolean) = "SongScreen/$artistName/$trackName/$trackId/$fromCollection"


@Composable
fun SongScreen(trackName: String, artistName: String, trackId: String, fromCollection: Boolean, onUpClick: () -> Unit, viewModel: SongScreenViewModel = koinViewModel()) {

    val trackInfo by viewModel.trackInfo.collectAsState()
    if (fromCollection) {
        viewModel.getBackendTrack(id = trackId)
    }
    else {
        viewModel.getTrackInfo(track = trackName, artist = artistName)
    }

    if(trackInfo==null)
    {

        return
    }
    else{
        DisplaySong(onUpClick = onUpClick, viewModel = viewModel)
    }
}

@SuppressLint("UnrememberedMutableState")
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DisplaySong(
    viewModel: SongScreenViewModel,
    onUpClick: () -> Unit
) {

    val trackInfo by viewModel.trackInfo.collectAsState()
    val artistInfo by viewModel.artistInfo.collectAsState()

    viewModel.getArtistInfo(artist = trackInfo!!.ArtistName)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(title = "Song", onUpClick)
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(shape = RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.background)
            ) {
                Image(
                    painter = painterResource(R.drawable.placeholder_image),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Title and Info Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = trackInfo?.Name ?: "",
                    color = MaterialTheme.colorScheme.secondary,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .align(Alignment.CenterVertically),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (trackInfo?.ArtistName?.isNotEmpty() == true) {
                            Text(
                                text = trackInfo?.ArtistName ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (trackInfo?.Album?.isNotEmpty() == true) {
                            Text(
                                text = trackInfo?.Album ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (trackInfo?.Duration != null) {
                            Text(
                                text = trackInfo?.Duration .toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                // Add IconButton with heart symbol
                IconButton(
                    onClick = {
                        // Handle the click event
                        viewModel.onFavoriteClick()


                        if (artistInfo != null) {
                            viewModel.saveArtistInfoToAzure(artistInfo!!)
                            Handler().postDelayed({
                                viewModel.saveTrackInfoToAzure(track = trackInfo!!)
                                // we need the delay so the artist is saved before the track can be saved
                            }, 3000)

                        }
                        else {
                            println("artistInfo ist null")
                        }
                    },
                        modifier = Modifier
                            .padding(start = 0.dp)
                            .size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = if (viewModel.isFavorite) Color.Red else Color.Black

                        )
                    }
                    if (viewModel.isFavorite) {
                        IconButton(
                            onClick = {
                                viewModel.onPlusClick()
                            },
                            modifier = Modifier
                                .padding(start = 8.dp) // Padding added here
                                .size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = null,
                                tint = Color.Black
                            )
                        }
                    }
                    if (viewModel.isNewDialogShown) {
                        PlaylistDialog(trackInfo = trackInfo!!, viewModel)
                    }
                }

            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tags Section
            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
            ) {
                trackInfo?.Tags?.forEach { tag ->
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        contentColor = Color.Black,
                        border = BorderStroke(2.dp, Color.Black)
                    ) {
                        Text(
                            text = tag,
                            modifier = Modifier.padding(8.dp),
                            softWrap = true,
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }

            if (trackInfo?.DescriptionSummary?.isNotEmpty() == true) {
                ExpandableText(
                    viewModel = viewModel,
                    text = trackInfo?.DescriptionSummary ?: "",
                    maxLines = 3
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Button Section
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Edit Metadata Button
                Button(
                    onClick = {
                        if (viewModel.isFavorite) {
                            viewModel.onConfirmClick()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        contentColor = Color.Black,
                        containerColor = if (viewModel.isFavorite) Color.White else Color.Gray,
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .border(2.dp, Color.Black, CircleShape)
                ) {
                    Text(
                        text = "Edit Metadata",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                    )
                }
            }

        if (viewModel.isDialogShown) {
            Metadata(
                onDismiss = {
                    viewModel.onDismissDialog()
                },
                onConfirm = { name, interpret, duration, tags ->
                    viewModel.updateMetadata(name, interpret, duration.toInt(), tags)

                    viewModel.updateBackendTrack(track = TrackInfo(Name = name, ArtistID = trackInfo!!.ArtistID ,id = trackInfo!!.id, Duration = duration.toInt(), Tags = tags ))


                    viewModel.onDismissDialog()
                }
            )
        }
    }
}
}

@Composable
fun PlaylistDialog(
    trackInfo: TrackInfo,
    viewModel: SongScreenViewModel
) {
    Dialog(onDismissRequest = { viewModel.onDismissPlusClick() }) {
        Surface(
            color = Color.White,
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(0.8f)
                .wrapContentSize()
        ) {
            viewModel.getBackendPlaylists()
            LazyColumn {
                item {
                    if (!viewModel.isInputShown) {
                        Button(
                            onClick = { viewModel.onAddPlaylistClick() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)

                        ) {
                            Text("Neue Playlist hinzufügen")
                        }
                    } else {
                        Surface(
                            color = Color.White,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxSize(1f)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                TextField(
                                    value = viewModel.playlistName,
                                    onValueChange = { viewModel.playlistName = it },
                                    label = { Text("Name") },
                                    modifier = Modifier.fillMaxWidth(),
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Button(
                                        onClick = { viewModel.onCancelClick() },
                                        modifier = Modifier.fillMaxWidth(0.5f)
                                    ) {
                                        Text(
                                            text = "Abbrechen",
                                            modifier = Modifier.wrapContentWidth(Alignment.Start)
                                        )
                                    }
                                    Button(
                                        onClick = {
                                            viewModel.playlistHinzufuegen(
                                                viewModel.playlistName,
                                                tracks = emptyList(),
                                                track = Track(
                                                    name = trackInfo.Name,
                                                    trackID = trackInfo.id,
                                                    artistName = trackInfo.ArtistName,
                                                    duration = trackInfo.Duration,
                                                    artistID = trackInfo.ArtistID
                                                )
                                            )
                                            viewModel.onDismissPlusClick()
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Hinzufügen",
                                            modifier = Modifier.wrapContentWidth(Alignment.Start)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    DisplayPlaylists(trackInfo, viewModel)
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Button(
                            onClick = { viewModel.onDismissPlusClick() },
                            modifier = Modifier.padding(8.dp)
                        ) {
                            Text("Zurück")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DisplayPlaylists(trackInfo: TrackInfo, viewModel: SongScreenViewModel) {
    val playlists by viewModel.playlists.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        playlists.forEach { playlist ->
            Text(
                text = playlist.Name,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .clickable {
                        playlist.Tracks =
                            if (playlist.Tracks.isNotEmpty()) emptyList() else playlist.Tracks + Track(
                                name = trackInfo.Name,
                                trackID = trackInfo.id,
                                artistName = trackInfo.ArtistName,
                                duration = trackInfo.Duration,
                                artistID = trackInfo.ArtistID
                            )
                        println(trackInfo.Name)
                        println(playlist.Tracks)
                        viewModel.updateBackendPlaylist(
                            playlist = PlaylistInfo(
                                Name = playlist.Name,
                                id = playlist.id,
                                CreationDate = playlist.CreationDate,
                                ImageURL = playlist.ImageURL,
                                Tags = playlist.Tags,
                                Tracks = playlist.Tracks,
                                DescriptionSummary = playlist.DescriptionSummary,
                                DescriptionDate = playlist.DescriptionDate
                            )
                        )
                    }
            )
        }
    }
}

@Composable
fun ExpandableText(viewModel: SongScreenViewModel, text: String, maxLines: Int) {
    val expanded by viewModel.expanded

    Box(modifier = Modifier.padding(16.dp)) {
        Text(
            text = text,
            maxLines = if (expanded) Int.MAX_VALUE else maxLines,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.clickable { viewModel.expanded.value = !viewModel.expanded.value }
        )
    }
}