package com.speakflow.feature.player

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.speakflow.domain.model.Cue

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlayerScreen(
    mediaId: String,
    onBack: () -> Unit,
    vm: PlayerViewModel = hiltViewModel()
) {
    val position by vm.position.collectAsStateWithLifecycle()
    val duration by vm.duration.collectAsStateWithLifecycle()
    val isPlaying by vm.isPlaying.collectAsStateWithLifecycle()
    val subtitle by vm.subtitle.collectAsStateWithLifecycle()
    val abLoop by vm.abLoop.collectAsStateWithLifecycle()
    val glossary by vm.glossary.collectAsStateWithLifecycle()
    val isGenerating by vm.isGenerating.collectAsStateWithLifecycle()

    LaunchedEffect(mediaId) { vm.loadById(mediaId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("跟读练习") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 媒体区（音频可替换为 PlayerView 视频画面）
            Surface(
                tonalElevation = 2.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(12.dp)
            ) {
                Box(contentAlignment = Alignment.Center) { Text("音频播放区") }
            }

            // 控制条
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(onClick = vm::playPause) {
                    Icon(
                        if (isPlaying) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                        contentDescription = "播放/暂停"
                    )
                }
                Text(formatTime(position), fontSize = 12.sp)
                SliderValue(position, duration, vm::seekTo)
                Text(formatTime(duration), fontSize = 12.sp)

                IconButton(onClick = vm::setLoopStart) { Icon(Icons.Filled.Repeat, "A点") }
                IconButton(onClick = vm::setLoopEnd) { Icon(Icons.Filled.Repeat, "B点") }
                if (abLoop != null) {
                    Button(onClick = vm::clearLoop) { Text("取消循环") }
                }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = { vm.generateSubtitles(mediaId, "demo_audio_path") },
                    enabled = !isGenerating
                ) {
                    if (isGenerating) CircularProgressIndicator(modifier = Modifier.height(16.dp))
                    else Icon(Icons.Filled.Subtitles, null)
                    Text(" 生成字幕")
                }
            }

            HorizontalDivider()

            // 交互式字幕（逐句/逐词定位 + 高亮）
            val listState = rememberLazyListState()
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                if (subtitle == null) {
                    item { Text("暂无字幕，点击「生成字幕」用 AI 识别。", modifier = Modifier.padding(16.dp)) }
                }
                items(subtitle?.cues ?: emptyList()) { cue ->
                    TranscriptCue(cue = cue, position = position, onSeek = vm::seekTo, onWordTap = vm::onWordTap)
                }
            }

            // 录音控制条
            RecordingBar(
                onStart = vm::startRecording,
                onStop = vm::stopRecording,
                onPlay = vm::playRecording
            )
        }

        // 查词面板
        if (glossary != null) {
            GlossarySheet(state = glossary!!, onDismiss = vm::dismissGlossary)
        }
    }
}

@Composable
private fun SliderValue(position: Long, duration: Long, onSeek: (Long) -> Unit) {
    androidx.compose.material3.Slider(
        value = if (duration > 0) position.toFloat() / duration else 0f,
        onValueChange = { onSeek((it * duration).toLong()) },
        modifier = Modifier.weight(1f)
    )
}

@Composable
private fun TranscriptCue(
    cue: Cue,
    position: Long,
    onSeek: (Long) -> Unit,
    onWordTap: (String) -> Unit
) {
    val isActive = cue.contains(position)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSeek(cue.startMs) }
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isActive) {
            Box(
                modifier = Modifier
                    .height(36.dp)
                    .width(3.dp)
                    .padding(end = 6.dp)
            ) { }
        }
        cue.words.forEach { w ->
            val hot = position in w.startMs..w.endMs
            Text(
                text = w.word,
                fontSize = 18.sp,
                fontWeight = if (hot) FontWeight.Bold else FontWeight.Normal,
                color = if (hot) MaterialTheme.colorScheme.primary else Color.Unspecified,
                modifier = Modifier
                    .clickable { onWordTap(w.word) }
                    .padding(end = 4.dp)
            )
        }
        if (cue.words.isEmpty()) {
            Text(cue.text, fontSize = 18.sp, modifier = Modifier.clickable { onWordTap(cue.text) })
        }
    }
}

@Composable
private fun RecordingBar(onStart: () -> Unit, onStop: () -> Unit, onPlay: () -> Unit) {
    Surface(tonalElevation = 2.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("跟读录音", fontWeight = FontWeight.Medium)
            Button(onClick = onStart) { Icon(Icons.Filled.Mic, null); Text(" 录音") }
            Button(onClick = onStop) { Text("停止") }
            Button(onClick = onPlay) { Text("回放") }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GlossarySheet(state: GlossaryUi, onDismiss: () -> Unit) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(Modifier.padding(20.dp)) {
            if (state.loading) {
                CircularProgressIndicator()
            } else {
                val e = state.entry
                if (e == null) {
                    Text("未找到该词释义。")
                } else {
                    Text(e.word, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    e.phonetic?.let { Text("/$it/", color = Color.Gray) }
                    e.definitions.forEach { Text("• $it") }
                    if (e.synonyms.isNotEmpty()) Text("同/近义：${e.synonyms.joinToString("、")}")
                    state.translation?.let { Text("翻译：$it", color = MaterialTheme.colorScheme.primary) }
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val s = (ms / 1000).toInt()
    return "%02d:%02d".format(s / 60, s % 60)
}
