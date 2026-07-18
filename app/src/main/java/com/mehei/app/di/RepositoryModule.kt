package com.mehei.app.di

import com.mehei.app.data.repository.ArtistRepositoryImpl
import com.mehei.app.data.repository.BookingRepositoryImpl
import com.mehei.app.data.repository.MockChatRepositoryImpl
import com.mehei.app.domain.repository.ArtistRepository
import com.mehei.app.domain.repository.BookingRepository
import com.mehei.app.domain.repository.ChatRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideArtistRepository(impl: ArtistRepositoryImpl): ArtistRepository {
        return impl
    }

    @Provides
    @Singleton
    fun provideBookingRepository(impl: BookingRepositoryImpl): BookingRepository {
        return impl
    }

    @Provides
    @Singleton
    fun provideChatRepository(impl: MockChatRepositoryImpl): ChatRepository {
        return impl
    }
}
