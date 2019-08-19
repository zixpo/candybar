if [ "$TRAVIS_PULL_REQUEST" = false ]; then
  cd $TRAVIS_BUILD_DIR/app/build/outputs/apk/release/

  for apk in $(find *.apk -type f); do
    apkName="${apk}"
    echo $apkName
    curl -v -F document=@"${TRAVIS_BUILD_DIR}/app/build/outputs/apk/release/${apkName}" https://api.telegram.org/bot$BOT_TOKEN/sendDocument?chat_id=@candybar_builds
  done
fi
