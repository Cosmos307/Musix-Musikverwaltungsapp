package com.mountainlabs.musix.dependencyInjection


import com.mountainlabs.musix.network.AzureDataSource
import com.mountainlabs.musix.network.AzureDataSourceImpl
import com.mountainlabs.musix.network.CollectionDataRepository
import com.mountainlabs.musix.network.CollectionDataRepositoryImpl
import com.mountainlabs.musix.network.RemoteCollectionDataSource
import com.mountainlabs.musix.network.RemoteCollectionDataSourceImpl
import com.mountainlabs.musix.ui.AlbumScreenViewModel
import com.mountainlabs.musix.ui.ArtistScreenViewModel
import com.mountainlabs.musix.ui.CallinApiPlaceholderViewModel
import com.mountainlabs.musix.ui.NewCollectionScreenViewModel
import com.mountainlabs.musix.ui.PlayListViewModel
import com.mountainlabs.musix.ui.SearchScreenViewModel
import com.mountainlabs.musix.ui.SongScreenViewModel
import org.koin.androidx.viewmodel.dsl.viewModelOf
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

val appModule = module {
    viewModelOf(::CallinApiPlaceholderViewModel)
    viewModelOf(::SearchScreenViewModel)
    viewModelOf(::SongScreenViewModel)
    viewModelOf(::PlayListViewModel)
    viewModelOf(::AlbumScreenViewModel)
    viewModelOf(::ArtistScreenViewModel)
    viewModelOf(::NewCollectionScreenViewModel)

    singleOf(::CollectionDataRepositoryImpl) bind CollectionDataRepository::class
    singleOf(::RemoteCollectionDataSourceImpl) bind RemoteCollectionDataSource::class
    singleOf(::AzureDataSourceImpl) bind AzureDataSource::class

}
