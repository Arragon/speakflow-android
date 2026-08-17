package com.speakflow.data.repo

import com.speakflow.data.db.AppDatabase
import com.speakflow.data.db.toDomain
import com.speakflow.data.db.toEntity
import com.speakflow.domain.model.SubtitleTrack
import com.speakflow.domain.repository.SubtitleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubtitleRepositoryImpl @Inject constructor(db: AppDatabase) : SubtitleRepository {
    private val dao = db.subtitleDao()

    override suspend fun save(track: SubtitleTrack) = dao.save(track.toEntity())

    override suspend fun get(mediaId: String, locale: String): SubtitleTrack? =
        dao.get(mediaId, locale)?.toDomain()

    override fun observe(mediaId: String): Flow<SubtitleTrack?> =
        dao.observe(mediaId).map { it?.toDomain() }
}
