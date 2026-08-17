package com.speakflow.feature.cloud

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.speakflow.domain.model.CloudFile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CloudScreen(
    onFinished: () -> Unit,
    vm: CloudViewModel = hiltViewModel()
) {
    val providers = vm.providers
    val currentId by vm.currentProviderId.collectAsStateWithLifecycle()
    val authed by vm.authed.collectAsStateWithLifecycle()
    val files by vm.files.collectAsStateWithLifecycle()
    val context = LocalContext.current

    Scaffold(
        topBar = { TopAppBar(title = { Text("云端文件") }) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            Row(modifier = Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                vm.providers.forEach { p ->
                    AssistChip(
                        selected = p.id == currentId,
                        onClick = { vm.selectProvider(p.id) },
                        label = { Text(p.label + if (!p.isOfficial) "（实验）" else "") }
                    )
                }
            }
            Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                Button(onClick = { vm.authenticate(context) }, enabled = !authed) {
                    Text(if (authed) "已授权" else "授权登录")
                }
                if (authed) {
                    Button(onClick = { vm.loadFiles() }) { Text("刷新列表") }
                }
            }
            LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                items(files, key = { it.id }) { file ->
                    if (file.isDir) {
                        ListItem(headlineContent = { Text("📁 ${file.name}") },
                            modifier = Modifier.clickable { vm.loadFiles(file.path) })
                    } else {
                        ListItem(
                            headlineContent = { Text(file.name) },
                            trailingContent = {
                                Button(onClick = {
                                    vm.getPlayableUrl(file) { url ->
                                        vm.addToLibrary(file, url)
                                        Toast.makeText(context, "已加入练习库", Toast.LENGTH_SHORT).show()
                                        onFinished()
                                    }
                                }) { Text("加入练习库") }
                            }
                        )
                    }
                }
            }
        }
    }
}
