package candybar.lib.helpers;

import android.content.Intent;

import androidx.annotation.Nullable;

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

public class IntentHelper {

    public static final int ACTION_DEFAULT = 0;
    public static final int ICON_PICKER = 1;
    public static final int IMAGE_PICKER = 2;
    public static final int WALLPAPER_PICKER = 3;

    public static int sAction = ACTION_DEFAULT;

    private static final String ACTION_ADW_PICK_ICON = "org.adw.launcher.icons.ACTION_PICK_ICON";
    private static final String ACTION_TURBO_PICK_ICON = "com.phonemetra.turbo.launcher.icons.ACTION_PICK_ICON";
    private static final String ACTION_LAWNCHAIR_ICONPACK = "ch.deletescape.lawnchair.ICONPACK";
    private static final String ACTION_NOVA_LAUNCHER = "com.novalauncher.THEME";
    private static final String ACTION_ONEPLUS_PICK_ICON = "net.oneplus.launcher.icons.ACTION_PICK_ICON";
    private static final String ACTION_PLUS_HOME = "jp.co.a_tm.android.launcher.icons.ACTION_PICK_ICON";
    private static final String ACTION_PROJECTIVY_PICK_ICON = "com.spocky.projengmenu.icons.ACTION_PICK_ICON";

    public static int getAction(@Nullable Intent intent) {
        if (intent == null) return ACTION_DEFAULT;
        String action = intent.getAction();
        if (action != null) {
            return switch (action) {
                case ACTION_ADW_PICK_ICON,
                     ACTION_TURBO_PICK_ICON,
                     ACTION_LAWNCHAIR_ICONPACK,
                     ACTION_NOVA_LAUNCHER,
                     ACTION_ONEPLUS_PICK_ICON,
                     ACTION_PLUS_HOME,
                     ACTION_PROJECTIVY_PICK_ICON -> ICON_PICKER;
                case Intent.ACTION_PICK,
                     Intent.ACTION_GET_CONTENT -> IMAGE_PICKER;
                case Intent.ACTION_SET_WALLPAPER -> WALLPAPER_PICKER;
                default -> ACTION_DEFAULT;
            };
        }

        return ACTION_DEFAULT;
    }
}
