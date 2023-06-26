package candybar.lib.utils;

import static candybar.lib.helpers.DrawableHelper.getPackageIcon;
import static candybar.lib.helpers.DrawableHelper.toBitmap;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.data.DataFetcher;
import com.danimahardhika.android.helpers.core.utils.LogUtil;

import java.io.IOException;
import java.io.InputStream;

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
        } else if (mModel.startsWith("package://")) {
            callback.onDataReady(getPackage(mModel));
        } else if (mModel.startsWith("assets://")) {
            callback.onDataReady(getAsset(mModel));
        }
    }

    @Nullable
    private Bitmap getPackage(String uri) {
        String componentName = uri.replaceFirst("package://", "");
        Drawable drawable = getPackageIcon(mContext, componentName);

        if (drawable != null) {
            return toBitmap(drawable, AdaptiveIcon.PATH_CIRCLE);
        }

        return null;
    }

    @Nullable
    private Bitmap getDrawable(String uri) {
        String drawableIdStr = uri.replaceFirst("drawable://", "");
        int drawableId = Integer.parseInt(drawableIdStr);
        Drawable drawable = ContextCompat.getDrawable(mContext, drawableId);
        return toBitmap(drawable, Preferences.get(mContext).getIconShape());
    }

    @Nullable
    private Bitmap getAsset(String uri) {
        try (InputStream stream = mContext.getAssets().open(uri.replaceFirst("assets://", ""))) {
            return BitmapFactory.decodeStream(stream);
        } catch (IOException e) {
            LogUtil.e(Log.getStackTraceString(e));
        }

        return null;
    }

    @Override
    public void cleanup() {
    }

    @Override
    public void cancel() {
    }

    @NonNull
    @Override
    public Class<Bitmap> getDataClass() {
        return Bitmap.class;
    }

    @NonNull
    @Override
    public DataSource getDataSource() {
        // Because transitions do not work with local resources
        return DataSource.REMOTE;
    }
}
