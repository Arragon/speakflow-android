package com.speakflow.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.speakflow.data.db.dao.GlossaryDao
import com.speakflow.data.db.dao.MediaDao
import com.speakflow.data.db.dao.SubtitleDao
import com.speakflow.data.db.entity.GlossaryCacheEntity
import com.speakflow.data.db.entity.MediaItemEntity
import com.speakflow.data.db.entity.SubtitleTrackEntity

@Database(
    entities = [MediaItemEntity::class, SubtitleTrackEntity::class, GlossaryCacheEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun mediaDao(): MediaDao
    abstract fun subtitleDao(): SubtitleDao
    abstract fun glossaryDao(): GlossaryDao

    companion object {
        const val NAME = "speakflow.db"
    }
}
