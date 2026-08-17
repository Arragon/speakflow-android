package com.speakflow.ml.asr

import android.content.Context
import com.arthenica.ffmpegkit.FFmpegKit
import com.speakflow.domain.model.AudioFile
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 把任意音视频抽取为 whisper 需要的 16k 单声道 PCM/WAV。
 * 视频文件必须先抽取音轨才能做 ASR。
 */
interface AudioExtractor {
    fun to16kMono(audio: AudioFile, force: Boolean = false): AudioFile
}

@Singleton
class FfmpegAudioExtractor @Inject constructor(
    @ApplicationContext private val ctx: Context
) : AudioExtractor {
    override fun to16kMono(audio: AudioFile, force: Boolean): AudioFile {
        val out = File(ctx.cacheDir, "asr_${audio.path.hashCode()}.wav")
        if (out.exists() && !force) return AudioFile(out.absolutePath)
        // -ac 1 单声道, -ar 16000 采样率, -f s16le PCM；此处输出 wav 便于复用
        FFmpegKit.execute(
            "-y -i \"${audio.path}\" -ac 1 -ar 16000 -c:a pcm_s16le \"${out.absolutePath}\""
        )
        return AudioFile(out.absolutePath)
    }
}
