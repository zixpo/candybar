package candybar.lib.fragments;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.SoftKeyboardHelper;
import com.danimahardhika.android.helpers.core.ViewHelper;
import com.danimahardhika.android.helpers.core.utils.LogUtil;
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import candybar.lib.R;
import candybar.lib.activities.CandyBarMainActivity;
import candybar.lib.adapters.IconsAdapter;
import candybar.lib.applications.CandyBarApplication;
import candybar.lib.helpers.IconsHelper;
import candybar.lib.items.Icon;
import candybar.lib.utils.AlphanumComparator;

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

public class IconsSearchFragment extends Fragment {

    private RecyclerView mRecyclerView;
    private RecyclerFastScroller mFastScroll;
    private TextView mSearchResult;
    private SearchView mSearchView;
    private final Fragment mFragment = this;

    private IconsAdapter mAdapter;
    private AsyncTask<Void, Void, ?> mAsyncTask;

    public static final String TAG = "icons_search";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_icons_search, container, false);
        mRecyclerView = view.findViewById(R.id.icons_grid);
        mFastScroll = view.findViewById(R.id.fastscroll);
        mSearchResult = view.findViewById(R.id.search_result);
        return view;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setHasOptionsMenu(true);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),
                getActivity().getResources().getInteger(R.integer.icons_column_count)));

        setFastScrollColor(mFastScroll);
        mFastScroll.attachRecyclerView(mRecyclerView);

        mAsyncTask = new IconsLoader().execute();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_icons_search, menu);
        MenuItem search = menu.findItem(R.id.menu_search);
        MenuItem iconShape = menu.findItem(R.id.menu_icon_shape);

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
                !getActivity().getResources().getBoolean(R.bool.includes_adaptive_icons)) {
            iconShape.setVisible(false);
        }

        mSearchView = (SearchView) search.getActionView();
        mSearchView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI | EditorInfo.IME_ACTION_SEARCH);
        mSearchView.setQueryHint(getActivity().getResources().getString(R.string.search_icon));
        mSearchView.setMaxWidth(Integer.MAX_VALUE);

        search.expandActionView();
        mSearchView.setIconifiedByDefault(false);
        mSearchView.clearFocus();

        int color = ColorHelper.getAttributeColor(getActivity(), R.attr.toolbar_icon);
        ViewHelper.setSearchViewTextColor(mSearchView, color);
        ViewHelper.setSearchViewBackgroundColor(mSearchView, Color.TRANSPARENT);
        ViewHelper.setSearchViewCloseIcon(mSearchView, R.drawable.ic_toolbar_close);
        ViewHelper.setSearchViewSearchIcon(mSearchView, null);

        search.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                getActivity().onBackPressed();
                return true;
            }
        });

        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String string) {
                filterSearch(string);
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String string) {
                mSearchView.clearFocus();
                return true;
            }
        });
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ViewHelper.resetSpanCount(mRecyclerView, getActivity().getResources().getInteger(R.integer.icons_column_count));
    }

    @Override
    public void onDestroy() {
        if (mAsyncTask != null) mAsyncTask.cancel(true);
        super.onDestroy();
    }

    @SuppressLint("StringFormatInvalid")
    @SuppressWarnings("ConstantConditions")
    private void filterSearch(String query) {
        try {
            mAdapter.search(query);
            if (mAdapter.getItemCount() == 0) {
                String text = String.format(getActivity().getResources().getString(
                        R.string.search_noresult), query);
                mSearchResult.setText(text);
                mSearchResult.setVisibility(View.VISIBLE);
            } else mSearchResult.setVisibility(View.GONE);
        } catch (Exception e) {
            LogUtil.e(Log.getStackTraceString(e));
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class IconsLoader extends AsyncTask<Void, Void, Boolean> {

        private List<Icon> icons;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            icons = new ArrayList<>();
        }

        @Override
        @SuppressWarnings("ConstantConditions")
        protected Boolean doInBackground(Void... voids) {
            if (!isCancelled()) {
                try {
                    Thread.sleep(1);
                    if (CandyBarMainActivity.sSections == null) {
                        CandyBarMainActivity.sSections = IconsHelper.getIconsList(getActivity());

                        for (Icon section : CandyBarMainActivity.sSections) {
                            if (getActivity().getResources().getBoolean(R.bool.show_icon_name)) {
                                for (Icon icon : section.getIcons()) {
                                    String name;
                                    if ((icon.getCustomName() != null) && (!icon.getCustomName().contentEquals(""))) {
                                        name = icon.getCustomName();
                                    } else {
                                        name = IconsHelper.replaceName(getActivity(),
                                                getActivity().getResources().getBoolean(R.bool.enable_icon_name_replacer),
                                                icon.getTitle());
                                    }
                                    icon.setTitle(name);
                                }
                            }
                        }

                        if (CandyBarApplication.getConfiguration().isShowTabAllIcons()) {
                            List<Icon> icons = IconsHelper.getTabAllIcons();
                            CandyBarMainActivity.sSections.add(new Icon(
                                    CandyBarApplication.getConfiguration().getTabAllIconsTitle(), icons));
                        }
                    }

                    for (Icon icon : CandyBarMainActivity.sSections) {
                        if (CandyBarApplication.getConfiguration().isShowTabAllIcons()) {
                            if (!icon.getTitle().equals(CandyBarApplication.getConfiguration().getTabAllIconsTitle())) {
                                icons.addAll(icon.getIcons());
                            }
                        } else {
                            icons.addAll(icon.getIcons());
                        }
                    }

                    Collections.sort(icons, new AlphanumComparator() {
                        @Override
                        public int compare(Object o1, Object o2) {
                            String s1 = ((Icon) o1).getTitle();
                            String s2 = ((Icon) o2).getTitle();
                            return super.compare(s1, s2);
                        }
                    });
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
            if (aBoolean) {
                mAdapter = new IconsAdapter(getActivity(), icons, mFragment);
                mRecyclerView.setAdapter(mAdapter);
                filterSearch("");
                mSearchView.requestFocus();
                SoftKeyboardHelper.openKeyboard(getActivity());
            } else {
                // Unable to load all icons
                Toast.makeText(getActivity(), R.string.icons_load_failed,
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
