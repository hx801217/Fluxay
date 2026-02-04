@echo off
echo 正在导出崩溃日志...
echo.

REM 清空旧日志
echo 清空旧日志...
adb logcat -c
echo.

echo 请在手机上操作触发崩溃...
pause

REM 导出日志
echo 导出日志到 crash_log.txt ...
adb logcat -v time -d > crash_log.txt

echo.
echo 日志已导出到 crash_log.txt
echo.
echo 只显示崩溃相关日志：
adb logcat -v time -d AndroidRuntime:E *:S
echo.
pause
