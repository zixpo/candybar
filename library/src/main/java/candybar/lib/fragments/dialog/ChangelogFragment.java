package candybar.lib.fragments.dialog;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.afollestad.materialdialogs.MaterialDialog;

import candybar.lib.R;
import candybar.lib.adapters.dialog.ChangelogAdapter;
import candybar.lib.helpers.TypefaceHelper;
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

public class ChangelogFragment extends DialogFragment {

    private static final String TAG = "candybar.dialog.changelog";
    private final Runnable onPositive;

    private static ChangelogFragment newInstance(Runnable onPositive) {
        return new ChangelogFragment(onPositive);
    }

    ChangelogFragment(Runnable onPositive) {
        super();
        this.onPositive = onPositive;
    }

    public static void showChangelog(FragmentManager fm, Runnable onPositive) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(TAG);
        if (prev != null) {
            ft.remove(prev);
        }

        try {
            DialogFragment dialog = ChangelogFragment.newInstance(onPositive);
            dialog.show(ft, TAG);
        } catch (IllegalArgumentException | IllegalStateException ignored) {
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog dialog = new MaterialDialog.Builder(requireActivity())
                .typeface(TypefaceHelper.getMedium(requireActivity()), TypefaceHelper.getRegular(requireActivity()))
                .customView(R.layout.fragment_changelog, false)
                .positiveText(R.string.close)
                .onPositive((d, which) -> this.onPositive.run())
                .build();
        dialog.show();

        ListView changelogList = (ListView) dialog.findViewById(R.id.changelog_list);
        TextView changelogDate = (TextView) dialog.findViewById(R.id.changelog_date);
        TextView changelogVersion = (TextView) dialog.findViewById(R.id.changelog_version);

        Activity activity = requireActivity();
        try {
            String version = activity.getPackageManager().getPackageInfo(
                    activity.getPackageName(), 0).versionName;
            if (version != null && !version.isEmpty()) {
                changelogVersion.setText(activity.getResources().getString(
                        R.string.changelog_version));
                changelogVersion.append(" " + version);
            }
        } catch (Exception ignored) {
        }

        String date = activity.getResources().getString(R.string.changelog_date);
        if (!date.isEmpty()) changelogDate.setText(date);
        else changelogDate.setVisibility(View.GONE);

        String[] changelog = activity.getResources().getStringArray(R.array.changelog);
        changelogList.setAdapter(new ChangelogAdapter(requireActivity(), changelog));

        return dialog;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        super.onDismiss(dialog);

        FragmentManager fm = requireActivity().getSupportFragmentManager();
        if (fm != null) {
            Fragment fragment = fm.findFragmentByTag("home");
            if (fragment != null) {
                HomeListener listener = (HomeListener) fragment;
                listener.onHomeIntroInit();
            }
        }
    }
}
