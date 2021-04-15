package candybar.lib.fragments.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.afollestad.materialdialogs.MaterialDialog;
import com.danimahardhika.android.helpers.core.utils.LogUtil;

import java.util.List;
import java.util.Locale;

import candybar.lib.R;
import candybar.lib.adapters.dialog.LanguagesAdapter;
import candybar.lib.helpers.LocaleHelper;
import candybar.lib.helpers.TypefaceHelper;
import candybar.lib.items.Language;
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

public class LanguagesFragment extends DialogFragment {

    private ListView mListView;
    private Locale mLocale;
    private AsyncTask<Void, Void, ?> mAsyncTask;

    public static final String TAG = "candybar.dialog.languages";

    private static LanguagesFragment newInstance() {
        return new LanguagesFragment();
    }

    public static void showLanguageChooser(@NonNull FragmentManager fm) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(TAG);
        if (prev != null) {
            ft.remove(prev);
        }

        try {
            DialogFragment dialog = LanguagesFragment.newInstance();
            dialog.show(ft, TAG);
        } catch (IllegalArgumentException | IllegalStateException ignored) {
        }
    }

    @NonNull
    @Override
    @SuppressWarnings("ConstantConditions")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        builder.customView(R.layout.fragment_languages, false);
        builder.typeface(TypefaceHelper.getMedium(getActivity()), TypefaceHelper.getRegular(getActivity()));
        builder.title(R.string.pref_language_header);
        builder.negativeText(R.string.close);
        MaterialDialog dialog = builder.build();
        dialog.show();

        mListView = (ListView) dialog.findViewById(R.id.listview);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAsyncTask = new LanguagesLoader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onDestroy() {
        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
        }
        super.onDestroy();
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onDismiss(@NonNull DialogInterface dialog) {
        if (mLocale != null) {
            Preferences.get(getActivity()).setCurrentLocale(mLocale.toString());
            LocaleHelper.setLocale(getActivity());
            getActivity().recreate();
        }
        super.onDismiss(dialog);
    }

    public void setLanguage(@NonNull Locale locale) {
        mLocale = locale;
        dismiss();
    }

    @SuppressLint("StaticFieldLeak")
    private class LanguagesLoader extends AsyncTask<Void, Void, Boolean> {

        private List<Language> languages;
        private int index = 0;

        @Override
        @SuppressWarnings("ConstantConditions")
        protected Boolean doInBackground(Void... voids) {
            if (!isCancelled()) {
                try {
                    Thread.sleep(1);
                    languages = LocaleHelper.getAvailableLanguages(getActivity());
                    Locale locale = Preferences.get(getActivity()).getCurrentLocale();
                    for (int i = 0; i < languages.size(); i++) {
                        Locale l = languages.get(i).getLocale();
                        if (l.toString().equals(locale.toString())) {
                            index = i;
                            break;
                        }
                    }
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
                mListView.setAdapter(new LanguagesAdapter(getActivity(), languages, index));
            } else {
                dismiss();
            }
        }
    }
}
