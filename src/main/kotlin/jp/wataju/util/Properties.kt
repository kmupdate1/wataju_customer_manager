package jp.wataju.util

import java.io.InputStreamReader
import java.util.Properties

object Properties {
    private const val path = "properties"
    private const val jLanguage = "/japanese.properties"
    private const val routePath = "/path.properties"
    private const val conditions = "/condition.properties"

    private val properties = Properties()

    fun getText(key: String): String = properties.getProperty(key) ?: key

    init {
        this::class.java.classLoader.getResourceAsStream("$path$jLanguage")
            .use { stream ->
                properties.load(stream?.let { InputStreamReader(it, Charsets.UTF_8) })
            }
        this::class.java.classLoader.getResourceAsStream("$path$routePath")
            .use { stream ->
                properties.load(stream?.let { InputStreamReader(it, Charsets.UTF_8) })
            }
        this::class.java.classLoader.getResourceAsStream("$path$conditions")
            .use { stream ->
                properties.load(stream?.let { InputStreamReader(it, Charsets.UTF_8) })
            }
    }
}
