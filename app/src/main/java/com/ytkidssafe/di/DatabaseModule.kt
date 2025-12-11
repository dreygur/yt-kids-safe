package com.ytkidssafe.di

import android.content.Context
import androidx.room.Room
import com.ytkidssafe.data.local.AppDatabase
import com.ytkidssafe.data.local.dao.ChannelDao
import com.ytkidssafe.data.local.dao.PlaylistDao
import com.ytkidssafe.data.local.dao.ProfileDao
import com.ytkidssafe.data.local.dao.VideoDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "yt_kids_db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideProfileDao(db: AppDatabase): ProfileDao = db.profileDao()

    @Provides
    fun provideChannelDao(db: AppDatabase): ChannelDao = db.channelDao()

    @Provides
    fun providePlaylistDao(db: AppDatabase): PlaylistDao = db.playlistDao()

    @Provides
    fun provideVideoDao(db: AppDatabase): VideoDao = db.videoDao()
}
