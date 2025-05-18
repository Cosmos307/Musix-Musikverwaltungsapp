package com.mountainlabs.musix.ui.state

import com.mountainlabs.musix.domain.SearchItem

/**
 * State of the search screen, used to refresh the result-list<searchItem> when necessary
 */
data class SearchScreenState (
    val inputText: String,
    val isSearchActive: Boolean,
    val item: List<SearchItem>,
)

/*
sealed interface SearchScreenState {
    data object Loading : SearchScreenState

    data class SearchScreenState : SearchScreenState {
        val inputText: String,
        val isSearchActive: Boolean,
        val..
    }
}
*/