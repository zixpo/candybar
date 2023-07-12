package candybar.lib.fragments;

import static candybar.lib.helpers.ViewHelper.setFastScrollColor;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.danimahardhika.android.helpers.core.SoftKeyboardHelper;
import com.danimahardhika.android.helpers.core.ViewHelper;
import com.danimahardhika.android.helpers.core.utils.LogUtil;
import com.pluscubed.recyclerfastscroll.RecyclerFastScroller;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import candybar.lib.R;
import candybar.lib.activities.CandyBarMainActivity;
import candybar.lib.adapters.IconsAdapter;
import candybar.lib.applications.CandyBarApplication;
import candybar.lib.fragments.dialog.IconShapeChooserFragment;
import candybar.lib.helpers.IconsHelper;
import candybar.lib.items.Icon;
import candybar.lib.utils.AlphanumComparator;
import candybar.lib.utils.AsyncTaskBase;
import candybar.lib.utils.listeners.SearchListener;

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
    private EditText mSearchInput;
    private final Fragment mFragment = this;

    private IconsAdapter mAdapter;
    private AsyncTaskBase mAsyncTask;

    public static final String TAG = "icons_search";

    private static WeakReference<IconsAdapter> currentAdapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_icons_search, container, false);
        mRecyclerView = view.findViewById(R.id.icons_grid);
        mFastScroll = view.findViewById(R.id.fastscroll);
        mSearchResult = view.findViewById(R.id.search_result);
//        Glide.get(getActivity()).setMemoryCategory(MemoryCategory.LOW);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        CandyBarApplication.getConfiguration().getAnalyticsHandler().logEvent(
                "view",
                new HashMap<String, Object>() {{ put("section", "icons_search"); }}
        );

        setHasOptionsMenu(true);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),
                requireActivity().getResources().getInteger(R.integer.icons_column_count)));

        setFastScrollColor(mFastScroll);
        mFastScroll.attachRecyclerView(mRecyclerView);
        mAsyncTask = new IconsLoader().execute();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_icons_search, menu);
        MenuItem search = menu.findItem(R.id.menu_search);
        MenuItem iconShape = menu.findItem(R.id.menu_icon_shape);
        View searchView = search.getActionView();

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O ||
                !requireActivity().getResources().getBoolean(R.bool.includes_adaptive_icons)) {
            iconShape.setVisible(false);
        } else {
            searchView.findViewById(R.id.container).setPadding(0, 0, 0, 0);
        }

        View clearQueryButton = searchView.findViewById(R.id.clear_query_button);
        mSearchInput = searchView.findViewById(R.id.search_input);
        mSearchInput.setHint(R.string.search_icon);

        search.expandActionView();

        search.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                requireActivity().getSupportFragmentManager().popBackStack();

                Activity activity = requireActivity();
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    ((SearchListener) activity).onSearchExpanded(false);
                }, 500);
                return true;
            }
        });

        mSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                String query = charSequence.toString();
                filterSearch(query);
                clearQueryButton.setVisibility(query.contentEquals("") ? View.GONE : View.VISIBLE);
            }
        });

        clearQueryButton.setOnClickListener(view -> mSearchInput.setText(""));

        iconShape.setOnMenuItemClickListener(menuItem -> {
            IconShapeChooserFragment.showIconShapeChooser(requireActivity().getSupportFragmentManager());
            return false;
        });
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        ViewHelper.resetSpanCount(mRecyclerView, requireActivity().getResources().getInteger(R.integer.icons_column_count));
    }

    @Override
    public void onDestroy() {
        if (mAsyncTask != null) mAsyncTask.cancel(true);
        currentAdapter = null;
        Activity activity = getActivity();
        if (activity != null) {
//            Glide.get(activity).setMemoryCategory(MemoryCategory.NORMAL);
            Glide.get(activity).clearMemory();
        }
        super.onDestroy();
    }

    @SuppressLint("StringFormatInvalid")
    private void filterSearch(String query) {
        try {
            mAdapter.search(query);
            if (mAdapter.getItemCount() == 0) {
                String text = requireActivity().getResources().getString(R.string.search_noresult, query);
                mSearchResult.setText(text);
                mSearchResult.setVisibility(View.VISIBLE);
            } else mSearchResult.setVisibility(View.GONE);
        } catch (Exception e) {
            LogUtil.e(Log.getStackTraceString(e));
        }
    }

    public static void reloadIcons() {
        if (currentAdapter != null && currentAdapter.get() != null)
            currentAdapter.get().reloadIcons();
    }

    private class IconsLoader extends AsyncTaskBase {

        private Set<Icon> iconSet;
        private List<Icon> iconList;

        @Override
        protected void preRun() {
            iconSet = new HashSet<>();
        }

        @Override
        protected boolean run() {
            if (!isCancelled()) {
                try {
                    Thread.sleep(1);

                    IconsHelper.loadIcons(requireActivity(), false);

                    for (Icon icon : CandyBarMainActivity.sSections) {
                        if (CandyBarApplication.getConfiguration().isShowTabAllIcons()) {
                            if (!icon.getTitle().equals(CandyBarApplication.getConfiguration().getTabAllIconsTitle())) {
                                iconSet.addAll(icon.getIcons());
                            }
                        } else {
                            iconSet.addAll(icon.getIcons());
                        }
                    }

                    iconList = new ArrayList<>(iconSet);

                    // Sort them in lowercase
                    Collections.sort(iconList, new AlphanumComparator() {
                        @Override
                        public int compare(Object o1, Object o2) {
                            String s1 = ((Icon) o1).getTitle().toLowerCase().trim();
                            String s2 = ((Icon) o2).getTitle().toLowerCase().trim();
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
        protected void postRun(boolean ok) {
            if (getActivity() == null) return;
            if (getActivity().isFinishing()) return;

            mAsyncTask = null;
            if (ok) {
                mAdapter = new IconsAdapter(getActivity(), iconList, mFragment, false);
                currentAdapter = new WeakReference<>(mAdapter);
                mRecyclerView.setAdapter(mAdapter);
                filterSearch("");
                mSearchInput.requestFocus();
                SoftKeyboardHelper.openKeyboard(getActivity());
            } else {
                // Unable to load all icons
                Toast.makeText(getActivity(), R.string.icons_load_failed,
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
