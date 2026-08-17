package com.speakflow.data.repo

import com.speakflow.data.db.AppDatabase
import com.speakflow.data.db.toDomain
import com.speakflow.data.db.toEntity
import com.speakflow.domain.model.GlossaryEntry
import com.speakflow.domain.repository.GlossaryRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GlossaryRepositoryImpl @Inject constructor(db: AppDatabase) : GlossaryRepository {
    private val dao = db.glossaryDao()

    override suspend fun lookup(word: String, locale: String): GlossaryEntry? =
        dao.get(word, locale)?.toDomain()

    override suspend fun cache(entry: GlossaryEntry) = dao.upsert(entry.toEntity())
}
