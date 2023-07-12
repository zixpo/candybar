package candybar.lib.helpers;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.RectF;
import android.net.Uri;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.danimahardhika.android.helpers.core.WindowHelper;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import candybar.lib.applications.CandyBarApplication;
import candybar.lib.items.ImageSize;

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

public class WallpaperHelper {

    public static final int UNKNOWN = 0;
    public static final int CLOUD_WALLPAPERS = 1;
    public static final int EXTERNAL_APP = 2;

    public static int getWallpaperType(@NonNull Context context) {
        String url = CandyBarApplication.getConfiguration().getConfigHandler().wallpaperJson(context);
        if (url.startsWith("assets://") || URLUtil.isValidUrl(url)) {
            return CLOUD_WALLPAPERS;
        } else if (url.length() > 0) {
            return EXTERNAL_APP;
        }
        return UNKNOWN;
    }

    public static InputStream getJSONStream(@NonNull Context context) throws IOException {
        return getStream(context, CandyBarApplication.getConfiguration().getConfigHandler().wallpaperJson(context));
    }

    /**
     * This method adds support for `assets` protocol for loading file from `assets` directory
     */
    public static InputStream getStream(Context context, String urlStr) throws IOException {
        InputStream stream = null;

        if (urlStr.startsWith("assets://")) {
            stream = context.getAssets().open(urlStr.replaceFirst("assets://", ""));
        } else {
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(15000);

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                stream = connection.getInputStream();
            }
        }

        return stream;
    }

    public static void launchExternalApp(@NonNull Context context) {
        String packageName = CandyBarApplication.getConfiguration().getConfigHandler().wallpaperJson(context);

        PackageManager pm = context.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(packageName);
        if (intent != null) {
            try {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
                return;
            } catch (Exception ignored) {
            }
        }

        try {
            Intent store = new Intent(Intent.ACTION_VIEW, Uri.parse(
                    "https://play.google.com/store/apps/details?id=" + packageName));
            context.startActivity(store);
        } catch (ActivityNotFoundException ignored) {
        }
    }

    public static String getFormat(String mimeType) {
        if (mimeType == null) return "jpg";
        if ("image/png".equals(mimeType)) {
            return "png";
        }
        return "jpg";
    }

    public static ImageSize getTargetSize(@NonNull Context context) {
        Point point = WindowHelper.getScreenSize(context);
        int targetHeight = point.y;
        int targetWidth = point.x;

        if (context.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            targetHeight = point.x;
            targetWidth = point.y;
        }

        return new ImageSize(targetWidth, targetHeight);
    }

    @Nullable
    public static RectF getScaledRectF(@Nullable RectF rectF, float heightFactor, float widthFactor) {
        if (rectF == null) return null;

        RectF scaledRectF = new RectF(rectF);
        scaledRectF.top *= heightFactor;
        scaledRectF.bottom *= heightFactor;
        scaledRectF.left *= widthFactor;
        scaledRectF.right *= widthFactor;
        return scaledRectF;
    }
}
