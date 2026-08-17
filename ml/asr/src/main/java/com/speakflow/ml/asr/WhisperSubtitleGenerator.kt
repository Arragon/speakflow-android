package com.speakflow.ml.asr

import android.content.Context
import com.speakflow.domain.model.AudioFile
import com.speakflow.domain.model.Cue
import com.speakflow.domain.model.SubtitleSource
import com.speakflow.domain.model.SubtitleTrack
import com.speakflow.domain.provider.SubtitleGenerator
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 真实的字幕生成器：抽取音轨 → whisper.cpp 推理（逐词时间戳）→ 映射为 SubtitleTrack。
 * 默认端侧运行，无网络、零流量、保护隐私。
 *
 * 接入步骤：
 * 1) 在 native-lib.cpp 中封装 whisper.cpp，并把结果解析为 WhisperResult；
 * 2) 在 AppModule 中把 LibWhisperEngine（JNI 实现）绑定为 WhisperEngine；
 * 3) 首次使用时下载对应语言的量化模型（ggml）到 cache 并 loadModel。
 */
class WhisperSubtitleGenerator @Inject constructor(
    private val engine: WhisperEngine,
    private val extractor: AudioExtractor,
    @ApplicationContext private val ctx: Context
) : SubtitleGenerator {

    override val supportedLocales: Set<String>
        get() = setOf("en", "zh", "ja", "de", "fr", "es", "ru", "ko")

    override val requiresNetwork: Boolean get() = false

    override suspend fun generate(audio: AudioFile, locale: String): SubtitleTrack {
        val pcm = withContext(Dispatchers.IO) { extractor.to16kMono(audio) }
        val result = withContext(Dispatchers.Default) {
            engine.transcribe(pcm, language = locale, tokenTimestamps = true)
        }
        return SubtitleTrack(
            mediaId = "",
            locale = locale,
            source = SubtitleSource.AI,
            cues = result.segments.mapIndexed { i, seg ->
                Cue(
                    id = "c$i",
                    startMs = seg.startMs,
                    endMs = seg.endMs,
                    text = seg.text,
                    words = seg.words
                )
            }
        )
    }
}
