package candybar.lib.helpers;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.danimahardhika.android.helpers.core.utils.LogUtil;

import java.io.ByteArrayOutputStream;
import java.security.MessageDigest;

import candybar.lib.R;
import sarsamurmu.adaptiveicon.AdaptiveIcon;

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

public class DrawableHelper {

    public static Drawable getAppIcon(@NonNull Context context, ResolveInfo info) {
        try {
            return info.activityInfo.loadIcon(context.getPackageManager());
        } catch (OutOfMemoryError | Exception e) {
            return ContextCompat.getDrawable(context, R.drawable.ic_app_default);
        }
    }

    @Nullable
    public static Drawable getReqIcon(@NonNull Context context, String componentNameStr) {
        PackageManager packageManager = context.getPackageManager();

        int slashIndex = componentNameStr.indexOf("/");
        String packageName = componentNameStr.substring(0, slashIndex);
        String activityName = componentNameStr.substring(slashIndex + 1);
        ComponentName componentName = new ComponentName(packageName, activityName);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // Load Adaptive icon if possible
            Intent intent = new Intent();
            intent.setComponent(componentName);
            ResolveInfo resolveInfo = packageManager.resolveActivity(intent, 0);
            if (resolveInfo != null) {
                Drawable adaptiveDrawable = resolveInfo.loadIcon(packageManager);
                if (adaptiveDrawable instanceof AdaptiveIconDrawable) return adaptiveDrawable;
            }
        }

        // Fallback to legacy icon if AdaptiveIcon is not found
        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            Resources appResources = packageManager.getResourcesForApplication(appInfo);

            int density = DisplayMetrics.DENSITY_XHIGH;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                density = DisplayMetrics.DENSITY_XXHIGH;
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                density = DisplayMetrics.DENSITY_XXXHIGH;
            }
            Drawable drawable = ResourcesCompat.getDrawableForDensity(appResources, appInfo.icon,
                    density, null);

            if (drawable != null) return drawable;
            LogUtil.e("DrawableHelper - drawable is null");
        } catch (Exception | OutOfMemoryError e) {
            LogUtil.e(Log.getStackTraceString(e));
        }
        return null;
    }

    public static Bitmap getRightIcon(Drawable drawable) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else {
            if (drawable instanceof BitmapDrawable) {
                return ((BitmapDrawable) drawable).getBitmap();
            } else if (drawable instanceof AdaptiveIconDrawable) {
                return new AdaptiveIcon()
                        .setDrawable((AdaptiveIconDrawable) drawable)
                        .render();
            }
        }
        return null;
    }

    public static String getReqIconBase64(@NonNull Drawable drawable) {
        Bitmap appBitmap = getRightIcon(drawable);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assert appBitmap != null;
        appBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        String base64Icon = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        return base64Icon.trim();
    }

    public static @ColorInt
    int stringToColor(Context context, String input) {
        try {
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update((context.getPackageName() + "_" + input).getBytes());
            int i = (new String(messageDigest.digest())).hashCode();
            String colorCode = "#" +
                    /*Integer.toHexString(((i >> 24) & 0xFF)) + // <<-- This is for alpha*/
                    Integer.toHexString(((i >> 16) & 0xFF)) +
                    Integer.toHexString(((i >> 8) & 0xFF)) +
                    Integer.toHexString((i & 0xFF));
            try {
                return Color.parseColor(colorCode);
            } catch (IllegalArgumentException e) {
                return stringToColor(context, colorCode);
            }
        } catch (Exception ignored) {
            return 0x000000;
        }
    }
}
