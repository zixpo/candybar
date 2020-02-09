package candybar.lib.fragments.dialog;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.drawable.AdaptiveIconDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.afollestad.materialdialogs.MaterialDialog;
import com.bumptech.glide.Glide;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.io.ByteArrayOutputStream;

import candybar.lib.R;
import candybar.lib.helpers.IconsHelper;
import candybar.lib.helpers.TypefaceHelper;
import candybar.lib.preferences.Preferences;
import candybar.lib.utils.ImageConfig;
import sarsamurmu.adaptiveicon.AdaptiveIcon;

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

public class IconPreviewFragment extends DialogFragment {

    private TextView mName;
    private ImageView mIcon;

    private String mIconName;
    private int mIconId;

    private static final String NAME = "name";
    private static final String ID = "id";

    private static final String TAG = "candybar.dialog.icon.preview";

    private static IconPreviewFragment newInstance(String name, int id) {
        IconPreviewFragment fragment = new IconPreviewFragment();
        Bundle bundle = new Bundle();
        bundle.putString(NAME, name);
        bundle.putInt(ID, id);
        fragment.setArguments(bundle);
        return fragment;
    }

    public static void showIconPreview(@NonNull FragmentManager fm, @NonNull String name, int id) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(TAG);
        if (prev != null) {
            ft.remove(prev);
        }

        try {
            DialogFragment dialog = IconPreviewFragment.newInstance(name, id);
            dialog.show(ft, TAG);
        } catch (IllegalArgumentException | IllegalStateException ignored) {
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIconName = getArguments().getString(NAME);
        mIconId = getArguments().getInt(ID);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog dialog = new MaterialDialog.Builder(getActivity())
                .customView(R.layout.fragment_icon_preview, false)
                .typeface(
                        TypefaceHelper.getMedium(getActivity()),
                        TypefaceHelper.getRegular(getActivity()))
                .positiveText(R.string.close)
                .build();

        dialog.show();

        mName = (TextView) dialog.findViewById(R.id.name);
        mIcon = (ImageView) dialog.findViewById(R.id.icon);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null) {
            mIconName = savedInstanceState.getString(NAME);
            mIconId = savedInstanceState.getInt(ID);
        }

        if (!getActivity().getResources().getBoolean(R.bool.show_icon_name)) {
            boolean iconNameReplacer = getActivity().getResources().getBoolean(
                    R.bool.enable_icon_name_replacer);
            mIconName = IconsHelper.replaceName(getActivity(), iconNameReplacer, mIconName);
        }

        mName.setText(mIconName);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Drawable drawable = getActivity().getDrawable(mIconId);

            if (drawable instanceof AdaptiveIconDrawable) {
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                new AdaptiveIcon()
                        .setDrawable((AdaptiveIconDrawable) drawable)
                        .setPath(Preferences.get(getActivity()).getIconShape())
                        .setSize(272)
                        .render()
                        .compress(Bitmap.CompressFormat.PNG, 100, stream);

                Glide.with(this)
                        .load(stream.toByteArray())
                        .into(mIcon);
                return;
            }
        }

        ImageLoader.getInstance().displayImage("drawable://" + mIconId, mIcon,
                ImageConfig.getDefaultImageOptions(false));
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(NAME, mIconName);
        outState.putInt(ID, mIconId);
        super.onSaveInstanceState(outState);
    }

}
