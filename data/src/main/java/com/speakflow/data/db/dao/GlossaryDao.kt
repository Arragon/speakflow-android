package com.speakflow.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.speakflow.data.db.entity.GlossaryCacheEntity

@Dao
interface GlossaryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: GlossaryCacheEntity)

    @Query("SELECT * FROM glossary_cache WHERE word = :word AND locale = :locale")
    suspend fun get(word: String, locale: String): GlossaryCacheEntity?
}
