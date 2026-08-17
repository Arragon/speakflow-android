package com.speakflow.domain.model

/**
 * 语言表达。多语言扩展的核心描述对象：
 * 仅需在 registry / 资源中增加一条 Language + 对应模型资产，即可支持新语言，
 * UI 与 feature 层无需改动。
 */
data class Language(
    val code: String,            // BCP-47，如 "en" / "ja" / "zh"
    val displayName: String,     // 英文 / 日本語 / 中文
    val isRTL: Boolean = false,
    val defaultEnabled: Boolean = true
) {
    companion object {
        val ENGLISH = Language("en", "英语")
        val CHINESE = Language("zh", "中文")
        val JAPANESE = Language("ja", "日本語", defaultEnabled = false)

        val BUILTIN = listOf(ENGLISH, CHINESE, JAPANESE)
    }
}
