package candybar.lib.fragments;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import java.util.ArrayList;
import java.util.List;

import candybar.lib.R;
import candybar.lib.activities.CandyBarMainActivity;
import candybar.lib.adapters.HomeAdapter;
import candybar.lib.applications.CandyBarApplication;
import candybar.lib.helpers.TapIntroHelper;
import candybar.lib.helpers.WallpaperHelper;
import candybar.lib.items.Home;
import candybar.lib.preferences.Preferences;
import candybar.lib.utils.listeners.HomeListener;

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

public class HomeFragment extends Fragment implements HomeListener {

    private RecyclerView mRecyclerView;
    private StaggeredGridLayoutManager mManager;

    @Nullable
    @Override
    @SuppressWarnings("ConstantConditions")
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        mRecyclerView = view.findViewById(R.id.recyclerview);

        if (!Preferences.get(getActivity()).isToolbarShadowEnabled()) {
            View shadow = view.findViewById(R.id.shadow);
            if (shadow != null) shadow.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mManager = new StaggeredGridLayoutManager(
                getActivity().getResources().getInteger(R.integer.home_column_count),
                StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(mManager);

        if (CandyBarApplication.getConfiguration().getHomeGrid() == CandyBarApplication.GridStyle.FLAT) {
            int padding = getActivity().getResources().getDimensionPixelSize(R.dimen.card_margin);
            mRecyclerView.setPadding(padding, padding, 0, 0);
        }

        initHome();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        HomeAdapter adapter = (HomeAdapter) mRecyclerView.getAdapter();
        if (adapter != null) adapter.setOrientation(newConfig.orientation);
    }

    @Override
    public void onHomeDataUpdated(Home home) {
        if (mRecyclerView == null) return;
        if (mRecyclerView.getAdapter() == null) return;

        if (home != null) {
            HomeAdapter adapter = (HomeAdapter) mRecyclerView.getAdapter();
            if (CandyBarApplication.getConfiguration().isAutomaticIconsCountEnabled()) {
                int index = adapter.getIconsIndex();
                if (index >= 0 && index < adapter.getItemCount()) {
                    adapter.getItem(index).setTitle(String.valueOf(CandyBarMainActivity.sIconsCount));
                    adapter.getItem(index).setLoading(false);
                    adapter.notifyItemChanged(index);
                }
            }

            int dimensionsIndex = adapter.getDimensionsIndex();
            if (dimensionsIndex < 0) {
                adapter.addNewContent(home);
            }
            return;
        }

        RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
        if (adapter.getItemCount() > 8) {
            //Probably the original adapter already modified
            adapter.notifyDataSetChanged();
            return;
        }

        if (adapter instanceof HomeAdapter) {
            HomeAdapter homeAdapter = (HomeAdapter) adapter;
            int index = homeAdapter.getIconRequestIndex();
            if (index >= 0 && index < adapter.getItemCount()) {
                adapter.notifyItemChanged(index);
            }
        }
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onHomeIntroInit() {
        if (getActivity().getResources().getBoolean(R.bool.show_intro)) {
            TapIntroHelper.showHomeIntros(getActivity(),
                    mRecyclerView, mManager,
                    ((HomeAdapter) mRecyclerView.getAdapter()).getApplyIndex());
        }
    }

    @SuppressLint("StringFormatInvalid")
    @SuppressWarnings("ConstantConditions")
    private void initHome() {
        List<Home> homes = new ArrayList<>();

        if (getActivity().getResources().getBoolean(R.bool.enable_apply)) {
            homes.add(new Home(
                    R.drawable.ic_toolbar_apply_launcher,
                    String.format(getActivity().getResources().getString(R.string.home_apply_icon_pack),
                            getActivity().getResources().getString(R.string.app_name)),
                    "",
                    Home.Type.APPLY,
                    false));
        }

        if (getActivity().getResources().getBoolean(R.bool.enable_donation)) {
            homes.add(new Home(
                    R.drawable.ic_toolbar_donate,
                    getActivity().getResources().getString(R.string.home_donate),
                    getActivity().getResources().getString(R.string.home_donate_desc),
                    Home.Type.DONATE,
                    false));
        }

        homes.add(new Home(
                -1,
                CandyBarApplication.getConfiguration().isAutomaticIconsCountEnabled() ?
                        String.valueOf(CandyBarMainActivity.sIconsCount) :
                        String.valueOf(CandyBarApplication.getConfiguration().getCustomIconsCount()),
                getActivity().getResources().getString(R.string.home_icons),
                Home.Type.ICONS,
                true));

        if (CandyBarMainActivity.sHomeIcon != null) {
            homes.add(CandyBarMainActivity.sHomeIcon);
        }

        mRecyclerView.setAdapter(new HomeAdapter(getActivity(), homes,
                getActivity().getResources().getConfiguration().orientation));
    }

    @SuppressWarnings("ConstantConditions")
    public void resetWallpapersCount() {
        if (WallpaperHelper.getWallpaperType(getActivity()) == WallpaperHelper.CLOUD_WALLPAPERS) {
            if (mRecyclerView == null) return;
            if (mRecyclerView.getAdapter() == null) return;

            RecyclerView.Adapter adapter = mRecyclerView.getAdapter();
            if (adapter.getItemCount() > 8) {
                //Probably the original adapter already modified
                adapter.notifyDataSetChanged();
                return;
            }

            if (adapter instanceof HomeAdapter) {
                HomeAdapter homeAdapter = (HomeAdapter) adapter;
                int index = homeAdapter.getWallpapersIndex();
                if (index >= 0 && index < adapter.getItemCount()) {
                    adapter.notifyItemChanged(index);
                }
            }
        }
    }
}
