# EINK Launcher

专为电子墨水屏（EINK）设备优化的Android启动器，基于YAM Launcher改造。

## 特性

### 核心功能
- 极简设计，纯文本界面
- 快速应用搜索和启动
- 自定义快捷方式（最多15个）
- 手势控制（上滑、下滑、左右滑动）
- 联系人支持
- 天气显示

### EINK优化
- 纯黑白高对比度显示模式
- 可配置的屏幕刷新模式（全局/局部/无刷新）
- 禁用硬件加速以获得更好的显示效果
- 减少动画效果，避免残影
- 专为EINK屏幕优化的字体渲染

### Android Go支持
- 最低SDK 28 (Android 9.0)
- 优化内存使用
- 轻量化设计
- 减少后台资源占用

### 自定义功能
- 自定义本地字体支持（TTF/OTF/TTC）
- 简体中文日期格式显示
- 可配置的背景和文字颜色
- 字体大小和对齐方式自定义
- 动画速度调节

## 构建

### 前置要求
- JDK 17 或更高版本
- Android SDK 36
- Gradle 8.13.2

### 构建APK
```bash
# 构建Debug版本
./gradlew assembleDebug

# 构建Release版本
./gradlew assembleRelease
```

输出文件位于 `app/build/outputs/apk/`

### 签名APK
在项目根目录创建 `keystore.properties` 文件：
```properties
storeFile=path/to/keystore.jks
storePassword=your_password
keyAlias=your_alias
keyPassword=your_key_password
```

或通过命令行参数签名：
```bash
./gradlew assembleRelease \
  -Pandroid.injected.signing.store.file=keystore.jks \
  -Pandroid.injected.signing.store.password=your_password \
  -Pandroid.injected.signing.key.alias=your_alias \
  -Pandroid.injected.signing.key.password=your_key_password
```

## GitHub Actions

项目包含GitHub Actions工作流用于自动构建：

- `build-apk.yml`: 每次推送和PR时构建APK
- `build-release.yml`: 推送标签时创建发布版本

### Secrets配置
在GitHub仓库设置中配置以下Secrets以支持签名：

- `KEYSTORE_BASE64`: Base64编码的keystore文件
- `KEYSTORE_PASSWORD`: Keystore密码
- `KEY_ALIAS`: 密钥别名
- `KEY_PASSWORD`: 密钥密码

### 发布流程
1. 创建标签：`git tag v1.0.0 && git push --tags`
2. GitHub Actions自动构建并创建Release
3. 在Release页面下载签名后的APK

## 项目结构

```
EINK Launcher/
├── app/
│   └── src/main/
│       ├── java/com/eink/launcher/
│       │   ├── MainActivity.kt
│       │   ├── settings/          # 设置相关
│       │   ├── utils/             # 工具类
│       │   └── tasks/             # 后台服务
│       └── res/
│           ├── xml/               # 设置配置
│           ├── values/            # 资源文件
│           └── values-zh/         # 中文资源
├── .github/workflows/             # GitHub Actions
└── build.gradle.kts
```

## 配置说明

### EINK显示模式
在设置 > EINK显示中配置：
- **启用EINK模式**: 优化电子墨水屏显示
- **高对比度模式**: 使用纯黑白显示
- **刷新模式**: 选择全局/局部/无刷新

### 自定义字体
1. 将TTF/OTF/TTC字体文件复制到设备
2. 在设置 > EINK显示 > 选择字体中导入
3. 启用自定义字体
4. 重启启动器生效

### 日期格式
支持三种简体中文格式：
- 完整: 2026年1月29日 星期四
- 中等: 2026年1月29日
- 简短: 1月29日

## 依赖库

- AndroidX Core KTX 1.16.0
- Material Components 1.12.0
- Preference KTX 1.2.1
- RecyclerView 1.4.0
- Biometric KTX 1.4.0-alpha02

## 许可证

基于YAM Launcher修改，遵循原项目许可证。

## 致谢

- 原始项目: YAM Launcher by Otto, maintained by ThomasNow Productions
- 本版本为EINK设备优化改造

## 版本历史

- **v1.0.0** (2026-01-29)
  - 初始EINK版本发布
  - Android Go支持
  - 自定义字体功能
  - 简体中文日期格式
  - EINK显示优化
