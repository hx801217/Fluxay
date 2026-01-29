package com.eink.launcher.utils

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.OpenableColumns
import android.widget.TextView
import androidx.documentfile.provider.DocumentFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * 字体选择和管理工具
 * 支持从本地存储选择字体文件
 */
object FontSelector {

    private const val CUSTOM_FONT_DIR = "EInkFonts"
    private const val FONT_SHARED_PREF = "custom_font_path"
    private const val DEFAULT_FONT = "default"

    /**
     * 支持的字体文件扩展名
     */
    private val SUPPORTED_FONT_EXTENSIONS = listOf(
        "ttf", "otf", "ttc"
    )

    /**
     * 应用自定义字体到TextView
     */
    fun applyFontToTextView(context: Context, textView: TextView, fontPath: String? = null) {
        val typeface = getCustomTypeface(context, fontPath)
        textView.typeface = typeface
    }

    /**
     * 获取自定义字体Typeface
     */
    fun getCustomTypeface(context: Context, fontPath: String? = null): Typeface? {
        val path = fontPath ?: getSavedCustomFontPath(context)
            ?: return null // 使用默认字体

        return try {
            Typeface.createFromFile(path)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 保存自定义字体路径
     */
    fun saveCustomFontPath(context: Context, fontPath: String) {
        context.getSharedPreferences("eink_launcher_prefs", Context.MODE_PRIVATE)
            .edit()
            .putString(FONT_SHARED_PREF, fontPath)
            .apply()
    }

    /**
     * 获取保存的自定义字体路径
     */
    fun getSavedCustomFontPath(context: Context): String? {
        return context.getSharedPreferences("eink_launcher_prefs", Context.MODE_PRIVATE)
            .getString(FONT_SHARED_PREF, null)
    }

    /**
     * 检查字体文件是否有效
     */
    fun isValidFontFile(file: File): Boolean {
        if (!file.exists() || !file.isFile) {
            return false
        }

        val extension = file.extension.lowercase()
        return SUPPORTED_FONT_EXTENSIONS.contains(extension) && file.length() > 0
    }

    /**
     * 获取自定义字体目录
     */
    fun getCustomFontDir(context: Context): File {
        val fontDir = File(context.getExternalFilesDir(null), CUSTOM_FONT_DIR)
        if (!fontDir.exists()) {
            fontDir.mkdirs()
        }
        return fontDir
    }

    /**
     * 列出所有可用的自定义字体
     */
    fun listCustomFonts(context: Context): List<FontInfo> {
        val fontDir = getCustomFontDir(context)
        if (!fontDir.exists() || !fontDir.isDirectory) {
            return emptyList()
        }

        return fontDir.listFiles()?.mapNotNull { file ->
            if (isValidFontFile(file)) {
                FontInfo(
                    name = file.nameWithoutExtension,
                    path = file.absolutePath,
                    fileName = file.name
                )
            } else {
                null
            }
        } ?: emptyList()
    }

    /**
     * 复制字体文件到自定义字体目录
     */
    suspend fun copyFontToCustomDir(
        context: Context,
        sourceUri: Uri,
        targetFileName: String
    ): Result<String> = withContext(Dispatchers.IO) {
        try {
            val fontDir = getCustomFontDir(context)
            val targetFile = File(fontDir, targetFileName)

            context.contentResolver.openInputStream(sourceUri)?.use { input ->
                FileOutputStream(targetFile).use { output ->
                    input.copyTo(output)
                }
            }

            Result.success(targetFile.absolutePath)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * 删除自定义字体
     */
    fun deleteCustomFont(context: Context, fontPath: String): Boolean {
        val file = File(fontPath)
        val deleted = file.delete()

        // 如果删除的是当前使用的字体，清除设置
        if (deleted && fontPath == getSavedCustomFontPath(context)) {
            saveCustomFontPath(context, DEFAULT_FONT)
        }

        return deleted
    }

    /**
     * 重置为默认字体
     */
    fun resetToDefaultFont(context: Context) {
        saveCustomFontPath(context, DEFAULT_FONT)
    }

    /**
     * 获取字体文件名
     */
    fun getFontFileName(context: Context, uri: Uri): String? {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && index >= 0) {
                cursor.getString(index)
            } else {
                null
            }
        }
    }

    /**
     * 检查是否使用默认字体
     */
    fun isUsingDefaultFont(context: Context): Boolean {
        return getSavedCustomFontPath(context) == null ||
               getSavedCustomFontPath(context) == DEFAULT_FONT
    }

    /**
     * 字体信息数据类
     */
    data class FontInfo(
        val name: String,
        val path: String,
        val fileName: String
    ) {
        override fun toString(): String {
            return name
        }
    }

    /**
     * 获取所有可用的字体选项（包括默认字体）
     */
    fun getAllFontOptions(context: Context): List<FontInfo> {
        val fonts = listCustomFonts(context).toMutableList()

        // 添加默认字体选项
        fonts.add(0, FontInfo(
            name = "默认字体",
            path = DEFAULT_FONT,
            fileName = DEFAULT_FONT
        ))

        return fonts
    }

    /**
     * 从Uri获取文件路径
     * 用于处理不同来源的文件Uri
     */
    fun getFilePathFromUri(context: Context, uri: Uri): String? {
        // DocumentProvider
        if (DocumentsContract.isDocumentUri(context, uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            val split = docId.split(":")
            val type = split[0]

            if ("primary".equals(type, ignoreCase = true)) {
                return Environment.getExternalStorageDirectory().absolutePath + "/" + split[1]
            }
        }

        // MediaStore等
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val index = cursor.getColumnIndex("_data")
            if (cursor.moveToFirst() && index >= 0) {
                return cursor.getString(index)
            }
        }

        return null
    }

    /**
     * 预加载字体到内存
     * 用于性能优化
     */
    fun preloadFont(context: Context, fontPath: String): Typeface? {
        return getCustomTypeface(context, fontPath)
    }

    /**
     * 应用字体到所有TextView
     * 递归遍历View树
     */
    fun applyFontToViewTree(view: android.view.View, typeface: Typeface?) {
        if (view is TextView && typeface != null) {
            view.typeface = typeface
        }

        if (view is android.view.ViewGroup) {
            for (i in 0 until view.childCount) {
                applyFontToViewTree(view.getChildAt(i), typeface)
            }
        }
    }
}
