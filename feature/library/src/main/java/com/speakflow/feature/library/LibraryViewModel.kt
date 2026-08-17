package com.speakflow.feature.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.speakflow.domain.model.MediaItem
import com.speakflow.domain.model.MediaSource
import com.speakflow.domain.usecase.ImportMediaUseCase
import com.speakflow.domain.usecase.ObserveMediaUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(
    observe: ObserveMediaUseCase,
    private val import: ImportMediaUseCase
) : ViewModel() {

    val media: StateFlow<List<MediaItem>> = observe().stateIn(
        viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList()
    )

    fun importFromUri(uri: String, displayName: String) {
        viewModelScope.launch {
            import(
                MediaItem(
                    id = UUID.randomUUID().toString(),
                    title = displayName,
                    uri = uri,
                    source = MediaSource.LOCAL
                )
            )
        }
    }
}
