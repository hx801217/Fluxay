@echo off
echo ===================================
echo Fluxay Launcher 崩溃日志收集脚本
echo ===================================
echo.

REM 检查设备连接
echo 检查设备连接状态...
adb devices
echo.

echo ===================================
echo 选择要执行的操作：
echo ===================================
echo.
echo 1. 实时监控日志（用于复现崩溃）
echo 2. 导出最近的崩溃日志
echo 3. 导出完整日志
echo 4. 导出 bugreport（最详细）
echo 5. 只显示 AndroidRuntime 崩溃信息
echo.
set /p choice=请输入选项 (1-5):

if "%choice%"=="1" goto realtime
if "%choice%"=="2" goto recent
if "%choice%"=="3" goto full
if "%choice%"=="4" goto bugreport
if "%choice%"=="5" goto runtime
goto invalid

:realtime
echo.
echo 清空旧日志...
adb logcat -c
echo.
echo 正在实时监控日志，请触发崩溃...
echo 日志将保存到 realtime_log.txt
echo 按 Ctrl+C 停止
adb logcat -v time > realtime_log.txt
goto end

:recent
echo.
echo 导出最近的崩溃日志...
adb logcat -v time -d -t 500 > recent_crash_log.txt
echo 日志已保存到 recent_crash_log.txt
echo.
echo 显示最近 500 行中的崩溃信息：
adb logcat -v time -d -t 500 AndroidRuntime:E *:S
goto end

:full
echo.
echo 导出完整日志（可能很大）...
adb logcat -v time -d > full_log.txt
echo 日志已保存到 full_log.txt
goto end

:bugreport
echo.
echo 正在生成 bugreport，这可能需要几分钟...
adb bugreport bugreport.zip
echo Bugreport 已保存到 bugreport.zip
goto end

:runtime
echo.
echo 只显示 AndroidRuntime 崩溃信息：
adb logcat -v time -d AndroidRuntime:E *:S
goto end

:invalid
echo.
echo 无效的选项！
goto end

:end
echo.
echo 完成！
pause
