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

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.List;

import candybar.lib.R;
import candybar.lib.adapters.dialog.IconShapeAdapter;
import candybar.lib.fragments.IconsFragment;
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

public class IconShapeChooserFragment extends DialogFragment {

    private ListView mListView;
    private int mShape;

    public static final String TAG = "candybar.dialog.iconshapes";

    private static IconShapeChooserFragment newInstance() {
        return new IconShapeChooserFragment();
    }

    public static void showIconShapeChooser(@NonNull FragmentManager fm) {
        FragmentTransaction ft = fm.beginTransaction();
        Fragment prev = fm.findFragmentByTag(TAG);
        if (prev != null) {
            ft.remove(prev);
        }

        try {
            DialogFragment dialog = IconShapeChooserFragment.newInstance();
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
        builder.title(R.string.icon_shape);
        builder.negativeText(R.string.close);
        MaterialDialog dialog = builder.build();
        dialog.show();

        mListView = (ListView) dialog.findViewById(R.id.listview);
        return dialog;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        List<IconShape> iconShapes = IconShapeHelper.getShapes();
        int currentShape = Preferences.get(getActivity()).getIconShape();
        int currentShapeIndex = 0;

        for (int i = 0; i < iconShapes.size(); i++) {
            int shape = iconShapes.get(i).getShape();
            if (shape == currentShape) {
                currentShapeIndex = i;
                break;
            }
        }

        mListView.setAdapter(new IconShapeAdapter(getActivity(), iconShapes, currentShapeIndex));
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public void onDismiss(@NonNull DialogInterface dialog) {
        Preferences.get(getActivity()).setIconShape(mShape);
        IconsFragment.reloadIcons();
        super.onDismiss(dialog);
    }

    public void setShape(int shape) {
        mShape = shape;
    }
}
