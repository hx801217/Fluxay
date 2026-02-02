package eu.ottop.yamlauncher.settings

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceFragmentCompat
import eu.ottop.yamlauncher.R

class UISettingsFragment : PreferenceFragmentCompat(), TitleProvider {

    private var fontSpinnerPreference: FontSpinnerPreference? = null
    private lateinit var sharedPreferenceManager: SharedPreferenceManager

    private val pickFontLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        android.util.Log.d("UISettingsFragment", "Font picker result: ${result.resultCode}")
        val requestCode = FontSpinnerPreference.REQUEST_CODE_PICK_FONT
        val resultCode = result.resultCode
        val data = result.data

        if (resultCode == android.app.Activity.RESULT_OK && data != null) {
            android.util.Log.d("UISettingsFragment", "Font selected, processing...")
            fontSpinnerPreference?.onActivityResult(requestCode, resultCode, data)
        } else if (resultCode == android.app.Activity.RESULT_CANCELED) {
            android.util.Log.d("UISettingsFragment", "Font selection cancelled")
            Toast.makeText(
                requireContext(),
                "Font selection cancelled",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        android.util.Log.d("UISettingsFragment", "onCreatePreferences")
        setPreferencesFromResource(R.xml.ui_preferences, rootKey)

        sharedPreferenceManager = SharedPreferenceManager(requireContext())

        // Find the FontSpinnerPreference
        fontSpinnerPreference = findPreference("textFont")

        // Set the callback to handle font picker
        fontSpinnerPreference?.setCallback(object : FontSpinnerPreference.FontPickerCallback {
            override fun startActivityForResultForFontPicker(intent: Intent, requestCode: Int) {
                android.util.Log.d("UISettingsFragment", "Launching font picker")
                try {
                    pickFontLauncher.launch(intent)
                } catch (e: Exception) {
                    android.util.Log.e("UISettingsFragment", "Failed to open font picker", e)
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
        android.util.Log.d("UISettingsFragment", "Current font: $currentFont")
        if (currentFont?.startsWith("custom:") == true) {
            val fileName = currentFont.substringAfter(":")
            fontSpinnerPreference?.summary = "Custom: $fileName"
        }
    }

    override fun getTitle(): String {
        return getString(R.string.ui_settings_title)
    }
}