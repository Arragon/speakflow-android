package com.speakflow.provider.quark

import android.content.Context
import android.util.Log
import com.speakflow.domain.model.AuthResult
import com.speakflow.domain.model.CloudFile
import com.speakflow.domain.provider.CloudStorageProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * ⚠️ 夸克网盘【实验性 / 非官方】适配。
 *
 * 调研结论：夸克网盘目前没有面向第三方的官方开放 API，网络上的方案均为对 Web 端
 * 私有加密协议的反向工程（cookie / sign 调用）。存在以下风险：
 *   - 稳定性差，协议变动即失效；
 *   - 可能违反服务条款，存在账号封禁风险；
 *   - 需要用户手动提供登录态（cookie），有隐私安全隐患。
 *
 * 因此本实现在 MVP 中【默认不启用】，仅在「设置 → 高级」中作为可选项出现，
 * 且会向用户明确提示风险。生产环境接入前请评估合规与稳定性，或等待官方开放。
 *
 * 接入思路（反向工程，仅供参考，非推荐）：
 *   1) 用户提供浏览器登录后的 cookie；
 *   2) 调用 quark 私有接口（如 /drive/v1/file/list）并补 sign 签名；
 *   3) 取得 stoken / 直链后交给播放器播放。
 */
@Singleton
class QuarkProvider @Inject constructor(
    @ApplicationContext private val ctx: Context
) : CloudStorageProvider {

    override val id = "quark"
    override val label = "夸克网盘（实验）"
    override val isOfficial = false

    override suspend fun authenticate(c: Context): AuthResult {
        Log.w("QuarkProvider", "夸克网盘无官方 API，实验性接入需用户提供登录态，存在合规与账号风险。")
        return AuthResult.Error("夸克网盘暂未开放官方 API；实验性接入请在「设置-高级」中手动配置 cookie。")
    }

    override suspend fun isAuthorized(): Boolean = false

    override suspend fun list(folderId: String): List<CloudFile> =
        throw UnsupportedOperationException("Quark 实验性适配未启用。")

    override suspend fun getPlayableUrl(file: CloudFile): String =
        throw UnsupportedOperationException("Quark 实验性适配未启用。")
}
