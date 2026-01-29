package eu.ottop.yamlauncher.settings

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Toast
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

    companion object {
        private const val TAG = "CustomFontDialog"
    }

    init {
        // Create custom fonts directory if it doesn't exist
        if (!customFontDir.exists()) {
            customFontDir.mkdirs()
            Log.d(TAG, "Created custom fonts directory: ${customFontDir.absolutePath}")
        }
    }

    fun setOnFontSelectedListener(listener: (String) -> Unit) {
        this.onFontSelected = listener
    }

    fun show() {
        val customFonts = listCustomFonts()
        Log.d(TAG, "Showing custom fonts dialog with ${customFonts.size} fonts")

        val dialogView = LayoutInflater.from(activity)
            .inflate(R.layout.dialog_custom_font, null)

        val listView = dialogView.findViewById<ListView>(R.id.customFontList)
        val adapter = ArrayAdapter(activity, android.R.layout.simple_list_item_1, customFonts)
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            val selectedFont = customFonts[position]
            Log.d(TAG, "Selected font: $selectedFont")
            onFontSelected?.invoke("custom:$selectedFont")
            currentDialog?.dismiss()
        }

        val dialog = AlertDialog.Builder(activity)
            .setTitle(R.string.select_custom_font)
            .setView(dialogView)
            .setPositiveButton(R.string.import_font) { _, _ ->
                Log.d(TAG, "Import font button clicked")
                openFontPicker()
            }
            .setNegativeButton(R.string.cancel) { _, _ ->
                Log.d(TAG, "Cancel button clicked")
                currentDialog = null
            }
            .create()

        currentDialog = dialog

        // Initialize file picker after dialog creation
        performFilePicker = activity.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            Log.d(TAG, "File picker result: code=${result.resultCode}, data=${result.data}")
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    importFontFile(uri)
                }
            } else {
                Log.w(TAG, "File picker cancelled or failed")
            }
        }

        dialog.show()
    }

    private fun openFontPicker() {
        try {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "*/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }
            performFilePicker?.launch(Intent.createChooser(intent, "Select Font File"))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open font picker", e)
            Toast.makeText(activity, "Failed to open file picker", Toast.LENGTH_SHORT).show()
        }
    }

    private fun importFontFile(uri: Uri) {
        try {
            Log.d(TAG, "Importing font from URI: $uri")

            val inputStream = activity.contentResolver.openInputStream(uri)
            val fileName = getFileName(uri) ?: "custom_font_${System.currentTimeMillis()}.ttf"
            val outputFile = File(customFontDir, fileName)

            Log.d(TAG, "Saving font to: ${outputFile.absolutePath}")

            inputStream?.use { input ->
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            Log.d(TAG, "Font imported successfully: $fileName")

            // Refresh the dialog with new font list
            currentDialog?.dismiss()
            show()

            Toast.makeText(activity, "Font imported: $fileName", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import font", e)
            Toast.makeText(activity, "Failed to import font: ${e.message}", Toast.LENGTH_SHORT).show()
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
