# SpeakFlow

Android 口语跟读练习应用（初期支持英语，架构预留多语言能力）。

导入本地 / 云端音频视频 → 逐句逐词跟读定位 → 一键查词 / 翻译 / 读音 / 同近义词 → AI 一键生成字幕。

## 功能

1. **导入**：本地音频 / 视频（SAF 选择器）+ 百度网盘 OAuth2 直连导入与播放（夸克网盘为实验性模块，无官方 API，默认不启用）。
2. **跟读**：ExoPlayer 播放 + 用户录音回播；基于字幕时间轴实现逐句 / 逐词进度拖动定位；内置字典抽屉（查词、翻译、读音、同 / 近义词）。
3. **字幕**：借助 ASR 实现语音段落识别并一键生成字幕（Demo 使用内置脚本 `FakeWhisperSubtitleGenerator`，生产可接入 whisper.cpp / Vosk）。

## 架构（多模块 Gradle）

```
app           组合根 (Hilt @IntoSet 绑定各能力实现)
domain        模型 + 能力 Provider 接口 + ProviderRegistry + UseCase（不依赖 Android）
data          Room 持久化 + Repository 实现
feature       library（媒体库）/ player（播放跟读）/ cloud（网盘）
ml            asr（字幕生成）/ align（音素对齐占位）
provider      dictionary-ecdict / youdao（翻译·TTS）/ baidupan / quark
core          通用工具与 UI 基础
```

多语言能力通过 `domain` 层的能力接口抽象：`SubtitleGenerator`、`DictionaryProvider`、`Translator`、`TtsProvider`、`CloudStorageProvider`、`PhoneticAligner`、`SpeechRecognizer`，由 Hilt 以 `@IntoSet` 注入，`ProviderRegistry` 按 `Locale` 路由。新增语种只需实现对应接口并绑定，无需改动 feature 层。

## 构建

- Android Studio + Android Gradle Plugin 8.6 / Kotlin 2.0.21
- 需要 Android SDK 与（生产 whisper 时）NDK
- `./gradlew assembleDebug`

> Demo 模式无需 NDK：`AppModule` 绑定 `FakeWhisperSubtitleGenerator`，可直接运行体验交互字幕。

## 配置（可选）

- 有道智云翻译 / TTS：在 `YoudaoConfig` 注入 `APP_KEY` / `APP_SECRET`，未配置时自动降级为系统 TTS。
- 百度网盘：在 `BaiduPanModule` 提供 `clientId` / `clientSecret`，走设备码 OAuth2 流程。

## 状态

当前为可运行骨架（Scaffold）：核心模块、类型、抽象与 UI 流程已就绪；whisper 原生实现、音素对齐、夸克网盘为后续接入项。
