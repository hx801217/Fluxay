# EINK Launcher - 项目结构

## 根目录

```
EINK Launcher/
├── .github/
│   └── workflows/
│       ├── build-apk.yml          # 自动构建工作流
│       ├── build-release.yml      # 发布版本工作流
│       └── mirror.yml             # 镜像工作流
├── app/
│   ├── build.gradle.kts           # 应用构建配置
│   └── src/main/
│       ├── AndroidManifest.xml     # 应用清单
│       ├── java/com/eink/launcher/
│       │   ├── MainActivity.kt              # 主界面
│       │   ├── AppActionMenu.kt             # 应用操作菜单
│       │   ├── AppMenuAdapter.kt            # 应用列表适配器
│       │   ├── ContactsAdapter.kt           # 联系人适配器
│       │   ├── settings/                    # 设置相关
│       │   │   ├── SettingsActivity.kt      # 设置Activity
│       │   │   ├── SettingsFragment.kt      # 设置主Fragment
│       │   │   ├── UISettingsFragment.kt    # UI设置
│       │   │   ├── HomeSettingsFragment.kt  # 主屏幕设置
│       │   │   ├── AppMenuSettingsFragment.kt # 应用菜单设置
│       │   │   ├── ContextMenuSettingsFragment.kt # 上下文菜单设置
│       │   │   ├── HiddenAppsFragment.kt   # 隐藏应用管理
│       │   │   ├── GestureAppsFragment.kt   # 手势应用选择
│       │   │   ├── LocationFragment.kt      # 位置选择
│       │   │   ├── AboutFragment.kt        # 关于页面
│       │   │   ├── SharedPreferenceManager.kt # 设置管理器
│       │   │   ├── SpinnerPreference.kt    # 下拉选择首选项
│       │   │   ├── TitleProvider.kt        # 标题提供者
│       │   │   ├── GestureAppsAdapter.kt   # 手势应用适配器
│       │   │   ├── HiddenAppsAdapter.kt   # 隐藏应用适配器
│       │   │   ├── LocationListAdapter.kt  # 位置列表适配器
│       │   │   └── EInkSettingsFragment.kt # EINK设置界面
│       │   ├── tasks/                       # 后台服务
│       │   │   ├── ScreenLockService.kt    # 锁屏服务
│       │   │   └── BatteryReceiver.kt      # 电池接收器
│       │   └── utils/                       # 工具类
│       │       ├── UIUtils.kt               # UI工具
│       │       ├── AppUtils.kt              # 应用工具
│       │       ├── StringUtils.kt           # 字符串工具
│       │       ├── WeatherSystem.kt         # 天气系统
│       │       ├── BiometricUtils.kt        # 生物识别工具
│       │       ├── GestureUtils.kt          # 手势工具
│       │       ├── PermissionUtils.kt       # 权限工具
│       │       ├── Animations.kt            # 动画工具
│       │       ├── AppMenuEdgeFactory.kt    # 菜单边缘效果
│       │       ├── AppMenuLinearLayoutManager.kt # 菜单布局管理
│       │       ├── FontMap.kt               # 字体映射
│       │       ├── EInkHelper.kt            # EINK助手
│       │       └── FontSelector.kt          # 字体选择器
│       └── res/
│           ├── layout/                      # 布局文件
│           │   ├── activity_main.xml
│           │   ├── activity_settings.xml
│           │   ├── app_item_layout.xml
│           │   ├── fragment_*.xml
│           │   └── location_item_layout.xml
│           ├── values/                       # 默认资源
│           │   ├── strings.xml
│           │   ├── colors.xml
│           │   ├── themes.xml
│           │   ├── attrs.xml
│           │   └── arrays.xml
│           ├── values-zh/                   # 中文资源
│           │   └── strings.xml
│           ├── values-de/                   # 德语
│           ├── values-es/                   # 西班牙语
│           ├── values-fi/                   # 芬兰语
│           ├── values-fr/                   # 法语
│           ├── values-it/                   # 意大利语
│           ├── values-nl/                   # 荷兰语
│           ├── values-pt/                   # 葡萄牙语
│           ├── values-ru/                   # 俄语
│           └── values-uk/                   # 乌克兰语
│           ├── drawable/                    # 图标资源
│           ├── xml/                         # 配置文件
│           │   ├── root_preferences.xml     # 主设置
│           │   ├── ui_preferences.xml       # UI设置
│           │   ├── home_preferences.xml      # 主屏幕设置
│           │   ├── app_menu_preferences.xml  # 应用菜单设置
│           │   ├── context_menu_preferences.xml # 上下文菜单设置
│           │   ├── eink_preferences.xml     # EINK设置
│           │   ├── backup_rules.xml
│           │   ├── data_extraction_rules.xml
│           │   └── screenlock_service_config.xml
│           └── mipmap-*                     # 应用图标
├── build.gradle.kts                        # 根构建配置
├── settings.gradle.kts                     # Gradle设置
├── gradle/                                 # Gradle文件
│   └── libs.versions.toml                  # 版本目录
├── gradlew                                 # Gradle包装器
├── gradlew.bat                             # Windows Gradle包装器
├── .gitignore                              # Git忽略文件
├── LICENSE                                 # 许可证
├── README.md                               # 项目说明
├── BUILD.md                                # 构建文档
├── PROJECT_SUMMARY.md                       # 项目总结
├── QUICKSTART.md                           # 快速开始
├── CHECKLIST.md                            # 检查清单
├── STRUCTURE.md                            # 结构说明
├── keystore.properties.example              # 签名示例
├── clean.sh                                # 清理脚本
├── CREDITS.md                              # 致谢
└── PrivacyPolicy.md                        # 隐私政策
```

