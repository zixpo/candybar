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
                .positiveText(R.string.change_icon_color_dialog_confirm)
                .negativeText(R.string.change_icon_color_dialog_close)
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

                LinearLayout itemLayout = new LinearLayout(requireActivity());
                itemLayout.setOrientation(LinearLayout.HORIZONTAL);
                itemLayout.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT));

                ImageView logoView = new ImageView(requireActivity());
                Drawable logoDrawable;

                // Check if the package name is the main app
                if (packageName.equals(requireContext().getPackageName())) {
                    Log.d("ChangeIconColorFragment", "Main app package: " + packageName);
                    logoDrawable = requireContext().getDrawable(R.drawable.theme_logo); // Main app logo
                } else {
                    try {
                        Log.d("ChangeIconColorFragment", "Side app package: " + packageName);
                        Context packageContext = requireContext().createPackageContext(packageName, 0);
                        int logoId = packageContext.getResources().getIdentifier("theme_logo", "drawable", packageName);
                        logoDrawable = packageContext.getDrawable(logoId);
                    } catch (PackageManager.NameNotFoundException e) {
                        e.printStackTrace();
                        logoDrawable = requireContext().getDrawable(R.drawable.theme_logo); // Fallback logo
                    } catch (Resources.NotFoundException e) {
                        e.printStackTrace();
                        logoDrawable = requireContext().getDrawable(R.drawable.theme_logo); // Fallback logo
                    }
                }
                logoView.setImageDrawable(logoDrawable);

                LinearLayout.LayoutParams logoParams = new LinearLayout.LayoutParams(
                        80, // width in dp
                        80  // height in dp
                );
                logoParams.setMargins(0, 0, 16, 0); // margin right
                logoView.setLayoutParams(logoParams);

                RadioButton radioButton = new RadioButton(requireActivity());
                radioButton.setText(StringUtils.capitalize(color));
                radioButton.setId(View.generateViewId());
                radioButton.setTextColor(textColor);

                itemLayout.addView(logoView);
                itemLayout.addView(radioButton);
                radioGroup.addView(itemLayout);

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
        String defaultIconPackColor = RequestHelper.getDefaultIconPackColor(context);

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
