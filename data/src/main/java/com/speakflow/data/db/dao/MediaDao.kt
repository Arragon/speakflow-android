package com.speakflow.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.speakflow.data.db.entity.MediaItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MediaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: MediaItemEntity)

    @Query("SELECT * FROM media_items ORDER BY importedAt DESC")
    fun observeAll(): Flow<List<MediaItemEntity>>

    @Query("SELECT * FROM media_items WHERE id = :id")
    suspend fun get(id: String): MediaItemEntity?

    @Delete
    suspend fun delete(item: MediaItemEntity)
}
