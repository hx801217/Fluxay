# EINK Launcher - 快速开始

## 前置要求

- JDK 17 或更高版本
- Android Studio 2022.1.0 或更高版本（可选）
- Git

## 克隆项目

```bash
git clone https://github.com/yourusername/Fluxay.git
cd Fluxay
```

## 本地构建

### 1. 清理项目
```bash
./gradlew clean
```

### 2. 构建Debug APK
```bash
./gradlew assembleDebug
```

输出: `app/build/outputs/apk/debug/EINK-Launcher-dev-1.0.0-dev.apk`

### 3. 构建Release APK
```bash
./gradlew assembleRelease
```

输出: `app/build/outputs/apk/release/EINK-Launcher-1.0.0.apk`

## 安装APK

### 通过ADB安装
```bash
# 安装Debug版本
adb install app/build/outputs/apk/debug/EINK-Launcher-dev-1.0.0-dev.apk

# 安装Release版本
adb install app/build/outputs/apk/release/EINK-Launcher-1.0.0.apk
```

### 直接在设备上安装
将APK文件复制到设备，使用文件管理器打开安装

## 签名APK（可选）

### 1. 创建Keystore
```bash
keytool -genkey -v -keystore app/keystore.jks -keyalg RSA -keysize 2048 -validity 10000 -alias eink-launcher
```

### 2. 创建keystore.properties
```bash
cp keystore.properties.example keystore.properties
# 编辑keystore.properties，填入实际值
```

### 3. 构建签名APK
```bash
./gradlew assembleRelease
```

## GitHub Actions自动构建

### 触发构建
推送任何代码到main分支都会触发自动构建：
```bash
git add .
git commit -m "Your changes"
git push origin main
```

### 创建发布版本
```bash
git tag v1.0.0
git push --tags
```

GitHub Actions将自动：
1. 构建Release APK
2. 签名APK（如果配置了密钥）
3. 创建GitHub Release
4. 上传APK到Release

## 配置GitHub Secrets（用于签名）

在GitHub仓库设置中添加以下Secrets：

| Secret名称 | 说明 |
|-----------|------|
| `KEYSTORE_BASE64` | Base64编码的keystore文件 |
| `KEYSTORE_PASSWORD` | Keystore密码 |
| `KEY_ALIAS` | 密钥别名 |
| `KEY_PASSWORD` | 密钥密码 |

### 生成Base64编码的keystore
```bash
base64 -w 0 app/keystore.jks
```

将输出复制到GitHub Secret `KEYSTORE_BASE64`

## 设置为默认启动器

安装后：
1. 按Home键
2. 选择"EINK Launcher"
3. 点击"始终"

## 基本使用

### 添加快捷方式
1. 在主屏幕长按
2. 选择应用
3. 设置快捷方式

### 打开应用菜单
- 上滑手势

### 搜索应用
- 打开应用菜单后输入应用名

### 手势操作
- 上滑: 打开应用菜单
- 下拉: 展开通知栏
- 左滑/右滑: 启动指定应用
- 双击: 锁屏（需在辅助功能中启用）
- 点击时钟: 打开时钟应用
- 点击日期: 打开日历应用

## EINK优化设置

### 启用EINK模式
1. 打开设置
2. 进入"EINK显示"
3. 启用"EINK模式"
4. 启用"高对比度模式"
5. 选择"刷新模式"

### 使用自定义字体
1. 将字体文件(TTF/OTF/TTC)复制到设备
2. 打开设置 > EINK显示 > 选择字体
3. 浏览并选择字体文件
4. 启用"使用自定义字体"
5. 重启启动器

### 设置日期格式
1. 打开设置 > EINK显示 > 日期格式
2. 选择格式：完整/中等/简短

## 故障排除

### 构建失败
```bash
# 清理并重新构建
./gradlew clean
./gradlew assembleDebug --stacktrace
```

### 签名错误
- 检查keystore.properties是否正确配置
- 确认keystore文件存在且密码正确

### 字体不显示
- 确认字体文件格式正确(TTF/OTF/TTC)
- 检查字体文件路径
- 重启启动器

### EINK模式不生效
- 确认设备为EINK屏幕
- 在设置中启用EINK模式
- 重启启动器

## 开发

### 使用Android Studio
1. 打开项目
2. 等待Gradle同步完成
3. 连接设备或启动模拟器
4. 点击"Run"按钮

### 修改代码
- 所有Kotlin源码在: `app/src/main/java/com/eink/launcher/`
- 资源文件在: `app/src/main/res/`

### 运行测试
```bash
./gradlew test
```

## 更多信息

- 构建文档: `BUILD.md`
- 项目总结: `PROJECT_SUMMARY.md`
- 原始项目: YAM Launcher

## 获取帮助

如有问题，请提交Issue到GitHub仓库。
