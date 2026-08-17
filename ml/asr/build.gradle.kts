plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
}

android {
    namespace = "com.speakflow.ml.asr"
    compileSdk = 34
    defaultConfig { minSdk = 24 }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
    // 注：whisper.cpp 原生库（NDK/CMake）作为后续步骤接入；
    // Demo 阶段通过 AppModule 绑定 FakeWhisperSubtitleGenerator，无需 native 产物。
}

dependencies {
    implementation(projects.domain)
    implementation(projects.core)

    implementation(libs.core.ktx)
    implementation(libs.coroutines.android)
    implementation(libs.ffmpeg.kit.full)
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
}
