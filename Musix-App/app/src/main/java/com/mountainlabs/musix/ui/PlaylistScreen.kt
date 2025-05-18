package com.mountainlabs.musix.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import org.koin.androidx.compose.koinViewModel


const val playListScreenRouteDefinition = "PlayListScreen/{playListName}/{creationDate}"

fun playListScreenRoute(playListName: String, creationDate: String) = "PlayListScreen/$playListName/$creationDate"


@Composable
fun PlayListScreen(playListName: String, creationDate: String, onUpClick: () -> Unit, viewModel: PlayListViewModel = koinViewModel()) {

    DisplayPlayList(playListName = playListName, playListInfo = creationDate, onUpClick, viewModel = viewModel)

}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DisplayPlayList(
    playListName: String,
    playListInfo: String,
    onUpClick: () -> Unit,
    viewModel: PlayListViewModel
) {

    Column () {
        TopAppBar(
            title = playListName,
            onUpClick = onUpClick
        )
        AlbumView(albumName = playListName, imageUrl = "", albumArtist = playListInfo, trackCount = 0)
        IconBar()

    }
}

@Composable
fun IconBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(5.dp),
        horizontalArrangement = Arrangement.SpaceAround
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Plus Button"
        )
        Icon(
            imageVector = Icons.Default.Favorite,
            contentDescription = "Like Button",
        )
        Icon(
            imageVector = Icons.Default.Search,
            contentDescription = "Search Icon"
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopAppBar(title: String, onUpClick: () -> Unit) {
    TopAppBar(
        title = {
            Text(text = title)
        },
        navigationIcon = {
            IconButton(onClick = { onUpClick() }) {
                Icon(Icons.Filled.ArrowBack, null)
            }
        }, actions = {
            IconButton(onClick = { }) {
                Icon(Icons.Filled.Settings, null)
            }
        }
    )
}

@Composable
fun AlbumView(
    albumName: String,
    imageUrl: String,
    albumArtist: String,
    trackCount: Int,
) {
    Row (
        modifier = Modifier
            //.padding(12.dp)
    )
    {
        AsyncImage(
            model = imageUrl,
            contentDescription = null,
            modifier = Modifier
                .fillMaxWidth(0.5f)
                .aspectRatio(1f)
        )
        Column(
            modifier = Modifier
                .padding(start = 8.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = albumName,
                fontSize = 22.sp,
                modifier = Modifier
                    .padding(5.dp)
            )
            Text(
                text = albumArtist,
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(5.dp)
            )
            Text(
                text = "${trackCount.toString()} Songs",
                fontSize = 14.sp,
                modifier = Modifier
                    .padding(5.dp)
            )

        }
    }
}