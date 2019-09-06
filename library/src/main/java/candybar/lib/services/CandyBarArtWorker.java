package candybar.lib.services;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.URLUtil;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.android.apps.muzei.api.provider.Artwork;
import com.google.android.apps.muzei.api.provider.ProviderClient;
import com.google.android.apps.muzei.api.provider.ProviderContract;

import java.util.ArrayList;
import java.util.List;

import candybar.lib.R;
import candybar.lib.databases.Database;
import candybar.lib.items.Wallpaper;
import candybar.lib.preferences.Preferences;

@SuppressLint("NewApi")
public class CandyBarArtWorker extends Worker {
    private final String WORKER_TAG = this.getApplicationContext().getPackageName() + "." + "ArtProvider";
    private Context mContext = getApplicationContext();

    public CandyBarArtWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    static void enqueueLoad(Context context) {
        WorkManager manager = WorkManager.getInstance(context);
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        WorkRequest request = new OneTimeWorkRequest.Builder(CandyBarArtWorker.class)
                .setConstraints(constraints)
                .build();
        manager.enqueue(request);
    }


    @Override
    @NonNull
    public Result doWork() {
        Log.d("CandyBar", "Executing doWork() for Muzei");
        if (!URLUtil.isValidUrl(mContext.getString(R.string.wallpaper_json))) {
            Log.e("CandyBar", "Not a valid Wallpaper JSON URL");
            return Result.failure();
        }

        List<Wallpaper> wallpapers = Database.get(mContext).getWallpapers();

        ProviderClient providerClient = ProviderContract.getProviderClient(getApplicationContext(), WORKER_TAG);

        if (Preferences.get(getApplicationContext()).isConnectedAsPreferred()) {
            ArrayList artworks = new ArrayList<Artwork>();

            for (Wallpaper wallpaper : wallpapers) {
                if (wallpaper != null) {
                    Uri uri = Uri.parse(wallpaper.getURL());

                    Artwork artwork = new Artwork.Builder()
                            .title(wallpaper.getName())
                            .byline(wallpaper.getAuthor())
                            .persistentUri(uri)
                            .build();

                    if (!artworks.contains(artwork)) {
                        artworks.add(artwork);
                    } else {
                        Log.d("CandyBar", "Already Contains Artwork" + wallpaper.getName());
                    }
                } else {
                    Log.d("CandyBar", "Wallpaper is Null");
                }
            }

            Log.d("CandyBar", "Closing Database - Muzei");
            Database.get(mContext).closeDatabase();

            providerClient.setArtwork(artworks);
            return Result.success();
        }

        return Result.failure();
    }
}
