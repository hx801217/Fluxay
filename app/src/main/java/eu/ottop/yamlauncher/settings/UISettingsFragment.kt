package eu.ottop.yamlauncher.settings

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.PreferenceFragmentCompat
import eu.ottop.yamlauncher.R

class UISettingsFragment : PreferenceFragmentCompat(), TitleProvider {

    private var fontSpinnerPreference: FontSpinnerPreference? = null
    private val pickFontLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val requestCode = FontSpinnerPreference.REQUEST_CODE_PICK_FONT
        val resultCode = result.resultCode
        val data = result.data
        fontSpinnerPreference?.onActivityResult(requestCode, resultCode, data)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.ui_preferences, rootKey)

        // Find the FontSpinnerPreference
        fontSpinnerPreference = findPreference("textFont")

        // Set the callback
        fontSpinnerPreference?.setCallback(object : FontSpinnerPreference.FontPickerCallback {
            override fun startActivityForResultForFontPicker(intent: Intent, requestCode: Int) {
                pickFontLauncher.launch(intent)
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        fontSpinnerPreference?.onActivityResult(requestCode, resultCode, data)
    }

    override fun getTitle(): String {
        return getString(R.string.ui_settings_title)
    }
}