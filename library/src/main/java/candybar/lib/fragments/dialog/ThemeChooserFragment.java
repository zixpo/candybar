package candybar.lib.fragments.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import androidx.appcompat.app.AlertDialog;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import android.view.LayoutInflater;
import android.view.View;

import java.util.Arrays;

import candybar.lib.R;
import candybar.lib.adapters.dialog.ThemeAdapter;
import candybar.lib.items.Theme;
import candybar.lib.preferences.Preferences;

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

public class ThemeChooserFragment extends DialogFragment {

    private Theme mChosenTheme;
    private Theme mCurrentTheme;

    public static final String TAG = "candybar.dialog.theme";

    private static ThemeChooserFragment newInstance() {
        return new ThemeChooserFragment();
    }

    public static void showThemeChooser(@NonNull FragmentManager fm) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(TAG);
        if (prev != null) {
            ft.remove(prev);
        }

        try {
            DialogFragment dialog = ThemeChooserFragment.newInstance();
            dialog.show(ft, TAG);
        } catch (IllegalArgumentException | IllegalStateException ignored) {
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = LayoutInflater.from(requireActivity()).inflate(R.layout.fragment_languages, null);
        AlertDialog dialog = new MaterialAlertDialogBuilder(requireActivity())
                .setTitle(R.string.pref_theme_header)
                .setView(view)
                .setNegativeButton(R.string.close, null)
                .create();

        ListView listView = view.findViewById(R.id.listview);
        mChosenTheme = mCurrentTheme = Preferences.get(requireActivity()).getTheme();

        listView.setAdapter(new ThemeAdapter(requireActivity(), Arrays.asList(Theme.values()), mCurrentTheme.ordinal()));

        return dialog;
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        if (mChosenTheme != mCurrentTheme) {
            Preferences.get(requireActivity()).setTheme(mChosenTheme);
            requireActivity().recreate();
        }
        super.onDismiss(dialog);
    }

    public void setTheme(Theme theme) {
        mChosenTheme = theme;
        dismiss();
    }
}
