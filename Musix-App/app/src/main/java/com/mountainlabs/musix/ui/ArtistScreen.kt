package com.mountainlabs.musix.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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

var artistScreenRouteDefinition = "ArtistScreen/{artistName}/{artistInfo}/{artistListeners}/{fromCollection}"

fun artistScreenRoute(
    artistName:String,
    artistInfo: String?,
    artistListeners: String?,
    fromCollection: Boolean?,
) = "ArtistScreen/$artistName/$artistInfo/$artistListeners/$fromCollection"


@Composable
fun InitArtist(
    artistName: String,
    artistInfo: String,
    artistListeners: String,
    fromCollection: Boolean,
    onUpClick: () -> Unit,
    onTrackItemClick: (String, String, String, Boolean) -> Unit,
    viewModel: ArtistScreenViewModel = koinViewModel()
) {
    println(artistName)
    if (fromCollection) {
        DisplayArtist(
            artistName = artistName,
            artistInfoFM = artistInfo,
            artistListener = artistListeners,
            fromCollection = true,
            viewModel,
            onUpClick,
            onTrackItemClick
        )
    }
    else {
        ArtistScreen(artistName, false, onUpClick, onTrackItemClick, viewModel)
    }
}

@Composable
fun ArtistScreen(
    artistName: String,
    fromCollection: Boolean,
    onUpClick: () -> Unit,
    onTrackItemClick: (String, String, String, Boolean) -> Unit,
    viewModel: ArtistScreenViewModel
) {

    viewModel.getArtistInfo(artist = artistName)
    val artistInfo by viewModel.artistInfo.collectAsStateWithLifecycle()



    if(artistInfo==null)
    {
        return
    }

    DisplayArtist(
        artistName = artistInfo!!.Name,
        artistInfoFM = artistInfo!!.DescriptionSummary,
        artistListener = artistInfo!!.Listeners.toString(),
        fromCollection = fromCollection,
        viewModel,
        onUpClick,
        onTrackItemClick
    )
}
@Composable
fun DisplayArtist(
    artistName: String,
    artistInfoFM: String,
    artistListener: String,
    fromCollection: Boolean,
    viewModel: ArtistScreenViewModel,
    onUpClick: () -> Unit,
    onTrackItemClick: (String, String, String, Boolean) -> Unit
) {
    viewModel.getBackendTracks()

    val artistInfo by viewModel.artistInfo.collectAsStateWithLifecycle()
    val tracksBackend by viewModel.tracksBackend.collectAsStateWithLifecycle()

    var isFavorite by remember { mutableStateOf(false) }
    Column (

    ) {
        TopAppBar(title = "Artist", onUpClick)
        ArtistView(
            artistName = artistName,
            artistInfo = artistInfoFM,
            listeners = artistListener
        )
        Row {
            if (fromCollection) {
                IconButton(
                    onClick = {


                    },
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        contentColor = Color.Black,
                        border = BorderStroke(2.dp, Color.Black),
                        modifier = Modifier.wrapContentHeight(Alignment.CenterVertically)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = null
                        )
                    }
                }
            }
            else {
                IconButton(
                    onClick = {
                        isFavorite = true
                        if(artistInfo != null) {
                            viewModel.saveArtistInfoToAzure(artist = artistInfo!!)
                        }
                    }
                ) {
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White,
                        contentColor = Color.Black,
                        modifier = Modifier.wrapContentHeight(Alignment.CenterVertically)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = if (isFavorite) Color.Red else Color.Black
                        )
                    }
                }
            }
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
        ) {
            items(tracksBackend) { track ->
                if (track.ArtistName == artistName) {
                    DisplayTrack(
                        artistName = track.ArtistName,
                        trackName = track.Name,
                        trackId = track.id,
                        fromCollection = fromCollection,
                        onTrackItemClick = onTrackItemClick)
                }
            }
        }
    }
}



@Composable
fun ArtistView(
    artistName: String,
    artistInfo: String,
    listeners: String,
) {
    Row (
        modifier = Modifier
            .padding(6.dp)
    )
    {
        Image(
            painter = painterResource(R.drawable.placeholder_image),
            contentDescription = "Potential real Image if LastFM Api had them",
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .aspectRatio(1f)
                .clip(shape = RoundedCornerShape(4.dp))
        )
        Column(
            modifier = Modifier
                .padding(start = 8.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = artistName,
                fontSize = 22.sp,
                modifier = Modifier
                    .padding(5.dp)
            )

            Text(
                text = artistInfo,
                modifier = Modifier
                    .fillMaxHeight(fraction = 0.2f)
                    .verticalScroll(rememberScrollState())
            )
            Text(
                text = "$listeners Listeners",
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(5.dp)
            )
        }
    }
}