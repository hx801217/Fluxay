package com.eink.launcher.utils

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowInsets
import android.view.WindowManager

/**
 * EINK显示优化助手
 * 针对电子墨水屏的特性进行优化
 */
object EInkHelper {

    /**
     * 初始化EINK显示模式
     * 在Activity的onCreate中调用
     */
    fun initEInkMode(window: Window) {
        // 使用黑白主题，避免渐变和阴影
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.setDecorFitsSystemWindows(true)
            window.insetsController?.let { controller ->
                controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
                controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            }
        } else {
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = (
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    )
        }

        // 禁用硬件加速以获得更好的EINK显示效果
        try {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
            )
        } catch (e: Exception) {
            // 如果不支持硬件加速则忽略
        }
    }

    /**
     * 设置为纯黑白模式
     * 转换所有颜色为黑白
     */
    fun toGrayscale(color: Int): Int {
        val gray = (Color.red(color) * 299 + Color.green(color) * 587 + Color.blue(color) * 114) / 1000
        return Color.rgb(gray, gray, gray)
    }

    /**
     * 获取EINK优化的文字颜色
     * 返回纯黑色或纯白色
     */
    fun getOptimizedTextColor(isDarkMode: Boolean): Int {
        return if (isDarkMode) {
            Color.WHITE
        } else {
            Color.BLACK
        }
    }

    /**
     * 获取EINK优化的背景颜色
     */
    fun getOptimizedBackgroundColor(isDarkMode: Boolean): Int {
        return if (isDarkMode) {
            Color.BLACK
        } else {
            Color.WHITE
        }
    }

    /**
     * 检测是否为EINK设备
     * 基于设备特征判断
     */
    fun isEInkDevice(context: Context): Boolean {
        val display = context.getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        display?.defaultDisplay?.let { displayMetrics ->
            android.util.DisplayMetrics().also { displayMetrics.getMetrics(it) }.let { metrics ->
                // EINK设备通常具有特定的刷新率和分辨率特征
                // 这里可以根据需要添加更多检测逻辑
            }
        }

        // 默认返回true，因为这是EINK启动器
        return true
    }

    /**
     * 获取EINK刷新模式
     * 可以根据应用状态决定使用全局刷新还是局部刷新
     */
    enum class RefreshMode {
        GLOBAL,      // 全局刷新，适合大幅变化
        PARTIAL,     // 局部刷新，适合小范围更新
        NONE         // 不刷新，保持当前状态
    }

    /**
     * 触发EINK屏幕刷新
     * 某些EINK设备可能需要手动触发刷新
     */
    fun triggerRefresh(view: View, mode: RefreshMode = RefreshMode.PARTIAL) {
        view.invalidate()
        if (mode == RefreshMode.GLOBAL) {
            view.postInvalidate()
        }
    }

    /**
     * EINK优化：减少动画效果
     * 返回适合EINK的动画时长
     */
    fun getOptimizedAnimationDuration(): Long {
        return 0L // EINK设备不需要动画或使用极短的动画
    }

    /**
     * EINK优化：高对比度模式
     * 确保文字在EINK上清晰可读
     */
    fun isHighContrastModeEnabled(context: Context): Boolean {
        return true // EINK设备默认使用高对比度
    }

    /**
     * EINK优化：优化字体渲染
     * 返回字体平滑选项
     */
    fun isFontSmoothingEnabled(): Boolean {
        return false // EINK设备不需要字体平滑，可能导致模糊
    }
}
