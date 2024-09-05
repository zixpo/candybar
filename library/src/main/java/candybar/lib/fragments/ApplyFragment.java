package candybar.lib.fragments;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.danimahardhika.android.helpers.core.utils.LogUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import candybar.lib.R;
import candybar.lib.adapters.LauncherAdapter;
import candybar.lib.applications.CandyBarApplication;
import candybar.lib.helpers.LauncherHelper;
import candybar.lib.items.Icon;
import candybar.lib.preferences.Preferences;
import candybar.lib.utils.AsyncTaskBase;

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

public class ApplyFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private AsyncTaskBase mAsyncTask;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_apply, container, false);
        mRecyclerView = view.findViewById(R.id.recyclerview);

        if (!Preferences.get(requireActivity()).isToolbarShadowEnabled()) {
            View shadow = view.findViewById(R.id.shadow);
            if (shadow != null) shadow.setVisibility(View.GONE);
        }
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        CandyBarApplication.getConfiguration().getAnalyticsHandler().logEvent(
                "view",
                new HashMap<String, Object>() {{ put("section", "icon_apply"); }}
        );

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 1));

        if (CandyBarApplication.getConfiguration().getApplyGrid() == CandyBarApplication.GridStyle.FLAT) {
            int padding = requireActivity().getResources().getDimensionPixelSize(R.dimen.card_margin);
            mRecyclerView.setPadding(padding, padding, 0, 0);
        }

        mAsyncTask = new LaunchersLoader().executeOnThreadPool();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onDestroy() {
        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
        }
        super.onDestroy();
    }

    private boolean isPackageInstalled(String pkg) {
        try {
            PackageInfo packageInfo = requireActivity().getPackageManager().getPackageInfo(
                    pkg, PackageManager.GET_ACTIVITIES);
            return packageInfo != null;
        } catch (Exception e) {
            return false;
        }
    }

    private String getInstalledPackage(String[] pkgs) {
        for (String pkg : pkgs) {
            if (isPackageInstalled(pkg)) {
                return pkg;
            }
        }
        return null;
    }

    private boolean shouldLauncherBeAdded(String packageName) {
        assert getActivity() != null;
        if (("com.lge.launcher2").equals(packageName) ||
                ("com.lge.launcher3").equals(packageName)) {
            int id = getResources().getIdentifier("theme_resources", "xml", getActivity().getPackageName());
            return id > 0;
        }
        if ("com.oppo.launcher".equals(packageName)) return
                (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R)
                        && (Build.MANUFACTURER.equalsIgnoreCase("OPPO")
                        || Build.MANUFACTURER.equalsIgnoreCase("realme"));
        if ("com.android.launcher".equals(packageName)) return
                (Build.VERSION.SDK_INT > Build.VERSION_CODES.R)
                        && (Build.MANUFACTURER.equalsIgnoreCase("OnePlus")
                        || Build.MANUFACTURER.equalsIgnoreCase("OPPO")
                        || Build.MANUFACTURER.equalsIgnoreCase("realme"))
                || ((Build.VERSION.SDK_INT == Build.VERSION_CODES.R)
                        && (Build.MANUFACTURER.equalsIgnoreCase("realme")
                ));
        return true;
    }

private class LaunchersLoader extends AsyncTaskBase {

        private List<Icon> launchers;

        @Override
        protected void preRun() {
            launchers = new ArrayList<>();
        }

        @Override
        protected boolean run() {
            if (!isCancelled()) {
                try {
                    Thread.sleep(1);

                    LauncherHelper.Launcher[] dataLaunchers = LauncherHelper.Launcher.values();
                    String[] showableLauncherNames = getResources().getStringArray(
                            R.array.dashboard_launchers);

                    List<Icon> installed = new ArrayList<>();
                    List<Icon> supported = new ArrayList<>();

                    List<String> showable = new ArrayList<>();

                    for (String name : showableLauncherNames) {
                        String filtered_name = name.toLowerCase().replaceAll(" ", "_");
                        showable.add(filtered_name);
                    }

                    for (LauncherHelper.Launcher value : dataLaunchers) {
                        if (value.name == null) continue;
                        if (value.packages == null) continue;

                        String lowercaseLauncherName = value.name.toLowerCase().replaceAll(" ", "_");

                        if (!showable.contains(lowercaseLauncherName)) {
                            LogUtil.d("Launcher Excluded: " + lowercaseLauncherName);
                            continue;
                        }

                        String installedPackage = getInstalledPackage(value.packages);

                        Icon launcher = new Icon(value.name, value.icon, value.packages[0]);
                        if (shouldLauncherBeAdded(value.packages[0])) {
                            if (installedPackage != null) {
                                installed.add(launcher);
                                launcher.setPackageName(installedPackage);
                            } else supported.add(launcher);
                        }
                    }

                    try {
                        Collections.sort(installed, Icon.TitleComparator);
                    } catch (Exception ignored) {
                    }

                    try {
                        Collections.sort(supported, Icon.TitleComparator);
                    } catch (Exception ignored) {
                    }

                    if (installed.size() == 1) {
                        launchers.add(new Icon(getResources().getString(
                                R.string.apply_installed), -1, null));
                    }
                    else{
                        launchers.add(new Icon(getResources().getString(
                                R.string.apply_installed_launchers), -3, null));
                    }

                    launchers.addAll(installed);
                    launchers.add(new Icon(getResources().getString(
                            R.string.apply_supported), -2, null));
                    launchers.addAll(supported);

                    return true;
                } catch (Exception e) {
                    LogUtil.e(Log.getStackTraceString(e));
                    return false;
                }
            }
            return false;
        }

        @Override
        protected void postRun(boolean ok) {
            if (getActivity() == null) return;
            if (getActivity().isFinishing()) return;

            mAsyncTask = null;
            if (ok) {
                mRecyclerView.setAdapter(new LauncherAdapter(getActivity(), launchers));
            }
        }
    }
}
