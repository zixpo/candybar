package candybar.lib.fragments.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import com.afollestad.materialdialogs.MaterialDialog;
import candybar.lib.R;
import candybar.lib.helpers.RequestHelper;
import candybar.lib.preferences.Preferences;
import candybar.lib.activities.CandyBarMainActivity;
import candybar.lib.applications.CandyBarApplication;
import java.util.HashMap;
import candybar.lib.helpers.LauncherHelper;

public class ChangeIconColorFragment extends DialogFragment {

    private static final String TAG = "ChangeIconColorFragment"; // Tag for logging
    private static final String ARG_COLOR_OPTIONS = "color_options";
    private static final String ARG_ICON_PACK_NAME = "icon_pack_name";

    private String selectedOption;
    private String selectedIconPackName;

    // Static method to show the ChangeIconColorFragment dialog
    public static void showChangeIconColorDialog(@NonNull FragmentManager fm, @NonNull String[] colorOptions, @NonNull String iconPackName) {
        ChangeIconColorFragment fragment = new ChangeIconColorFragment();
        Bundle args = new Bundle();
        args.putStringArray(ARG_COLOR_OPTIONS, colorOptions);
        args.putString(ARG_ICON_PACK_NAME, iconPackName);
        fragment.setArguments(args);
        fragment.show(fm, "ChangeIconColorFragment");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@NonNull Bundle savedInstanceState) {
        Bundle args = getArguments();
        String[] colorOptions = args != null ? args.getStringArray(ARG_COLOR_OPTIONS) : new String[0];
        selectedIconPackName = args != null ? args.getString(ARG_ICON_PACK_NAME) : "";

        MaterialDialog dialog = new MaterialDialog.Builder(requireActivity())
                .customView(R.layout.dialog_change_icon_color, false)
                .title(R.string.change_icon_color_dialog_title) // Set the dialog title
                .positiveText(R.string.change_icon_color_dialog_confirm) // Confirm button text
                .negativeText(R.string.change_icon_color_dialog_close) // Close button text
                .onNegative((dialog1, which) -> dismiss())
                .onPositive((dialog1, which) -> {
                    handleOptionSelected(selectedOption, selectedIconPackName);
                    dismiss();
                })
                .build();

        // Initialize views from custom layout
        View dialogView = dialog.getCustomView();
        if (dialogView != null) {
            RadioGroup radioGroup = dialogView.findViewById(R.id.radio_group_options);

            for (String color : colorOptions) {
                RadioButton radioButton = new RadioButton(requireActivity());
                radioButton.setText(color);
                radioButton.setId(View.generateViewId());
                radioGroup.addView(radioButton);

                radioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        selectedOption = color;
                    }
                });
            }
        }

        return dialog;
    }

    private void handleOptionSelected(String option, String iconPackName) {
        // Convert the selected color option to lowercase and replace spaces with underscores
        String iconPackId;
        option = option.toLowerCase().replace(" ", "_");
        Context context = requireContext();
        String defaultIconPack = RequestHelper.getDefaultIconPack(context);
        String defaultIconPackColor = RequestHelper.getDefaultIconPackColor(context);

        if (defaultIconPack.equals(iconPackName.toLowerCase()) && defaultIconPackColor.equals(option)) {
            iconPackId = "";
        } else {
            iconPackId = "." + iconPackName.toLowerCase() + "_" + option;
        }
        Preferences.get(requireContext()).setSelectedIconPackId(iconPackId);

        // Log the formatted drawable name for verification
        Log.d(TAG, "Drawable name:" + iconPackId);

        // Log the event and navigate to the apply section
        CandyBarApplication.getConfiguration().getAnalyticsHandler().logEvent(
                "click",
                new HashMap<String, Object>() {{
                    put("section", "home");
                    put("action", "navigate");
                    put("item", "icon_apply");
                }}
        );

        if (context instanceof CandyBarMainActivity) {
            CandyBarMainActivity activity = (CandyBarMainActivity) context;
            if (!LauncherHelper.quickApply(context)) {
                activity.selectPosition(2);
            }
        }
    }
}
