package candybar.lib.helpers;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.res.ResourcesCompat;
import android.util.DisplayMetrics;
import android.util.Log;

import com.danimahardhika.android.helpers.core.utils.LogUtil;
import com.dm.material.dashboard.candybar.R;

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
    public static Drawable getReqIcon(@NonNull Context context, String packageName) {
        try {
            PackageManager packageManager = context.getPackageManager();
            Drawable drawable = packageManager.getApplicationIcon(packageName);
            return drawable;
        } catch (Exception | OutOfMemoryError e) {
            LogUtil.e(Log.getStackTraceString(e));
        }
        return null;
    }

    @Nullable
    public static Bitmap getMergedIcon(Drawable drawable) {

        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.O) {
            Log.d("Merged Icon", "Executed Normal in Low SDK");
            return ((BitmapDrawable) drawable).getBitmap();
        } else {
            if (drawable instanceof BitmapDrawable) {
                Log.d("Merged Icon", "Executed Normal in High SDK");
                return ((BitmapDrawable) drawable).getBitmap();
            } else {
                if (drawable instanceof AdaptiveIconDrawable) {
                    AdaptiveIconDrawable aid = ((AdaptiveIconDrawable) drawable);

                    Drawable[] drr = new Drawable[2];
                    drr[0] = aid.getBackground();
                    drr[1] = aid.getForeground();

                    LayerDrawable layerDrawable = new LayerDrawable(drr);

                    int width = layerDrawable.getIntrinsicWidth();
                    int height = layerDrawable.getIntrinsicHeight();

                    Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                    Canvas canvas = new Canvas(bitmap);

                    layerDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                    layerDrawable.draw(canvas);

                    bitmap = GetBitmapClippedCircle(bitmap);
                    Log.d("Merged Icon", "Executed Adaptive in High SDK");
                    return bitmap;
                }
            }
        }
        return null;
    }

    private static Bitmap GetBitmapClippedCircle(Bitmap bitmap) {
        final int width = bitmap.getWidth();
        final int height = bitmap.getHeight();
        final Bitmap outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        float toScale = 1.3f;

        final Path path = new Path();
        Matrix scaleMatrix = new Matrix();
        scaleMatrix.setScale(0.76f, 0.76f);
        path.addCircle(
                (float) (width / 2)
                , (float) (height / 2)
                , (float) Math.min(width, (height / 2))
                , Path.Direction.CCW);
        path.transform(scaleMatrix);

        final Canvas canvas = new Canvas(outputBitmap);
        //canvas.translate(-50, -50);
        canvas.scale(toScale, toScale);
        canvas.clipPath(path);
        canvas.drawBitmap(bitmap, -36.5f, -36.5f, null);
        return outputBitmap;
    }

    @Nullable
    public static Drawable getHighQualityIcon(@NonNull Context context, String packageName) {
        try {
            PackageManager packageManager = context.getPackageManager();
            ApplicationInfo info = packageManager.getApplicationInfo(
                    packageName, PackageManager.GET_META_DATA);

            Resources resources = packageManager.getResourcesForApplication(packageName);
            int density = DisplayMetrics.DENSITY_XXHIGH;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                density = DisplayMetrics.DENSITY_XXXHIGH;
            }

            Drawable drawable = ResourcesCompat.getDrawableForDensity(
                    resources, info.icon, density, null);
            if (drawable != null) return drawable;
            return info.loadIcon(packageManager);
        } catch (Exception | OutOfMemoryError e) {
            LogUtil.e(Log.getStackTraceString(e));
        }
        return null;
    }
}

