package com.speakflow.provider.dictionary.ecdict

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import com.speakflow.domain.model.GlossaryEntry
import com.speakflow.domain.provider.DictionaryProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 离线英中词典：读取 ECDICT 导出的 SQLite（表 stardict）。
 * 1) 将 ecdict.sqlite 放入 app assets/ecdict.db（或预置到数据库目录）；
 * 2) 首次打开时从 assets 拷贝；
 * 3) 查询 word / phonetic / translation。
 * 未放入完整词库时，使用内置 MINI 词表兜底，保证 Demo 可查少量常用词。
 */
@Singleton
class ECDICTDictionary @Inject constructor(
    @ApplicationContext private val ctx: Context
) : DictionaryProvider {

    override val supportedLocales = setOf("en")

    private var db: SQLiteDatabase? = null

    private fun open(): SQLiteDatabase? {
        if (db != null) return db
        val dbFile = ctx.getDatabasePath("ecdict.db")
        if (!dbFile.exists()) {
            try {
                ctx.assets.open("ecdict.db").use { input ->
                    dbFile.parentFile?.mkdirs()
                    dbFile.outputStream().use { input.copyTo(it) }
                }
            } catch (_: Exception) {
                return null
            }
        }
        db = runCatching {
            SQLiteDatabase.openDatabase(dbFile.absolutePath, null, SQLiteDatabase.OPEN_READONLY)
        }.getOrNull()
        return db
    }

    override suspend fun lookup(word: String, locale: String): GlossaryEntry? {
        open()?.let { database ->
            database.rawQuery(
                "SELECT word, phonetic, translation FROM stardict WHERE word = ? COLLATE NOCASE LIMIT 1",
                arrayOf(word)
            ).use { c ->
                if (c.moveToFirst()) {
                    val phonetic = c.getString(1)
                    val translation = c.getString(2)
                    return GlossaryEntry(
                        word = word,
                        locale = locale,
                        phonetic = phonetic.takeIf { it.isNotBlank() },
                        definitions = translation.split("\\n".toRegex())
                            .map { it.trim() }.filter { it.isNotBlank() }
                    )
                }
            }
        }
        return fallback(word, locale)
    }

    private fun fallback(word: String, locale: String): GlossaryEntry? =
        MINI[word.lowercase()]?.let { (ph, def) ->
            GlossaryEntry(word = word, locale = locale, phonetic = ph, definitions = listOf(def))
        }

    companion object {
        private val MINI = mapOf(
            "hello" to ("/həˈloʊ/" to "你好；喂"),
            "welcome" to ("/ˈwelkəm/" to "欢迎"),
            "practice" to ("/ˈpræktɪs/" to "练习；实践"),
            "sentence" to ("/ˈsentəns/" to "句子"),
            "clearly" to ("/ˈklɪrli/" to "清楚地"),
            "repeat" to ("/rɪˈpiːt/" to "重复；复述"),
            "ready" to ("/ˈredi/" to "准备好的")
        )
    }
}
