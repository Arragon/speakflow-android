package com.speakflow.provider.youdao

import com.speakflow.domain.provider.Translator
import javax.inject.Inject

/**
 * 有道智云文本翻译。未配置密钥时返回空字符串，调用方（如查词面板）应做降级处理。
 */
class YoudaoTranslator @Inject constructor(
    private val api: YoudaoApi
) : Translator {

    override val supportedLocales = setOf("en", "zh", "ja", "ko", "fr", "de", "ru", "es", "pt")

    override suspend fun translate(text: String, src: String, dst: String): String {
        if (!YoudaoConfig.configured) return ""
        val (sign, salt, curtime) = youdaoSign(text)
        val resp = runCatching {
            api.translate(
                youdaoForm(
                    "q" to text, "from" to src, "to" to dst,
                    "appKey" to YoudaoConfig.APP_KEY, "salt" to salt,
                    "sign" to sign, "signType" to "v3", "curtime" to curtime
                )
            )
        }.getOrNull() ?: return ""
        if (resp.errorCode != "0") return ""
        return resp.translation?.firstOrNull() ?: resp.basic?.explains?.firstOrNull() ?: ""
    }
}
