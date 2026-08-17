package com.speakflow.ml.asr

import com.speakflow.domain.model.AudioFile

/**
 * whisper.cpp 的 JNI 封装接口。真实实现在 native 层（见 src/main/cpp/native-lib.cpp），
 * 通过 System.loadLibrary("whisper") 加载。Demo 可注入 FakeWhisperEngine。
 */
interface WhisperEngine {
    /** 加载量化模型（ggml .bin）。可缓存，避免重复加载。 */
    fun loadModel(modelPath: String)

    /** 同步推理：输入 16k 单声道 PCM，返回带逐词时间的结果。 */
    fun transcribe(
        audio: AudioFile,
        language: String,
        tokenTimestamps: Boolean
    ): WhisperResult
}
