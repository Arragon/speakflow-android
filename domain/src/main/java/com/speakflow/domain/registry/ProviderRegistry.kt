package com.speakflow.domain.registry

import com.speakflow.domain.model.Language
import com.speakflow.domain.provider.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 按语言解析可用能力。所有 provider 在构造时通过 Set<out X> 注入，
 * 由各实现类自行声明 supportedLocales。
 *
 * 新增语言时：只要对应 provider 的 supportedLocales 包含该 locale，
 * 这里就能自动发现，UI 无需改动。
 */
@Singleton
class ProviderRegistry @Inject constructor(
    private val generators: @JvmSuppressWildcards Set<SubtitleGenerator>,
    private val dictionaries: @JvmSuppressWildcards Set<DictionaryProvider>,
    private val translators: @JvmSuppressWildcards Set<Translator>,
    private val ttsProviders: @JvmSuppressWildcards Set<TtsProvider>,
    private val cloudProviders: @JvmSuppressWildcards Set<CloudStorageProvider>,
    private val aligners: @JvmSuppressWildcards Set<PhoneticAligner>
) {
    fun subtitleGeneratorsFor(locale: String): SubtitleGenerator =
        generators.first { locale in it.supportedLocales }

    fun dictionariesFor(locale: String): DictionaryProvider =
        dictionaries.first { locale in it.supportedLocales }

    fun translatorsFor(locale: String): Translator =
        translators.first { locale in it.supportedLocales }

    fun ttsFor(locale: String): TtsProvider =
        ttsProviders.first { locale in it.supportedLocales }

    fun cloudProvider(id: String): CloudStorageProvider =
        cloudProviders.first { it.id == id }

    fun allCloudProviders(): List<CloudStorageProvider> = cloudProviders.toList()

    fun supportedLanguages(): List<Language> =
        Language.BUILTIN.filter { lang ->
            generators.any { lang.code in it.supportedLocales } ||
            dictionaries.any { lang.code in it.supportedLocales }
        }
}
