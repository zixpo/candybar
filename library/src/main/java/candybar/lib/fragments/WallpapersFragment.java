package candybar.lib.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bumptech.glide.Glide;
import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.ViewHelper;
import com.danimahardhika.android.helpers.core.utils.LogUtil;
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

import candybar.lib.R;
import candybar.lib.adapters.WallpapersAdapter;
import candybar.lib.applications.CandyBarApplication;
import candybar.lib.databases.Database;
import candybar.lib.helpers.JsonHelper;
import candybar.lib.helpers.TapIntroHelper;
import candybar.lib.items.Wallpaper;
import candybar.lib.preferences.Preferences;
import candybar.lib.utils.listeners.WallpapersListener;

import static candybar.lib.helpers.ViewHelper.setFastScrollColor;

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

public class WallpapersFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private SwipeRefreshLayout mSwipe;
    private ProgressBar mProgress;
    private RecyclerFastScroller mFastScroll;

    private AsyncTask<Void, Void, ?> mAsyncTask;

    @Nullable
    @Override
    @SuppressWarnings("ConstantConditions")
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_wallpapers, container, false);
        mRecyclerView = view.findViewById(R.id.wallpapers_grid);
        mSwipe = view.findViewById(R.id.swipe);
        mProgress = view.findViewById(R.id.progress);
        mFastScroll = view.findViewById(R.id.fastscroll);

        if (!Preferences.get(getActivity()).isToolbarShadowEnabled()) {
            View shadow = view.findViewById(R.id.shadow);
            if (shadow != null) shadow.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setNestedScrollingEnabled(mRecyclerView, false);

        mProgress.getIndeterminateDrawable().setColorFilter(
                ColorHelper.getAttributeColor(getActivity(), R.attr.colorAccent),
                PorterDuff.Mode.SRC_IN);
        mSwipe.setColorSchemeColors(
                ContextCompat.getColor(getActivity(), R.color.swipeRefresh));

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setHasFixedSize(false);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),
                getActivity().getResources().getInteger(R.integer.wallpapers_column_count)));

        setFastScrollColor(mFastScroll);
        mFastScroll.attachRecyclerView(mRecyclerView);

        mSwipe.setOnRefreshListener(() -> {
            if (mProgress.getVisibility() == View.GONE)
                mAsyncTask = new WallpapersLoader(true).execute();
            else mSwipe.setRefreshing(false);
        });

        mAsyncTask = new WallpapersLoader(false).execute();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ViewHelper.resetSpanCount(mRecyclerView,
                getActivity().getResources().getInteger(R.integer.wallpapers_column_count));
    }

    @Override
    public void onDestroy() {
        if (mAsyncTask != null) mAsyncTask.cancel(true);
        Activity activity = getActivity();
        if (activity != null) Glide.get(activity).clearMemory();
        super.onDestroy();
    }

    @SuppressLint("StaticFieldLeak")
    private class WallpapersLoader extends AsyncTask<Void, Void, Boolean> {

        private List<Wallpaper> wallpapers;
        private final boolean refreshing;

        private WallpapersLoader(boolean refreshing) {
            this.refreshing = refreshing;
        }

        @Override
        protected void onPreExecute() {
            if (!refreshing) mProgress.setVisibility(View.VISIBLE);
            else mSwipe.setRefreshing(true);
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        protected Boolean doInBackground(Void... voids) {
            if (!isCancelled()) {
                try {
                    Thread.sleep(1);

                    URL url = new URL(getString(R.string.wallpaper_json));
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setConnectTimeout(15000);

                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream stream = connection.getInputStream();
                        List list = JsonHelper.parseList(stream);
                        if (list == null) {
                            LogUtil.e("Json error, no array with name: "
                                    + CandyBarApplication.getConfiguration().getWallpaperJsonStructure().getArrayName());
                            return false;
                        }

                        if (Database.get(getActivity()).getWallpapersCount() > 0) {
                            Database.get(getActivity()).deleteWallpapers();
                        }

                        Database.get(getActivity()).addWallpapers(null, list);
                        wallpapers = Database.get(getActivity()).getWallpapers(null);

                        return true;
                    }
                } catch (Exception e) {
                    LogUtil.e(Log.getStackTraceString(e));
                    return false;
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            if (getActivity() == null) return;
            if (getActivity().isFinishing()) return;

            mAsyncTask = null;
            mProgress.setVisibility(View.GONE);
            mSwipe.setRefreshing(false);

            if (aBoolean) {
                mRecyclerView.setAdapter(new WallpapersAdapter(getActivity(), wallpapers));

                ((WallpapersListener) getActivity())
                        .onWallpapersChecked(Database.get(getActivity()).getWallpapersCount());

                try {
                    if (getActivity().getResources().getBoolean(R.bool.show_intro)) {
                        TapIntroHelper.showWallpapersIntro(getActivity(), mRecyclerView);
                    }
                } catch (Exception e) {
                    LogUtil.e(Log.getStackTraceString(e));
                }
            } else {
                Toast.makeText(getActivity(), R.string.connection_failed,
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
