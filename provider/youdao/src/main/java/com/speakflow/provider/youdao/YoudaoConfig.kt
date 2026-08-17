package com.speakflow.provider.youdao

/**
 * 有道智云凭证。创建应用后填入（建议通过 BuildConfig / 远程配置注入，避免硬编码）。
 * 未配置时，相关能力自动降级（见 YoudaoTtsProvider 的本地 TTS 兜底）。
 */
object YoudaoConfig {
    var APP_KEY: String = ""
    var APP_SECRET: String = ""
    val configured: Boolean get() = APP_KEY.isNotBlank() && APP_SECRET.isNotBlank()
}
