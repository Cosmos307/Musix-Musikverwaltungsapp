package com.mountainlabs.musix.ui

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.mountainlabs.musix.R
import org.koin.androidx.compose.koinViewModel



var albumScreenRouteDefinition = "AlbumScreen/{artistNameAlbum}/{albumName}"


fun albumScreenRoute(artistNameAlbum: String, albumName: String) = "AlbumScreen/$artistNameAlbum/$albumName"
@Composable
fun AlbumScreen(
    artistNameAlbum: String,
    albumName: String,
    onUpClick: () -> Unit,
    onTrackItemClick: (String, String, String, Boolean) -> Unit,
    viewModel: AlbumScreenViewModel = koinViewModel()

) {
    Log.w("Artist&Albumname", "Artist: $artistNameAlbum, Album: $albumName")

    val albumInfo by viewModel.albumInfo.collectAsStateWithLifecycle()
    viewModel.getAlbumInfo(artist = artistNameAlbum, album = albumName)

    if(albumInfo==null)
    {
        return
    }
    println(albumInfo!!.Tracks.toString())
    DisplayAlbum(
        albumTitle = albumInfo!!.Name,
        albumArtist = albumInfo!!.ArtistName,
        albumTracks = albumInfo!!.Tracks,
        imageUrl = albumInfo!!.ImageURL,
        onUpClick,
        onTrackItemClick
    )

}



@Composable
fun DisplayAlbum(
    albumTitle: String,
    albumArtist: String,
    albumTracks: List<com.mountainlabs.musix.domain.Track>,
    imageUrl: String,
    onUpClick: () -> Unit,
    onTrackItemClick: (String, String, String, Boolean) -> Unit
) {
    var text by remember { mutableStateOf("Hello") }
    Column {
        TopAppBar(title = "Album",onUpClick)

        AlbumView(
            albumName = albumTitle,
            imageUrl = imageUrl,
            albumArtist = albumArtist,
            trackCount = albumTracks.size
        )
        TextField(
            modifier = Modifier.fillMaxWidth(),
            value = text,
            onValueChange = { text = it },
            label = { Text("Notes") }
        )

        IconBar()
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(bottom = 80.dp)
        ) {
            items(albumTracks) { result ->
                DisplayTrack(
                    result.artistName,
                    result.name,
                    result.trackID,
                    false,
                    onTrackItemClick
                )
            }
        }
    }
}

@Composable
fun DisplayTrack(
    artistName: String,
    trackName: String,
    trackId: String,
    fromCollection:Boolean,
    onTrackItemClick: (String, String, String, Boolean) -> Unit
) {
    Row (
        modifier = Modifier
            .padding(5.dp)
            .clickable {
                onTrackItemClick(artistName, trackName, trackId, fromCollection)
            }
    ) {
        Image(
            painter = painterResource(R.drawable.placeholder_image),
            contentDescription = "Album Cover",
            modifier = Modifier
                .fillMaxWidth(0.15f)
                .aspectRatio(1f)
                .clip(shape = RoundedCornerShape(4.dp))
        )
        Column(
            modifier = Modifier
                .padding(start = 8.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = trackName,
                fontSize = 16.sp,
                modifier = Modifier
                    .padding(5.dp)
            )
            Text(
                text = artistName,
                fontSize = 12.sp,
                color = Color.DarkGray,
                modifier = Modifier
                    .padding(5.dp)
            )
        }
    }
}