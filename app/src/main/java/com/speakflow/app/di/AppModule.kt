package com.speakflow.app.di

import com.speakflow.domain.provider.CloudStorageProvider
import com.speakflow.domain.provider.DictionaryProvider
import com.speakflow.domain.provider.PhoneticAligner
import com.speakflow.domain.provider.SubtitleGenerator
import com.speakflow.domain.provider.Translator
import com.speakflow.domain.provider.TtsProvider
import com.speakflow.ml.align.SimpleAligner
import com.speakflow.ml.asr.FakeWhisperSubtitleGenerator
import com.speakflow.provider.baidupan.BaiduPanProvider
import com.speakflow.provider.dictionary.ecdict.ECDICTDictionary
import com.speakflow.provider.quark.QuarkProvider
import com.speakflow.provider.youdao.YoudaoTranslator
import com.speakflow.provider.youdao.YoudaoTtsProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 把 domain 定义的抽象能力绑定到具体实现：
 * - 字幕生成：Demo 阶段绑定 FakeWhisperSubtitleGenerator（无需 native 模型即可体验）。
 *   正式接入时改为绑定 WhisperSubtitleGenerator（需先实现 LibWhisperEngine + NDK 编译）。
 * - 词典：ECDICT 离线优先；有道在线词典可作为补充（在 Youdao 模块扩展后加入 Set）。
 * - 翻译 / TTS：有道智云（未配置密钥时 TTS 自动降级到系统 TTS）。
 * - 网盘：百度（官方）+ 夸克（实验，默认不启用）。
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds @IntoSet
    abstract fun bindSubtitleGenerator(g: FakeWhisperSubtitleGenerator): SubtitleGenerator

    @Binds @IntoSet
    abstract fun bindDictionary(g: ECDICTDictionary): DictionaryProvider

    @Binds @IntoSet
    abstract fun bindTranslator(g: YoudaoTranslator): Translator

    @Binds @IntoSet
    abstract fun bindTts(g: YoudaoTtsProvider): TtsProvider

    @Binds @IntoSet
    abstract fun bindBaiduPan(g: BaiduPanProvider): CloudStorageProvider

    @Binds @IntoSet
    abstract fun bindQuark(g: QuarkProvider): CloudStorageProvider

    @Binds @IntoSet
    abstract fun bindAligner(g: SimpleAligner): PhoneticAligner
}
