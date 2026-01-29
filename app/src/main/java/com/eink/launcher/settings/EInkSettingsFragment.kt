package com.eink.launcher.settings

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.eink.launcher.R
import com.eink.launcher.utils.FontSelector
import kotlinx.coroutines.launch
import java.io.File

class EInkSettingsFragment : PreferenceFragmentCompat() {

    private lateinit var customFontPreference: Preference
    private lateinit var enableCustomFontPreference: SwitchPreference

    private val selectFontLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        result.data?.data?.let { uri ->
            handleFontSelection(uri)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            openFontPicker()
        }
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.eink_preferences, rootKey)

        customFontPreference = findPreference("customFont")!!
        enableCustomFontPreference = findPreference("enableCustomFont")!!

        setupPreferences()
        updateFontSummary()
    }

    private fun setupPreferences() {
        // EINK模式开关
        findPreference<SwitchPreference>("einkMode")?.setOnPreferenceChangeListener { _, newValue ->
            val enabled = newValue as Boolean
            // 重启应用以应用EINK模式更改
            requireActivity().recreate()
            true
        }

        // 自定义字体开关
        enableCustomFontPreference.setOnPreferenceChangeListener { _, newValue ->
            val enabled = newValue as Boolean
            if (enabled) {
                // 检查是否有已选择的字体
                val fontPath = FontSelector.getSavedCustomFontPath(requireContext())
                if (fontPath == null || fontPath == "default") {
                    // 没有字体，自动打开选择器
                    openFontPicker()
                }
            }
            true
        }

        // 自定义字体选择
        customFontPreference.setOnPreferenceClickListener {
            checkStoragePermissionAndOpenPicker()
            true
        }

        // 重置字体
        findPreference<Preference>("resetFont")?.setOnPreferenceClickListener {
            FontSelector.resetToDefaultFont(requireContext())
            enableCustomFontPreference.isChecked = false
            updateFontSummary()
            true
        }

        // EINK刷新模式
        findPreference<Preference>("einkRefreshMode")?.setOnPreferenceChangeListener { _, newValue ->
            // 刷新模式更改通知
            true
        }
    }

    private fun checkStoragePermissionAndOpenPicker() {
        when {
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.R -> {
                // Android 11+ 使用存储访问框架，不需要权限
                openFontPicker()
            }
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED -> {
                openFontPicker()
            }
            else -> {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun openFontPicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf(
                "font/ttf",
                "font/otf",
                "application/x-font-ttf",
                "application/x-font-opentype"
            ))
        }
        selectFontLauncher.launch(Intent.createChooser(intent, "选择字体文件"))
    }

    private fun handleFontSelection(uri: Uri) {
        val fileName = FontSelector.getFontFileName(requireContext(), uri) ?: "font.ttf"

        // 检查文件扩展名
        val extension = fileName.substringAfterLast('.', "").lowercase()
        if (extension !in listOf("ttf", "otf", "ttc")) {
            // 显示错误提示
            return
        }

        // 复制文件到应用目录
        val targetFileName = if (extension in listOf("ttf", "otf", "ttc")) fileName else "$fileName.ttf"

        // 异步复制字体文件
        requireActivity().lifecycleScope.launch {
            try {
                val result = FontSelector.copyFontToCustomDir(requireContext(), uri, targetFileName)

                result.onSuccess { path ->
                    // 保存字体路径
                    FontSelector.saveCustomFontPath(requireContext(), path)

                    // 启用自定义字体
                    enableCustomFontPreference.isChecked = true

                    // 更新UI
                    updateFontSummary()

                    // 提示重启应用
                    showRestartDialog()
                }.onFailure { error ->
                    error.printStackTrace()
                    // 显示错误提示
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun updateFontSummary() {
        val fontPath = FontSelector.getSavedCustomFontPath(requireContext())
        val fontName = when {
            fontPath == null || fontPath == "default" -> "使用默认字体"
            else -> {
                val file = File(fontPath)
                if (file.exists()) {
                    file.nameWithoutExtension
                } else {
                    "使用默认字体"
                }
            }
        }

        customFontPreference.summary = fontName
    }

    private fun showRestartDialog() {
        // 显示重启对话框，重启应用以应用字体
        requireActivity().recreate()
    }
}

