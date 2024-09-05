package candybar.lib.adapters;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Context;
import android.util.TypedValue;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.danimahardhika.android.helpers.core.ColorHelper;
import com.danimahardhika.android.helpers.core.DrawableHelper;
import com.danimahardhika.android.helpers.core.SoftKeyboardHelper;
import com.google.android.material.tabs.TabLayout;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import candybar.lib.R;
import candybar.lib.applications.CandyBarApplication;
import candybar.lib.databases.Database;
import candybar.lib.fragments.IconsFragment;
import candybar.lib.helpers.IconsHelper;
import candybar.lib.helpers.IntentHelper;
import candybar.lib.items.Icon;

import android.content.res.Resources;
import android.content.pm.PackageManager;
import android.net.Uri;

import android.util.Log;


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

public class IconsAdapter extends RecyclerView.Adapter<IconsAdapter.ViewHolder> {

    private final Context mContext;
    private List<Icon> mIcons;
    private List<Icon> mIconsAll;
    private final Fragment mFragment;
    private WeakReference<RecyclerView> mRecyclerView = null;

    private List<Icon> mSelectedIcons = new ArrayList<>();

    private int visibleStart;
    private int visibleEnd;

    private final boolean mIsShowIconName;
    private final boolean mIsBookmarkMode;

