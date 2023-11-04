package jp.wataju.util

import java.lang.NumberFormatException

object IsNumeric {
    fun isNumeric(string: String): Boolean {
        return try {
            string.toInt()
            true
        } catch (e: NumberFormatException) {
            false
        }
    }
}
