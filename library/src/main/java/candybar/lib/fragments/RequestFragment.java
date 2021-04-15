package candybar.lib.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.interpolator.view.animation.LinearOutSlowInInterpolator;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.afollestad.materialdialogs.MaterialDialog;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.danimahardhika.android.helpers.animation.AnimationHelper;
import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.danimahardhika.android.helpers.core.FileHelper;
import com.danimahardhika.android.helpers.core.ViewHelper;
import com.danimahardhika.android.helpers.core.utils.LogUtil;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import candybar.lib.R;
import candybar.lib.activities.CandyBarMainActivity;
import candybar.lib.adapters.RequestAdapter;
import candybar.lib.applications.CandyBarApplication;
import candybar.lib.fragments.dialog.IntentChooserFragment;
import candybar.lib.helpers.IconsHelper;
import candybar.lib.helpers.RequestHelper;
import candybar.lib.helpers.TapIntroHelper;
import candybar.lib.helpers.TypefaceHelper;
import candybar.lib.items.Request;
import candybar.lib.preferences.Preferences;
import candybar.lib.utils.InAppBillingProcessor;
import candybar.lib.utils.listeners.InAppBillingListener;
import candybar.lib.utils.listeners.RequestListener;

import static candybar.lib.helpers.DrawableHelper.getReqIcon;
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

public class RequestFragment extends Fragment implements View.OnClickListener {

    private RecyclerView mRecyclerView;
    private FloatingActionButton mFab;
    private RecyclerFastScroller mFastScroll;
    private ProgressBar mProgress;

    private MenuItem mMenuItem;
    private RequestAdapter mAdapter;
    private StaggeredGridLayoutManager mManager;
    private AsyncTask<Void, Void, ?> mAsyncTask;

    public static List<Integer> sSelectedRequests;

    private boolean noEmailClientError = false;

