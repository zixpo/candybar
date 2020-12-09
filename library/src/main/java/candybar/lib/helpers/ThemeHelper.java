package candybar.lib.helpers;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;

import java.util.ArrayList;
import java.util.List;

import candybar.lib.R;
import candybar.lib.applications.CandyBarApplication;
import candybar.lib.items.Theme;
import candybar.lib.preferences.Preferences;

public class ThemeHelper {
    public static final int THEME_AUTO = 0;
    public static final int THEME_LIGHT = 1;
    public static final int THEME_DARK = 2;

    public static List<Theme> getThemes(Context context) {
        Resources resources = context.getResources();
        List<Theme> themes = new ArrayList<>();
        themes.add(new Theme(resources.getString(R.string.theme_name_auto), THEME_AUTO));
        themes.add(new Theme(resources.getString(R.string.theme_name_light), THEME_LIGHT));
        themes.add(new Theme(resources.getString(R.string.theme_name_dark), THEME_DARK));
        return themes;
    }

    public static String getCurrentThemeName(Context context) {
        Resources resources = context.getResources();
        int currentTheme = Preferences.get(context).getTheme();
        switch (currentTheme) {
            case THEME_AUTO:
                return resources.getString(R.string.theme_name_auto);
            case THEME_LIGHT:
                return resources.getString(R.string.theme_name_light);
            case THEME_DARK:
                return resources.getString(R.string.theme_name_dark);
        }
        return "";
    }

    public static boolean isDarkTheme(Context context) {
        boolean useDarkTheme = context.getResources().getBoolean(R.bool.use_dark_theme);
        boolean isThemingEnabled = CandyBarApplication.getConfiguration().isDashboardThemingEnabled();
        if (!isThemingEnabled) return useDarkTheme;
        int currentTheme = Preferences.get(context).getTheme();
        if (currentTheme == THEME_AUTO) {
            switch (context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK) {
                case Configuration.UI_MODE_NIGHT_YES:
                    return true;
                case Configuration.UI_MODE_NIGHT_NO:
                    return false;
            }
        }
        return currentTheme == THEME_DARK;
    }
}
