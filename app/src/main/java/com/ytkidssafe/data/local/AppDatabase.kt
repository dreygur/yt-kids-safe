package com.ytkidssafe.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ytkidssafe.data.local.converter.Converters
import com.ytkidssafe.data.local.dao.ChannelDao
import com.ytkidssafe.data.local.dao.PlaylistDao
import com.ytkidssafe.data.local.dao.ProfileDao
import com.ytkidssafe.data.local.dao.VideoDao
import com.ytkidssafe.data.local.entity.*

@Database(
    entities = [
        ProfileEntity::class,
        ChannelEntity::class,
        ProfileChannelCrossRef::class,
        PlaylistEntity::class,
        ProfilePlaylistCrossRef::class,
        VideoEntity::class
    ],
    version = 5,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun channelDao(): ChannelDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun videoDao(): VideoDao
}
