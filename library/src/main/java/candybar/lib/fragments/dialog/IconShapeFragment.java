package candybar.lib.fragments.dialog;

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

import candybar.lib.R;
import candybar.lib.adapters.IconShapeAdapter;
import candybar.lib.helpers.IconShapeHelper;
import candybar.lib.helpers.TypefaceHelper;
import candybar.lib.items.IconShape;
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

public class IconShapeFragment extends DialogFragment {

    private ListView mListView;
    private int mShape;
    private AsyncTask mAsyncTask;

    public static final String TAG = "candybar.dialog.iconshapes";

    private static IconShapeFragment newInstance() {
        return new IconShapeFragment();
    }

    public static void showIconShapeChooser(@NonNull FragmentManager fm) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(TAG);
        if (prev != null) {
            ft.remove(prev);
        }

        try {
            DialogFragment dialog = IconShapeFragment.newInstance();
            dialog.show(ft, TAG);
        } catch (IllegalArgumentException | IllegalStateException ignored) {
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        MaterialDialog.Builder builder = new MaterialDialog.Builder(getActivity());
        builder.customView(R.layout.fragment_languages, false);
        builder.typeface(TypefaceHelper.getMedium(getActivity()), TypefaceHelper.getRegular(getActivity()));
        builder.title(R.string.icon_shape);
        builder.negativeText(R.string.close);
        MaterialDialog dialog = builder.build();
        dialog.show();

        mListView = (ListView) dialog.findViewById(R.id.listview);
        return dialog;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mAsyncTask = new IconShapeLoader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    @Override
    public void onDestroy() {
        if (mAsyncTask != null) {
            mAsyncTask.cancel(true);
        }
        super.onDestroy();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        Preferences.get(getActivity()).setIconShape(mShape);
        getActivity().recreate();
        super.onDismiss(dialog);
    }

    public void setShape(@NonNull int shape) {
        mShape = shape;
        dismiss();
    }

    private class IconShapeLoader extends AsyncTask<Void, Void, Boolean> {

        private List<IconShape> iconShapes;
        private int index = 0;

        @Override
        protected Boolean doInBackground(Void... voids) {
            while (!isCancelled()) {
                try {
                    Thread.sleep(1);
                    iconShapes = IconShapeHelper.getShapes();
                    int currentShape = Preferences.get(getActivity()).getIconShape();
                    for (int i = 0; i < iconShapes.size(); i++) {
                        int shape = iconShapes.get(i).getShape();
                        if (shape == currentShape) {
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
                mListView.setAdapter(new IconShapeAdapter(getActivity(), iconShapes, index));
            } else {
                dismiss();
            }
        }
    }
}
