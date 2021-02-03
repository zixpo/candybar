package candybar.lib.utils;

import android.content.Context;
import android.graphics.Bitmap;

import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.module.AppGlideModule;

import org.jetbrains.annotations.NotNull;

@GlideModule
public final class CandyBarGlideModule extends AppGlideModule {
    @Override
    public void registerComponents(@NotNull Context context, @NotNull Glide glide, Registry registry) {
        registry.prepend(String.class, Bitmap.class, new CommonModelLoaderFactory(context));
    }
}
