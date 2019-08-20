package candybar.lib.helpers;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import com.danimahardhika.android.helpers.core.utils.LogUtil;

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

@SuppressLint("NewApi")
public class DrawableHelper {

    public static Drawable getAppIcon(@NonNull Context context, ResolveInfo info) {
        try {
            return info.activityInfo.loadIcon(context.getPackageManager());
        } catch (OutOfMemoryError | Exception e) {
            return ContextCompat.getDrawable(context, R.drawable.ic_app_default);
        }
    }

    @Nullable
    public static Drawable getReqIcon(@NonNull Context context, String fullComponentName) {
        PackageManager packageManager = context.getPackageManager();

        int slashIndex = fullComponentName.indexOf("/");
        String activityName = fullComponentName.substring(slashIndex).replace("/", "");
        String packageName = fullComponentName.replace("/" + activityName, "");
        ComponentName componentName = new ComponentName(packageName, activityName);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            // Load Adaptive Icons if found
            Intent intent = new Intent();
            intent.setComponent(componentName);
            ResolveInfo resolveInfo = packageManager.resolveActivity(intent, 0);
            Drawable normalDrawable = resolveInfo.loadIcon(packageManager);

            if (normalDrawable instanceof AdaptiveIconDrawable) return normalDrawable;
        }

        try {
            // Get XXXHDPI Icon for Non-Adaptive Icons
            ActivityInfo info = packageManager.getActivityInfo(
                    componentName, PackageManager.GET_META_DATA);
            Resources resources = packageManager.getResourcesForActivity(componentName);

            int density = DisplayMetrics.DENSITY_XXHIGH;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                density = DisplayMetrics.DENSITY_XXXHIGH;
            }

            Drawable drawable = ResourcesCompat.getDrawableForDensity(
                    resources, info.icon, density, null);

            if (drawable != null) return drawable;
        } catch (Exception | OutOfMemoryError e) {
            LogUtil.e(Log.getStackTraceString(e));
        }
        return null;
    }

    public static Bitmap getRightIcon(Drawable drawable) {
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
            Log.d("CandyBar", "Made Normal Icon in Low SDK");
            return ((BitmapDrawable) drawable).getBitmap();
        } else {
            if (drawable instanceof BitmapDrawable) {
                Log.d("CandyBar", "Made Normal Icon in High SDK");
                return ((BitmapDrawable) drawable).getBitmap();
            } else if (drawable instanceof AdaptiveIconDrawable) {
                AdaptiveIconDrawable adaptiveID = ((AdaptiveIconDrawable) drawable);
                AdaptiveIcon adaptiveIcon = new AdaptiveIcon();
                adaptiveIcon.setDrawables(adaptiveID.getForeground(), adaptiveID.getBackground());
                Bitmap iconBitmap = adaptiveIcon.render();
                Log.d("CandyBar", "Made Adaptive Icon in High SDK");
                return iconBitmap;
            }
        }
        return null;
    }
}

