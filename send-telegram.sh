LAST_COMMIT_LOG=$(git log -1 --pretty=format:'%s %b')

if !([[ "$GITHUB_EVENT_NAME" == "release" ]] || [ "$(echo "$LAST_COMMIT_LOG" | grep -c '\[make apk\]')" -gt 0 ]); then
  exit 0
fi

apk_name='CandyBar-'
if [[ "$GITHUB_EVENT_NAME" == "release" ]]; then
  apk_name+="$GITHUB_REF_NAME"
else
  apk_name+=$(date +%d%m%Y-%H%M)
fi
apk_name+='.apk'

echo $apk_name

cd app/build/outputs/apk/release/
mv 'app-release.apk' $apk_name

curl -v \
  -F document=@"$apk_name" \
  -F chat_id=-1001381276297 \
  -F disable_notification=true \
  https://api.telegram.org/bot$BOT_TOKEN/sendDocument
