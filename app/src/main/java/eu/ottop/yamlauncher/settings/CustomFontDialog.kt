package eu.ottop.yamlauncher.settings

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import eu.ottop.yamlauncher.R
import java.io.File

class CustomFontDialog(private val activity: FragmentActivity) {

    private val customFontDir = File(activity.filesDir, "custom_fonts")
    private var onFontSelected: ((String) -> Unit)? = null
    private var performFilePicker: ActivityResultLauncher<Intent>? = null
    private var currentDialog: AlertDialog? = null

    init {
        // Create custom fonts directory if it doesn't exist
        if (!customFontDir.exists()) {
            customFontDir.mkdirs()
        }
    }

    fun setOnFontSelectedListener(listener: (String) -> Unit) {
        this.onFontSelected = listener
    }

    fun show() {
        val customFonts = listCustomFonts()

        val dialogView = LayoutInflater.from(activity)
            .inflate(R.layout.dialog_custom_font, null)

        val listView = dialogView.findViewById<ListView>(R.id.customFontList)
        val adapter = ArrayAdapter(activity, android.R.layout.simple_list_item_1, customFonts)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            onFontSelected?.invoke("custom:${customFonts[position]}")
            currentDialog?.dismiss()
        }

        val dialog = AlertDialog.Builder(activity)
            .setTitle(R.string.select_custom_font)
            .setView(dialogView)
            .setPositiveButton(R.string.import_font) { _, _ ->
                openFontPicker()
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                currentDialog = null
            }
            .create()

        currentDialog = dialog

        // Initialize file picker after dialog creation
        performFilePicker = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    importFontFile(uri)
                }
            }
        }

        dialog.show()
    }

    private fun openFontPicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "font/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("font/ttf", "font/otf", "font/woff", "font/woff2"))
        }
        performFilePicker?.launch(Intent.createChooser(intent, "Select Font File"))
    }

    private fun importFontFile(uri: Uri) {
        try {
            val inputStream = activity.contentResolver.openInputStream(uri)
            val fileName = getFileName(uri) ?: "custom_font_${System.currentTimeMillis()}.ttf"
            val outputFile = File(customFontDir, fileName)

            inputStream?.use { input ->
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            // Refresh the dialog with new font list
            currentDialog?.dismiss()
            show()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            activity.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val index = cursor.getColumnIndex("_display_name")
                    if (index >= 0) {
                        result = cursor.getString(index)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1) {
                result = result?.substring(cut!! + 1)
            }
        }
        return result
    }

    private fun listCustomFonts(): List<String> {
        return customFontDir.listFiles()
            ?.filter { it.extension in listOf("ttf", "otf", "woff", "woff2") }
            ?.map { it.name }
            ?: emptyList()
    }

    fun getCustomFontPath(fontName: String): String? {
        val fontFile = File(customFontDir, fontName)
        return if (fontFile.exists()) fontFile.absolutePath else null
    }

    fun deleteCustomFont(fontName: String): Boolean {
        val fontFile = File(customFontDir, fontName)
        return fontFile.delete()
    }

    fun clearAllCustomFonts() {
        customFontDir.listFiles()?.forEach { it.delete() }
    }
}