    // NOTE: In bookmark mode we don't need to optimize things a lot
    // It's not like the users are going to bookmark a lot
    private ActionMode actionMode;
    private final ActionMode.Callback actionModeCallback = new ActionMode.Callback() {
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            actionMode = mode;
            mode.getMenuInflater().inflate(R.menu.menu_bookmark_icons, menu);
            Activity activity = (Activity) mContext;
            TabLayout tabLayout = activity.findViewById(R.id.tab);
            View shadow = activity.findViewById(R.id.shadow);
            if (shadow != null) {
                shadow.animate().translationY(-tabLayout.getHeight()).setDuration(200).start();
            }
            tabLayout.animate().translationY(-tabLayout.getHeight()).setDuration(200)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            super.onAnimationEnd(animation);
                            tabLayout.setVisibility(View.GONE);
                            if (shadow != null) {
                                shadow.setTranslationY(0);
                            }
                            tabLayout.animate().setListener(null);
                        }
                    }).start();
            ((ViewPager2) activity.findViewById(R.id.pager)).setUserInputEnabled(false);
            ((DrawerLayout) activity.findViewById(R.id.drawer_layout))
                    .setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            for (int i = 0; i < mIcons.size(); i++) {
                ViewHolder holder = getViewHolderAt(i);
                if (holder != null) holder.onActionModeChange();
            }
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            mode.setTitle(mContext.getResources().getString(R.string.items_selected, mSelectedIcons.size()));
            menu.findItem(R.id.menu_select_all).setIcon(mSelectedIcons.size() == mIcons.size()
                    ? R.drawable.ic_toolbar_select_all_selected : R.drawable.ic_toolbar_select_all);
            menu.findItem(R.id.menu_delete).setVisible(mSelectedIcons.size() > 0);
            return true;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem menuItem) {
            int itemId = menuItem.getItemId();
            if (itemId == R.id.menu_delete) {
                List<String> drawableNames = new ArrayList<>();
                for (Icon icon : mSelectedIcons) drawableNames.add(icon.getDrawableName());
                Database.get(mContext).deleteBookmarkedIcons(drawableNames);
                IconsFragment.reloadBookmarks();
                mode.finish();
                return true;
            } else if (itemId == R.id.menu_select_all) {
                if (mSelectedIcons.size() != mIcons.size()) {
                    for (int i = 0; i < mIcons.size(); i++) {
                        ViewHolder holder = getViewHolderAt(i);
                        if (holder != null) holder.setChecked(true, true);
                    }
                    mSelectedIcons = new ArrayList<>(mIcons);
                } else {
                    for (int i = 0; i < mIcons.size(); i++) {
                        ViewHolder holder = getViewHolderAt(i);
                        if (holder != null) holder.setChecked(false, true);
                    }
                    mSelectedIcons = new ArrayList<>();
                }
                actionMode.invalidate();
            }
            return false;
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            actionMode = null;
            mSelectedIcons = new ArrayList<>();
            Activity activity = (Activity) mContext;
            TabLayout tabLayout = activity.findViewById(R.id.tab);
            View shadow = activity.findViewById(R.id.shadow);
            if (shadow != null) {
                shadow.setTranslationY(-tabLayout.getHeight());
                shadow.animate().translationY(0).setDuration(200).start();
            }
            tabLayout.setVisibility(View.VISIBLE);
            tabLayout.animate().translationY(0).setDuration(200).start();
            ((ViewPager2) activity.findViewById(R.id.pager)).setUserInputEnabled(true);
            ((DrawerLayout) activity.findViewById(R.id.drawer_layout))
                    .setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
            for (int i = 0; i < mIcons.size(); i++) {
                ViewHolder holder = getViewHolderAt(i);
                if (holder != null) holder.onActionModeChange();
            }
        }
    };

    public IconsAdapter(@NonNull Context context, @NonNull List<Icon> icons, Fragment fragment, boolean isBookmarkMode) {
        mContext = context;
        mFragment = fragment;
        mIcons = icons;
        mIsShowIconName = mContext.getResources().getBoolean(R.bool.show_icon_name);
        mIsBookmarkMode = isBookmarkMode;
    }

    public void setIcons(@NonNull List<Icon> icons) {
        mIcons = icons;
        notifyDataSetChanged();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = new WeakReference<>(recyclerView);
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if (manager instanceof GridLayoutManager && getItemCount() > 0) {
            GridLayoutManager glm = (GridLayoutManager) manager;
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    visibleStart = glm.findFirstVisibleItemPosition();
                    visibleEnd = glm.findLastVisibleItemPosition();
                    // LogUtil.d(String.format(Locale.ENGLISH, "[Start, End]: [%d, %d]", visibleStart, visibleEnd));
                }
            });
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(
                R.layout.fragment_icons_item_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Icon icon = mIcons.get(position);
        holder.name.setText(icon.getTitle());
        loadIconInto(holder.icon, position);
        if (mIsBookmarkMode) {
            holder.setCheckChangedListener(null);
            holder.setChecked(mSelectedIcons.contains(icon), false);
            holder.setCheckChangedListener(isChecked -> {
                if (isChecked) {
                    mSelectedIcons.add(icon);
                } else {
                    mSelectedIcons.remove(icon);
                }
                if (actionMode != null) actionMode.invalidate();
            });
        }
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        Glide.with(mFragment).clear(holder.icon);
        super.onViewRecycled(holder);
    }

    @Override
    public int getItemCount() {
        return mIcons.size();
    }

    private ViewHolder getViewHolderAt(int position) {
        return (ViewHolder) mRecyclerView.get().findViewHolderForAdapterPosition(position);
    }

    private void loadIconInto(ImageView imageView, int position) {
        if (mFragment.getActivity() == null) return;

        int resId = mIcons.get(position).getRes();
        String packageName = mIcons.get(position).getPackageName();

        String glideLoadUrl = "android.resource://" + packageName + "/" + resId;

        // Load the drawable using Glide
        Glide.with(mFragment)
                .load(Uri.parse(glideLoadUrl))
                .skipMemoryCache(true)
                .transition(DrawableTransitionOptions.withCrossFade(300))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(imageView);
    }

    public void reloadIcons() {
        for (int i = visibleStart; i <= visibleEnd; i++) {
            ViewHolder holder = getViewHolderAt(i);
            if (holder != null) loadIconInto(holder.icon, i);
        }
    }

    private interface CheckChangedListener {
        void onCheckChanged(boolean isChecked);
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {
        private final ImageView icon;
        private final TextView name;

        private final View container;
        private final View innerContainer;
        private final View checkBackground;
        private boolean isChecked;
        private CheckChangedListener checkChangedListener;

        ViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            name = itemView.findViewById(R.id.name);
            innerContainer = itemView.findViewById(R.id.inner_container);
            checkBackground = itemView.findViewById(R.id.check_background);

            container = itemView.findViewById(R.id.container);
            container.setOnClickListener(this);

            if (mIsBookmarkMode) {
                container.setOnLongClickListener(this);
                int color = ColorHelper.getAttributeColor(mContext, com.google.android.material.R.attr.colorSecondary);
                ((ImageView) checkBackground.findViewById(R.id.checkmark))
                        .setImageDrawable(DrawableHelper.getTintedDrawable(mContext, R.drawable.ic_check_circle, color));
            }

            if (!mIsShowIconName) {
                name.setVisibility(View.GONE);
            }

            onActionModeChange();
        }

        private void onActionModeChange() {
            TypedValue outValue = new TypedValue();
            if (actionMode != null) {
                mContext.getTheme().resolveAttribute(androidx.appcompat.R.attr.selectableItemBackground, outValue, true);
                container.setBackgroundResource(outValue.resourceId);
                innerContainer.setBackgroundResource(0);
            } else {
                mContext.getTheme().resolveAttribute(androidx.appcompat.R.attr.selectableItemBackgroundBorderless, outValue, true);
                container.setBackgroundResource(0);
                innerContainer.setBackgroundResource(outValue.resourceId);
                setChecked(false, true);
            }
        }

        private void setCheckChangedListener(CheckChangedListener checkChangedListener) {
            this.checkChangedListener = checkChangedListener;
        }

        private void setChecked(boolean isChecked, boolean animate) {
            this.isChecked = isChecked;
            float scale = isChecked ? (float) 0.6 : 1;
            if (animate) {
                checkBackground.animate().alpha(isChecked ? 1 : 0).setDuration(200).start();
                icon.animate().scaleX(scale).scaleY(scale).setDuration(200).start();
            } else {
                checkBackground.setAlpha(isChecked ? 1 : 0);
                icon.setScaleX(scale);
                icon.setScaleY(scale);
            }
            if (checkChangedListener != null) {
                checkChangedListener.onCheckChanged(isChecked);
            }
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            int position = getBindingAdapterPosition();
            if (id == R.id.container) {
                if (position < 0 || position > mIcons.size()) return;
                if (actionMode != null) {
                    setChecked(!isChecked, true);
                } else {
                    SoftKeyboardHelper.closeKeyboard(mContext);
                    IconsHelper.selectIcon(mContext, IntentHelper.sAction, mIcons.get(position));
                }
            }
        }

        @Override
        public boolean onLongClick(View view) {
            if (actionMode == null) {
                ((Activity) mContext).startActionMode(actionModeCallback);
            }
            setChecked(!isChecked, true);
            return true;
        }
    }

    public void search(String string) {
        if (mIconsAll == null) {
            // For searching, mIcons
            //      - Contains all icons
            //      - Icons are sorted
            // Check IconSearchFragment.java line 205-275
            // We don't need to do any sorting here

            // Copy icons to `mIconsAll`
            mIconsAll = mIcons;
        }

        String query = string.toLowerCase(Locale.ENGLISH).trim();

        mIcons = new ArrayList<>();
        if (query.length() == 0) mIcons.addAll(mIconsAll);
        else {
            for (int i = 0; i < mIconsAll.size(); i++) {
                Icon icon = mIconsAll.get(i);
                String name = icon.getTitle();
                name = name.toLowerCase(Locale.ENGLISH);
                if (name.contains(query)) {
                    mIcons.add(icon);
                }
            }
        }

        if (mIcons.size() == 0) {
            CandyBarApplication.getConfiguration().getAnalyticsHandler().logEvent(
                    "click",
                    new HashMap<String, Object>() {{
                        put("section", "icons");
                        put("action", "search");
                        put("item", query);
                        put("found", "no");
                        put("number_of_icons", mIcons.size());
                    }}
            );
        } else {
            CandyBarApplication.getConfiguration().getAnalyticsHandler().logEvent(
                    "click",
                    new HashMap<String, Object>() {{
                        put("section", "icons");
                        put("action", "search");
                        put("item", query);
                        put("found", "yes");
                        put("number_of_icons", mIcons.size());
                    }}
            );
        }

        notifyDataSetChanged();
    }
}
