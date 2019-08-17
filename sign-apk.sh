cd $TRAVIS_BUILD_DIR/app/build/outputs/apk/release/
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore $TRAVIS_BUILD_DIR/app/candybar.jks -storepass candybar -keypass candybar candybar.apk key0
jarsigner -verify candybar.apk
TIME=`date +%d%m%Y-%H%M`
$ANDROID_HOME/build-tools/29.0.0/zipalign -f 4 candybar.apk candybar-${TIME}.apk
rm candybar.apk
