#!/bin/bash
set -xe
pkg=jp.co.cyberagent.stf
cls=Agent
APK=$(adb shell pm path jp.co.cyberagent.stf | tr -d '\r' | awk -F: '{print $2}')
adb shell export CLASSPATH="$APK"\; exec app_process /system/bin jp.co.cyberagent.stf.Agent
