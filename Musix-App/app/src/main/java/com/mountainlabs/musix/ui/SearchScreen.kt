package com.mountainlabs.musix.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.mountainlabs.musix.R
import com.mountainlabs.musix.domain.SearchItem
import org.koin.androidx.compose.koinViewModel

const val searchRouteDefinition = "SearchScreen"

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter", "RememberReturnType")
@Composable
fun ScreenSearchBar(
    heightFraction: Float,
    viewModel: SearchScreenViewModel = koinViewModel(),
    onSearchItemClick: (SearchItem, String) -> Unit
) {

    val isSearching by viewModel.isSearching.collectAsState()
    val uiState by viewModel.uiState.collectAsState()

    var searchBarFocused by remember { mutableStateOf(false) }
    var searchBarInputQuery by remember { mutableStateOf("") }
    var searchHistory = remember { mutableStateListOf<String>() }
    var searchSort by remember { mutableStateOf("") }


    Column(
        modifier = Modifier
            .fillMaxHeight(heightFraction)
    ) {
        SearchBar(
            modifier = Modifier.fillMaxWidth(),
            query = searchBarInputQuery,
            onQueryChange = {
                searchBarInputQuery = it
            },
            onSearch = {
                searchBarFocused = false
                searchHistory.add(searchBarInputQuery)

                startSearch(searchBarInputQuery, searchSort, viewModel)

            },
            active = searchBarFocused,
            onActiveChange = {
                searchBarFocused = it
            },
            placeholder = {
                Text(text="Search . . .")
            },
            leadingIcon = {
                Icon(
                    modifier = Modifier.clickable { startSearch(searchBarInputQuery, searchSort, viewModel) },
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search Icon"
                )
            },
            trailingIcon = {
                if(searchBarFocused) {
                    Icon(
                        modifier = Modifier.clickable {
                            if (searchBarInputQuery.isNotEmpty()) {
                                searchBarInputQuery=""
                            } else {
                                searchBarFocused = false
                            }
                        },
                        imageVector = Icons.Default.Close,
                        contentDescription = "Search Icon"
                    )
                }
            }

        ) {
            searchHistory.forEach{
                Row (modifier = Modifier.padding(all = 14.dp)) {
                    Icon(imageVector = Icons.Default.History, contentDescription = "History Icon")
                    Text(text = it)
                }
            }
        }
        Row  (
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            listOf("Album", "Artist", "Song").forEach { label ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(2.dp, color = Color.Gray),
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable {
                            searchSort = label
                            startSearch(searchBarInputQuery, searchSort, viewModel)
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
        if (isSearching) {
            Box(modifier = Modifier
                .fillMaxSize()
            )
            {
                CircularProgressIndicator( modifier = Modifier.align(Alignment.Center))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
            ) {
                items(uiState.item) { result ->
                    SearchItem(result, searchSort, onSearchItemClick)
                }
            }
        }
    }
}

fun startSearch(queryText : String, searchSort : String, viewModel :SearchScreenViewModel) {
    if (queryText == "") {
        return
    }
    when (searchSort) {
        "Album" -> {
            viewModel.getAlbumSearch(queryText)
        }
        "Artist" -> {
            viewModel.getArtistSearch(queryText)
        }
        "Song" -> {
            viewModel.getTrackSearch(queryText)
        }
    }
}

@Composable
fun SearchItem (
    result: SearchItem,
    searchSort: String,
    onSearchItemClick: (SearchItem, String) -> Unit
) {

    Row (
        modifier = Modifier
            .padding(5.dp)
            .clickable {
                onSearchItemClick(result, searchSort)
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
            val displayText = if (searchSort == "Artist") {
                result.artist
            } else {
                result.text
            }
            Text(
                text = displayText,
                fontSize = 16.sp,
                modifier = Modifier
                    .padding(5.dp)
            )
            if (searchSort != "Artist") {
                Text(
                    text = result.artist,
                    fontSize = 12.sp,
                    color = Color.DarkGray,
                    modifier = Modifier
                        .padding(5.dp)
                )
            }
        }
    }
}