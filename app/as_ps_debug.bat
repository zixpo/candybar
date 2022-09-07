adb shell ping `pm uninstall com.candybar.dev`
adb push "./debug/app-debug.apk" "/data/local/tmp"
adb shell ping `pm install --user 0 -i "com.android.vending" -r "/data/local/tmp/app-debug.apk"`
adb shell ping `am start -n com.candybar.dev/com.candybar.dev.activities.SplashActivity`