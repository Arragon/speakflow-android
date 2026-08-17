package com.speakflow.ml.asr

import com.speakflow.domain.model.TimedWord

/**
 * whisper.cpp 推理结果（已在 native 层解析为结构化数据）。
 * 与 domain 的 Cue/TimedWord 对应，由生成器负责映射。
 */
data class WhisperSegment(
    val startMs: Long,
    val endMs: Long,
    val text: String,
    val words: List<TimedWord>
)

data class WhisperResult(
    val language: String,
    val segments: List<WhisperSegment>
)
