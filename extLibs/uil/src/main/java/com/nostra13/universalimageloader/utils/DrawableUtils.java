package com.nostra13.universalimageloader.utils;

import android.content.Context;
import android.graphics.drawable.Drawable;

import androidx.appcompat.widget.AppCompatDrawableManager;

public class DrawableUtils {

    public static Drawable getDrawable(Context context, int res) {
        try {
            Drawable drawable = AppCompatDrawableManager.get().getDrawable(context, res);
            return drawable.mutate();
        } catch (OutOfMemoryError e) {
            return null;
        }
    }
}
