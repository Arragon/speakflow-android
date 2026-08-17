package com.speakflow.data.db.entity

import androidx.room.Entity

@Entity(tableName = "glossary_cache", primaryKeys = ["word", "locale"])
data class GlossaryCacheEntity(
    val word: String,
    val locale: String,
    val json: String,            // 序列化后的 GlossaryEntry
    val updatedAt: Long
)
