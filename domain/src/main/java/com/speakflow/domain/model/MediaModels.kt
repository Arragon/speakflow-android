package com.speakflow.domain.model

import kotlinx.serialization.Serializable

/**
 * 媒体来源：本地文件 / 百度网盘 / 夸克网盘。
 * 与具体网盘实现解耦——UI 只关心来源类型。
 */
enum class MediaSource { LOCAL, CLOUD_BAIDU, CLOUD_QUARK }

/**
 * 一个可练习的媒体文件（音频或视频）。
 * @param uri        本地 content uri 或云端可播放直链
 * @param cloudProvider 当 source 为云端时，对应的 CloudStorageProvider.id
 */
@Serializable
data class MediaItem(
    val id: String,
    val title: String,
    val uri: String,
    val durationMs: Long = 0L,
    val source: MediaSource = MediaSource.LOCAL,
    val cloudProvider: String? = null,
    val importedAt: Long = System.currentTimeMillis()
)

/** 云端文件节点（浏览网盘时返回） */
@Serializable
data class CloudFile(
    val id: String,
    val name: String,
    val sizeBytes: Long,
    val isDir: Boolean,
    val path: String,
    val playableUrl: String? = null
)

/** 网盘授权结果 */
sealed interface AuthResult {
    data object Success : AuthResult
    data class Error(val message: String) : AuthResult
}

/** 本地音频文件句柄（ASR / 音频抽取使用） */
@JvmInline
value class AudioFile(val path: String)
