package com.speakflow.feature.cloud

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speakflow.domain.model.AuthResult
import com.speakflow.domain.model.CloudFile
import com.speakflow.domain.model.MediaItem
import com.speakflow.domain.model.MediaSource
import com.speakflow.domain.provider.CloudStorageProvider
import com.speakflow.domain.registry.ProviderRegistry
import com.speakflow.domain.usecase.GetPlayableUrlUseCase
import com.speakflow.domain.usecase.ImportMediaUseCase
import com.speakflow.domain.usecase.ListCloudFilesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CloudViewModel @Inject constructor(
    private val registry: ProviderRegistry,
    private val listFiles: ListCloudFilesUseCase,
    private val getUrl: GetPlayableUrlUseCase,
    private val import: ImportMediaUseCase
) : ViewModel() {

    val providers: List<CloudStorageProvider> = registry.allCloudProviders()

    private val _currentProviderId = MutableStateFlow<String?>(providers.firstOrNull()?.id)
    val currentProviderId = _currentProviderId.asStateFlow()

    private val _authed = MutableStateFlow(false)
    val authed = _authed.asStateFlow()

    private val _files = MutableStateFlow<List<CloudFile>>(emptyList())
    val files = _files.asStateFlow()

    fun selectProvider(id: String) {
        _currentProviderId.value = id
        _authed.value = false
        _files.value = emptyList()
    }

    fun authenticate(ctx: Context) {
        val id = _currentProviderId.value ?: return
        viewModelScope.launch {
            val res = runCatching { registry.cloudProvider(id).authenticate(ctx) }.getOrElse { AuthResult.Error(it.message ?: "") }
            _authed.value = res is AuthResult.Success
            if (res is AuthResult.Success) loadFiles()
        }
    }

    fun loadFiles(folderId: String = "/") {
        val id = _currentProviderId.value ?: return
        viewModelScope.launch {
            runCatching { listFiles(id, folderId) }.onSuccess { _files.value = it }
        }
    }

    fun getPlayableUrl(file: CloudFile, onResult: (String) -> Unit) {
        val id = _currentProviderId.value ?: return
        viewModelScope.launch {
            runCatching { getUrl(id, file) }.onSuccess { onResult(it) }
        }
    }

    /** 把云端直链作为云端来源媒体加入练习库 */
    fun addToLibrary(file: CloudFile, url: String) {
        val providerId = _currentProviderId.value ?: return
        viewModelScope.launch {
            import(
                MediaItem(
                    id = UUID.randomUUID().toString(),
                    title = file.name,
                    uri = url,
                    source = MediaSource.CLOUD_BAIDU,
                    cloudProvider = providerId
                )
            )
        }
    }
}
