package com.speakflow.domain.provider

import android.content.Context
import com.speakflow.domain.model.*
import kotlinx.coroutines.flow.Flow

/**
 * 所有外部能力均以接口形式定义，具体实现位于 provider/ 与 ml/ 模块。
 * 这样新增语言或替换供应商时，feature/domain 层完全无需改动。
 */

/** 实时语音识别（流式，可选能力） */
interface SpeechRecognizer {
    val supportedLocales: Set<String>
    fun stream(audio: AudioSource, locale: String): Flow<PartialResult>
}

/** 离线/在线生成字幕：产品核心 AI 能力 */
interface SubtitleGenerator {
    val supportedLocales: Set<String>
    val requiresNetwork: Boolean get() = false
    /** 传入音频 + 目标语言，返回带逐词时间的字幕轨 */
    suspend fun generate(audio: AudioFile, locale: String): SubtitleTrack
}

/** 查词（离线优先，可在线兜底） */
interface DictionaryProvider {
    val supportedLocales: Set<String>
    suspend fun lookup(word: String, locale: String): GlossaryEntry?
}

/** 翻译 */
interface Translator {
    val supportedLocales: Set<String>
    suspend fun translate(text: String, src: String, dst: String): String
}

/** 查读音 / 文本朗读 */
interface TtsProvider {
    val supportedLocales: Set<String>
    suspend fun speak(text: String, locale: String, speed: Float = 1f): AudioFile
}

/** 网盘接入（百度官方 / 夸克实验 等） */
interface CloudStorageProvider {
    val id: String
    val label: String
    val isOfficial: Boolean
    suspend fun authenticate(ctx: Context): AuthResult
    suspend fun isAuthorized(): Boolean
    suspend fun list(folderId: String = "/"): List<CloudFile>
    suspend fun getPlayableUrl(file: CloudFile): String
}

/** 逐词精修（强制对齐，可选增强） */
interface PhoneticAligner {
    val supportedLocales: Set<String>
    suspend fun align(audio: AudioFile, text: String, locale: String): List<TimedWord>
}

/** 流式识别的增量结果 */
data class PartialResult(val text: String, val isFinal: Boolean)
data class AudioSource(val sampleRate: Int = 16000)
