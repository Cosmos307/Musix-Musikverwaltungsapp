@file:OptIn(ExperimentalMaterial3Api::class)

package com.mountainlabs.musix.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mountainlabs.musix.ui.theme.MusixTheme
data class BottomNavigationItem(val label: String, val route : String, val selectedIcon: ImageVector, val unselectedIcon: ImageVector, val hasNews: Boolean){
    //
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MusixTheme {
                //The following code is needed for the bottom items
                val bottomNavItems = listOf<BottomNavigationItem>(
                    BottomNavigationItem(
                        label = "Collection",
                        route = "NewCollectionScreen",
                        selectedIcon = Icons.Filled.Home,
                        unselectedIcon = Icons.Outlined.Home,
                        hasNews = false
                    ),
                    BottomNavigationItem(
                        label = "Search",
                        route = "SearchScreen",
                        selectedIcon = Icons.Filled.Search,
                        unselectedIcon = Icons.Outlined.Search,
                        hasNews = false
                    ),
                    BottomNavigationItem(
                        label = "Settings",
                        route = "CollectionScreen",
                        selectedIcon = Icons.Filled.Settings,
                        unselectedIcon = Icons.Outlined.Settings,
                        hasNews = true
                    ),
                )

                var selectedItemIndex: Int by rememberSaveable { mutableIntStateOf(0) }

                //Navigation Logic
                val navController = rememberNavController()
                NavHost(
                    navController = navController,
                    startDestination = newCollectionScreenRouteDefinition,
                    enterTransition = { EnterTransition.None },
                    exitTransition = { ExitTransition.None },
                    modifier = Modifier.background(Color.DarkGray),

                    ) {

                    //var songScreenRouteDefinition = "SongScreen/{artistName}/{trackName}"
                    //fun songScreenRoute(artistName: String, trackName: String,) = "SongScreen/$artistName/$trackName"
                    //Navigate to SearchScreen
                    composable(searchRouteDefinition) {
                        BottomBarScreen(bottomNavItems = bottomNavItems, selectedItemIndex = selectedItemIndex, onIndexChange = { newIndex -> selectedItemIndex = newIndex},
                            navigateTo = {route -> navController.navigate(route)},
                        )
                        ScreenSearchBar(
                            0.9f,
                            onSearchItemClick = { searchItem, searchSort ->
                                when (searchSort) {
                                    "Album" -> {
                                        navController.navigate(
                                            albumScreenRoute(
                                                albumName = searchItem.text,
                                                artistNameAlbum = searchItem.artist
                                            )
                                        )
                                    }

                                    "Artist" -> {
                                        navController.navigate(
                                            artistScreenRoute(
                                                artistName = searchItem.artist,
                                                artistInfo = searchItem.text,
                                                artistListeners = searchItem.text,
                                                fromCollection = false,
                                            )
                                        )
                                    }

                                    "Song" -> {
                                        navController.navigate(
                                            songScreenRoute(
                                                artistName = searchItem.artist,
                                                trackName = searchItem.text,
                                                trackId = searchItem.text, // not used
                                                fromCollection = false
                                            )
                                        )
                                    }
                                }
                            }
                        )
                    }

                    //Navigate to CollectionScreen
                    composable(newCollectionScreenRouteDefinition) {
                        BottomBarScreen(bottomNavItems = bottomNavItems, selectedItemIndex = selectedItemIndex, onIndexChange = { newIndex -> selectedItemIndex = newIndex},
                            navigateTo = {route -> navController.navigate(route)},
                        )
                        NewCollectionScreen(
                            //onCollectionItemClick: (String, String, Int, String, String) -> Unit
                            onCollectionItemClick =  { collectionItemName, collectionItemText, listeners, collectionItemId, searchSort ->
                            when (searchSort) {
                                "Playlist" -> {
                                    navController.navigate(
                                        playListScreenRoute(
                                            playListName = collectionItemText,
                                            creationDate = collectionItemName
                                        )
                                    )
                                }

                                "Artist" -> {
                                    navController.navigate(
                                        artistScreenRoute(
                                            artistName = collectionItemName,
                                            artistInfo = collectionItemText,
                                            artistListeners = listeners.toString(),
                                            fromCollection = true,
                                        )
                                    )
                                }

                                "Song" -> {
                                    navController.navigate(
                                        songScreenRoute(
                                            artistName = collectionItemName,
                                            trackName = collectionItemText,
                                            trackId = collectionItemId,
                                            fromCollection = true
                                        )
                                    )
                                }
                            }
                        })
                    }

                    composable(
                        songScreenRouteDefinition,
                        arguments = listOf(
                            navArgument("trackName") { type = NavType.StringType },
                            navArgument("artistName") { type = NavType.StringType },
                            navArgument("trackId") { type = NavType.StringType; defaultValue = "" },
                            navArgument("fromCollection") { type = NavType.BoolType; defaultValue = false },
                        ),
                    ) { backStackEntry ->
                        val trackName = backStackEntry.arguments?.getString("trackName")
                        val artistName = backStackEntry.arguments?.getString("artistName")
                        val trackId = backStackEntry.arguments?.getString("trackId")
                        val fromCollection = backStackEntry.arguments?.getBoolean("fromCollection")

                        SongScreen(
                            trackName = trackName ?: "",
                            artistName = artistName ?: "",
                            trackId = trackId?: "",
                            fromCollection = fromCollection?: false,
                            onUpClick = { navController.navigateUp() }
                        )
                    }

                    composable(
                        playListScreenRouteDefinition,
                        arguments = listOf(
                            navArgument("playListName") { type = NavType.StringType },
                            navArgument("creationDate") { type = NavType.StringType },
                        ),
                    ) { backStackEntry ->
                        val playListName = backStackEntry.arguments?.getString("playListName")
                        val creationDate = backStackEntry.arguments?.getString("creationDate")
                        BottomBarScreen(bottomNavItems = bottomNavItems, selectedItemIndex = selectedItemIndex, onIndexChange = { newIndex -> selectedItemIndex = newIndex},
                            navigateTo = {route -> navController.navigate(route)},
                        )

                        PlayListScreen(
                            playListName = playListName ?: "",
                            creationDate = creationDate ?: "",
                            onUpClick = { navController.navigateUp(   ) },
                        )
                    }

                    composable(
                        albumScreenRouteDefinition,
                        arguments = listOf(
                            navArgument("albumName") { type = NavType.StringType },
                            navArgument("artistNameAlbum") { type = NavType.StringType }
                        ),
                    ) { backStackEntry ->
                        val albumName = backStackEntry.arguments?.getString("albumName")
                        val artistNameAlbum = backStackEntry.arguments?.getString("artistNameAlbum")
                        BottomBarScreen(bottomNavItems = bottomNavItems, selectedItemIndex = selectedItemIndex, onIndexChange = { newIndex -> selectedItemIndex = newIndex},
                            navigateTo = {route -> navController.navigate(route)},
                        )
                        AlbumScreen(
                            albumName = albumName ?: "",
                            artistNameAlbum = artistNameAlbum ?: "",
                            onUpClick = { navController.navigateUp(   ) },
                            onTrackItemClick = { artistName: String, trackName: String, trackId: String, fromCollection:Boolean  ->  navController.navigate(songScreenRoute(
                                artistName, trackName, trackId = "filler", fromCollection = false))}
                        )
                    }

                    composable(
                        route = artistScreenRouteDefinition,
                        arguments = listOf(
                            navArgument("artistName") { type = NavType.StringType; defaultValue = "" },
                            navArgument("artistInfo") { type = NavType.StringType; defaultValue = "" },
                            navArgument("artistListeners") { type = NavType.StringType; defaultValue = "" },
                            navArgument("fromCollection") { type = NavType.BoolType; defaultValue = false },

                        ),
                    ) { backStackEntry ->
                        val artistName = backStackEntry.arguments?.getString("artistName")
                        val artistInfo = backStackEntry.arguments?.getString("artistInfo")
                        val artistListeners = backStackEntry.arguments?.getString("artistListeners")
                        val fromCollection = backStackEntry.arguments?.getBoolean("fromCollection")

                        BottomBarScreen(bottomNavItems = bottomNavItems, selectedItemIndex = selectedItemIndex, onIndexChange = { newIndex -> selectedItemIndex = newIndex},
                            navigateTo = {route -> navController.navigate(route)},
                        )

                        InitArtist(
                            artistName = artistName?: "",
                            artistInfo = artistInfo?: "",
                            artistListeners = artistListeners?: "",
                            fromCollection = fromCollection?: false,
                            onTrackItemClick = { artistName: String, trackName: String, trackId: String, fromCollection: Boolean  ->  navController.navigate(songScreenRoute(
                                artistName, trackName, trackId = trackId, fromCollection = fromCollection))},
                            onUpClick = { navController.navigateUp() }
                        )
                    }



                    //PLACEHOLDER: Navigate to CollectionScreen
                    composable(collectionsScreenRouteDefinition) {
                        BottomBarScreen(bottomNavItems = bottomNavItems, selectedItemIndex = selectedItemIndex, onIndexChange = { newIndex -> selectedItemIndex = newIndex},
                            navigateTo = {route -> navController.navigate(route)},
                            )
                        CallinApiScreenPlaceholder( )
                    }
                }
            }
        }
    }
}

