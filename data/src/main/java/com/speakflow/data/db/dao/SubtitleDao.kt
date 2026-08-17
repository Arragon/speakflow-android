package com.speakflow.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.speakflow.data.db.entity.SubtitleTrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SubtitleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(entity: SubtitleTrackEntity)

    @Query("SELECT * FROM subtitle_tracks WHERE mediaId = :mediaId AND locale = :locale LIMIT 1")
    suspend fun get(mediaId: String, locale: String): SubtitleTrackEntity?

    @Query("SELECT * FROM subtitle_tracks WHERE mediaId = :mediaId")
    fun observe(mediaId: String): Flow<SubtitleTrackEntity?>
}
