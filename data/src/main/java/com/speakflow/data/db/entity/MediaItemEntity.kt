package com.speakflow.data.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.speakflow.domain.model.MediaItem
import com.speakflow.domain.model.MediaSource

@Entity(tableName = "media_items")
data class MediaItemEntity(
    @PrimaryKey val id: String,
    val title: String,
    val uri: String,
    val durationMs: Long,
    val source: String,            // MediaSource.name
    val cloudProvider: String?,
    val importedAt: Long
)

fun MediaItem.toEntity() = MediaItemEntity(
    id = id, title = title, uri = uri, durationMs = durationMs,
    source = source.name, cloudProvider = cloudProvider, importedAt = importedAt
)

fun MediaItemEntity.toDomain() = MediaItem(
    id = id, title = title, uri = uri, durationMs = durationMs,
    source = MediaSource.valueOf(source), cloudProvider = cloudProvider, importedAt = importedAt
)
