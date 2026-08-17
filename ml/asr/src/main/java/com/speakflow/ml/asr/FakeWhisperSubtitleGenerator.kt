package com.speakflow.ml.asr

import com.speakflow.domain.model.AudioFile
import com.speakflow.domain.model.Cue
import com.speakflow.domain.model.SubtitleSource
import com.speakflow.domain.model.SubtitleTrack
import com.speakflow.domain.model.TimedWord
import com.speakflow.domain.provider.SubtitleGenerator
import javax.inject.Inject

/**
 * Demo 用假生成器：不依赖 native 模型，直接返回一段带逐词时间的示例字幕，
 * 便于在没有编译 whisper 原生库的设备上直接体验「交互式逐词字幕」。
 * 正式构建时，用 WhisperSubtitleGenerator 替换即可（见 AppModule）。
 */
class FakeWhisperSubtitleGenerator @Inject constructor() : SubtitleGenerator {
    override val supportedLocales = setOf("en", "zh")

    override suspend fun generate(audio: AudioFile, locale: String): SubtitleTrack {
        val script = if (locale == "zh") zhScript else enScript
        return SubtitleTrack(
            mediaId = "", locale = locale, source = SubtitleSource.AI,
            cues = script.mapIndexed { i, (text, words) ->
                Cue(id = "c$i", startMs = i * 4000L, endMs = (i + 1) * 4000L,
                    text = text, words = words)
            }
        )
    }

    private val enScript = listOf(
        "Hello everyone and welcome to today's practice." to listOf("Hello", "everyone", "and", "welcome", "to", "today's", "practice"),
        "Let's read this sentence slowly and clearly." to listOf("Let's", "read", "this", "sentence", "slowly", "and", "clearly"),
        "Repeat after me when you are ready." to listOf("Repeat", "after", "me", "when", "you", "are", "ready")
    ).map { (t, ws) -> t to ws.mapIndexed { j, w -> TimedWord(w, 0, 0).copy(startMs = j * 500L, endMs = (j + 1) * 500L) } }

    private val zhScript = listOf(
        "大家好，欢迎来到今天的练习。" to listOf("大家", "好", "欢迎", "来到", "今天", "的", "练习"),
        "我们一起来慢慢地、清楚地朗读这句话。" to listOf("我们", "一起", "慢慢", "地", "清楚", "地", "朗读", "这", "句", "话")
    ).map { (t, ws) -> t to ws.mapIndexed { j, w -> TimedWord(w, j * 400L, (j + 1) * 400L) } }
}
