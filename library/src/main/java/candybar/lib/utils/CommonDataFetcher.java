package candybar.lib.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;

import candybar.lib.preferences.Preferences;
import sarsamurmu.adaptiveicon.AdaptiveIcon;

public class CommonDataFetcher implements DataFetcher<Bitmap> {
    private final Context mContext;
    private final String mModel;

    CommonDataFetcher(Context context, String model) {
        mContext = context;
        mModel = model;
    }

    @Override
    public void loadData(@NonNull Priority priority, @NonNull DataCallback<? super Bitmap> callback) {
        if (mModel.startsWith("drawable://")) {
            callback.onDataReady(getDrawable(mModel));
        }
    }

    @Nullable
    private Bitmap getDrawable(String uri) {
        String drawableIdStr = uri.replaceFirst("drawable://", "");
        int drawableId = Integer.parseInt(drawableIdStr);
        Drawable drawable = ContextCompat.getDrawable(mContext, drawableId);

        if (drawable instanceof BitmapDrawable) return ((BitmapDrawable) drawable).getBitmap();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && drawable instanceof AdaptiveIconDrawable) {
            return new AdaptiveIcon()
                    .setDrawable((AdaptiveIconDrawable) drawable)
                    .setPath(Preferences.get(mContext).getIconShape())
                    .render();
        }

        return null;
    }

    @Override
    public void cleanup() {
    }

    @Override
    public void cancel() {
    }

    @Override
    public Class<Bitmap> getDataClass() {
        return Bitmap.class;
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
        return DataSource.MEMORY_CACHE;
    }
}
