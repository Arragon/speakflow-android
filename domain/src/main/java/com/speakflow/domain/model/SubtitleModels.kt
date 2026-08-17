package com.speakflow.domain.model

import kotlinx.serialization.Serializable

/**
 * 逐词时间戳：一个词在音视频中的起止位置（毫秒）。
 * 这是「逐词进度拖动定位」的数据基础。
 */
@Serializable
data class TimedWord(
    val word: String,
    val startMs: Long,
    val endMs: Long
)

/**
 * 一句字幕（cue）。words 为空时表示只有整句时间、无逐词粒度。
 */
@Serializable
data class Cue(
    val id: String,
    val startMs: Long,
    val endMs: Long,
    val text: String,
    val words: List<TimedWord> = emptyList()
) {
    /** 该句覆盖的时间区间是否包含给定播放位置 */
    fun contains(posMs: Long): Boolean = posMs in startMs..endMs

    /** 在给定播放位置处于高亮状态的词（用于卡拉OK式高亮），无则 null */
    fun wordAt(posMs: Long): TimedWord? = words.firstOrNull { posMs in it.startMs..it.endMs }
}

enum class SubtitleSource { MANUAL, AI, SRT, NONE }

/**
 * 一条完整字幕轨。支持按语言与来源（手动/SRT/AI）区分。
 */
@Serializable
data class SubtitleTrack(
    val mediaId: String,
    val locale: String,
    val source: SubtitleSource,
    val cues: List<Cue>
) {
    fun cueAt(posMs: Long): Cue? = cues.firstOrNull { it.contains(posMs) }
    fun wordAt(posMs: Long): TimedWord? = cues.firstNotNullOfOrNull { it.wordAt(posMs) }
}
