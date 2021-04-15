package candybar.lib.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.danimahardhika.android.helpers.core.utils.LogUtil;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;

import candybar.lib.R;
import candybar.lib.applications.CandyBarApplication;
import candybar.lib.databases.Database;
import candybar.lib.helpers.JsonHelper;
import candybar.lib.helpers.WallpaperHelper;
import candybar.lib.utils.ImageConfig;
import io.github.inflationx.viewpump.ViewPumpContextWrapper;

/*
 * CandyBar - Material Dashboard
 *
 * Copyright (c) 2014-2016 Dani Mahardhika
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

public abstract class CandyBarSplashActivity extends AppCompatActivity {

    private AsyncTask<Void, Void, Boolean> mSplashScreenLoader;
    private AsyncTask<Void, Void, Boolean> mCloudWallpapersLoader;

    @NonNull
    public abstract Class<?> getMainActivity();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSplashScreenLoader = new SplashScreenLoader(this)
                .mainActivity(getMainActivity())
                .executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        mCloudWallpapersLoader = new CloudWallpapersLoader(this).execute();
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase));
    }

    @Override
    public void onBackPressed() {
        if (mCloudWallpapersLoader != null) {
            mCloudWallpapersLoader.cancel(true);
        }
        Database.get(this.getApplicationContext()).closeDatabase();
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (mSplashScreenLoader != null) {
            mSplashScreenLoader.cancel(true);
        }
        super.onDestroy();
    }

    private static class SplashScreenLoader extends AsyncTask<Void, Void, Boolean> {

        private final WeakReference<Context> context;
        private Class<?> mainActivity;

        private SplashScreenLoader(@NonNull Context context) {
            this.context = new WeakReference<>(context);
        }

        private SplashScreenLoader mainActivity(@NonNull Class<?> mainActivity) {
            this.mainActivity = mainActivity;
            return this;
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (!isCancelled()) {
                try {
                    Thread.sleep(400);
                    return true;
                } catch (Exception e) {
                    LogUtil.e(Log.getStackTraceString(e));
                    return false;
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (context.get() == null) return;
            if (context.get() instanceof Activity) {
                if (((Activity) context.get()).isFinishing()) return;
            }

            Intent intent = new Intent(context.get(), mainActivity);
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

            Activity activity = (Activity) context.get();
            activity.startActivity(intent);
            activity.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            activity.finish();
        }
    }

    private static class CloudWallpapersLoader extends AsyncTask<Void, Void, Boolean> {

        private final WeakReference<Context> context;

        private CloudWallpapersLoader(@NonNull Context context) {
            this.context = new WeakReference<>(context);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (!isCancelled()) {
                try {
                    Thread.sleep(1);
                    if (WallpaperHelper.getWallpaperType(context.get()) != WallpaperHelper.CLOUD_WALLPAPERS)
                        return true;

                    if (Database.get(context.get().getApplicationContext()).getWallpapersCount() > 0)
                        return true;

                    URL url = new URL(context.get().getString(R.string.wallpaper_json));
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(15000);
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream stream = connection.getInputStream();
                        List list = JsonHelper.parseList(stream);
                        if (list == null) {
                            LogUtil.e("Json error, no array with name: "
                                    + CandyBarApplication.getConfiguration().getWallpaperJsonStructure().getArrayName());
                            return false;
                        }

                        if (Database.get(context.get().getApplicationContext()).getWallpapersCount() > 0) {
                            Database.get(context.get().getApplicationContext()).deleteWallpapers();
                        }

                        Database.get(context.get().getApplicationContext()).addWallpapers(list);

                        if (list.size() > 0 && list.get(0) instanceof Map) {
                            Map map = (Map) list.get(0);
                            String thumbUrl = JsonHelper.getThumbUrl(map);

                            Glide.with(context.get())
                                    .load(thumbUrl)
                                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                    .override(ImageConfig.getThumbnailSize())
                                    .preload();
                        }
                    }
                    return true;
                } catch (Exception e) {
                    LogUtil.e(Log.getStackTraceString(e));
                    return false;
                }
            }
            return false;
        }
    }
}

