package candybar.lib.fragments.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.util.Log;
import android.content.res.Resources;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.HashMap;

import candybar.lib.R;
import candybar.lib.activities.CandyBarMainActivity;
import candybar.lib.applications.CandyBarApplication;
import candybar.lib.helpers.LauncherHelper;
import candybar.lib.helpers.RequestHelper;
import candybar.lib.helpers.ThemeHelper;
import candybar.lib.items.IconPack;
import candybar.lib.preferences.Preferences;
import candybar.lib.helpers.StringUtils;

public class ChangeIconColorFragment extends DialogFragment {

    private static final String ARG_COLOR_OPTIONS = "color_options";
    private static final String ARG_ICON_PACK_NAME = "icon_pack_name";
    private static final String ARG_PACKAGE_NAMES = "package_names";

    private String selectedOption;
    private String selectedIconPackName;
    private String[] selectedPackageNames; // Array to hold selected package names
    private String selectedPackageName; // Variable to hold the currently selected package name

    public static void showChangeIconColorDialog(@NonNull FragmentManager fm, @NonNull String[] colorOptions, @NonNull String iconPackName, @NonNull String[] packageNames) {
        ChangeIconColorFragment fragment = new ChangeIconColorFragment();
        Bundle args = new Bundle();
        args.putStringArray(ARG_COLOR_OPTIONS, colorOptions);
        args.putString(ARG_ICON_PACK_NAME, iconPackName);
        args.putStringArray(ARG_PACKAGE_NAMES, packageNames);
        fragment.setArguments(args);
        fragment.show(fm, "ChangeIconColorFragment");
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@NonNull Bundle savedInstanceState) {
        Bundle args = getArguments();
        String[] colorOptions = args != null ? args.getStringArray(ARG_COLOR_OPTIONS) : new String[0];
        selectedIconPackName = args != null ? args.getString(ARG_ICON_PACK_NAME) : "";
        selectedPackageNames = args != null ? args.getStringArray(ARG_PACKAGE_NAMES) : new String[0];

        MaterialDialog dialog = new MaterialDialog.Builder(requireActivity())
                .customView(R.layout.dialog_change_icon_color, false)
                .title(R.string.change_icon_color_dialog_title)
                .positiveText(R.string.confirm)
                .negativeText(R.string.close)
                .onNegative((dialog1, which) -> dismiss())
                .onPositive((dialog1, which) -> {
                    handleOptionSelected(selectedOption, selectedIconPackName);
                    dismiss();
                })
                .build();

        View dialogView = dialog.getCustomView();
        if (dialogView != null) {
            RadioGroup radioGroup = dialogView.findViewById(R.id.radio_group_options);
            int textColor = getResources().getColor(
                    ThemeHelper.isDarkTheme(requireContext()) ? android.R.color.white : android.R.color.black);

            for (int i = 0; i < colorOptions.length; i++) {
                String color = colorOptions[i];
                String packageName = selectedPackageNames[i]; // Get the corresponding package name for the color

                // Create the RadioButton and set its properties
                RadioButton radioButton = new RadioButton(requireActivity());
                radioButton.setText(StringUtils.capitalize(color));
                radioButton.setId(View.generateViewId());
                radioButton.setTextColor(textColor);

                Drawable logoDrawable;

                // Check if the package name is the main app
                if (packageName.equals(requireContext().getPackageName())) {
                    logoDrawable = requireContext().getDrawable(R.drawable.ic_icon_pack_color); // Main app logo
                } else {
                    try {
                        Context packageContext = requireContext().createPackageContext(packageName, 0);
                        int logoId = packageContext.getResources().getIdentifier("ic_icon_pack_color", "drawable", packageName);
                        logoDrawable = packageContext.getDrawable(logoId);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                        logoDrawable = requireContext().getDrawable(R.drawable.ic_icon_pack_color); // Fallback logo
                    } catch (Resources.NotFoundException e) {
                        e.printStackTrace();
                        logoDrawable = requireContext().getDrawable(R.drawable.ic_icon_pack_color); // Fallback logo
                    }
                }

                // Resize the drawable to 80dp
                int iconSize = 80;
                logoDrawable.setBounds(0, 0, iconSize, iconSize);

                // Set the drawable as a compound drawable for the RadioButton (left side)
                radioButton.setCompoundDrawables(logoDrawable, null, null, null);
                radioButton.setCompoundDrawablePadding(16); // Set padding between the drawable and text

                radioGroup.addView(radioButton); // Add the RadioButton directly to the RadioGroup

                radioButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
                    if (isChecked) {
                        selectedOption = color;
                        selectedPackageName = packageName; // Store the selected package name
                    }
                });
            }

        }

        return dialog;
    }

    private void handleOptionSelected(String option, String iconPackName) {
        String iconPackId;
        option = option.toLowerCase().replace(" ", "_");
        iconPackName = iconPackName.toLowerCase().replace(" ", "_");
        Context context = requireContext();
        String defaultIconPack = RequestHelper.getDefaultIconPack(context);
        defaultIconPack = defaultIconPack.toLowerCase().replace(" ", "_");
        String defaultIconPackColor = RequestHelper.getDefaultIconPackColor(context);
        defaultIconPackColor = defaultIconPackColor.toLowerCase().replace(" ", "_");

        if (defaultIconPack.equals(iconPackName) && defaultIconPackColor.equals(option)) {
            iconPackId = "";
        } else {
            iconPackId = "." + iconPackName + "_" + option;
        }
        Preferences.get(requireContext()).setSelectedIconPackId(iconPackId);

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
