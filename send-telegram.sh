if [ "$TRAVIS_PULL_REQUEST" = true ]; then
  exit 0
fi

readonly local last_commit_log=$(git log -1 --pretty=format:'%s %b')

if [ "$(echo "$last_commit_log" | grep -c '\[skip apk\]')" -gt 0 ]; then
  echo 'Found `[skip apk]` tag. Skipping APK publishing.'
  exit 0
fi

cd $TRAVIS_BUILD_DIR/app/build/outputs/apk/release/

local name='CandyBar-'
if [ "$TRAVIS_TAG" ]; then
  name+=TRAVIS_TAG
else
  name+=`date +%d%m%Y-%H%M`
fi
name+='.apk'

mv 'app-release.apk' $name

curl -v \
  -F document=@"$name" \
  -F disable_notification=true \
  https://api.telegram.org/bot$BOT_TOKEN/sendDocument?chat_id=@candybar_builds
