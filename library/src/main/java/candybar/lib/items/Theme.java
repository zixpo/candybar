package candybar.lib.items;

import android.content.Context;

import androidx.annotation.StringRes;

import candybar.lib.R;

public enum Theme {
    AUTO(R.string.theme_name_auto),
    LIGHT(R.string.theme_name_light),
    DARK(R.string.theme_name_dark)/*,
    MATERIAL_YOU(R.string.theme_name_material_you)*/;

    private final int nameStringRes;

    Theme(@StringRes int nameRes) {
        nameStringRes = nameRes;
    }

    public String displayName(Context context) {
        return context.getResources().getString(nameStringRes);
    }
}
