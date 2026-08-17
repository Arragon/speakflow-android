package com.speakflow.feature.player

import com.speakflow.domain.model.GlossaryEntry

/** 查词面板 UI 状态 */
data class GlossaryUi(
    val loading: Boolean,
    val entry: GlossaryEntry? = null,
    val translation: String? = null
)
