package candybar.lib.fragments.dialog;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.afollestad.materialdialogs.MaterialDialog;
import com.danimahardhika.android.helpers.core.utils.LogUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import candybar.lib.R;
import candybar.lib.helpers.LocaleHelper;
import candybar.lib.helpers.TypefaceHelper;

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

public class LicensesFragment extends DialogFragment {

    private WebView mWebView;
    private AsyncTask<Void, Void, ?> mAsyncTask;

    private static final String TAG = "candybar.dialog.licenses";

    private static LicensesFragment newInstance() {
        return new LicensesFragment();
    }

    public static void showLicensesDialog(FragmentManager fm) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(TAG);
        if (prev != null) {
            ft.remove(prev);
        }

        try {
            DialogFragment dialog = LicensesFragment.newInstance();
            dialog.show(ft, TAG);
        } catch (IllegalArgumentException | IllegalStateException ignored) {
        }
    }

    @NonNull
    @Override
    @SuppressWarnings("ConstantConditions")
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        builder.customView(R.layout.fragment_licenses, false);
        builder.typeface(
                TypefaceHelper.getMedium(getActivity()),
                TypefaceHelper.getRegular(getActivity()));
        builder.title(R.string.about_open_source_licenses);
        builder.negativeText(R.string.close);
        MaterialDialog dialog = builder.build();
        dialog.show();

        mWebView = (WebView) dialog.findViewById(R.id.webview);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAsyncTask = new LicensesLoader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onDismiss(@NonNull DialogInterface dialog) {
        if (mAsyncTask != null) mAsyncTask.cancel(true);
        super.onDismiss(dialog);
    }

    @SuppressLint("StaticFieldLeak")
    private class LicensesLoader extends AsyncTask<Void, Void, Boolean> {

        private StringBuilder sb;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            sb = new StringBuilder();
        }

        @Override
        protected Boolean doInBackground(Void... voids) {
            if (!isCancelled()) {
                try {
                    Thread.sleep(1);
                    InputStream rawResource = getResources().openRawResource(R.raw.licenses);
                    BufferedReader bufferedReader = new BufferedReader(
                            new InputStreamReader(rawResource));

                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        sb.append(line);
                        sb.append("\n");
                    }
                    bufferedReader.close();
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
            LocaleHelper.setLocale(getActivity());
            if (aBoolean) {
                mWebView.setVisibility(View.VISIBLE);
                mWebView.loadDataWithBaseURL(null,
                        sb.toString(), "text/html", "utf-8", null);
            }
        }
    }
}


