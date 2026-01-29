#!/bin/bash

# 清理项目构建文件和临时文件

echo "Cleaning EINK Launcher project..."

# 清理Gradle缓存
echo "Cleaning Gradle build files..."
./gradlew clean

# 删除构建输出
echo "Removing build directories..."
rm -rf app/build/
rm -rf build/

# 删除临时文件
echo "Removing temporary files..."
find . -name "*.log" -type f -delete
find . -name "*.tmp" -type f -delete

echo "Cleanup completed!"