    @Nullable
    @Override
    @SuppressWarnings("ConstantConditions")
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_request, container, false);
        mRecyclerView = view.findViewById(R.id.request_list);
        mFab = view.findViewById(R.id.fab);
        mFastScroll = view.findViewById(R.id.fastscroll);
        mProgress = view.findViewById(R.id.progress);

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
        setHasOptionsMenu(false);
        resetRecyclerViewPadding(getResources().getConfiguration().orientation);

        mProgress.getIndeterminateDrawable().setColorFilter(
                ColorHelper.getAttributeColor(getActivity(), R.attr.colorAccent),
                PorterDuff.Mode.SRC_IN);

        int color = ColorHelper.getTitleTextColor(ColorHelper
                .getAttributeColor(getActivity(), R.attr.colorAccent));
        mFab.setImageDrawable(DrawableHelper.getTintedDrawable(
                getActivity(), R.drawable.ic_fab_send, color));
        mFab.setOnClickListener(this);

        if (!Preferences.get(getActivity()).isFabShadowEnabled()) {
            mFab.setCompatElevation(0f);
        }

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.getItemAnimator().setChangeDuration(0);
        mManager = new StaggeredGridLayoutManager(
                getActivity().getResources().getInteger(R.integer.request_column_count),
                StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mManager);

        setFastScrollColor(mFastScroll);
        mFastScroll.attachRecyclerView(mRecyclerView);

        mAsyncTask = new MissingAppsLoader().execute();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        resetRecyclerViewPadding(newConfig.orientation);
        if (mAsyncTask != null) return;

        int[] positions = mManager.findFirstVisibleItemPositions(null);

        SparseBooleanArray selectedItems = mAdapter.getSelectedItemsArray();
        ViewHelper.resetSpanCount(mRecyclerView,
                getActivity().getResources().getInteger(R.integer.request_column_count));

        mAdapter = new RequestAdapter(getActivity(),
                CandyBarMainActivity.sMissedApps,
                mManager.getSpanCount());
        mRecyclerView.setAdapter(mAdapter);
        mAdapter.setSelectedItemsArray(selectedItems);

        if (positions.length > 0)
            mRecyclerView.scrollToPosition(positions[0]);
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_request, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onDestroy() {
        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
        }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_select_all) {
            mMenuItem = item;
            if (mAdapter == null) return false;
            if (mAdapter.selectAll()) {
                item.setIcon(R.drawable.ic_toolbar_select_all_selected);
                return true;
            }

            item.setIcon(R.drawable.ic_toolbar_select_all);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onClick(View view) {
        int id = view.getId();
        if (id == R.id.fab) {
            if (mAdapter == null) return;

            int selected = mAdapter.getSelectedItemsSize();
            if (selected > 0) {
                if (mAdapter.isContainsRequested()) {
                    RequestHelper.showAlreadyRequestedDialog(getActivity());
                    return;
                }

                boolean requestLimit = getResources().getBoolean(
                        R.bool.enable_icon_request_limit);
                boolean iconRequest = getResources().getBoolean(
                        R.bool.enable_icon_request);
                boolean premiumRequest = getResources().getBoolean(
                        R.bool.enable_premium_request);

                if (Preferences.get(getActivity()).isPremiumRequest()) {
                    int count = Preferences.get(getActivity()).getPremiumRequestCount();
                    if (selected > count) {
                        RequestHelper.showPremiumRequestLimitDialog(getActivity(), selected);
                        return;
                    }

                    if (!RequestHelper.isReadyToSendPremiumRequest(getActivity())) return;

                    try {
                        InAppBillingListener listener = (InAppBillingListener) getActivity();
                        listener.onInAppBillingRequest();
                    } catch (Exception ignored) {
                    }
                    return;
                }

                if (!iconRequest && premiumRequest) {
                    RequestHelper.showPremiumRequestRequired(getActivity());
                    return;
                }

                if (requestLimit) {
                    int limit = getActivity().getResources().getInteger(R.integer.icon_request_limit);
                    int used = Preferences.get(getActivity()).getRegularRequestUsed();
                    if (selected > (limit - used)) {
                        RequestHelper.showIconRequestLimitDialog(getActivity());
                        return;
                    }
                }

                if ((getActivity().getResources().getBoolean(R.bool.json_check_before_request)) && (getActivity().getResources().getString(R.string.config_json).length() != 0)) {
                    mAsyncTask = new CheckConfig().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                } else {
                    mAsyncTask = new RequestLoader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }

            } else {
                Toast.makeText(getActivity(), R.string.request_not_selected,
                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void resetRecyclerViewPadding(int orientation) {
        if (mRecyclerView == null) return;

        int padding = 0;
        boolean tabletMode = getResources().getBoolean(R.bool.android_helpers_tablet_mode);
        if (tabletMode || orientation == Configuration.ORIENTATION_LANDSCAPE) {
            padding = getActivity().getResources().getDimensionPixelSize(R.dimen.content_padding);

            if (CandyBarApplication.getConfiguration().getRequestStyle() == CandyBarApplication.Style.PORTRAIT_FLAT_LANDSCAPE_FLAT) {
                padding = getActivity().getResources().getDimensionPixelSize(R.dimen.card_margin);
            }
        }

        int size = getActivity().getResources().getDimensionPixelSize(R.dimen.fab_size);
        int marginGlobal = getActivity().getResources().getDimensionPixelSize(R.dimen.fab_margin_global);

        mRecyclerView.setPadding(padding, padding, 0, size + (marginGlobal * 2));
    }

    public void prepareRequest() {
        if (mAsyncTask != null) return;

        mAsyncTask = new RequestLoader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    public void refreshIconRequest() {
        if (mAdapter == null) {
            RequestFragment.sSelectedRequests = null;
            return;
        }

        if (RequestFragment.sSelectedRequests == null)
            mAdapter.notifyItemChanged(0);

        for (Integer integer : RequestFragment.sSelectedRequests) {
            mAdapter.setRequested(integer, true);
        }

        mAdapter.notifyDataSetChanged();
        RequestFragment.sSelectedRequests = null;
    }

    @SuppressLint("StaticFieldLeak")
    private class MissingAppsLoader extends AsyncTask<Void, Void, Boolean> {

        private List<Request> requests;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (CandyBarMainActivity.sMissedApps == null) {
                mProgress.setVisibility(View.VISIBLE);
            }
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        protected Boolean doInBackground(Void... voids) {
            if (!isCancelled()) {
                try {
                    Thread.sleep(1);
                    if (CandyBarMainActivity.sMissedApps == null) {
                        CandyBarMainActivity.sMissedApps = RequestHelper.getMissingApps(getActivity());
                    }

                    requests = CandyBarMainActivity.sMissedApps;
                    return true;
                } catch (Exception e) {
                    LogUtil.e(Log.getStackTraceString(e));
                    return false;
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (getActivity() == null) return;
            if (getActivity().isFinishing()) return;

            mAsyncTask = null;
            mProgress.setVisibility(View.GONE);
            if (aBoolean) {
                setHasOptionsMenu(true);
                mAdapter = new RequestAdapter(getActivity(),
                        requests, mManager.getSpanCount());
                mRecyclerView.setAdapter(mAdapter);

                AnimationHelper.show(mFab)
                        .interpolator(new LinearOutSlowInInterpolator())
                        .start();

                if (getActivity().getResources().getBoolean(R.bool.show_intro)) {
                    TapIntroHelper.showRequestIntro(getActivity(), mRecyclerView);
                }
            } else {
                mRecyclerView.setAdapter(null);
                Toast.makeText(getActivity(), R.string.request_appfilter_failed, Toast.LENGTH_LONG).show();
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class RequestLoader extends AsyncTask<Void, Void, Boolean> {

        private MaterialDialog dialog;
        private boolean isArctic;
        private String arcticApiKey;
        private String errorMessage;

        @Override
        @SuppressWarnings("ConstantConditions")
        protected void onPreExecute() {
            super.onPreExecute();

            if (Preferences.get(getActivity()).isPremiumRequest()) {
                isArctic = RequestHelper.isPremiumArcticEnabled(getActivity());
                arcticApiKey = RequestHelper.getPremiumArcticApiKey(getActivity());
            } else {
                isArctic = RequestHelper.isRegularArcticEnabled(getActivity());
                arcticApiKey = RequestHelper.getRegularArcticApiKey(getActivity());
            }

            dialog = new MaterialDialog.Builder(getActivity())
                    .typeface(
                            TypefaceHelper.getMedium(getActivity()),
                            TypefaceHelper.getRegular(getActivity()))
                    .content(R.string.request_building)
                    .cancelable(false)
                    .canceledOnTouchOutside(false)
                    .progress(true, 0)
                    .progressIndeterminateStyle(true)
                    .build();

            dialog.show();
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        protected Boolean doInBackground(Void... voids) {
            if (!isCancelled()) {
                try {
                    Thread.sleep(2);

                    RequestFragment.sSelectedRequests = mAdapter.getSelectedItems();
                    List<Request> requests = mAdapter.getSelectedApps();

                    File directory = getActivity().getCacheDir();
                    List<String> files = new ArrayList<>();

                    for (Request request : requests) {
                        Drawable drawable = getReqIcon(getActivity(), request.getActivity());
                        String icon = IconsHelper.saveIcon(files, directory, drawable,
                                isArctic ? request.getPackageName() : RequestHelper.fixNameForRequest(request.getName()));
                        if (icon != null) files.add(icon);
                    }

                    if (isArctic) {
                        errorMessage = RequestHelper.sendArcticRequest(requests, files, directory, arcticApiKey);
                        return errorMessage == null;
                    } else {
                        boolean nonMailingAppSend = getResources().getBoolean(R.bool.enable_non_mail_app_request);
                        Intent intent;

                        if (!nonMailingAppSend) {
                            intent = new Intent(Intent.ACTION_SENDTO);
                            intent.setData(Uri.parse("mailto:"));
                        } else {
                            intent = new Intent(Intent.ACTION_SEND);
                            intent.setType("application/zip");
                        }

                        List<ResolveInfo> resolveInfos = getActivity().getPackageManager()
                                .queryIntentActivities(intent, 0);
                        if (resolveInfos.size() == 0) {
                            noEmailClientError = true;
                            return false;
                        }

                        if (Preferences.get(getActivity()).isPremiumRequest()) {
                            TransactionDetails details = InAppBillingProcessor.get(getActivity())
                                    .getProcessor().getPurchaseTransactionDetails(
                                            Preferences.get(getActivity()).getPremiumRequestProductId());
                            if (details == null) return false;

                            CandyBarApplication.sRequestProperty = new Request.Property(null,
                                    details.purchaseInfo.purchaseData.orderId,
                                    details.purchaseInfo.purchaseData.productId);
                        }

                        File appFilter = RequestHelper.buildXml(getActivity(), requests, RequestHelper.XmlType.APPFILTER);
                        File appMap = RequestHelper.buildXml(getActivity(), requests, RequestHelper.XmlType.APPMAP);
                        File themeResources = RequestHelper.buildXml(getActivity(), requests, RequestHelper.XmlType.THEME_RESOURCES);

                        if (appFilter != null) files.add(appFilter.toString());

                        if (appMap != null) files.add(appMap.toString());

                        if (themeResources != null) files.add(themeResources.toString());

                        CandyBarApplication.sZipPath = FileHelper.createZip(files, new File(directory.toString(),
                                RequestHelper.getGeneratedZipName(RequestHelper.ZIP)));
                    }
                    return true;
                } catch (RuntimeException | InterruptedException e) {
                    LogUtil.e(Log.getStackTraceString(e));
                    return false;
                }
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            if (getActivity() == null) return;
            if (getActivity().isFinishing()) return;

            dialog.dismiss();
            mAsyncTask = null;
            dialog = null;

            if (aBoolean) {
                if (isArctic) {
                    Toast.makeText(getActivity(), R.string.request_arctic_success, Toast.LENGTH_LONG).show();
                    ((RequestListener) getActivity()).onRequestBuilt(null, IntentChooserFragment.ICON_REQUEST);
                } else {
                    IntentChooserFragment.showIntentChooserDialog(getActivity().getSupportFragmentManager(),
                            IntentChooserFragment.ICON_REQUEST);
                }
                mAdapter.resetSelectedItems();
                if (mMenuItem != null) mMenuItem.setIcon(R.drawable.ic_toolbar_select_all);
            } else {
                if (isArctic) {
                    new MaterialDialog.Builder(getActivity())
                            .typeface(
                                    TypefaceHelper.getMedium(getActivity()),
                                    TypefaceHelper.getRegular(getActivity()))
                            .content(R.string.request_arctic_error, "\"" + errorMessage + "\"")
                            .cancelable(true)
                            .canceledOnTouchOutside(false)
                            .positiveText(R.string.close)
                            .build()
                            .show();
                } else if (noEmailClientError) {
                    Toast.makeText(getActivity(), R.string.no_email_app,
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), R.string.request_build_failed,
                            Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    public class CheckConfig extends AsyncTask<Void, Void, Boolean> {

        private MaterialDialog dialog;
        private boolean canRequest = true;
        private String updateUrl;

        @Override
        @SuppressWarnings("ConstantConditions")
        protected void onPreExecute() {
            super.onPreExecute();

            dialog = new MaterialDialog.Builder(getActivity())
                    .typeface(
                            TypefaceHelper.getMedium(getActivity()),
                            TypefaceHelper.getRegular(getActivity()))
                    .content(R.string.request_fetching_data)
                    .cancelable(false)
                    .canceledOnTouchOutside(false)
                    .progress(true, 0)
                    .progressIndeterminateStyle(true)
                    .build();

            dialog.show();
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        protected Boolean doInBackground(Void... params) {
            String configJsonUrl = getActivity().getResources().getString(R.string.config_json);
            URLConnection urlConnection;
            BufferedReader bufferedReader = null;

            try {
                urlConnection = new URL(configJsonUrl).openConnection();
                bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                String line;
                StringBuilder stringBuilder = new StringBuilder();
                while ((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line);
                }

                JSONObject configJson = new JSONObject(stringBuilder.toString());
                updateUrl = configJson.getString("url");

                JSONObject disableRequestObj = configJson.getJSONObject("disableRequest");
                long disableRequestBelow = disableRequestObj.optLong("below", 0);
                String disableRequestOn = disableRequestObj.optString("on", "");
                PackageInfo packageInfo = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0);
                long appVersionCode = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P ? packageInfo.getLongVersionCode() : packageInfo.versionCode;

                if ((appVersionCode < disableRequestBelow) ||
                        disableRequestOn.matches(".*\\b" + appVersionCode + "\\b.*")) {
                    canRequest = false;
                }

                return true;
            } catch (Exception ex) {
                LogUtil.e("Error loading Configuration JSON " + Log.getStackTraceString(ex));
            } finally {
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e) {
                        LogUtil.e(Log.getStackTraceString(e));
                    }
                }
            }

            return false;
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        protected void onPostExecute(Boolean aBoolean) {
            dialog.dismiss();
            dialog = null;

            if (aBoolean) {
                if (!canRequest) {
                    new MaterialDialog.Builder(getActivity())
                            .typeface(
                                    TypefaceHelper.getMedium(getActivity()),
                                    TypefaceHelper.getRegular(getActivity()))
                            .content(R.string.request_app_disabled)
                            .negativeText(R.string.close)
                            .positiveText(R.string.update)
                            .onPositive(((dialog, which) -> {
                                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(updateUrl));
                                intent.addFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
                                getActivity().startActivity(intent);
                            }))
                            .cancelable(false)
                            .canceledOnTouchOutside(false)
                            .build()
                            .show();

                    mAdapter.resetSelectedItems();
                    if (mMenuItem != null) mMenuItem.setIcon(R.drawable.ic_toolbar_select_all);
                } else {
                    mAsyncTask = new RequestLoader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
                }
            } else {
                new MaterialDialog.Builder(getActivity())
                        .typeface(
                                TypefaceHelper.getMedium(getActivity()),
                                TypefaceHelper.getRegular(getActivity()))
                        .content(R.string.unable_to_load_config)
                        .canceledOnTouchOutside(false)
                        .positiveText(R.string.close)
                        .build()
                        .show();
            }
        }
    }
}
