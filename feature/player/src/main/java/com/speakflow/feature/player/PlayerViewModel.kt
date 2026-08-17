package com.speakflow.feature.player

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem as ExoMediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.speakflow.domain.model.AudioFile
import com.speakflow.domain.model.MediaItem
import com.speakflow.domain.repository.MediaRepository
import com.speakflow.domain.repository.SubtitleRepository
import com.speakflow.domain.usecase.GenerateSubtitlesUseCase
import com.speakflow.domain.usecase.LookupWordUseCase
import com.speakflow.domain.usecase.TranslateUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/** A-B 循环区间（毫秒） */
data class ABLoop(val startMs: Long, val endMs: Long)

/**
 * 播放器核心 ViewModel：
 * - 包装 ExoPlayer，并以 40ms 频率把播放进度暴露为 StateFlow
 * - 驱动交互式逐词/逐句字幕高亮与点击定位
 * - 管理 A-B 循环、录音、查词面板
 */
@HiltViewModel
class PlayerViewModel @Inject constructor(
    app: Application,
    private val generateSubtitles: GenerateSubtitlesUseCase,
    private val lookupWord: LookupWordUseCase,
    private val translate: TranslateUseCase,
    private val subtitleRepo: SubtitleRepository,
    private val mediaRepo: MediaRepository
) : AndroidViewModel(app) {

    private val player = ExoPlayer.Builder(app).build().apply { playWhenReady = false }

    private val _position = MutableStateFlow(0L)
    val position = _position.asStateFlow()
    private val _duration = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()
    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()
    private val _subtitle = MutableStateFlow<com.speakflow.domain.model.SubtitleTrack?>(null)
    val subtitle = _subtitle.asStateFlow()
    private val _abLoop = MutableStateFlow<ABLoop?>(null)
    val abLoop = _abLoop.asStateFlow()
    private val _glossary = MutableStateFlow<GlossaryUi?>(null)
    val glossary = _glossary.asStateFlow()
    private val _recordingPath = MutableStateFlow<String?>(null)
    val recordingPath = _recordingPath.asStateFlow()
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating = _isGenerating.asStateFlow()

    private val recorder = AudioRecorder(app)
    private var tickJob: Job? = null

    init {
        player.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                _duration.value = player.duration.coerceAtLeast(0)
                if (state == Player.STATE_ENDED) _isPlaying.value = false
            }
            override fun onIsPlayingChanged(playing: Boolean) {
                _isPlaying.value = playing
                if (playing) startTicking() else tickJob?.cancel()
            }
        })
    }

    fun loadById(id: String, locale: String = "en") {
        viewModelScope.launch {
            mediaRepo.get(id)?.let { load(it, locale) }
        }
    }

    fun load(item: MediaItem, locale: String = "en") {
        player.setMediaItem(ExoMediaItem.fromUri(item.uri))
        player.prepare()
        viewModelScope.launch { _subtitle.value = subtitleRepo.get(item.id, locale) }
    }

    private fun startTicking() {
        tickJob?.cancel()
        tickJob = viewModelScope.launch {
            while (true) {
                delay(40)
                val pos = player.currentPosition
                _position.value = pos
                _duration.value = player.duration.coerceAtLeast(0)
                _abLoop.value?.let { loop ->
                    if (pos >= loop.endMs) player.seekTo(loop.startMs)
                }
            }
        }
    }

    fun playPause() { if (player.isPlaying) player.pause() else player.play() }
    fun seekTo(ms: Long) { player.seekTo(ms) }

    fun setLoopStart() =
        _abLoop.value = ABLoop(player.currentPosition, _abLoop.value?.endMs ?: player.duration.coerceAtLeast(0))
    fun setLoopEnd() =
        _abLoop.value = ABLoop(_abLoop.value?.startMs ?: 0, player.currentPosition)
    fun clearLoop() = _abLoop.value?.let { _abLoop.value = null }

    fun generateSubtitles(mediaId: String, audioPath: String, locale: String = "en") {
        viewModelScope.launch {
            _isGenerating.value = true
            runCatching { generateSubtitles(mediaId, AudioFile(audioPath), locale) }
                .onSuccess { _subtitle.value = it }
            _isGenerating.value = false
        }
    }

    fun onWordTap(word: String, locale: String = "en") {
        viewModelScope.launch {
            _glossary.value = GlossaryUi(loading = true)
            val entry = lookupWord(word, locale)
            val trans = entry?.let { runCatching { translate(word, locale, "zh") }.getOrNull() }
            _glossary.value = GlossaryUi(entry = entry, loading = false, translation = trans)
        }
    }
    fun dismissGlossary() { _glossary.value = null }

    fun startRecording() { _recordingPath.value = recorder.start() }
    fun stopRecording() { recorder.stop() }
    fun playRecording() {
        _recordingPath.value?.let { path ->
            player.stop()
            player.setMediaItem(ExoMediaItem.fromUri(Uri.fromFile(java.io.File(path))))
            player.prepare()
            player.play()
        }
    }

    override fun onCleared() {
        tickJob?.cancel()
        player.release()
        recorder.stop()
        super.onCleared()
    }
}