## 核心文件说明

### 构建配置
- `build.gradle.kts` - 项目级构建配置
- `settings.gradle.kts` - Gradle设置
- `app/build.gradle.kts` - 应用级构建配置
- `gradle/libs.versions.toml` - 依赖版本管理

### 清单和资源
- `AndroidManifest.xml` - 应用清单，定义权限、组件等
- `res/xml/*.xml` - 设置界面配置
- `res/values/strings.xml` - 英文字符串资源
- `res/values-zh/strings.xml` - 中文字符串资源

### 主要Activity
- `MainActivity.kt` - 主界面，处理手势、时钟、日期、快捷方式
- `SettingsActivity.kt` - 设置界面，管理所有设置Fragment

### 主要Fragment
- `EInkSettingsFragment.kt` - EINK显示和字体设置
- `UISettingsFragment.kt` - UI外观设置
- `HomeSettingsFragment.kt` - 主屏幕配置
- `AppMenuSettingsFragment.kt` - 应用菜单配置
- `HiddenAppsFragment.kt` - 隐藏应用管理

### 工具类
- `EInkHelper.kt` - EINK显示优化助手
- `FontSelector.kt` - 自定义字体选择器
- `UIUtils.kt` - UI元素配置工具
- `AppUtils.kt` - 应用管理工具
- `WeatherSystem.kt` - 天气显示系统
- `StringUtils.kt` - 字符串处理工具

### 适配器
- `AppMenuAdapter.kt` - 应用列表适配器
- `ContactsAdapter.kt` - 联系人列表适配器
- `GestureAppsAdapter.kt` - 手势应用选择适配器
- `HiddenAppsAdapter.kt` - 隐藏应用管理适配器

### 服务和接收器
- `ScreenLockService.kt` - 无障碍服务，双击锁屏
- `BatteryReceiver.kt` - 广播接收器，电池状态更新

### GitHub Actions
- `.github/workflows/build-apk.yml` - 自动构建APK
- `.github/workflows/build-release.yml` - 创建发布版本

## 文档文件

### 用户文档
- `README.md` - 项目说明
- `QUICKSTART.md` - 快速开始指南
- `BUILD.md` - 构建文档

### 开发文档
- `PROJECT_SUMMARY.md` - 项目总结
- `STRUCTURE.md` - 项目结构（本文件）
- `CHECKLIST.md` - 检查清单

### 配置文件
- `keystore.properties.example` - 签名配置示例
- `clean.sh` - 清理脚本

## 依赖库

### AndroidX
- core-ktx 1.16.0
- appcompat 1.7.1
- material 1.12.0
- recyclerview 1.4.0
- preference-ktx 1.2.1
- activity-ktx 1.10.1
- constraintlayout 2.2.1
- biometric-ktx 1.4.0-alpha02

### 无第三方依赖
项目仅使用AndroidX和Material官方库，无其他第三方依赖。

## 构建输出

```
app/build/outputs/
├── apk/
│   ├── debug/
│   │   └── EINK-Launcher-dev-1.0.0-dev.apk
│   └── release/
│       └── EINK-Launcher-1.0.0.apk
└── mapping/
    └── release/
        └── mapping.txt  # 代码混淆映射（Release版本）
```

## 特殊目录

### 字体存储
自定义字体存储在：
`<设备存储>/Android/data/com.eink.launcher/files/EInkFonts/`

### 备份文件
设置备份存储在：
`<设备存储>/Android/data/com.eink.launcher/files/backups/`

## 包结构

```
com.eink.launcher/
├── MainActivity           # 主界面
├── AppActionMenu         # 应用操作菜单
├── AppMenuAdapter        # 应用列表适配器
├── ContactsAdapter       # 联系人适配器
├── settings/             # 设置相关
├── tasks/                # 后台服务
└── utils/                # 工具类
```

## 权限说明

应用需要以下权限：
- `REQUEST_DELETE_PACKAGES` - 卸载应用
- `EXPAND_STATUS_BAR` - 展开状态栏
- `INTERNET` - 天气API
- `ACCESS_COARSE_LOCATION` - GPS定位
- `READ_CONTACTS` - 联系人访问
- `USE_BIOMETRIC` - 生物识别
- `SET_ALARM` - 闹钟应用
- `QUERY_ALL_PACKAGES` - 查询所有应用
- `READ_EXTERNAL_STORAGE` - 读取本地字体（Android 10以下）
- `MANAGE_EXTERNAL_STORAGE` - 管理外部存储（Android 11以上）

---

文档版本: 1.0.0
最后更新: 2026年1月29日
