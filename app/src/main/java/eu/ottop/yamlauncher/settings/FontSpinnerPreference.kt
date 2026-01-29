package eu.ottop.yamlauncher.settings

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import eu.ottop.yamlauncher.R
import java.io.File

class FontSpinnerPreference(context: Context, attrs: AttributeSet? = null) : Preference(context, attrs) {

    interface FontPickerCallback {
        fun startActivityForResultForFontPicker(intent: Intent, requestCode: Int)
    }

    private var entries: Array<CharSequence>? = null
    private var entryValues: Array<CharSequence>? = null
    private var currentValue: String? = null
    private var defaultNo: String? = null
    private var spinner: Spinner? = null
    private var callback: FontPickerCallback? = null

    companion object {
        private const val TAG = "FontSpinnerPreference"
        const val REQUEST_CODE_PICK_FONT = 1001
    }

    fun setCallback(callback: FontPickerCallback) {
        this.callback = callback
    }

    init {
        widgetLayoutResource = R.layout.preference_spinner
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.SpinnerPreference,
            0, 0).apply {

            try {
                entries = getTextArray(R.styleable.SpinnerPreference_android_entries)
                entryValues = getTextArray(R.styleable.SpinnerPreference_android_entryValues)
                defaultNo = getString(R.styleable.SpinnerPreference_android_defaultValue)
            } finally {
                recycle()
            }
        }
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        spinner = holder.findViewById(R.id.preferenceOptions) as Spinner

        // Get activity from context
        activity = context as? Activity

        if (activity == null) {
            Log.e(TAG, "Activity is null, font picker may not work")
        }

        if (entries != null) {
            val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, entries!!)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner?.adapter = adapter
        }

        val selectedIndex = entryValues?.indexOf(currentValue as? CharSequence) ?: entryValues?.indexOf(defaultNo as CharSequence) ?: 0
        spinner?.setSelection(selectedIndex)

        val handler = android.os.Handler(android.os.Looper.getMainLooper())
        handler.postDelayed({
            if (selectedIndex >= 0) {
                summary = entries?.get(selectedIndex)
            }
        }, 0)

        spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val newValue = entryValues?.get(position).toString()

                // Check if custom font is selected
                if (newValue == "custom") {
                    Log.d(TAG, "Custom font selected, opening picker")
                    openFontPicker()
                    // Reset to current value until user selects a font
                    val currentSelectedIndex = entryValues?.indexOf(currentValue as? CharSequence) ?: 0
                    spinner?.post {
                        spinner?.setSelection(currentSelectedIndex)
                    }
                    return@onItemSelected
                }

                if (callChangeListener(newValue)) {
                    currentValue = newValue
                    persistString(newValue)
                    summary = entries?.get(position)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun openFontPicker() {
        try {
            Log.d(TAG, "Opening font picker")

            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                type = "*/*"
                addCategory(Intent.CATEGORY_OPENABLE)
            }

            callback?.startActivityForResultForFontPicker(intent, REQUEST_CODE_PICK_FONT)
                ?: run {
                    Log.e(TAG, "Callback is null, cannot open font picker")
                    Toast.makeText(context, "Font picker not available", Toast.LENGTH_SHORT).show()
                }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to open font picker", e)
            Toast.makeText(context, "Failed to open file picker", Toast.LENGTH_SHORT).show()
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.d(TAG, "onActivityResult: requestCode=$requestCode, resultCode=$resultCode")

        if (requestCode == REQUEST_CODE_PICK_FONT && resultCode == Activity.RESULT_OK) {
            data?.data?.let { uri ->
                importFontFile(uri)
            }
        }
    }

    private fun importFontFile(uri: Uri) {
        try {
            Log.d(TAG, "Importing font from URI: $uri")

            val customFontDir = context.filesDir?.let { File(it, "custom_fonts") }
                ?: return

            if (!customFontDir.exists()) {
                customFontDir.mkdirs()
            }

            val fileName = getFileName(uri) ?: "custom_font_${System.currentTimeMillis()}.ttf"
            val outputFile = File(customFontDir, fileName)

            Log.d(TAG, "Saving font to: ${outputFile.absolutePath}")

            context.contentResolver.openInputStream(uri)?.use { input ->
                outputFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            Log.d(TAG, "Font imported successfully: $fileName")

            // Set the custom font as the current value
            val fontPath = "custom:$fileName"
            if (callChangeListener(fontPath)) {
                currentValue = fontPath
                persistString(fontPath)
                summary = "Custom: $fileName"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to import font", e)
            Toast.makeText(context, "Failed to import font: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFileName(uri: Uri): String? {
        var result: String? = null
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
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

    override fun onClick() {
        spinner?.performClick()
    }

    override fun onAttached() {
        super.onAttached()
        currentValue = getPersistedString(defaultNo)
    }
}
