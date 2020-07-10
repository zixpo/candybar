cd $TRAVIS_BUILD_DIR/app/build/outputs/apk/release/
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore $TRAVIS_BUILD_DIR/app/candybar.jks -storepass candybar -keypass candybar candybar.apk key0
jarsigner -verify candybar.apk

if [ "$TRAVIS_TAG" ]; then
  $ANDROID_HOME/build-tools/29.0.2/zipalign -f 4 candybar.apk candybar-${TRAVIS_TAG}.apk
else
  TIME=`date +%d%m%Y-%H%M`
  $ANDROID_HOME/build-tools/29.0.2/zipalign -f 4 candybar.apk candybar-${TIME}.apk
fi

rm candybar.apk
