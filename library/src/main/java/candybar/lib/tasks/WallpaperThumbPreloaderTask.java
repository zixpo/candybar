package candybar.lib.tasks;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.danimahardhika.android.helpers.core.utils.LogUtil;

import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Map;

import candybar.lib.applications.CandyBarApplication;
import candybar.lib.helpers.JsonHelper;
import candybar.lib.helpers.WallpaperHelper;
import candybar.lib.utils.AsyncTaskBase;
import candybar.lib.utils.ImageConfig;

public class WallpaperThumbPreloaderTask extends AsyncTaskBase {

    private final WeakReference<Context> context;

    public WallpaperThumbPreloaderTask(@NonNull Context context) {
        this.context = new WeakReference<>(context);
    }

    @Override
    protected boolean run() {
        if (!isCancelled()) {
            try {
                Thread.sleep(1);

                if (WallpaperHelper.getWallpaperType(context.get()) != WallpaperHelper.CLOUD_WALLPAPERS)
                    return true;

                InputStream stream = WallpaperHelper.getJSONStream(context.get());

                if (stream != null) {
                    List<?> list = JsonHelper.parseList(stream);
                    if (list == null) {
                        LogUtil.e("Json error, no array with name: "
                                + CandyBarApplication.getConfiguration().getWallpaperJsonStructure().getArrayName());
                        return false;
                    }

                    if (list.size() > 0 && list.get(0) instanceof Map) {
                        Map<?, ?> map = (Map<?, ?>) list.get(0);
                        String thumbUrl = JsonHelper.getThumbUrl(map);

                        // Preload the first wallpaper's thumbnail
                        // It should show up immediately without any delay on first run
                        // so that the intro popup works correctly
                        if (context.get() != null) {
                            Glide.with(context.get())
                                    .load(thumbUrl)
                                    .diskCacheStrategy(DiskCacheStrategy.RESOURCE)
                                    .override(ImageConfig.getThumbnailSize())
                                    .preload();
                        }
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
