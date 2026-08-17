package com.speakflow.data.repo

import com.speakflow.data.db.AppDatabase
import com.speakflow.data.db.entity.toDomain
import com.speakflow.data.db.entity.toEntity
import com.speakflow.domain.model.MediaItem
import com.speakflow.domain.repository.MediaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MediaRepositoryImpl @Inject constructor(db: AppDatabase) : MediaRepository {
    private val dao = db.mediaDao()

    override suspend fun import(item: MediaItem) = dao.insert(item.toEntity())

    override suspend fun get(id: String): MediaItem? = dao.get(id)?.toDomain()

    override fun observeAll(): Flow<List<MediaItem>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun delete(id: String) {
        dao.get(id)?.let { dao.delete(it) }
    }
}
