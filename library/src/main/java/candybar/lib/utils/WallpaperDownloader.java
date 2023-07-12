package candybar.lib.utils;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

import com.danimahardhika.android.helpers.core.utils.LogUtil;
import com.danimahardhika.android.helpers.permission.PermissionHelper;
import com.danimahardhika.cafebar.CafeBar;
import com.danimahardhika.cafebar.CafeBarTheme;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import candybar.lib.R;
import candybar.lib.helpers.TypefaceHelper;
import candybar.lib.helpers.WallpaperHelper;
import candybar.lib.items.Wallpaper;

/*
 * Wallpaper Board
 *
 * Copyright (c) 2017 Dani Mahardhika
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

public class WallpaperDownloader {

    private final Context mContext;
    private Wallpaper mWallpaper;

    private WallpaperDownloader(Context context) {
        mContext = context;
    }

    public WallpaperDownloader wallpaper(@NonNull Wallpaper wallpaper) {
        mWallpaper = wallpaper;
        return this;
    }

    private void showCafeBar(int res) {
        CafeBar.builder(mContext)
                .theme(CafeBarTheme.Custom(ContextCompat.getColor(mContext, R.color.cardBackground)))
                .contentTypeface(TypefaceHelper.getRegular(mContext))
                .content(res)
                .floating(true)
                .fitSystemWindow()
                .show();
    }

    private void showOpenFileCafeBar(@StringRes int textRes, File target) {
        CafeBar.builder(mContext)
                .theme(CafeBarTheme.Custom(ContextCompat.getColor(mContext, R.color.cardBackground)))
                .floating(true)
                .fitSystemWindow()
                .duration(CafeBar.Duration.MEDIUM)
                .typeface(TypefaceHelper.getRegular(mContext), TypefaceHelper.getBold(mContext))
                .content(textRes)
                .neutralText(R.string.open)
                .onNeutral(cafeBar -> {
                    Uri uri = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
                            ? Uri.parse(target.toString())
                            : Uri.fromFile(target);

                    if (uri == null) {
                        cafeBar.dismiss();
                        return;
                    }

                    mContext.startActivity(new Intent()
                            .setAction(Intent.ACTION_VIEW)
                            .setDataAndType(uri, "image/*")
                            .setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION));

                    cafeBar.dismiss();
                })
                .show();
    }

    public void start() {
        String fileName = mWallpaper.getName() + "." + WallpaperHelper.getFormat(mWallpaper.getMimeType());
        String appName = mContext.getResources().getString(R.string.app_name);

        File directory = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES) + File.separator + appName);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (!PermissionHelper.isStorageGranted(mContext)) {
                PermissionHelper.requestStorage(mContext);
                return;
            }

            if (!directory.exists() && !directory.mkdirs()) {
                LogUtil.e("Unable to create directory " + directory.toString());
                showCafeBar(R.string.wallpaper_download_failed);
                return;
            }
        }

        try {
            File target = new File(directory, fileName);

            if (target.exists()) {
                showOpenFileCafeBar(R.string.wallpaper_already_downloaded, target);
                return;
            }
        } catch (SecurityException e) {
            LogUtil.e(Log.getStackTraceString(e));
        }

        String url = mWallpaper.getURL();

        if (!(url.startsWith("assets://") || URLUtil.isValidUrl(mWallpaper.getURL()))) {
            LogUtil.e("Download: wallpaper url is not valid");
            return;
        }

        if (url.startsWith("assets://")) {
            showCafeBar(R.string.wallpaper_downloading);

            try {
                InputStream is = mContext.getAssets().open(url.replaceFirst("assets://", ""));
                File output = new File(directory, fileName);
                if (!directory.exists()) {
                    if (!directory.mkdirs()) return;
                }
                if (!output.exists()) {
                    if (!output.createNewFile()) return;
                }
                OutputStream os = new FileOutputStream(output);
                byte[] buffer = new byte[1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }

                mContext.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(output)));

                showOpenFileCafeBar(R.string.wallpaper_download_success, output);
            } catch (Exception e) {
                LogUtil.e(Log.getStackTraceString(e));
                showCafeBar(R.string.wallpaper_download_failed);
            }
        } else {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setMimeType(mWallpaper.getMimeType());
            request.setTitle(fileName);
            request.setDescription(mContext.getResources().getString(R.string.wallpaper_downloading));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setDestinationInExternalPublicDir(Environment.DIRECTORY_PICTURES,
                    File.separator + appName + File.separator + fileName);

            DownloadManager downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);

            try {
                if (downloadManager != null) {
                    downloadManager.enqueue(request);
                }
            } catch (IllegalArgumentException e) {
                LogUtil.e(Log.getStackTraceString(e));
                showCafeBar(R.string.wallpaper_download_failed);
                return;
            }

            showCafeBar(R.string.wallpaper_downloading);
        }
    }

    public static WallpaperDownloader prepare(@NonNull Context context) {
        return new WallpaperDownloader(context);
    }
}
