package com.speakflow.ml.align

import com.speakflow.domain.model.AudioFile
import com.speakflow.domain.model.TimedWord
import com.speakflow.domain.provider.PhoneticAligner
import javax.inject.Inject

/**
 * 逐词精修占位实现：在已知（或估算）时长内把单词均匀分布。
 * 生产环境可替换为 WhisperX / aeneas / MFA 等强制对齐，得到更精确的时间戳。
 */
class SimpleAligner @Inject constructor() : PhoneticAligner {
    override val supportedLocales = setOf("en", "zh", "ja", "de", "fr")

    override suspend fun align(audio: AudioFile, text: String, locale: String): List<TimedWord> {
        val words = text.split(Regex("\\s+")).filter { it.isNotBlank() }
        val durationMs = estimateDuration(words.size)
        val step = if (words.isNotEmpty()) durationMs / words.size else durationMs
        return words.mapIndexed { i, w -> TimedWord(w, i * step, (i + 1) * step) }
    }

    private fun estimateDuration(wordCount: Int) = (wordCount * 600L).coerceAtLeast(1000L)
}
