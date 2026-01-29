package eu.ottop.yamlauncher.utils

import android.content.Context
import android.graphics.Typeface
import java.io.File

class FontManager private constructor(private val context: Context) {

    private val customFontDir = File(context.filesDir, "custom_fonts")

    companion object {
        private var instance: FontManager? = null

        fun getInstance(context: Context): FontManager {
            if (instance == null) {
                instance = FontManager(context.applicationContext)
            }
            return instance!!
        }
    }

    init {
        if (!customFontDir.exists()) {
            customFontDir.mkdirs()
        }
    }

    fun getFont(fontName: String): Typeface? {
        return try {
            android.util.Log.d("FontManager", "Loading font: $fontName")

            when {
                fontName == "system" -> {
                    android.util.Log.d("FontManager", "Font is system, returning null")
                    null
                }
                fontName.startsWith("custom:") -> {
                    val fileName = fontName.substringAfter("custom:")
                    val fontFile = File(customFontDir, fileName)
                    android.util.Log.d("FontManager", "Looking for custom font file: ${fontFile.absolutePath}")

                    if (fontFile.exists()) {
                        val typeface = Typeface.createFromFile(fontFile)
                        android.util.Log.d("FontManager", "Successfully loaded custom font from file")
                        typeface
                    } else {
                        android.util.Log.e("FontManager", "Custom font file does not exist: ${fontFile.absolutePath}")
                        null
                    }
                }
                else -> {
                    // Handle built-in fonts
                    android.util.Log.d("FontManager", "Loading built-in font: $fontName")
                    Typeface.create(fontName, Typeface.NORMAL)
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("FontManager", "Failed to load font: $fontName", e)
            e.printStackTrace()
            null
        }
    }

    fun isCustomFont(fontName: String): Boolean {
        return fontName.startsWith("custom:")
    }

    fun getCustomFontPath(fontName: String): String? {
        if (!fontName.startsWith("custom:")) {
            return null
        }
        val fileName = fontName.substringAfter("custom:")
        val fontFile = File(customFontDir, fileName)
        return if (fontFile.exists()) fontFile.absolutePath else null
    }

    fun listCustomFonts(): List<String> {
        return customFontDir.listFiles()
            ?.filter { it.extension in listOf("ttf", "otf", "woff", "woff2") }
            ?.map { it.name }
            ?: emptyList()
    }

    fun deleteCustomFont(fontName: String): Boolean {
        val fontFile = File(customFontDir, fontName)
        return fontFile.delete()
    }

    fun clearAllCustomFonts() {
        customFontDir.listFiles()?.forEach { it.delete() }
    }

    fun getCustomFontsDir(): File {
        return customFontDir
    }
}
