package eu.ottop.yamlauncher.settings

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import eu.ottop.yamlauncher.R
import eu.ottop.yamlauncher.utils.UIUtils

class SettingsFragment : PreferenceFragmentCompat(), TitleProvider {

    private lateinit var sharedPreferenceManager: SharedPreferenceManager

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)

        val uiUtils = UIUtils(requireContext())

        sharedPreferenceManager = SharedPreferenceManager(requireContext())

        val homePref = findPreference<Preference>("defaultHome")

        val uiSettings = findPreference<Preference>("uiSettings")
        val homeSettings = findPreference<Preference>("homeSettings")
        val appMenuSettings = findPreference<Preference>("appMenuSettings")

        val hiddenPref = findPreference<Preference>("hiddenApps")
        val backupPref = findPreference<Preference>("backup")
        val restorePref = findPreference<Preference>("restore")
        val aboutPref = findPreference<Preference>("aboutPage")
        val restartPref = findPreference<Preference>("restartLauncher")
        val resetPref = findPreference<Preference>("resetAll")

        homePref?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                val intent = Intent(Settings.ACTION_HOME_SETTINGS)
                if (intent.resolveActivity(requireContext().packageManager) != null) {
                    startActivity(intent)
                } else {
                    Toast.makeText(requireContext(),
                        getString(R.string.unable_to_launch_settings), Toast.LENGTH_SHORT).show()
                }
                true }

        uiSettings?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                uiUtils.switchFragment(requireActivity(), UISettingsFragment())
                true }

        homeSettings?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                uiUtils.switchFragment(requireActivity(), HomeSettingsFragment())
                true }

        appMenuSettings?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                uiUtils.switchFragment(requireActivity(), AppMenuSettingsFragment())
                true }

        hiddenPref?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                uiUtils.switchFragment(requireActivity(), HiddenAppsFragment())
                true }

        backupPref?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                try {
                    val activity = requireActivity()
                    if (activity is SettingsActivity) {
                        activity.createBackup()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("SettingsFragment", "Backup failed", e)
                    Toast.makeText(requireContext(), "Failed to open backup: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                true }

        restorePref?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                try {
                    val activity = requireActivity()
                    if (activity is SettingsActivity) {
                        activity.restoreBackup()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("SettingsFragment", "Restore failed", e)
                    Toast.makeText(requireContext(), "Failed to open restore: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                true }

        aboutPref?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                uiUtils.switchFragment(requireActivity(), AboutFragment())
                true }

        restartPref?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                (requireActivity() as SettingsActivity).restartApp()
                true }

        resetPref?.onPreferenceClickListener =
            Preference.OnPreferenceClickListener {
                sharedPreferenceManager.resetAllPreferences()
                true }
    }

    override fun getTitle(): String {
        return getString(R.string.settings_title)
    }



}