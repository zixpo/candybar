package candybar.lib.items;

import android.content.Context;
import android.content.res.Resources;

import candybar.lib.R;

public enum Theme {
    AUTO,
    LIGHT,
    DARK;

    public String displayName(Context context) {
        Resources resources = context.getResources();
        switch (this) {
            case AUTO:
                return resources.getString(R.string.theme_name_auto);
            case LIGHT:
                return resources.getString(R.string.theme_name_light);
            case DARK:
                return resources.getString(R.string.theme_name_dark);
        }
        return "";
    }
}
