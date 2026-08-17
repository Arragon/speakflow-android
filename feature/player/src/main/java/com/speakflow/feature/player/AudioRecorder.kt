package com.speakflow.feature.player

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

/**
 * 跟读录音封装。录音文件写入 cache 目录，回播由播放器负责。
 */
class AudioRecorder(private val ctx: Context) {
    private var recorder: MediaRecorder? = null
    var outputPath: String? = null
        private set

    fun start(fileName: String = "rec_${System.currentTimeMillis()}.m4a"): String {
        val file = File(ctx.cacheDir, fileName)
        val r = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(ctx)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
        r.setAudioSource(MediaRecorder.AudioSource.MIC)
        r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
        r.setOutputFile(file.absolutePath)
        r.prepare()
        r.start()
        recorder = r
        outputPath = file.absolutePath
        return file.absolutePath
    }

    fun stop() {
        try {
            recorder?.stop()
        } catch (_: Exception) {
            // 极短录音可能抛异常，忽略
        }
        recorder?.release()
        recorder = null
    }
}
