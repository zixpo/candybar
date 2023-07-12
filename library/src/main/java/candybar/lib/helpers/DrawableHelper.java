package candybar.lib.helpers;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.VectorDrawable;
import android.os.Build;
import android.util.Base64;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.danimahardhika.android.helpers.core.utils.LogUtil;

import java.io.ByteArrayOutputStream;
import java.lang.reflect.Field;

import candybar.lib.R;
import candybar.lib.applications.CandyBarApplication;
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
    public static Drawable getPackageIcon(@NonNull Context context, String componentNameStr) {
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

            Drawable drawable = ResourcesCompat.getDrawableForDensity(appResources, appInfo.icon,
                    DisplayMetrics.DENSITY_XXXHIGH, null);

            if (drawable != null) return drawable;
        } catch (Exception | OutOfMemoryError e) {
            LogUtil.e(Log.getStackTraceString(e));
        }

        LogUtil.e("DrawableHelper - drawable is null");

        return null;
    }

    public static Bitmap toBitmap(Drawable drawable) {
        // Using square shape for more detail (area) in icon image
        return toBitmap(drawable, AdaptiveIcon.PATH_SQUARE);
    }

    public static Bitmap toBitmap(Drawable drawable, int shape) {
        if (drawable instanceof BitmapDrawable) return ((BitmapDrawable) drawable).getBitmap();
        if (drawable instanceof LayerDrawable || drawable instanceof VectorDrawable) {
            final boolean isVector = drawable instanceof VectorDrawable;
            final int width = isVector ? 256 : drawable.getIntrinsicWidth();
            final int height = isVector ? 256 : drawable.getIntrinsicHeight();
            final Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            drawable.setBounds(0, 0, width, height);
            drawable.draw(new Canvas(bitmap));
            return bitmap;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && drawable instanceof AdaptiveIconDrawable) {
            if (shape == -1) {
                // System default icon shape
                Bitmap bitmap = Bitmap.createBitmap(256, 256, Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(new Rect(0, 0, 256, 256));
                drawable.draw(canvas);
                return bitmap;
            }

            return new AdaptiveIcon()
                    .setDrawable((AdaptiveIconDrawable) drawable)
                    .setPath(shape)
                    .render();
        }
        return null;
    }

    public static String getReqIconBase64(@NonNull Drawable drawable) {
        Bitmap appBitmap = toBitmap(drawable);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        assert appBitmap != null;
        appBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        String base64Icon = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
        return base64Icon.trim();
    }

    public static int getDrawableId(String resource) {
        try {
            Field idField = CandyBarApplication.mDrawableClass.getDeclaredField(resource);
            return idField.getInt(null);
        } catch (Exception e) {
            LogUtil.e("Reflect resource not found with name - " + resource);
            return -1;
        }
    }
}
