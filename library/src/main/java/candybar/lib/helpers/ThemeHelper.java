package candybar.lib.helpers;

import android.content.Context;
import android.content.res.Configuration;

import java.util.ArrayList;
import java.util.List;

import candybar.lib.R;
import candybar.lib.applications.CandyBarApplication;
import candybar.lib.items.Theme;
import candybar.lib.preferences.Preferences;

public class ThemeHelper {
    public static final int THEME_AUTO = 0;
    private static final String THEME_AUTO_NAME = "Auto";
    public static final int THEME_LIGHT = 1;
    private static final String THEME_LIGHT_NAME = "Light";
    public static final int THEME_DARK = 2;
    private static final String THEME_DARK_NAME = "Dark";

    public static List<Theme> getThemes() {
        List<Theme> themes = new ArrayList<>();
        themes.add(new Theme(THEME_AUTO_NAME, THEME_AUTO));
        themes.add(new Theme(THEME_LIGHT_NAME, THEME_LIGHT));
        themes.add(new Theme(THEME_DARK_NAME, THEME_DARK));
        return themes;
    }

    public static String getCurrentThemeName(Context context) {
        int currentTheme = Preferences.get(context).getTheme();
        switch (currentTheme) {
            case THEME_AUTO:
                return THEME_AUTO_NAME;
            case THEME_LIGHT:
                return THEME_LIGHT_NAME;
            case THEME_DARK:
                return THEME_DARK_NAME;
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
