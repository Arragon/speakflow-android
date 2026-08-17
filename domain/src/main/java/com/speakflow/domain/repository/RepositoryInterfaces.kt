package com.speakflow.domain.repository

import com.speakflow.domain.model.*
import kotlinx.coroutines.flow.Flow

/** 媒体库持久化 */
interface MediaRepository {
    suspend fun import(item: MediaItem)
    suspend fun get(id: String): MediaItem?
    fun observeAll(): Flow<List<MediaItem>>
    suspend fun delete(id: String)
}

/** 字幕持久化（含逐词时间） */
interface SubtitleRepository {
    suspend fun save(track: SubtitleTrack)
    suspend fun get(mediaId: String, locale: String): SubtitleTrack?
    fun observe(mediaId: String): Flow<SubtitleTrack?>
}

/** 查词结果缓存（避免重复联网） */
interface GlossaryRepository {
    suspend fun lookup(word: String, locale: String): GlossaryEntry?
    suspend fun cache(entry: GlossaryEntry)
}
