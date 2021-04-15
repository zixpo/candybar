package candybar.lib.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.danimahardhika.android.helpers.core.SoftKeyboardHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import candybar.lib.R;
import candybar.lib.helpers.IconsHelper;
import candybar.lib.helpers.IntentHelper;
import candybar.lib.items.Icon;

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
    private final List<Icon> mIcons;
    private List<Icon> mIconsAll;
    private final Fragment mFragment;
    private final List<ViewHolder> mViewHolders;

    private final boolean mIsShowIconName;

    public IconsAdapter(@NonNull Context context, @NonNull List<Icon> icons, Fragment fragment) {
        mContext = context;
        mFragment = fragment;
        mIcons = icons;
        mIsShowIconName = mContext.getResources().getBoolean(R.bool.show_icon_name);
        mViewHolders = new ArrayList<>();
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
        holder.name.setText(mIcons.get(position).getTitle());
        mViewHolders.add(holder);
        loadIconInto(holder.icon, position);
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        mViewHolders.remove(holder);
        super.onViewRecycled(holder);
    }

    private void loadIconInto(ImageView imageView, int position) {
        Glide.with(mFragment)
                .load("drawable://" + mIcons.get(position).getRes())
                .skipMemoryCache(true)
                .transition(DrawableTransitionOptions.withCrossFade(300))
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(imageView);
    }

    public void reloadIcons() {
        Glide.get(mContext).clearMemory();
        for (ViewHolder holder : mViewHolders) {
            loadIconInto(holder.icon, holder.getAdapterPosition());
        }
    }

    @Override
    public int getItemCount() {
        return mIcons.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private final ImageView icon;
        private final TextView name;

        ViewHolder(View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.icon);
            name = itemView.findViewById(R.id.name);
            LinearLayout container = itemView.findViewById(R.id.container);
            container.setOnClickListener(this);

            if (!mIsShowIconName) {
                name.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View view) {
            int id = view.getId();
            int position = getAdapterPosition();
            if (id == R.id.container) {
                if (position < 0 || position > mIcons.size()) return;
                SoftKeyboardHelper.closeKeyboard(mContext);
                IconsHelper.selectIcon(mContext, IntentHelper.sAction, mIcons.get(position));
            }
        }
    }

    public void search(String string) {
        // Initialize mIconsAll if not initialized
        // Also remove duplicates
        if (mIconsAll == null) {
            mIconsAll = new ArrayList<>();
            Set<String> addedNames = new HashSet<>();
            Locale defaultLocale = Locale.getDefault();
            for (int i = 0; i < mIcons.size(); i++) {
                Icon icon = mIcons.get(i);
                String name = icon.getTitle();
                if (icon.getCustomName() != null && !icon.getCustomName().contentEquals("")) {
                    name = icon.getCustomName();
                }
                name = name.toLowerCase(defaultLocale);
                if (!addedNames.contains(name)) {
                    mIconsAll.add(icon);
                    addedNames.add(name);
                }
            }
        }

        String query = string.toLowerCase(Locale.getDefault()).trim();
        mIcons.clear();
        if (query.length() == 0) mIcons.addAll(mIconsAll);
        else {
            Locale defaultLocale = Locale.getDefault();
            for (int i = 0; i < mIconsAll.size(); i++) {
                Icon icon = mIconsAll.get(i);
                String name = icon.getTitle();
                if (icon.getCustomName() != null && !icon.getCustomName().contentEquals("")) {
                    name = icon.getCustomName();
                }
                name = name.toLowerCase(defaultLocale);
                if (name.contains(query)) {
                    mIcons.add(icon);
                }
            }
        }
        notifyDataSetChanged();
    }
}
