package com.speakflow.data.db.entity

import androidx.room.Entity
import com.speakflow.domain.model.SubtitleSource
import com.speakflow.domain.model.SubtitleTrack

/**
 * 字幕轨持久化。cues（含逐词时间）以 JSON 整列存储，避免对嵌套结构过度范式化。
 */
@Entity(tableName = "subtitle_tracks", primaryKeys = ["mediaId", "locale"])
data class SubtitleTrackEntity(
    val mediaId: String,
    val locale: String,
    val source: String,        // SubtitleSource.name
    val json: String           // 序列化后的 SubtitleTrack（主要用 cues）
)
