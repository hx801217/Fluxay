# 构建错误修复 - 2026-01-29

## 问题描述

GitHub Actions 构建失败，错误信息：
```
Android resource linking failed
ERROR: resource array/h_alignment_options (aka com.eink.launcher:array/h_alignment_options) not found.
ERROR: resource array/size_options (aka com.eink.launcher:array/size_options) not found.
... (多个数组资源缺失)
```

## 根本原因

在重命名包名和添加新功能时，未将原始项目中的 `arrays_common.xml` 文件的内容合并到 `arrays.xml`，导致多个必需的数组资源定义缺失。

## 修复方案

### 1. 更新 arrays.xml
在 `app/src/main/res/values/arrays.xml` 中添加了所有缺失的数组定义：

**General UI 数组**
- `bg_options` / `bg_values` - 背景选项
- `color_options` / `color_values` - 颜色选项
- `font_options` / `font_values` - 字体选项
- `style_options` / `style_values` - 样式选项
- `animation_options` / `animation_values` - 动画速度
- `swipe_values` - 滑动阈值

**Home and App Menu 数组**
- `shortcut_options` - 快捷方式数量
- `h_alignment_options` / `h_alignment_values` - 水平对齐
- `v_alignment_options` / `v_alignment_values` - 垂直对齐
- `size_options` / `size_values` - 大小选项
- `shortcut_spacing_options` / `shortcut_spacing_values` - 快捷方式间距
- `app_spacing_options` / `app_spacing_values` - 应用间距

**Weather 数组**
- `temp_units` - 温度单位
- `unit_values` - 单位值

**EINK 新增数组**
- `eink_refresh_mode_entries` / `eink_refresh_mode_values` - EINK 刷新模式
- `chinese_date_format_entries` / `chinese_date_format_values` - 日期格式

### 2. 修复 Gradle 警告
修正 `app/build.gradle.kts` 中的废弃警告：

**修改前**:
```kotlin
kotlinOptions {
    jvmTarget = JavaVersion.VERSION_17.toString()
}
```

**修改后**:
```kotlin
kotlin {
    jvmToolchain(17)
}
```

## 修复的文件

- ✅ `app/src/main/res/values/arrays.xml` - 添加所有缺失的数组定义
- ✅ `app/build.gradle.kts` - 修复 Kotlin JVM 目标设置警告

## 验证

### 本地验证
```bash
./gradlew clean
./gradlew assembleDebug
./gradlew assembleRelease
```

### CI/CD 验证
推送到 GitHub 后，GitHub Actions 自动构建应能成功。

## 构建命令

### Debug 版本
```bash
./gradlew assembleDebug
```
输出: `app/build/outputs/apk/debug/EINK-Launcher-dev-1.0.0-dev.apk`

### Release 版本
```bash
./gradlew assembleRelease
```
输出: `app/build/outputs/apk/release/EINK-Launcher-1.0.0.apk`

### 清理构建
```bash
./gradlew clean
```

## 技术细节

### 数组资源命名约定

每个设置选项通常需要两个数组：
- `name_options` - 显示给用户的选项名称
- `name_values` - 实际使用的值

例如：
```xml
<string-array name="size_options">
    <item>Tiny</item>
    <item>Small</item>
    <item>Medium</item>
    <item>Large</item>
    <item>Extra</item>
    <item>Huge</item>
</string-array>

<string-array name="size_values">
    <item>tiny</item>
    <item>small</item>
    <item>medium</item>
    <item>large</item>
    <item>extra</item>
    <item>huge</item>
</string-array>
```

### Gradle Kotlin 编译器配置

Kotlin 2.2.0 推荐使用新的 `kotlin` DSL 块来配置编译器选项，而不是已废弃的 `kotlinOptions`。

**新语法**:
```kotlin
kotlin {
    jvmToolchain(17)
}
```

**旧语法** (已废弃):
```kotlin
kotlinOptions {
    jvmTarget = JavaVersion.VERSION_17.toString()
}
```

## 预期结果

修复后，构建应该能够：
1. ✅ 成功编译所有资源
2. ✅ 链接所有数组资源
3. ✅ 生成 Debug APK
4. ✅ 生成 Release APK
5. ✅ 无警告信息

## 相关文档

- Android 资源文档: https://developer.android.com/guide/topics/resources/string-resource
- Kotlin 编译器文档: https://kotlinlang.org/docs/gradle-configure-project.html#gradle-java-toolchains

---

修复完成时间: 2026年1月29日
版本: 1.0.0
