package com.speakflow.provider.baidupan

import android.content.Context
import com.speakflow.domain.model.AuthResult
import com.speakflow.domain.model.CloudFile
import com.speakflow.domain.provider.CloudStorageProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import retrofit2.http.GET
import retrofit2.http.Query
import javax.inject.Inject
import javax.inject.Singleton
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

/** 百度网盘凭证（开放平台创建应用后填入） */
object BaiduConfig {
    var API_KEY: String = ""
    var SECRET: String = ""
    val configured: Boolean get() = API_KEY.isNotBlank() && SECRET.isNotBlank()
}

private val Context.dataStore by preferencesDataStore("baidu_pan_auth")
private val KEY_TOKEN = stringPreferencesKey("access_token")
private val KEY_REFRESH = stringPreferencesKey("refresh_token")

@Serializable
data class DeviceCodeResp(
    @SerialName("device_code") val deviceCode: String,
    @SerialName("user_code") val userCode: String,
    @SerialName("verification_url") val verificationUrl: String,
    @SerialName("interval") val interval: Int = 5,
    @SerialName("expires_in") val expiresIn: Int = 300
)

@Serializable
data class TokenResp(
    @SerialName("access_token") val accessToken: String? = null,
    @SerialName("refresh_token") val refreshToken: String? = null,
    @SerialName("error") val error: String? = null
)

@Serializable
data class FileListResp(
    @SerialName("list") val list: List<FileItem> = emptyList()
)

@Serializable
data class FileItem(
    @SerialName("fs_id") val fsId: Long,
    @SerialName("path") val path: String,
    @SerialName("server_filename") val name: String,
    @SerialName("size") val size: Long,
    @SerialName("isdir") val isDir: Int
)

@Serializable
data class FileMetaResp(
    @SerialName("list") val list: List<FileMeta> = emptyList()
)

@Serializable
data class FileMeta(
    @SerialName("fs_id") val fsId: Long,
    @SerialName("dlink") val dlink: String? = null
)

interface BaiduPanApi {
    @GET("https://openapi.baidu.com/oauth/2.0/device/code")
    suspend fun deviceCode(
        @Query("client_id") clientId: String,
        @Query("scope") scope: String = "basic,netdisk"
    ): DeviceCodeResp

    @GET("https://openapi.baidu.com/oauth/2.0/token")
    suspend fun token(
        @Query("grant_type") grantType: String = "device_token",
        @Query("code") code: String,
        @Query("client_id") clientId: String,
        @Query("client_secret") clientSecret: String
    ): TokenResp

    @GET("https://pan.baidu.com/rest/2.0/xpan/file")
    suspend fun list(
        @Query("method") method: String = "list",
        @Query("access_token") token: String,
        @Query("dir") dir: String = "/"
    ): FileListResp

    @GET("https://pan.baidu.com/rest/2.0/xpan/multimedia")
    suspend fun filemetas(
        @Query("method") method: String = "filemetas",
        @Query("access_token") token: String,
        @Query("fsids") fsids: String,
        @Query("dlink") dlink: Int = 1
    ): FileMetaResp
}

/**
 * 百度网盘（官方开放平台）：OAuth2 设备码授权 + 文件列表 + 直链播放。
 * 接入步骤：在开放平台创建应用，把 API_KEY / SECRET 填入 BaiduConfig。
 */
@Singleton
class BaiduPanProvider @Inject constructor(
    @ApplicationContext private val ctx: Context,
    private val api: BaiduPanApi
) : CloudStorageProvider {

    override val id = "baidupan"
    override val label = "百度网盘"
    override val isOfficial = true

    override suspend fun authenticate(c: Context): AuthResult {
        if (!BaiduConfig.configured) return AuthResult.Error("未配置百度网盘 API_KEY/SECRET")
        val code = api.deviceCode(BaiduConfig.API_KEY)
        // 实际产品应引导用户在浏览器打开 code.verificationUrl 并输入 code.userCode
        var waited = 0
        while (waited < code.expiresIn) {
            delay(code.interval * 1000L)
            waited += code.interval
            val t = api.token(code = code.deviceCode, clientId = BaiduConfig.API_KEY, clientSecret = BaiduConfig.SECRET)
            if (t.accessToken != null) {
                ctx.dataStore.edit {
                    it[KEY_TOKEN] = t.accessToken
                    if (t.refreshToken != null) it[KEY_REFRESH] = t.refreshToken
                }
                return AuthResult.Success
            }
            if (t.error != null && t.error != "authorization_pending") break
        }
        return AuthResult.Error("授权未完成（请在浏览器完成登录后重试）")
    }

    override suspend fun isAuthorized(): Boolean =
        ctx.dataStore.data.first()[KEY_TOKEN] != null

    override suspend fun list(folderId: String): List<CloudFile> {
        val token = ctx.dataStore.data.first()[KEY_TOKEN] ?: return emptyList()
        return api.list(token, dir = folderId).list.map {
            CloudFile(
                id = it.fsId.toString(),
                name = it.name,
                sizeBytes = it.size,
                isDir = it.isDir == 1,
                path = it.path
            )
        }
    }

    override suspend fun getPlayableUrl(file: CloudFile): String {
        val token = ctx.dataStore.data.first()[KEY_TOKEN] ?: return ""
        val meta = api.filemetas(token, fsids = "[${file.id}]")
        return meta.list.firstOrNull()?.dlink
            ?.plus("&access_token=$token") // 直链需带 token
            ?: ""
    }
}
