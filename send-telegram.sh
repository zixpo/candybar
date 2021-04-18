if [ "$TRAVIS_PULL_REQUEST" = false ]; then
  cd $TRAVIS_BUILD_DIR/app/build/outputs/apk/release/

  name='candybar-'
  if [ "$TRAVIS_TAG" ]; then
    name+=TRAVIS_TAG
  else
    name+=`date +%d%m%Y-%H%M`
  fi
  name+='.apk'

  mv 'app-release.apk' $name

  curl -v -F document=@"${TRAVIS_BUILD_DIR}/app/build/outputs/apk/release/${name}" \
    https://api.telegram.org/bot$BOT_TOKEN/sendDocument?chat_id=@candybar_builds
fi
