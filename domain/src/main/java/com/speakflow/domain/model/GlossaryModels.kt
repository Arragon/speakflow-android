package com.speakflow.domain.model

import kotlinx.serialization.Serializable

/**
 * 一条查词结果。覆盖「查词 / 查读音 / 同近义 / 例句」所需字段。
 * 音标与基础释义可由离线 ECDICT 提供；同近义/例句可由 LLM 补充。
 */
@Serializable
data class GlossaryEntry(
    val word: String,
    val locale: String,
    val phonetic: String? = null,        // 音标，如 /ˈwɔːtər/
    val posList: List<String> = emptyList(),   // 词性：n. v. adj. ...
    val definitions: List<String> = emptyList(),// 释义
    val exchange: Map<String, String> = emptyMap(), // 词形变化：过去式/复数...
    val synonyms: List<String> = emptyList(),
    val antonyms: List<String> = emptyList(),
    val examples: List<String> = emptyList(),
    val fromCache: Boolean = false
)
