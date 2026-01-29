package eu.ottop.yamlauncher.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.PreferenceFragmentCompat
import eu.ottop.yamlauncher.R

class UISettingsFragment : PreferenceFragmentCompat(), TitleProvider {

    private var fontSpinnerPreference: FontSpinnerPreference? = null
    private lateinit var sharedPreferenceManager: SharedPreferenceManager

    private val pickFontLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val requestCode = FontSpinnerPreference.REQUEST_CODE_PICK_FONT
        val resultCode = result.resultCode
        val data = result.data

        if (resultCode == android.app.Activity.RESULT_OK && data != null) {
            fontSpinnerPreference?.onActivityResult(requestCode, resultCode, data)
        } else if (resultCode == android.app.Activity.RESULT_CANCELED) {
            Toast.makeText(
                requireContext(),
                "Font selection cancelled",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.ui_preferences, rootKey)

        sharedPreferenceManager = SharedPreferenceManager(requireContext())

        // Find the FontSpinnerPreference
        fontSpinnerPreference = findPreference("textFont")

        // Set the callback to handle font picker
        fontSpinnerPreference?.setCallback(object : FontSpinnerPreference.FontPickerCallback {
            override fun startActivityForResultForFontPicker(intent: Intent, requestCode: Int) {
                try {
                    pickFontLauncher.launch(intent)
                } catch (e: Exception) {
                    Toast.makeText(
                        requireContext(),
                        "Failed to open font picker: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })
    }

    override fun onResume() {
        super.onResume()

        // Update summary to show current custom font if one is selected
        val currentFont = sharedPreferenceManager.getTextFont()
        if (currentFont?.startsWith("custom:") == true) {
            val fileName = currentFont.substringAfter(":")
            fontSpinnerPreference?.summary = "Custom: $fileName"
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        fontSpinnerPreference?.onActivityResult(requestCode, resultCode, data)
    }

    override fun getTitle(): String {
        return getString(R.string.ui_settings_title)
    }
}