@Composable
fun BottomBarScreen(bottomNavItems : List<BottomNavigationItem>, selectedItemIndex: Int, onIndexChange: (Int) -> Unit, navigateTo: (String) -> Unit) {
    // A surface container using the 'background' color from the theme
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Greeting("Android")
        MyBottomBar(bottomNavItems, selectedItemIndex, onIndexChange, navigateTo)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyBottomBar(bottomNavItems : List<BottomNavigationItem>, selectedItemIndex : Int, onIndexChange: (Int) -> Unit, navigateTo: (String) -> Unit) {
    Scaffold(
        bottomBar = {
                    NavigationBar {
                        bottomNavItems.forEachIndexed{ index, bottomNavigationItem ->
                            NavigationBarItem(
                                selected = (selectedItemIndex == index),
                                onClick = {
                                            onIndexChange(index)
                                            navigateTo(bottomNavigationItem.route)
                                          },
                                icon = {
                                    BadgedBox(badge = {
                                        if(bottomNavigationItem.hasNews)
                                        {
                                            Badge()
                                        }
                                    }) {
                                        Icon(
                                            imageVector = if(index == selectedItemIndex) {
                                                bottomNavigationItem.selectedIcon
                                            }
                                            else bottomNavigationItem.unselectedIcon,
                                            contentDescription = bottomNavigationItem.label
                                        )
                                    }
                                       },
                                label = { Text(text = bottomNavigationItem.label) },
                                alwaysShowLabel = true,
                            )
                        }
                    }
        },
    ) { innerPadding ->
        Text(
            text = "",
            modifier = Modifier.padding(innerPadding),
            style = MaterialTheme.typography.bodyLarge,
            //fontSize = 200.dp,
        )

    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun MainScreenPreview() {
    val bottomNavItems = listOf<BottomNavigationItem>(
        BottomNavigationItem(label = "Collection", route = "CollectionScreen",selectedIcon = Icons.Filled.Home, unselectedIcon = Icons.Outlined.Home, hasNews = false),        BottomNavigationItem(label = "Search", route = "Search", selectedIcon = Icons.Filled.Search, unselectedIcon = Icons.Outlined.Search, hasNews = false),
        BottomNavigationItem(label = "Settings", route = "SettingsScreen", selectedIcon = Icons.Filled.Settings, unselectedIcon = Icons.Outlined.Settings, hasNews = true),
    )

    var selectedItemIndex : Int by rememberSaveable { mutableIntStateOf(0) }

    BottomBarScreen(bottomNavItems = bottomNavItems, selectedItemIndex = 0, onIndexChange = {}, navigateTo = {})

    /*MusixTheme {
        Greeting("Android")
    }*/
}