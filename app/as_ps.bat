adb shell ping `pm uninstall com.candybar.dev`
adb push "./release/app-release.apk" "/data/local/tmp"
adb shell ping `pm install --user 0 -i "com.android.vending" -r "/data/local/tmp/app-release.apk"`
adb shell ping `am start -n com.candybar.dev/com.candybar.dev.activities.SplashActivity`