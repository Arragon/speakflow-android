package com.speakflow.data.db

import com.speakflow.domain.model.GlossaryEntry
import com.speakflow.domain.model.SubtitleTrack
import com.speakflow.data.db.entity.GlossaryCacheEntity
import com.speakflow.data.db.entity.SubtitleTrackEntity
import kotlinx.serialization.json.Json

private val format = Json { ignoreUnknownKeys = true }

fun SubtitleTrack.toEntity(): SubtitleTrackEntity =
    SubtitleTrackEntity(mediaId, locale, source.name, format.encodeToString(this))

fun SubtitleTrackEntity.toDomain(): SubtitleTrack =
    format.decodeFromString(this.json)

fun GlossaryEntry.toEntity(): GlossaryCacheEntity =
    GlossaryCacheEntity(word, locale, format.encodeToString(this), System.currentTimeMillis())

fun GlossaryCacheEntity.toDomain(): GlossaryEntry =
    format.decodeFromString(this.json)
