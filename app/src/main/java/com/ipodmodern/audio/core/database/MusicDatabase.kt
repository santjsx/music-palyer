package com.ipodmodern.audio.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ipodmodern.audio.core.database.dao.AlbumDao
import com.ipodmodern.audio.core.database.dao.ArtistDao
import com.ipodmodern.audio.core.database.dao.TrackDao
import com.ipodmodern.audio.core.database.entity.AlbumEntity
import com.ipodmodern.audio.core.database.entity.ArtistEntity
import com.ipodmodern.audio.core.database.entity.TrackEntity

@Database(
    entities = [
        TrackEntity::class,
        AlbumEntity::class,
        ArtistEntity::class,
        com.ipodmodern.audio.core.database.entity.PlaylistEntity::class,
        com.ipodmodern.audio.core.database.entity.PlaylistTrackCrossRef::class
    ],
    version = 2,
    exportSchema = false
)
abstract class MusicDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun albumDao(): AlbumDao
    abstract fun artistDao(): ArtistDao
    abstract fun playlistDao(): com.ipodmodern.audio.core.database.dao.PlaylistDao

    companion object {
        @Volatile
        private var INSTANCE: MusicDatabase? = null

        fun getInstance(context: Context): MusicDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MusicDatabase::class.java,
                    "ipod_modern_music.db"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
