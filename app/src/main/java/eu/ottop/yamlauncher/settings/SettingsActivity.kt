package eu.ottop.yamlauncher.settings

import android.Manifest
import android.app.Activity
import android.app.PendingIntent
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.preference.PreferenceManager
import eu.ottop.yamlauncher.MainActivity
import eu.ottop.yamlauncher.R
import eu.ottop.yamlauncher.databinding.ActivitySettingsBinding
import eu.ottop.yamlauncher.utils.PermissionUtils
import eu.ottop.yamlauncher.utils.UIUtils
import org.json.JSONObject

class SettingsActivity : AppCompatActivity() {

    private val permissionUtils = PermissionUtils()

    private lateinit var sharedPreferenceManager: SharedPreferenceManager
    private lateinit var preferences: SharedPreferences
    private lateinit var binding: ActivitySettingsBinding
    private lateinit var performBackup: ActivityResultLauncher<Intent>
    private lateinit var performRestore: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uiUtils = UIUtils(this@SettingsActivity)

        sharedPreferenceManager = SharedPreferenceManager(this@SettingsActivity)
        preferences = PreferenceManager.getDefaultSharedPreferences(this@SettingsActivity)

        // Initialize default preferences if they don't exist
        initializeDefaultPreferences()

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings_title)
        supportActionBar?.setDisplayShowTitleEnabled(true)

        uiUtils.adjustInsets(binding.root)

        if (supportFragmentManager.backStackEntryCount == 0) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settingsLayout, SettingsFragment())
                .commit()
        }

        supportFragmentManager.addOnBackStackChangedListener {
            updateActionBarTitle()
        }

        performBackup = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    saveSharedPreferencesToFile(uri)
                }
            }
        }

        performRestore = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    restoreSharedPreferencesFromFile(uri)
                }
            }
        }

    }

    private fun initializeDefaultPreferences() {
        // Only set defaults if they don't already exist
        if (!preferences.contains("bgColor")) {
            preferences.edit().putString("bgColor", "#FFFFFFFF").apply()
        }
        if (!preferences.contains("textColor")) {
            preferences.edit().putString("textColor", "#FF000000").apply()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        if (supportFragmentManager.backStackEntryCount > 0) {
            supportFragmentManager.popBackStack()
        } else {
            onBackPressedDispatcher.onBackPressed()
        }
        return true
    }

    private fun updateActionBarTitle() {
        val fragment = supportFragmentManager.findFragmentById(R.id.settingsLayout)
        if (fragment is TitleProvider) {
            supportActionBar?.title = fragment.getTitle()
        }
    }

    fun createBackup() {
        try {
            // Try ACTION_CREATE_DOCUMENT first (API 19+) - allows creating new files
            val createFileIntent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                putExtra(Intent.EXTRA_TITLE, "yamlauncher_backup_${System.currentTimeMillis()}.json")
            }

            // Check if there's an activity to handle the intent
            val activities = packageManager.queryIntentActivities(createFileIntent, 0)
            if (activities.isNotEmpty()) {
                performBackup.launch(createFileIntent)
                return
            }

            // Fallback: try ACTION_GET_CONTENT - works on many devices but behavior varies
            val fallbackIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                putExtra(Intent.EXTRA_TITLE, "yamlauncher_backup_${System.currentTimeMillis()}.json")
            }
            val fallbackActivities = packageManager.queryIntentActivities(fallbackIntent, 0)
            if (fallbackActivities.isNotEmpty()) {
                performBackup.launch(fallbackIntent)
                return
            }

            // No file picker available
            Toast.makeText(
                this,
                "No file picker available on this device. Please install a file manager app.",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Failed to open file picker: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun createBackupJson(): String {
        val allEntries = preferences.all

        val backupData = JSONObject().apply {
            put("app_id", application.packageName)
            val data = JSONObject()
            for ((key, value) in allEntries) {
                val entry = JSONObject().apply {
                    when (value) {
                        is String -> put("value", value).put("type", "String")
                        is Int -> put("value", value).put("type", "Int")
                        is Boolean -> put("value", value).put("type", "Boolean")
                        is Long -> put("value", value).put("type", "Long")
                        is Float -> put("value", value).put("type", "Float")
                    }
                }
                data.put(key, entry)
            }
            put("data", data)
        }

        return backupData.toString(4)
    }

    private fun saveSharedPreferencesToFile(uri: Uri) {
        try {
            val backupData = createBackupJson()
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(backupData.toByteArray())
            }
            Toast.makeText(this, getString(R.string.backup_success), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.backup_fail), Toast.LENGTH_SHORT).show()
        }
    }

    fun restoreBackup() {
        try {
            // Try ACTION_OPEN_DOCUMENT first
            val openFileIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "application/json"
            }

            // Check if there's an activity to handle the intent
            val activities = packageManager.queryIntentActivities(openFileIntent, 0)
            if (activities.isNotEmpty()) {
                performRestore.launch(openFileIntent)
            } else {
                // Fallback: try ACTION_GET_CONTENT for older devices
                try {
                    val fallbackIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
                        addCategory(Intent.CATEGORY_OPENABLE)
                        type = "application/json"
                    }
                    val fallbackActivities = packageManager.queryIntentActivities(fallbackIntent, 0)
                    if (fallbackActivities.isNotEmpty()) {
                        performRestore.launch(fallbackIntent)
                    } else {
                        Toast.makeText(
                            this,
                            "No file picker available on this device",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(
                        this,
                        "Failed to open file picker: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        } catch (e: Exception) {
            Toast.makeText(
                this,
                "Failed to open file picker: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun restoreSharedPreferencesFromFile(uri: Uri) {
        val jsonData = readJsonFile(uri)
        if (jsonData != null) {
            try {
                val backupData = JSONObject(jsonData)
                if (backupData.getString("app_id") != application.packageName) {
                    throw IllegalArgumentException(getString(R.string.restore_wrong_app))
                }
                val data = backupData.getJSONObject("data")

                val editor = preferences.edit()

                val keys = data.keys()

                while (keys.hasNext()){
                    val key = keys.next()
                    val entry = data.getJSONObject(key)
                    val type = entry.getString("type")

                    when (type) {
                        "String" -> editor.putString(key, entry.getString("value"))
                        "Int" -> editor.putInt(key, entry.getInt("value"))
                        "Boolean" -> editor.putBoolean(key, entry.getBoolean("value"))
                        "Long" -> editor.putLong(key, entry.getLong("value"))
                        "Float" -> editor.putFloat(key, entry.getDouble("value").toFloat())
                    }
                }
                editor.putBoolean("isRestored", true)

                editor.apply()

                Toast.makeText(this, getString(R.string.restore_success), Toast.LENGTH_SHORT).show()
            } catch(e: IllegalArgumentException) {
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this, getString(R.string.restore_fail), Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(this, getString(R.string.restore_error), Toast.LENGTH_SHORT).show()
        }
    }

    private fun readJsonFile(uri: Uri): String? {
        return try {
            contentResolver.openInputStream(uri)?.bufferedReader().use { reader ->
                reader?.readText()
            }
        } catch (e: Exception) {
            null
        }
    }

    fun requestLocationPermission() {
        try {
            ActivityCompat.requestPermissions(
                this@SettingsActivity,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION),
                0
            )
        } catch(_: Exception) {}
    }

    fun requestContactsPermission() {
        try {
            ActivityCompat.requestPermissions(
                this@SettingsActivity,
                arrayOf(Manifest.permission.READ_CONTACTS),
                1
            )
        } catch(_: Exception) {}
    }

    fun restartApp() {
        val restartIntent = Intent(applicationContext, MainActivity::class.java)
        restartIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_CANCEL_CURRENT or
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        PendingIntent.FLAG_IMMUTABLE
                    } else {
                        0
                    }
        } else {
            PendingIntent.FLAG_CANCEL_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0, restartIntent, flags
        )

        pendingIntent.send()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)


        if (requestCode == 0) {
            val fragment = supportFragmentManager.findFragmentById(R.id.settingsLayout) as HomeSettingsFragment
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fragment.setLocationPreference(true)
            } else {
                Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
                fragment.setLocationPreference(false)
            }
        }

        if (requestCode == 1) {
            val fragment = supportFragmentManager.findFragmentById(R.id.settingsLayout) as AppMenuSettingsFragment
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                fragment.setContactPreference(true)
            } else {
                Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_SHORT).show()
                fragment.setContactPreference(false)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!permissionUtils.hasPermission(this@SettingsActivity, Manifest.permission.READ_CONTACTS)) {
            sharedPreferenceManager.setContactsEnabled(false)
        }
        if (!permissionUtils.hasPermission(this@SettingsActivity, Manifest.permission.ACCESS_COARSE_LOCATION)) {
            sharedPreferenceManager.setWeatherGPS(false)
        }
    }

}