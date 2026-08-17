package com.speakflow.data.di

import android.content.Context
import androidx.room.Room
import com.speakflow.data.db.AppDatabase
import com.speakflow.data.repo.GlossaryRepositoryImpl
import com.speakflow.data.repo.MediaRepositoryImpl
import com.speakflow.data.repo.SubtitleRepositoryImpl
import com.speakflow.domain.repository.GlossaryRepository
import com.speakflow.domain.repository.MediaRepository
import com.speakflow.domain.repository.SubtitleRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Binds @Singleton
    abstract fun bindMediaRepository(impl: MediaRepositoryImpl): MediaRepository

    @Binds @Singleton
    abstract fun bindSubtitleRepository(impl: SubtitleRepositoryImpl): SubtitleRepository

    @Binds @Singleton
    abstract fun bindGlossaryRepository(impl: GlossaryRepositoryImpl): GlossaryRepository

    companion object {
        @Provides @Singleton
        fun provideDatabase(@ApplicationContext ctx: Context): AppDatabase =
            Room.databaseBuilder(ctx, AppDatabase::class.java, AppDatabase.NAME)
                .fallbackToDestructiveMigration()
                .build()
    }
}
