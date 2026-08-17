package com.speakflow.feature.library

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.speakflow.domain.model.MediaItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LibraryScreen(
    onOpenPlayer: (String) -> Unit,
    onOpenCloud: () -> Unit,
    vm: LibraryViewModel = hiltViewModel()
) {
    val items by vm.media.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val picker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        uri?.let {
            val name = queryDisplayName(context, it) ?: "音频 ${System.currentTimeMillis()}"
            vm.importFromUri(it.toString(), name)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的练习库") },
                actions = {
                    IconButton(onClick = onOpenCloud) { Icon(Icons.Filled.Cloud, "网盘") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                picker.launch(arrayOf("audio/*", "video/*"))
            }) { Icon(Icons.Filled.Add, "导入") }
        }
    ) { padding ->
        if (items.isEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
            ) { Text("还没有素材，点击 + 导入本地音视频") }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                modifier = Modifier.fillMaxSize().padding(padding)
            ) {
                items(items, key = { it.id }) { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                            .clickable { onOpenPlayer(item.id) }
                    ) {
                        ListItem(
                            headlineContent = { Text(item.title) },
                            supportingContent = { Text(item.source.name) }
                        )
                    }
                }
            }
        }
    }
}

private fun queryDisplayName(context: Context, uri: Uri): String? {
    return try {
        context.contentResolver.query(
            uri, arrayOf(MediaStore.MediaColumns.DISPLAY_NAME), null, null, null
        )?.use { c ->
            if (c.moveToFirst()) c.getString(0) else null
        }
    } catch (_: Exception) { null }
}
