package com.ipodmodern.audio.core.database.dao

import androidx.room.*
import com.ipodmodern.audio.core.database.entity.AlbumEntity
import com.ipodmodern.audio.core.database.entity.ArtistEntity
import com.ipodmodern.audio.core.database.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TrackDao {
    @Query("SELECT * FROM tracks ORDER BY artist COLLATE NOCASE ASC, album COLLATE NOCASE ASC, trackNumber ASC")
    fun getAllTracks(): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE artist = :artist ORDER BY album COLLATE NOCASE ASC, trackNumber ASC")
    fun getTracksByArtist(artist: String): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE album = :album ORDER BY trackNumber ASC")
    fun getTracksByAlbum(album: String): Flow<List<TrackEntity>>

    @Query("SELECT * FROM tracks WHERE id = :id")
    suspend fun getTrackById(id: Long): TrackEntity?

    @Query("SELECT * FROM tracks WHERE filePath = :path LIMIT 1")
    suspend fun getTrackByPath(path: String): TrackEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTracks(tracks: List<TrackEntity>)

    @Delete
    suspend fun deleteTrack(track: TrackEntity)

    @Query("DELETE FROM tracks WHERE filePath = :path")
    suspend fun deleteByPath(path: String)

    @Query("DELETE FROM tracks")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM tracks")
    fun getTrackCount(): Flow<Int>
}

@Dao
interface AlbumDao {
    @Query("SELECT * FROM albums ORDER BY title COLLATE NOCASE ASC")
    fun getAllAlbums(): Flow<List<AlbumEntity>>

    @Query("SELECT * FROM albums WHERE artist = :artist ORDER BY year DESC, title COLLATE NOCASE ASC")
    fun getAlbumsByArtist(artist: String): Flow<List<AlbumEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbum(album: AlbumEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAlbums(albums: List<AlbumEntity>)

    @Query("DELETE FROM albums")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM albums")
    fun getAlbumCount(): Flow<Int>
}

@Dao
interface ArtistDao {
    @Query("SELECT * FROM artists ORDER BY name COLLATE NOCASE ASC")
    fun getAllArtists(): Flow<List<ArtistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtist(artist: ArtistEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArtists(artists: List<ArtistEntity>)

    @Query("DELETE FROM artists")
    suspend fun clearAll()

    @Query("SELECT COUNT(*) FROM artists")
    fun getArtistCount(): Flow<Int>
}

@Dao
interface PlaylistDao {
    @Transaction
    @Query("SELECT * FROM playlists ORDER BY isAiGenerated DESC, createdAt DESC")
    fun getAllPlaylistsWithTracks(): Flow<List<com.ipodmodern.audio.core.database.entity.PlaylistWithTracks>>

    @Transaction
    @Query("SELECT * FROM playlists WHERE id = :playlistId")
    fun getPlaylistWithTracks(playlistId: Long): Flow<com.ipodmodern.audio.core.database.entity.PlaylistWithTracks?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlaylist(playlist: com.ipodmodern.audio.core.database.entity.PlaylistEntity): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRef(crossRef: com.ipodmodern.audio.core.database.entity.PlaylistTrackCrossRef)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCrossRefs(crossRefs: List<com.ipodmodern.audio.core.database.entity.PlaylistTrackCrossRef>)

    @Query("DELETE FROM playlist_track_cross_ref WHERE playlistId = :playlistId AND trackId = :trackId")
    suspend fun removeTrackFromPlaylist(playlistId: Long, trackId: Long)

    @Query("DELETE FROM playlist_track_cross_ref WHERE playlistId = :playlistId")
    suspend fun clearPlaylistTracks(playlistId: Long)

    @Query("DELETE FROM playlists WHERE id = :playlistId")
    suspend fun deletePlaylist(playlistId: Long)

    @Query("UPDATE playlists SET name = :newName WHERE id = :playlistId")
    suspend fun renamePlaylist(playlistId: Long, newName: String)

    @Transaction
    suspend fun createPlaylistWithTracks(
        playlist: com.ipodmodern.audio.core.database.entity.PlaylistEntity,
        trackIds: List<Long>
    ): Long {
        val playlistId = insertPlaylist(playlist)
        val crossRefs = trackIds.mapIndexed { idx, tId ->
            com.ipodmodern.audio.core.database.entity.PlaylistTrackCrossRef(
                playlistId = playlistId,
                trackId = tId,
                orderIndex = idx
            )
        }
        insertCrossRefs(crossRefs)
        return playlistId
    }
}
