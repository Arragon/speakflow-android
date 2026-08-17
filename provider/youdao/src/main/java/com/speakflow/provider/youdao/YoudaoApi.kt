package com.speakflow.provider.youdao

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import okhttp3.FormBody
import okhttp3.RequestBody
import retrofit2.http.Body
import retrofit2.http.POST
import java.security.MessageDigest

/** 文本翻译响应 */
@Serializable
data class YoudaoTranslateResponse(
    @SerialName("errorCode") val errorCode: String,
    @SerialName("translation") val translation: List<String>? = null,
    @SerialName("basic") val basic: YoudaoBasic? = null
)

@Serializable
data class YoudaoBasic(
    @SerialName("phonetic") val phonetic: String? = null,
    @SerialName("explains") val explains: List<String>? = null
)

/** TTS 响应：返回可播放的 mp3 URL 或本地路径 */
@Serializable
data class YoudaoTtsResponse(
    @SerialName("errorCode") val errorCode: String,
    @SerialName("url") val url: String? = null,
    @SerialName("filePath") val filePath: String? = null
)

interface YoudaoApi {
    @POST("api")
    suspend fun translate(@Body body: RequestBody): YoudaoTranslateResponse

    @POST("ttsapi")
    suspend fun tts(@Body body: RequestBody): YoudaoTtsResponse
}

/** 有道 v3 签名：sha256(appKey + input(前20字符) + salt + curtime + appSecret) */
fun youdaoSign(q: String): Triple<String, String, String> {
    val salt = System.currentTimeMillis().toString()
    val curtime = (System.currentTimeMillis() / 1000).toString()
    val input = if (q.length > 20) q.take(20) else q
    val raw = YoudaoConfig.APP_KEY + input + salt + curtime + YoudaoConfig.APP_SECRET
    val sign = MessageDigest.getInstance("SHA-256")
        .digest(raw.toByteArray(Charsets.UTF_8))
        .joinToString("") { "%02x".format(it) }
    return Triple(sign, salt, curtime)
}

fun youdaoForm(vararg pairs: Pair<String, String>): RequestBody {
    val b = FormBody.Builder()
    pairs.forEach { (k, v) -> b.add(k, v) }
    return b.build()
}
