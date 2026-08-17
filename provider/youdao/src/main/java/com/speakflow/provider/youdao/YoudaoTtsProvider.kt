package com.speakflow.provider.youdao

import android.content.Context
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.speakflow.domain.model.AudioFile
import com.speakflow.domain.provider.TtsProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.OkHttpClient
import java.io.File
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * 查读音 / 朗读。优先级：
 * 1) 有道智云 TTS（音质好、多音色），需配置密钥；
 * 2) 系统 TextToSpeech 离线兜底，无需联网也无密钥，保证 Demo 可用。
 */
@Singleton
class YoudaoTtsProvider @Inject constructor(
    @ApplicationContext private val ctx: Context,
    private val api: YoudaoApi
) : TtsProvider {

    override val supportedLocales = setOf("en", "zh", "ja", "ko", "fr", "de")

    private val client = OkHttpClient()
    private val initDeferred = CompletableDeferred<Boolean>()
    private val tts = TextToSpeech(ctx) { status ->
        initDeferred.complete(status == TextToSpeech.SUCCESS)
    }

    override suspend fun speak(text: String, locale: String, speed: Float): AudioFile {
        initDeferred.await()
        if (YoudaoConfig.configured) {
            runCatching { youdaoTts(text, locale, speed) }.getOrNull()?.let { return it }
        }
        return systemTts(text, locale)
    }

    private suspend fun youdaoTts(text: String, locale: String, speed: Float): AudioFile? {
        val (sign, salt, curtime) = youdaoSign(text)
        val resp = api.tts(
            youdaoForm(
                "q" to text, "langType" to locale, "appKey" to YoudaoConfig.APP_KEY,
                "salt" to salt, "sign" to sign, "signType" to "v3", "curtime" to curtime,
                "voice" to "0", "speed" to speed.toString()
            )
        )
        if (resp.errorCode != "0" || resp.url == null) return null
        val file = File(ctx.cacheDir, "tts_${text.hashCode()}.mp3")
        val req = okhttp3.Request.Builder().url(resp.url).build()
        client.newCall(req).execute().use { r ->
            r.body?.byteStream()?.use { inp -> file.outputStream().use { inp.copyTo(it) } }
        }
        return AudioFile(file.absolutePath)
    }

    @Suppress("DEPRECATION")
    private suspend fun systemTts(text: String, locale: String): AudioFile =
        suspendCancellableCoroutine { cont ->
            val file = File(ctx.cacheDir, "tts_sys_${text.hashCode()}.wav")
            tts.setLanguage(Locale.forLanguageTag(locale))
            tts.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}
                override fun onDone(utteranceId: String?) {
                    if (!cont.isCompleted) cont.resume(AudioFile(file.absolutePath))
                }
                override fun onError(utteranceId: String?) {
                    if (!cont.isCompleted) cont.resumeWithException(RuntimeException("system tts failed"))
                }
            })
            val code = tts.synthesizeToFile(text, null, file.absolutePath)
            if (code != TextToSpeech.SUCCESS) {
                if (!cont.isCompleted) cont.resumeWithException(RuntimeException("tts synthesize failed"))
            }
        }
}
