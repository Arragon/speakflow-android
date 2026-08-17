package com.speakflow.domain.usecase

import com.speakflow.domain.model.AudioFile
import com.speakflow.domain.model.CloudFile
import com.speakflow.domain.model.GlossaryEntry
import com.speakflow.domain.model.MediaItem
import com.speakflow.domain.model.SubtitleTrack
import com.speakflow.domain.provider.SubtitleGenerator
import com.speakflow.domain.registry.ProviderRegistry
import com.speakflow.domain.repository.GlossaryRepository
import com.speakflow.domain.repository.MediaRepository
import com.speakflow.domain.repository.SubtitleRepository
import javax.inject.Inject

/** 导入媒体到本地库 */
class ImportMediaUseCase @Inject constructor(
    private val repo: MediaRepository
) {
    suspend operator fun invoke(item: MediaItem) = repo.import(item)
}

/** 列出媒体库 */
class ObserveMediaUseCase @Inject constructor(
    private val repo: MediaRepository
) {
    operator fun invoke() = repo.observeAll()
}

/**
 * 一键生成字幕：选择该语言的 SubtitleGenerator → 推理 → 持久化。
 * 默认端侧 whisper.cpp（无网络），可经 ProviderRegistry 切换到云端实现。
 */
class GenerateSubtitlesUseCase @Inject constructor(
    private val registry: ProviderRegistry,
    private val subtitleRepo: SubtitleRepository
) {
    suspend operator fun invoke(
        mediaId: String,
        audio: AudioFile,
        locale: String
    ): SubtitleTrack {
        val generator: SubtitleGenerator = registry.subtitleGeneratorsFor(locale)
        val track = generator.generate(audio, locale)
            .copy(mediaId = mediaId)
        subtitleRepo.save(track)
        return track
    }
}

/** 查词：先查缓存，再走 provider，命中后回写缓存 */
class LookupWordUseCase @Inject constructor(
    private val registry: ProviderRegistry,
    private val glossaryRepo: GlossaryRepository
) {
    suspend operator fun invoke(word: String, locale: String): GlossaryEntry? {
        glossaryRepo.lookup(word, locale)?.let {
            return it.copy(fromCache = true)
        }
        val entry = registry.dictionariesFor(locale).lookup(word, locale)
        entry?.let { glossaryRepo.cache(it) }
        return entry
    }
}

/** 翻译整句/段落 */
class TranslateUseCase @Inject constructor(
    private val registry: ProviderRegistry
) {
    suspend operator fun invoke(text: String, src: String, dst: String): String =
        registry.translatorsFor(dst).translate(text, src, dst)
}

/** 列出某网盘目录 */
class ListCloudFilesUseCase @Inject constructor(
    private val registry: ProviderRegistry
) {
    suspend operator fun invoke(providerId: String, folderId: String = "/"): List<CloudFile> =
        registry.cloudProvider(providerId).list(folderId)
}

/** 获取网盘文件可播放直链 */
class GetPlayableUrlUseCase @Inject constructor(
    private val registry: ProviderRegistry
) {
    suspend operator fun invoke(providerId: String, file: CloudFile): String =
        registry.cloudProvider(providerId).getPlayableUrl(file)
}
